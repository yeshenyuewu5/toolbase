package com.tokenplatform.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tokenplatform.model.ApiKey;
import com.tokenplatform.model.User;
import com.tokenplatform.service.RateLimitService;
import com.tokenplatform.service.UsageService;
import com.tokenplatform.service.UserService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AiProxyService {

    private static final Logger log = LoggerFactory.getLogger(AiProxyService.class);

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final UsageService usageService;
    private final RateLimitService rateLimitService;

    @Value("${app.upstream.deepseek.api-url}")
    private String deepseekApiUrl;

    @Value("${app.upstream.deepseek.api-key}")
    private String deepseekApiKey;

    @Value("${app.upstream.deepseek.cost-input:0.07}")
    private double deepseekCostInput;

    @Value("${app.upstream.deepseek.cost-output:0.28}")
    private double deepseekCostOutput;

    public AiProxyService(UserService userService, UsageService usageService,
                          RateLimitService rateLimitService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.usageService = usageService;
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public JsonNode proxyChatCompletion(JsonNode requestBody, User user, ApiKey apiKey) throws IOException {
        // Check rate limit
        if (!rateLimitService.checkRateLimit(apiKey)) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("error", "rate_limit_exceeded");
            error.put("message", "API rate limit exceeded. Limit: " + apiKey.getRateLimitPerMinute() + " req/min");
            return error;
        }

        // Build upstream request
        String model = requestBody.has("model") ? requestBody.get("model").asText("deepseek-chat") : "deepseek-chat";

        ObjectNode upstreamBody = objectMapper.createObjectNode();
        upstreamBody.put("model", model);
        upstreamBody.set("messages", requestBody.get("messages"));

        if (requestBody.has("temperature")) {
            upstreamBody.put("temperature", requestBody.get("temperature").asDouble());
        }
        if (requestBody.has("max_tokens")) {
            upstreamBody.put("max_tokens", requestBody.get("max_tokens").asInt());
        }
        if (requestBody.has("stream")) {
            upstreamBody.put("stream", requestBody.get("stream").asBoolean());
        }

        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Request upstreamRequest = new Request.Builder()
                .url(deepseekApiUrl)
                .addHeader("Authorization", "Bearer " + deepseekApiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(upstreamBody.toString(), MediaType.parse("application/json")))
                .build();

        try (Response upstreamResponse = httpClient.newCall(upstreamRequest).execute()) {
            String responseBody = upstreamResponse.body() != null ? upstreamResponse.body().string() : "{}";
            JsonNode responseJson = objectMapper.readTree(responseBody);

            int inputTokens = 0;
            int outputTokens = 0;

            if (responseJson.has("usage")) {
                JsonNode usage = responseJson.get("usage");
                inputTokens = usage.has("prompt_tokens") ? usage.get("prompt_tokens").asInt() : 0;
                outputTokens = usage.has("completion_tokens") ? usage.get("completion_tokens").asInt() : 0;
            }

            // Calculate cost and deduct
            long costFen = rateLimitService.calculatePrice(
                    deepseekCostInput, deepseekCostOutput, inputTokens, outputTokens);

            boolean deducted = userService.deductBalance(user.getId(), costFen);

            // Record usage
            usageService.recordUsage(user, apiKey, model, inputTokens, outputTokens,
                    deducted ? costFen : 0, requestId);

            if (!deducted) {
                ObjectNode error = objectMapper.createObjectNode();
                error.put("error", "insufficient_balance");
                error.put("message", "Insufficient balance. Please recharge.");
                return error;
            }

            // Add balance info to response
            if (responseJson instanceof ObjectNode) {
                ObjectNode enriched = (ObjectNode) responseJson;
                enriched.put("_request_id", requestId);
                enriched.put("_cost_fen", costFen);
                enriched.put("_remaining_balance", userService.getById(user.getId()).getBalance());
            }

            return responseJson;
        } catch (Exception e) {
            log.error("Proxy request failed: {}", e.getMessage());
            ObjectNode error = objectMapper.createObjectNode();
            error.put("error", "proxy_error");
            error.put("message", "Upstream API error: " + e.getMessage());
            return error;
        }
    }
}

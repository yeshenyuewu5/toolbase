package com.tokenplatform.controller;

import com.tokenplatform.model.*;
import com.tokenplatform.proxy.AiProxyService;
import com.tokenplatform.repository.ApiKeyRepository;
import com.tokenplatform.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
public class ApiProxyController {

    private final AiProxyService aiProxyService;
    private final ApiKeyRepository apiKeyRepository;
    private final UserService userService;

    public ApiProxyController(AiProxyService aiProxyService,
                               ApiKeyRepository apiKeyRepository,
                               UserService userService) {
        this.aiProxyService = aiProxyService;
        this.apiKeyRepository = apiKeyRepository;
        this.userService = userService;
    }

    @PostMapping(value = "/chat/completions", consumes = "application/json", produces = "application/json")
    public ResponseEntity<JsonNode> chatCompletions(
            @RequestBody JsonNode requestBody,
            @RequestHeader("Authorization") String authHeader) {

        // Extract API key from Authorization header
        String apiKeyValue;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            apiKeyValue = authHeader.substring(7).trim();
        } else {
            // Try X-Api-Key header as fallback
            return ResponseEntity.status(401)
                    .body(createError("invalid_auth", "Missing or invalid Authorization header. Use: Bearer <your-api-key>"));
        }

        // Validate API key
        ApiKey apiKey = apiKeyRepository.findByKeyValue(apiKeyValue).orElse(null);
        if (apiKey == null) {
            return ResponseEntity.status(401)
                    .body(createError("invalid_api_key", "Invalid API key"));
        }

        if (!apiKey.getEnabled()) {
            return ResponseEntity.status(403)
                    .body(createError("key_disabled", "API key is disabled"));
        }

        User user = apiKey.getUser();
        if (!user.getEnabled()) {
            return ResponseEntity.status(403)
                    .body(createError("account_disabled", "Account is disabled"));
        }

        try {
            JsonNode result = aiProxyService.proxyChatCompletion(requestBody, user, apiKey);

            // Check if it's an error response
            if (result.has("error")) {
                String error = result.get("error").asText();
                if ("rate_limit_exceeded".equals(error)) {
                    return ResponseEntity.status(429).body(result);
                }
                if ("insufficient_balance".equals(error)) {
                    return ResponseEntity.status(402).body(result);
                }
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (IOException e) {
            return ResponseEntity.status(500)
                    .body(createError("internal_error", "Proxy error: " + e.getMessage()));
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<JsonNode> checkBalance(@RequestHeader("Authorization") String authHeader) {
        String apiKeyValue = extractKey(authHeader);
        ApiKey apiKey = apiKeyRepository.findByKeyValue(apiKeyValue).orElse(null);
        if (apiKey == null) {
            return ResponseEntity.status(401).body(createError("invalid_api_key", "Invalid API key"));
        }

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.node.ObjectNode result = mapper.createObjectNode();
        User user = apiKey.getUser();
        result.put("balance", user.getBalance());
        result.put("balance_yuan", user.getBalance() / 100.0);
        result.put("username", user.getUsername());

        return ResponseEntity.ok(result);
    }

    private String extractKey(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return authHeader != null ? authHeader.trim() : "";
    }

    private JsonNode createError(String code, String message) {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.node.ObjectNode error = mapper.createObjectNode();
        error.put("error", code);
        error.put("message", message);
        return error;
    }
}

package com.tokenplatform.service;

import com.tokenplatform.model.ApiKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.pricing.markup-ratio:1.5}")
    private double markupRatio;

    public RateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Check if request is within rate limit.
     * Returns true if allowed, false if rate limited.
     */
    public boolean checkRateLimit(ApiKey apiKey) {
        String key = "ratelimit:" + apiKey.getKeyValue();
        String current = redisTemplate.opsForValue().get(key);

        int limit = apiKey.getRateLimitPerMinute() != null ? apiKey.getRateLimitPerMinute() : 60;

        if (current == null) {
            redisTemplate.opsForValue().set(key, "1", 60, TimeUnit.SECONDS);
            return true;
        }

        long count = Long.parseLong(current);
        if (count >= limit) {
            return false;
        }

        redisTemplate.opsForValue().increment(key);
        return true;
    }

    /**
     * Calculate sell price based on cost and markup.
     * Input/output values are in fen per 1K tokens.
     */
    public long calculatePrice(double costInputFen, double costOutputFen,
                                int inputTokens, int outputTokens) {
        double inputCost = (costInputFen * inputTokens) / 1000.0;
        double outputCost = (costOutputFen * outputTokens) / 1000.0;
        return (long) Math.ceil((inputCost + outputCost) * markupRatio);
    }
}

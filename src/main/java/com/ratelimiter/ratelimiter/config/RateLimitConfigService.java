package com.ratelimiter.ratelimiter.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RateLimitConfigService {
    @Autowired
    private StringRedisTemplate redis;
    private final ObjectMapper mapper = new ObjectMapper();

    // Loads config dynamically from Redis (per API, etc.). Fallbacks to default.
    public RateLimitConfig getConfigFor(String api) {
        String configJson = redis.opsForValue().get("ratelimit:config:" + api);
        if (configJson != null) {
            try {
                return mapper.readValue(configJson, RateLimitConfig.class);
            } catch (Exception e) {
                // log error
            }
        }
        // Default: FixedWindow, 100/60s, burst:10
        return new RateLimitConfig("FixedWindow", 100, 60, 10);
    }
}

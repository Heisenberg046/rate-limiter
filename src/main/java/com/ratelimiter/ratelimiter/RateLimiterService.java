package com.ratelimiter.ratelimiter;


import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ratelimiter.ratelimiter.config.RateLimitConfig;
import com.ratelimiter.ratelimiter.config.RateLimitConfigService;
import com.ratelimiter.ratelimiter.model.RateLimitRequest;
import com.ratelimiter.ratelimiter.strategies.RateLimitingStrategy;

import io.micrometer.core.instrument.MeterRegistry;

@Service
public class RateLimiterService {
    private static final Logger logger = LoggerFactory.getLogger(RateLimiterService.class);

    @Autowired
    private RateLimitConfigService configService;
    @Autowired
    private Map<String, RateLimitingStrategy> strategies;
    @Autowired
    private MeterRegistry meterRegistry;

    // Fallback: allow-all on Redis failure (could implement circuit breaker)
    public boolean allow(RateLimitRequest request) {
        RateLimitConfig config = configService.getConfigFor(request.getApi());
        String strategyName = config.getStrategy();
        RateLimitingStrategy strategy = strategies.get(strategyName);
        boolean allowed = true;
        try {
            allowed = strategy.allowRequest(request, config);
        } catch (Exception e) {
            logger.error("Rate limiting failed, fallback to allow all: {}", e.getMessage());
            allowed = true; // fallback mode
        }
        meterRegistry.counter("rate_limiter.requests", "api", request.getApi()).increment();
        if (!allowed) {
            meterRegistry.counter("rate_limiter.throttled", "api", request.getApi()).increment();
            logger.warn("Throttled: user={}, api={}, ip={}", request.getUserId(), request.getApi(), request.getIp());
        }
        return allowed;
    }
}

package com.ratelimiter.ratelimiter.strategies;

import com.ratelimiter.ratelimiter.config.RateLimitConfig;
import com.ratelimiter.ratelimiter.model.RateLimitRequest;

public interface RateLimitingStrategy {
    boolean allowRequest(RateLimitRequest req, RateLimitConfig config);
}

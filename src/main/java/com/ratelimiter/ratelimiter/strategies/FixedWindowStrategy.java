package com.ratelimiter.ratelimiter.strategies;

import org.springframework.beans.factory.annotation.Autowired;

import com.ratelimiter.ratelimiter.config.RateLimitConfig;
import com.ratelimiter.ratelimiter.model.RateLimitRequest;
import com.ratelimiter.ratelimiter.redis.RedisRateLimitStore;

public class FixedWindowStrategy implements RateLimitingStrategy {

	 @Autowired
	    private RedisRateLimitStore store;

	    @Override
	    public boolean allowRequest(RateLimitRequest req, RateLimitConfig config) {
	        String key = req.getUserId() + ":" + req.getApi();
	        long count = store.incrementAndGet("ratelimit:fw:"+key, config.getWindowSeconds());
	        return count <= config.getLimit() + config.getBurst();
	    }
	}
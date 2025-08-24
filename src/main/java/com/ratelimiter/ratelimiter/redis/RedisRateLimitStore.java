package com.ratelimiter.ratelimiter.redis;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
public class RedisRateLimitStore {
    @Autowired
    private StringRedisTemplate redis;

    // For Fixed Window: increments counter, sets expiry to window if new
    public long incrementAndGet(String key, long windowSeconds) {
        Long val = redis.opsForValue().increment(key);
        if (val != null && val == 1L) {
            redis.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        return val == null ? 0 : val;
    }

    // Token Bucket: Uses Redis scripts for accurate concurrency-many opensource scripts exist, simplified here for demo
    public synchronized boolean tryConsumeToken(String key, int limit, int refillSec, int burst) {
        String rateKey = "ratelimit:tokenbucket:" + key;
        // Try to get tokens, refill if needed
        Long curr = redis.opsForValue().increment(rateKey);
        if (curr != null && curr == 1L) {
            redis.expire(rateKey, refillSec, TimeUnit.SECONDS);
        }
        return curr != null && curr <= (limit+burst);
    }
}

package com.ratelimiter.ratelimiter.config;



public class RateLimitConfig {
    private String strategy; // FixedWindow, TokenBucket, etc.
    private int limit;
    private int windowSeconds; // e.g. 60
    private int burst; // for burst handling

    public RateLimitConfig(String strategy, int limit, int windowSeconds, int burst) {
        this.strategy = strategy;
        this.limit = limit;
        this.windowSeconds = windowSeconds;
        this.burst = burst;
    }
    // Getters & Setters
    public String getStrategy() { return strategy; }
    public int getLimit() { return limit; }
    public int getWindowSeconds() { return windowSeconds; }
    public int getBurst() { return burst; }
}

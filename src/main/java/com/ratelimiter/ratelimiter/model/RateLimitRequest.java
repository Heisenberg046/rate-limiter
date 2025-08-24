package com.ratelimiter.ratelimiter.model;


public class RateLimitRequest {
    private String userId;
    private String clientId;
    private String api;
    private String ip;

    public RateLimitRequest(String userId, String clientId, String api, String ip) {
        this.userId = userId;
        this.clientId = clientId;
        this.api = api;
        this.ip = ip;
    }
   
    public String getUserId() { return userId; }
    public String getClientId() { return clientId; }
    public String getApi() { return api; }
    public String getIp() { return ip; }
}

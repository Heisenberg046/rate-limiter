package com.ratelimiter.ratelimiter.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.ratelimiter.ratelimiter.RateLimiterService;
import com.ratelimiter.ratelimiter.model.RateLimitRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimiterInterceptor implements HandlerInterceptor {
    @Autowired
    private RateLimiterService rateLimiter;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        RateLimitRequest req = new RateLimitRequest(
            request.getHeader("X-User-Id"),
            request.getHeader("X-Client-Id"),
            request.getRequestURI(),
            request.getRemoteAddr()
        );
        if (!rateLimiter.allow(req)) {
            response.setStatus(429);
            response.getWriter().write("Too Many Requests");
            return false;
        }
        return true;
    }
}

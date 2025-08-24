package com.ratelimiter.ratelimiter.integration;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ratelimiter.ratelimiter.RateLimiterService;
import com.ratelimiter.ratelimiter.model.RateLimitRequest;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class RateLimitAspect {
    @Autowired
    private RateLimiterService rateLimiter;

    @Around("@annotation(com.example.ratelimiter.integration.RateLimit)")
    public Object rateLimited(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        RateLimitRequest req = new RateLimitRequest(
                request.getHeader("X-User-Id"), request.getHeader("X-Client-Id"), request.getRequestURI(), request.getRemoteAddr()
        );
        if (!rateLimiter.allow(req)) {
            throw new RuntimeException("Rate limit exceeded");
        }
        return joinPoint.proceed();
    }
}

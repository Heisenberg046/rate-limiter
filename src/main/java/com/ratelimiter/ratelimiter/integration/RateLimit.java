package com.ratelimiter.ratelimiter.integration;


import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    String strategy() default "FixedWindow";
    int limit() default 100;
}

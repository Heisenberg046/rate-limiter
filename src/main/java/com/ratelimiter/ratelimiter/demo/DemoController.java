package com.ratelimiter.ratelimiter.demo;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ratelimiter.ratelimiter.integration.RateLimit;

@RestController
public class DemoController {

    @GetMapping("/hello")
    @RateLimit(strategy = "FixedWindow", limit = 2)
    public String hello() {
        return "Hello, world!";
    }
}

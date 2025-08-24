# Distributed Rate Limiter for Microservices

## Overview

This project is a highly configurable, production-grade **rate limiting** system for cloud-native microservices.  
It provides **plug-and-play integration**, **multiple strategies**, **real-time distributed coordination** (via Redis), and **enterprise observability** (via Prometheus/Micrometer).

## Key Features

- **Granular Limits:** Apply limits per User/API, IP/API, Client/API, or set global defaults via config.
- **Multiple Strategies:** Easily switch between Fixed Window and Token Bucket via config (add new strategies via simple interface).
- **Distributed Coordination:** Uses Redis for synchronizing counters and tokens across all service instances in a cluster.
- **Burst Handling:** Grace period supported (with `burst` config, allows short spikes).
- **Dynamic Configuration:** Rate limit rules can be updated live in Redis.
- **Observability:** Integrated Prometheus metrics and detailed logging for all throttled requests.
- **Fail-Open Fallback:** Designed to permit traffic (and log/alert) if Redis is temporarily unavailable.
- **Spring Boot Starter Style:** Exposes both Interceptor (automatic for APIs) and annotation-based (per method) integration.

---

## Architectural Diagram

Client --> API Gateway --> RateLimiter Interceptor / Aspect --> Business Logic
| |
[Config pulls from Redis] |
[Counters/tokens stored in Redis]
|
[Metrics/logs/Kafka for ops]



---

## How It Works

### 1. **Configuration**

- **Each API (or pattern) has a rate-limit config** in Redis (`ratelimit:config:{apiName}`), defining:
  - `strategy`: `"FixedWindow"` or `"TokenBucket"` (can add others)
  - `limit`: maximum requests
  - `windowSeconds`: time interval (for fixed window or refill rate)
  - `burst`: extra burst capacity allowed
- **Default config** applies if per-API config not found.

### 2. **Integration Patterns**

- **REST APIs:** Just add the RateLimiterInterceptor—no code changes needed.
- **Method-level:** Use `@RateLimit` annotation with the AOP aspect to limit specific methods.

### 3. **How Requests Are Checked**

- Incoming request arrives.
- Extract user, client ID, API, IP (from headers or URI).
- **RateLimiterService pulls config** from Redis or defaults.
- Selected **strategy** (e.g., Fixed Window, Token Bucket) checks if the request is allowed using Redis centralized counters.
- Allowed requests proceed; denied requests are blocked with HTTP 429.
- All request outcomes update Prometheus metrics; throttled requests are logged with context.

### 4. **Distributed Safety**

- Counters and tokens are centrally stored/updated in Redis with atomic operations.
- Safe for horizontal scaling across many service instances.

### 5. **Fallback/Resilience**

- If Redis fails, fallback mode permits all requests and logs the incident to avoid blocking traffic.
- Circuit breaker pattern can be enhanced for more sophisticated resilience.

---

## Quick Start

### 1. Clone and Build

git clone https://github.com/Heisenberg046/rate-limiter
cd rate-limiter
mvn clean package


### 2. Environment Setup

- Run Redis locally or on your cluster.
- Configure `application.properties` as below.

### 3. Running the Service
mvn spring-boot:run


### 4. Setting Rate Limit Config in Redis

Example: Limit `/hello` API to 5 requests per minute with 2 extra bursts:
redis-cli
SET ratelimit:config:/hello '{"strategy":"FixedWindow","limit":5,"windowSeconds":60,"burst":2}'


---

## Code Structure

src/main/java/com/example/ratelimiter/
├─ config/ // RateLimitConfig & RateLimitConfigService
├─ integration/ // Interceptor, annotation, AOP aspect
├─ model/ // RateLimitRequest model
├─ redis/ // RedisRateLimitStore abstraction
├─ strategies/ // FixedWindowStrategy, TokenBucketStrategy
├─ RateLimiterService.java // Central orchestrator


---

## Usage Examples

### As Embedded Library in Microservice Controller

RateLimitRequest req = new RateLimitRequest(userId, clientId, apiPath, ip);
boolean allowed = rateLimiterService.allow(req);
if (!allowed) {
throw new TooManyRequestsException();
}
// Continue processing



### Gateway Integration with Interceptor

Register `RateLimiterInterceptor` in gateway configuration:

@Override
public void addInterceptors(InterceptorRegistry registry) {
registry.addInterceptor(rateLimiterInterceptor).addPathPatterns("/**");
}


### Annotation-based Limiting

@RateLimit(strategy="FixedWindow", limit=10)
@GetMapping("/limited")
public String limitedEndpoint() {
return "Rate limited!";
}


---

## Metrics and Monitoring

- Metrics are exposed at `/actuator/prometheus`.
- Key metrics:
  - `rate_limiter_requests_total`
  - `rate_limiter_throttled_total`
- All throttled requests logged at WARN level with user/client context.

---

## Integration Options in Distributed Environments

| Method                     | Description                             | Pros                             | Cons                         |
|----------------------------|---------------------------------------|---------------------------------|------------------------------|
| Embedded Library            | In each microservice’s code            | Fine-grained control            | Requires redeployment         |
| API Gateway Interceptor     | At centralized API gateway             | Central control, efficient      | Limited to gateway’s visibility |
| Standalone Rate Limiter API | Separate microservice called remotely  | Language agnostic, centralized  | Adds network latency          |
| Sidecar/Proxy              | Local proxy container or service mesh | Decouples logic, works with any | More infra complexity         |

---

## Redis Config Details

### What is stored?

Keys like `ratelimit:config:{apiName}` store JSON config:

{
"strategy": "FixedWindow",
"limit": 100,
"windowSeconds": 60,
"burst": 10
}

{
"strategy": "FixedWindow",
"limit": 100,
"windowSeconds": 60,
"burst": 10
}

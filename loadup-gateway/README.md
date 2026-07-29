# LoadUp Gateway

<div align="center">

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 4.1](https://img.shields.io/badge/Spring_Boot-4.1.0-green.svg)](https://spring.io/projects/spring-boot)

</div>

A high-performance, multi-protocol API gateway built on Spring Boot 4.1, featuring a plugin-based SPI architecture, Groovy template engine, distributed tracing, and hot-reloadable route configuration. Licensed under GPL-3.0.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Quick Start](#quick-start)
- [Architecture](#architecture)
- [Configuration](#configuration)
- [Route Configuration](#route-configuration)
- [Protocol Support](#protocol-support)
- [Security](#security)
- [Template Engine](#template-engine)
- [Response Wrapping](#response-wrapping)
- [Distributed Tracing](#distributed-tracing)
- [Plugin System](#plugin-system)
- [Module Structure](#module-structure)
- [Development](#development)
- [Exception Handling](#exception-handling)
- [Roadmap & Improvement Opportunities](#roadmap--improvement-opportunities)
- [License](#license)

---

## Overview

LoadUp Gateway is a universal API gateway that accepts HTTP requests from clients and proxies them to backend services using configurable protocols — plain HTTP, Apache Dubbo RPC, or direct Spring Bean method invocation. It provides centralized authentication, request/response transformation via Groovy scripts, unified response wrapping, and OpenTelemetry-based distributed tracing.

The gateway is designed around a strict separation between its **facade** (SPI interfaces and shared models), **core** (the processing engine), **plugins** (protocol adapters and storage backends), and a **starter** (auto-configuration for Spring Boot). This makes it easy to extend without modifying the core.

## Features

- **Multi-protocol proxying**: HTTP, Dubbo RPC, and Spring Bean invocation — switch protocols per route
- **Pluggable SPI architecture**: Add new protocols or storage backends by implementing interfaces
- **Groovy template engine**: Transform requests before proxying and responses before returning, with compiled script caching
- **Chain of Responsibility processing**: 8 configurable actions form the request pipeline (exception handling, tracing, routing, security, request transform, proxy, response transform, response wrap)
- **Multiple security strategies**: JWT authentication, HMAC-SHA256 signature verification, internal-call whitelist, or no security — selectable per route
- **Dual storage backends**: CSV files (zero-infrastructure, good for development) or relational database (production-ready, with Flyway migrations)
- **Hot-reloadable routes**: Route cache uses atomic reference swap (double-buffering) to avoid thundering-herd on refresh
- **OpenTelemetry tracing**: Automatic span creation, context propagation to downstream services
- **Unified response format**: Optional wrapping of all responses in a standard `{ result, data, meta }` envelope
- **Comprehensive error handling**: Typed exceptions (GatewayException) with structured error responses
- **Spring Boot 4.1 auto-configuration**: Zero XML, plug-and-play via `@AutoConfiguration` and `@ConditionalOnProperty`

---

## Quick Start

### Prerequisites

- Java 21
- Maven 3.9+

### 1. Add Maven Dependency

Add the starter to your Spring Boot project:

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-gateway-starter</artifactId>
    <version>0.0.2-SNAPSHOT</version>
</dependency>
```

The starter pulls in `loadup-gateway-core`, `loadup-gateway-facade`, and all plugin modules transitively.

### 2. Configure the Gateway

Create `application.yml` in your Spring Boot application:

```yaml
loadup:
  gateway:
    enabled: true
    route-refresh-interval: 30        # seconds
    default-timeout: 10000            # milliseconds
    default-retry-count: 1
    storage:
      type: FILE                      # FILE or DATABASE
      file:
        base-path: classpath:/gateway-config
    security:
      secret: "your-jwt-secret-key-must-be-long-enough-32bytes"
      header: "Authorization"
      prefix: "Bearer "
    response:
      wrap: true
      wrapResult: true
      wrapMeta: true
    proxy-plugins:
      http:
        enabled: true
        max-connections: 100
      rpc:
        enabled: false
        registry-address: "zookeeper://localhost:2181"
        timeout: 5000
        retries: 2
      bean:
        enabled: true
```

### 3. Define Routes

For **file-based storage** (default), place a `routes.csv` and Groovy templates on your classpath under `gateway-config/`:

#### routes.csv

```csv
path,method,target,securityCode,requestTemplate,responseTemplate,enabled,properties
/api/user/login,POST,http://user-service:8080/api/login,default,login_request,login_response,true,timeout=5000
/api/user/profile,GET,http://user-service:8080/api/profile,default,,,true,timeout=3000;retryCount=1
/api/order/create,POST,http://order-service:8080/api/order,default,,,true,timeout=10000;wrapResponse=false
/api/internal/stats,GET,bean://statsService:getStats,internal,,,true,
/api/product/list,GET,rpc://com.example.ProductService:listProducts:1.0.0,signature,,,true,timeout=5000
```

#### Column Reference

| Column | Description | Example |
|--------|-------------|---------|
| `path` | Request path to match | `/api/user/login` |
| `method` | HTTP method | `GET`, `POST`, `PUT`, `DELETE`, `PATCH` |
| `target` | Backend target (see format below) | `http://host:port/path` |
| `securityCode` | Security strategy | `default`, `signature`, `internal`, `OFF` |
| `requestTemplate` | Groovy template name for request transform | `login_request` (maps to `templates/login_request.groovy`) |
| `responseTemplate` | Groovy template name for response transform | (leave empty for none) |
| `enabled` | Whether route is active | `true` / `false` |
| `properties` | Key-value config pairs | `timeout=5000;retryCount=2` |

#### Target Format by Protocol

| Protocol | Format | Example |
|----------|--------|---------|
| HTTP | `http://host:port/path` | `http://user-service:8080/api/login` |
| Bean | `bean://beanName:methodName` | `bean://userService:getUserById` |
| RPC | `rpc://interface:method:version` | `rpc://com.example.ProductService:listProducts:1.0.0` |

#### Route Properties

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `timeout` | long | 30000 | Request timeout in milliseconds |
| `retryCount` | int | 3 | Number of retries on failure |
| `wrapResponse` | boolean | null | Override global response wrapping |

### 4. Write Groovy Templates (Optional)

Templates are Groovy scripts executed by the template engine. The engine provides two bound variables: `request` (`GatewayRequest`) or `response` (`GatewayResponse`), plus `log` (SLF4J logger).

#### Request Template Example (`templates/login_request.groovy`)

```groovy
import io.github.loadup.commons.util.JsonUtil

// Add common headers
request.headers.put("X-Gateway-Source", "loadup")
request.headers.put("X-Request-Time", request.requestTime.toString())

// Transform request body
if (request.body != null) {
    def bodyMap = JsonUtil.toMap(request.body)
    bodyMap.put("_gateway_meta", [
        requestId: request.requestId,
        clientIp: request.clientIp,
        timestamp: System.currentTimeMillis()
    ])
    // Normalize phone numbers
    if (bodyMap.phone) {
        bodyMap.phone = bodyMap.phone.toString().replaceAll("[^0-9]", "")
    }
    request.body = JsonUtil.toJson(bodyMap)
}

log.info("Login request processed: {}", request.requestId)
return request
```

#### Response Template Example (`templates/login_response.groovy`)

```groovy
import io.github.loadup.commons.util.JsonUtil

response.headers.put("X-Processed-By", "loadup-gateway")

if (response.statusCode == 200 && response.body != null) {
    def bodyMap = JsonUtil.toMap(response.body)
    // Remove sensitive fields
    bodyMap.remove("password")
    bodyMap.remove("secretKey")
    response.body = JsonUtil.toJson(bodyMap)
}

return response
```

### 5. That's It

Start your Spring Boot application. The gateway intercepts incoming HTTP requests, matches them against configured routes, and proxies them accordingly. Requests that don't match any route fall through to your application's normal controllers.

---

## Architecture

For a detailed architectural overview including component diagrams, data flow, design decisions, and SPI contract details, see [ARCHITECTURE.md](ARCHITECTURE.md).

In brief, request processing follows this chain:

```
HTTP Request
    │
    ▼
GatewayHandlerMapping     ← Maps URL to route (Spring HandlerMapping)
    │
    ▼
GatewayHandlerAdapter     ← Builds GatewayContext from HttpServletRequest
    │
    ▼
ActionDispatcher          ← Orchestrates the action chain
    │
    ▼
┌─────────────────────────────────────────────────────────┐
│ ExceptionAction       ← Wraps everything in try/catch   │
│   ├─ TracingAction    ← OpenTelemetry span + propagate  │
│   ├─ RouteAction      ← Resolve route from cache        │
│   ├─ SecurityAction   ← Authenticate/verify             │
│   ├─ RequestTemplate  ← Transform request (Groovy)      │
│   ├─ ProxyAction      ← Forward to backend              │
│   ├─ ResponseTemplate ← Transform response (Groovy)     │
│   └─ ResponseWrapper  ← Wrap in unified format          │
└─────────────────────────────────────────────────────────┘
    │
    ▼
HTTP Response
```

---

## Configuration

All configuration is under the `loadup.gateway` prefix.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `loadup.gateway.enabled` | boolean | `true` | Enable/disable the gateway |
| `loadup.gateway.route-refresh-interval` | int | `5` | Route cache refresh interval (seconds) |
| `loadup.gateway.template-cache-size` | int | `100` | Groovy script cache size |
| `loadup.gateway.default-timeout` | long | `10000` | Default request timeout (ms) |
| `loadup.gateway.default-retry-count` | int | `1` | Default retry count |
| `loadup.gateway.default-wrap-response` | boolean | `false` | Default response wrapping flag |
| `loadup.gateway.security.secret` | string | `"loadup-gateway-secret-key-..."` | JWT secret key |
| `loadup.gateway.security.header` | string | `"Authorization"` | Auth header name |
| `loadup.gateway.security.prefix` | string | `"Bearer "` | Auth header prefix |
| `loadup.gateway.response.wrap` | boolean | `true` | Global: wrap responses |
| `loadup.gateway.response.wrap-result` | boolean | `true` | Include `result` block |
| `loadup.gateway.response.wrap-meta` | boolean | `true` | Include `meta` block |
| `loadup.gateway.storage.type` | enum | `FILE` | Storage backend: `FILE` or `DATABASE` |
| `loadup.gateway.storage.file.base-path` | string | `classpath:/gateway-config` | File storage base path |
| `loadup.gateway.proxy-plugins.http.enabled` | boolean | `true` | Enable HTTP proxy |
| `loadup.gateway.proxy-plugins.http.max-connections` | int | `100` | Max HTTP connections |
| `loadup.gateway.proxy-plugins.rpc.enabled` | boolean | `true` | Enable RPC proxy |
| `loadup.gateway.proxy-plugins.rpc.registry-address` | string | — | Dubbo registry address |
| `loadup.gateway.proxy-plugins.rpc.timeout` | long | — | RPC call timeout |
| `loadup.gateway.proxy-plugins.rpc.retries` | long | — | RPC call retries |
| `loadup.gateway.proxy-plugins.bean.enabled` | boolean | `true` | Enable Bean proxy |

### Database Storage

To use database-backed route storage:

```yaml
loadup:
  gateway:
    storage:
      type: DATABASE
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/loadup_gateway
    username: root
    password: secret
```

The database plugin uses MyBatis-Flex for data access and Flyway for schema migrations. Tables are auto-created on startup.

---

## Route Configuration

### Route Matching

Routes are matched by HTTP method and exact path. Path pattern matching is not supported in the current version — a `/api/user/login` route only matches a literal `/api/user/login` request.

The route cache uses a method-sensitive key: `{METHOD}:{PATH}` (e.g., `POST:/api/user/login`). This means you can have different routes for `GET /api/user` and `POST /api/user`.

### Route Lifecycle

1. On startup, the `RouteResolver` loads all enabled routes from the storage plugin into an in-memory cache
2. On each request, the cache is checked first; a storage lookup is a fallback
3. A `refreshRoutes()` method rebuilds the cache using double-buffering (build new map, atomic reference swap) to avoid cache clear-to-populate gaps

### Disabled Routes

Routes with `enabled: false` are skipped during cache loading and will not match. Requests to disabled routes fall through to your application's normal request handling.

---

## Protocol Support

### HTTP Proxy (`proxy-http-plugin`)

Forwards requests to HTTP/HTTPS backends using Spring's `RestClient`. Supports all standard HTTP methods (GET, POST, PUT, DELETE, PATCH). Query parameters from the original request are appended to the target URL.

**Target format:** `http://host:port/path` or `https://host:port/path`

### RPC Proxy (`proxy-rpc-plugin`)

Proxies requests to Apache Dubbo services using Dubbo's `GenericService` for dynamic invocation. Service references are cached in a `ConcurrentHashMap` to avoid repeated `ReferenceConfig` construction.

**Target format:** `rpc://interfaceName:methodName:version`

Version is optional. Example: `rpc://com.example.OrderService:createOrder:1.0.0`

Parameters are parsed from the request body — JSON arrays become positional arguments, JSON objects become `Map` arguments. The plugin requires Dubbo and a registry (ZooKeeper/Nacos) to be configured.

### Bean Proxy (`proxy-springbean-plugin`)

Invokes methods on Spring-managed beans directly — no network calls. Useful for internal service composition where you want a single HTTP endpoint that aggregates multiple in-process services.

**Target format:** `bean://beanName:methodName`

The method receives typed arguments based on parameter types: `GatewayRequest` → the full request object, `String` → raw request body, other types → JSON deserialization from the request body. User context from security authentication is propagated via `UserContext.set()` before invocation and cleared in a `finally` block.

---

## Security

### Strategy Selection

Each route specifies a `securityCode` that maps to a registered `SecurityStrategy` bean. Built-in strategies:

| Code | Strategy | Description |
|------|----------|-------------|
| `OFF` | No security | Passes through (default if not configured) |
| `default` | JWT Authentication | Validates Bearer token, extracts user info |
| `signature` | HMAC-SHA256 | Validates request signature (replay protection) |
| `internal` | IP Whitelist | Allows only private/loopback IPs |

### Default (JWT) Strategy

Extracts the `Authorization: Bearer <token>` header, parses and validates the JWT, then populates downstream headers and request attributes:
- `X-User-Id`, `X-User-Name`, `X-User-Roles` — passed to backend services
- `userId`, `username`, `roles`, `claims` — stored in `request.attributes` for use in templates

### Signature Strategy

Validates HMAC-SHA256 signatures for API key authentication:
- Headers required: `X-App-Id`, `X-Timestamp`, `X-Nonce`, `X-Signature`
- Signature calculation: `HMAC-SHA256(sortedQueryParams&timestamp=<ts>&nonce=<nonce>, appSecret)`
- Timestamp tolerance: ±5 minutes (replay protection)
- App secrets are currently hardcoded (TODO: store in database)

### Internal Strategy

Allows requests only from internal IP ranges (127.0.0.1, 10.x.x.x, 172.16-31.x.x, 192.168.x.x) or requests with `X-Internal-Call: true` header. Checks `X-Forwarded-For` and `X-Real-IP` headers for proxy-awareness.

### Custom Security Strategy

Implement the `SecurityStrategy` SPI:

```java
public class MyCustomSecurity implements SecurityStrategy {
    @Override
    public String getCode() { return "my-strategy"; }

    @Override
    public void process(GatewayContext context) {
        // Your authentication logic here
        // Throw GatewayExceptionFactory.unauthorized("message") on failure
        // Store user info in context.getRequest().getAttributes()
    }
}
```

Register your implementation as a Spring bean — it will be auto-discovered by `SecurityStrategyManager`.

---

## Template Engine

The template engine executes Groovy scripts to transform requests before proxying and responses before returning. Scripts are [compiled and cached](https://groovy-lang.org/integrating.html) by script text (not by filename) for performance.

### Available Variables

**Request templates** receive: `request` (`GatewayRequest`), `log` (`org.slf4j.Logger`)

**Response templates** receive: `response` (`GatewayResponse`), `log` (`org.slf4j.Logger`)

### Import Convention

Templates have access to the application classpath. Common imports:

```groovy
import io.github.loadup.gateway.facade.model.GatewayRequest
import io.github.loadup.gateway.facade.model.GatewayResponse
import io.github.loadup.commons.util.JsonUtil
```

### Template Resolution (File Storage)

When a route references `requestTemplate: my_template`, the file repository plugin searches for:
1. `{basePath}/templates/my_template`
2. `{basePath}/templates/my_template.groovy`
3. `{basePath}/templates/my_template_request.groovy`
4. `{basePath}/templates/my_template_request_template.groovy`
5. Same paths as classpath resources (fallback)

---

## Response Wrapping

The `ResponseWrapperAction` can wrap backend responses in a unified envelope:

```json
{
  "result": {
    "code": "SUCCESS",
    "status": "SUCCESS",
    "message": null
  },
  "data": { /* original response body */ },
  "meta": {
    "requestId": "20260713120000001",
    "timestamp": "2026-07-13T12:00:00.123"
  }
}
```

Wrapping is controlled at three levels (evaluated in order):

1. **Route-level**: `wrapResponse` property in the route's `properties`
2. **Global default**: `loadup.gateway.response.wrap`
3. **Sub-settings**: `wrapResult` (include `result` block) and `wrapMeta` (include `meta` block)

---

## Distributed Tracing

The `TracingAction` integrates with OpenTelemetry to provide distributed tracing. It:

1. **Extracts** the parent trace context from incoming request headers
2. **Creates** a SERVER span named `gateway.{METHOD}` with attributes: `http.method`, `http.target`, `gateway.route`, `gateway.request_id`, `http.client_ip`
3. **Propagates** trace context into request headers before forwarding to the backend
4. **Records** the response status code on the span, with `StatusCode.ERROR` for 4xx/5xx
5. **Ends** the span in a `finally` block

The action is conditionally created — it requires `Tracer` and `TextMapPropagator` beans on the classpath and `loadup.tracer.enabled=true` (default).

---

## Plugin System

### SPI Interfaces

| Interface | Purpose | Key Methods |
|-----------|---------|-------------|
| `GatewayPlugin` | Base plugin contract | `getName()`, `getType()`, `initialize()`, `destroy()` |
| `ProxyProcessor` | Protocol-specific proxying | `proxy(request, route)`, `getSupportedProtocol()` |
| `RepositoryPlugin` | Route/template storage | `getRoute()`, `getRouteByPath()`, `getAllRoutes()`, `getTemplate()` |
| `SecurityStrategy` | Authentication/authorization | `getCode()`, `process(context)` |

### Plugin Discovery

All plugin implementations are registered as Spring beans in their respective `*AutoConfiguration` classes. The `PluginManager` collects all `ProxyProcessor` beans via constructor injection. The `SecurityStrategyManager` collects all `SecurityStrategy` beans. The gateway starter creates and wires these via `@AutoConfiguration`.

### Creating a Custom Proxy Plugin

1. Implement `ProxyProcessor`:
```java
public class GrpcProxyProcessor implements ProxyProcessor {
    @Override
    public String getSupportedProtocol() { return "GRPC"; }

    @Override
    public GatewayResponse proxy(GatewayRequest request, RouteConfig route) throws Exception {
        // gRPC invocation logic
    }
    // ... other methods
}
```

2. Create an auto-configuration:
```java
@AutoConfiguration
public class GrpcProxyAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public GrpcProxyProcessor grpcProxyProcessor() {
        return new GrpcProxyProcessor();
    }
}
```

3. Register in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

### Creating a Custom Storage Plugin

1. Implement `RepositoryPlugin`:
```java
public class RedisRepositoryPlugin implements RepositoryPlugin {
    @Override
    public String getSupportedStorageType() { return "REDIS"; }

    @Override
    public Optional<RouteConfig> getRouteByPath(String path, String method) {
        // Redis lookup logic
    }
    // ... other methods
}
```

2. Create auto-configuration and register it.

---

## Module Structure

```
loadup-cloud/loadup-gateway/
├── pom.xml                              # Parent POM (multi-module)
├── loadup-gateway-facade/               # SPI interfaces, models, exceptions, config
│   └── src/main/java/.../facade/
│       ├── api/GatewayHandler.java      # (empty marker interface)
│       ├── config/GatewayProperties.java
│       ├── constants/GatewayConstants.java
│       ├── context/GatewayContext.java
│       ├── dto/RouteStructure.java
│       ├── exception/                   # GatewayException + subclasses + factory
│       ├── model/                       # GatewayRequest, GatewayResponse, RouteConfig, PluginConfig
│       ├── spi/                         # GatewayPlugin, ProxyProcessor, RepositoryPlugin, SecurityStrategy
│       └── utils/                       # JsonUtils, CommonUtils
├── loadup-gateway-core/                 # Core processing engine
│   └── src/main/java/.../core/
│       ├── action/                      # GatewayAction chain (8 actions)
│       ├── handler/                     # GatewayHandler, GatewayHandlerMapping, GatewayHandlerAdapter
│       ├── plugin/PluginManager.java
│       ├── router/RouteResolver.java
│       ├── security/                    # SecurityStrategyManager + 3 strategies
│       └── template/TemplateEngine.java
├── loadup-gateway-starter/              # Spring Boot auto-configuration
│   └── src/main/java/.../starter/
│       └── GatewayAutoConfiguration.java
├── loadup-gateway-test/                 # Integration tests + sample configs
│   └── src/test/resources/
│       ├── csv/routes.csv               # Sample route definitions
│       └── templates/*.groovy           # Sample Groovy templates
└── plugins/                             # Protocol adapters & storage backends
    ├── proxy-http-plugin/               # HTTP proxy (Spring RestClient)
    ├── proxy-rpc-plugin/                # Dubbo RPC proxy (GenericService)
    ├── proxy-springbean-plugin/         # Spring Bean proxy (reflection)
    ├── repository-file-plugin/          # CSV file storage (OpenCSV)
    └── repository-database-plugin/      # Database storage (MyBatis-Flex + Flyway)
```

---

## Development

### Build

```bash
# Full build with all checks
mvn clean verify

# Quick build (skip tests, code quality checks)
mvn clean package -P dev -DskipTests

# Run tests
mvn test

# Run integration tests
mvn verify -Pit
```

### Code Quality

The project enforces code quality at build time through multiple Maven plugins:

| Plugin | Phase | Purpose |
|--------|-------|---------|
| Spotless | validate | Code formatting (Palantir Java Format) |
| Checkstyle | verify | Coding style conventions |
| PMD | verify | Static analysis (potential bugs) |
| SpotBugs + FindSecBugs | verify | Bytecode-level bug/security detection |
| OWASP Dependency Check | verify | Known vulnerability detection (CVSS >= 7) |
| Maven Enforcer | validate | Banned dependency enforcement |
| JaCoCo | test | Code coverage reporting |

### Profiles

| Profile | Purpose |
|---------|---------|
| `dev` (default) | Skips code quality checks for fast iteration |
| `github` | Publishes to GitHub Packages Maven registry |
| `release` | Publishes to Maven Central (with GPG signing) |
| `skip-tests` | Skips all tests (activated by `-DskipTests`) |

### Banned Dependencies

The enforcer plugin blocks: `commons-lang:commons-lang` (use `commons-lang3`), `cn.hutool`, `com.alibaba:fastjson` < 2.0, `log4j:log4j`, `junit:junit` (use JUnit 5), `slf4j-log4j12`, and old `commons-collections`.

---

## Exception Handling

### Exception Hierarchy

```
RuntimeException
  └── GatewayException (has errorCode, errorType, module)
        ├── RouteException
        ├── PluginException
        ├── ProxyException
        ├── ValidationException
        ├── SystemException
        ├── SerializationException
        └── TemplateException
```

### Error Types

`ErrorType` enum: `CONFIGURATION`, `ROUTING`, `PLUGIN`, `PROXY`, `VALIDATION`, `BUSINESS`, `SYSTEM`, `NETWORK`, `SERIALIZATION`, `TEMPLATE`, `STORAGE`, `SECURITY`, `AUTHORIZATION`, `RATE_LIMIT`, `TIMEOUT`, `UNKNOWN`

### Error Response Format

All exceptions caught by `ExceptionAction` produce:

```json
{
  "result": {
    "code": "PROCESS_FAIL",
    "status": "FAIL",
    "message": "Human-readable error message"
  },
  "data": null,
  "meta": {
    "requestId": "20260713120000001",
    "timestamp": "2026-07-13T12:00:00.123"
  }
}
```

HTTP status codes are derived from the error type: ROUTING → 404, VALIDATION → 400, SYSTEM/NETWORK → 500, others → 500.

---

## Roadmap & Improvement Opportunities

The following areas have been identified for future enhancement:

### High Priority

**1. Route pattern matching (wildcards & path variables)**
Currently only exact-path matching is supported. Adding Ant-style or regex path patterns (e.g., `/api/user/{id}`) and path variable extraction would significantly improve routing flexibility.

**2. Request rate limiting**
An `ErrorType.RATE_LIMIT` already exists in the enum. A new `RateLimitAction` in the chain could enforce per-route or per-client limits using token-bucket or sliding-window algorithms, backed by Redis for distributed deployments.

**3. Circuit breaker integration**
Proxy requests should be wrapped with a circuit breaker (Resilience4j or Sentinel). When a backend service becomes unhealthy, the circuit breaker would short-circuit requests and return fallback responses, preventing cascading failures.

**4. WebSocket support**
Currently only HTTP request-response is supported. WebSocket proxying would allow the gateway to handle real-time bidirectional communication for use cases like chat, notifications, and live dashboards.

### Medium Priority

**5. Response caching**
Add configurable response caching at the gateway level (per-route TTL, cache-by-headers). This would reduce load on backend services for idempotent, frequently-accessed endpoints. Integration with Redis or Caffeine.

**6. Request/response logging and audit trail**
A dedicated audit action could log full request/response payloads (with sensitive field masking) for compliance and debugging. Configurable per-route with sampling rates.

**7. Dynamic route management API**
A REST API for CRUD operations on routes at runtime (without restart). Currently routes are loaded from storage at startup. An admin API plus a notification mechanism (e.g., Spring events) would enable true dynamic routing.

**8. Multi-tenancy support**
Add tenant-aware routing where a `X-Tenant-Id` header influences route resolution, allowing different tenants to have different routing rules, rate limits, or backend targets.

**9. Request/response compression and content negotiation**
Automatic gzip/brotli decompression of incoming requests and compression of responses. Content negotiation based on `Accept` headers to transform between JSON, XML, and protobuf.

### Lower Priority

**10. gRPC protocol support**
Add a `proxy-grpc-plugin` using gRPC-Java's dynamic message support, similar to how the RPC plugin uses Dubbo's `GenericService`.

**11. API versioning strategy**
Support versioned routes (e.g., `/v1/api/user` vs `/v2/api/user`) with automatic header-based or path-based version routing and deprecation notices.

**12. Configurable retry with backoff**
The current retry mechanism uses a flat count. Adding exponential backoff with jitter, configurable retry conditions (only on 5xx, only on specific exceptions), and a dead-letter queue for exhausted retries would improve resilience.

**13. Prometheus metrics and health endpoints**
Expose gateway-level metrics (request count, latency percentiles, error rates, cache hit rates) via Micrometer/Prometheus. Add a `/health` endpoint that checks connectivity to all configured backends and storage.

**14. GraalVM native image support**
Configure the project for Ahead-of-Time compilation with Spring Boot's native support. This would dramatically reduce startup time and memory footprint, making the gateway suitable for serverless and edge deployments.

**15. Security strategy enhancements**
- Move app secrets from hardcoded maps to database/config
- Add OAuth2/OIDC support with token introspection
- Add IP blacklisting alongside the existing whitelist
- Support mTLS for backend connections

---

## License

This project is licensed under the GNU General Public License v3.0 — see the [LICENSE](LICENSE) file for details.

**Author:** [Laysan](mailto:lslvxy@gmail.com)
**Organization:** [LoadUp Cloud](https://github.com/loadup-cloud)

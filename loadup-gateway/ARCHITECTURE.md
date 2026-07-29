# LoadUp Gateway — Architecture

## Overview

LoadUp Gateway is an **embedded, multi-protocol API gateway** distributed as a Spring Boot library. It intercepts HTTP requests via Spring MVC `HandlerMapping` / `HandlerAdapter`, resolves routes from YAML (or database) configuration, executes per-route named filter chains, and proxies to backend services via HTTP, Dubbo RPC, or in-process Spring Bean invocation.

**Core design principle**: every route declares its own filter pipeline. No hardcoded global chain.

```
HTTP Request
  → GatewayHandlerMapping  (Spring HandlerMapping, highest precedence)
  → GatewayHandlerAdapter  (builds GatewayContext from HttpServletRequest)
  → GatewayEngine          (orchestrator)
      → ExceptionFilter    (outermost try/catch)
        → TracingFilter    (OpenTelemetry, optional)
          → RouteFilter    (resolve route → build per-route sub-chain)
            → [route.filters...]     (YAML-declared: body-parser, rate-limit, security, ...)
            → ProxyFilter            (HTTP / RPC / Bean dispatch)
            → [route.responseFilters...]
            → ResponseWrapperFilter
```

---

## Module Architecture

```
loadup-gateway/
├── loadup-gateway-facade/          SPI + models + config (zero internal deps)
│   ├── spi/GatewayFilter.java      Named filter interface
│   ├── spi/FilterChain.java        Chain cursor
│   ├── spi/RouteStore.java         Route definition storage SPI
│   ├── spi/ProxyProcessor.java     Backend protocol SPI
│   ├── spi/SecurityStrategy.java   Auth strategy SPI
│   ├── model/RouteDefinition.java  YAML-friendly route config
│   ├── model/FilterDefinition.java YAML-friendly filter config
│   ├── model/RouteConfig.java      Compiled runtime route (immutable)
│   ├── model/GatewayRequest.java   Inbound request
│   ├── model/GatewayResponse.java  Outbound response
│   ├── context/GatewayContext.java Per-request state bucket
│   └── config/GatewayProperties.java
├── loadup-gateway-core/            Processing engine
│   ├── engine/DefaultGatewayEngine.java  Orchestrator
│   ├── engine/DefaultFilterChain.java    Array-based cursor chain
│   ├── filter/*.java                     Exception, Route, Proxy, Security,
│   │                                     RateLimit, CircuitBreaker, BodyParser,
│   │                                     ResponseWrapper, Tracing
│   ├── router/RouteResolver.java         Route cache + pattern matching
│   ├── security/*.java                   JWT, HMAC signature, internal IP
│   └── handler/*.java                    Spring MVC integration
├── loadup-gateway-starter/          Auto-configuration
│   └── GatewayAutoConfiguration.java
└── plugins/
    ├── proxy-http-plugin/           HTTP proxy (RestClient)
    ├── proxy-rpc-plugin/            Dubbo RPC (GenericService)
    ├── proxy-springbean-plugin/     Spring Bean invocation
    ├── repository-yaml-plugin/      YAML file store + WatchService hot reload
    └── repository-database-plugin/  JDBC store (Spring Data, admin CRUD)
```

**Dependency direction**: `facade` ← `core` ← `plugins` ← `starter`

---

## Request Processing Pipeline

### Entry Points

1. **GatewayHandlerMapping** (`extends AbstractHandlerMapping`, `Ordered.HIGHEST_PRECEDENCE`)
   - Spring calls `getHandlerInternal(request)` for every request
   - Uses `RouteResolver.resolve()` for exact + pattern matching
   - Returns `GatewayHandler` with pre-resolved `RouteConfig`, or `null` to fall through to normal controllers

2. **GatewayHandlerAdapter** (`implements HandlerAdapter`)
   - `supports(handler)` → returns `true` for `GatewayHandler`
   - `handle()`: builds `GatewayContext`, calls `engine.execute(context)`, writes HTTP response
   - Extracts real client IP from `X-Forwarded-For` / `X-Real-IP` headers

### Engine Execution

`DefaultGatewayEngine.execute(context)`:

```
ExceptionFilter.filter(context, chain)
  → TracingFilter.filter(context, chain)          [optional]
    → RouteFilter.filter(context, chain)
      1. resolve RouteConfig via RouteResolver
      2. context.setRoute(route)
      3. build per-route sub-chain from RouteDefinition:
         [request filters by name] → ProxyFilter → [response filters by name] → ResponseWrapperFilter
      4. new DefaultFilterChain(subFilters).filter(context)
```

### Filter Chain

`DefaultFilterChain` is an array-based cursor:

```java
public void filter(GatewayContext context) {
    if (cursor >= filters.size()) return;
    filters.get(cursor++).filter(context, this);  // passes itself as 'chain'
}
```

Each filter calls `chain.filter(context)` to advance. A filter that does not call `chain.filter()` terminates the pipeline (used by ExceptionFilter on error, CircuitBreakerFilter on OPEN).

---

## Route Configuration (YAML)

Routes are declared in `gateway-routes.yml`:

```yaml
routes:
  - id: user-api
    path: /api/users
    method: POST
    backend:
      protocol: http
      url: http://user-service:8080/users
    filters:
      - name: body-parser
      - name: rate-limit
        props:
          capacity: 100
          refillRate: 10
      - name: security
    responseFilters:
      - name: response-wrapper
    securityCode: default
    timeout: 5000
    wrapResponse: true
```

**Hot reload**: `YamlRouteStore` uses Java `WatchService` to monitor the YAML file. Changes are detected within 5 seconds, routes re-parsed, and the `RouteResolver` cache refreshed atomically.

---

## Key Components

### RouteStore (SPI)

```java
public interface RouteStore {
    List<RouteDefinition> loadAll();
    Optional<RouteDefinition> load(String routeId);
    RouteDefinition save(RouteDefinition def);   // admin API
    void delete(String routeId);                  // admin API
}
```

Implementations:
- `YamlRouteStore` — file-based, default, hot reload via WatchService
- `DatabaseRouteStore` — Spring Data JDBC, full CRUD

Switched via `loadup.gateway.storage.type=FILE|DATABASE`.

### GatewayFilter (SPI)

```java
public interface GatewayFilter {
    String name();   // matches YAML filter name (e.g. "rate-limit")
    void filter(GatewayContext context, FilterChain chain);
}
```

Built-in filters:

| Name | Class | Phase | Description |
|------|-------|-------|-------------|
| `exception` | ExceptionFilter | global pre | Outermost try/catch, unified `{result,data,meta}` error JSON |
| `tracing` | TracingFilter | global pre | OpenTelemetry span (optional, `@ConditionalOnClass`) |
| `route` | RouteFilter | global pre | Route resolution pivot, builds per-route sub-chain |
| `body-parser` | BodyParserFilter | request | Parses JSON/form body → `request.attributes["parsedBody"]` |
| `rate-limit` | RateLimitFilter | request | Token-bucket, Caffeine eviction, per-route config |
| `security` | SecurityFilter | request | Delegates to SecurityStrategy by route.securityCode |
| `circuit-breaker` | CircuitBreakerFilter | request | CLOSED→OPEN→HALF_OPEN lifecycle, Caffeine eviction |
| `proxy` | ProxyFilter | terminal | Dispatches to ProxyProcessor by protocol |
| `response-wrapper` | ResponseWrapperFilter | response | Wraps in `{result, data, meta}`, respects wrapResponse config |

### RouteResolver

- Exact-match cache: `ConcurrentHashMap<String, RouteConfig>`, key `METHOD:PATH`
- Pattern registry: `PatternRouteRegistry` for Ant-style paths (`/api/user/{id}`)
- Double-buffered atomic reference swap on refresh
- Converts `RouteDefinition → RouteConfig` internally

### Security Strategies

| Code | Strategy | Config |
|------|----------|--------|
| `OFF` | No-op | — |
| `default` | JWT Bearer token | `loadup.gateway.security.secret` |
| `signature` | HMAC-SHA256 | `loadup.gateway.security.app-secrets.{appId}` |
| `internal` | IP whitelist | Hardcoded private ranges |

All instantiated via direct constructor (no reflection). App secrets loaded from GatewayProperties.

---

## Thread Safety

| Component | Shared State | Strategy |
|-----------|-------------|----------|
| RouteResolver | `volatile ConcurrentHashMap` | Atomic reference swap |
| RateLimitFilter | `Caffeine Cache` | Built-in concurrency |
| CircuitBreakerFilter | `Caffeine Cache` | Built-in concurrency |
| TemplateEngine | `ConcurrentHashMap` | computeIfAbsent |
| DefaultGatewayEngine | `Map<String, GatewayFilter>` | Write-once, read-only |
| GatewayContext | Per-request instance | No sharing |

---

## Design Decisions

1. **Per-route filter chains**: Each route declares its own pipeline in YAML, rather than passing through a fixed global chain. This avoids coupling — a public health-check route has zero filters, a payment route has 5.

2. **RouteFilter as pivot**: Route resolution happens inside the chain (not before it), so route-not-found errors are caught by the ExceptionFilter and produce structured 404 responses.

3. **Caffeine for bounded maps**: All formerly unbounded `ConcurrentHashMap` instances (rate limit buckets, circuit breakers) now use Caffeine with `maximumSize` + `expireAfterAccess`.

4. **Direct constructor injection**: No `@Resource`, no `Class.forName()` reflection in auto-configuration. All beans are constructed with explicit constructor arguments.

5. **Unified error format**: Single `ExceptionFilter` produces `{result: {code, status, message}, data: null, meta: {requestId, timestamp}}`. No more dual-format inconsistency.

6. **Real client IP extraction**: GatewayHandlerAdapter extracts from `X-Forwarded-For` → `X-Real-IP` → `Proxy-Client-IP` → `getRemoteAddr()`.

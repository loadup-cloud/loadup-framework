# LoadUp Gateway — Architecture Document

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Module Architecture](#2-module-architecture)
3. [Request Processing Pipeline](#3-request-processing-pipeline)
4. [Component Reference](#4-component-reference)
5. [SPI Contracts](#5-spi-contracts)
6. [Data Flow Diagrams](#6-data-flow-diagrams)
7. [Design Decisions](#7-design-decisions)
8. [Thread Safety & Concurrency](#8-thread-safety--concurrency)
9. [Extending the Gateway](#9-extending-the-gateway)

---

## 1. System Overview

LoadUp Gateway is a **multi-protocol, plugin-based API gateway** implemented as a Spring Boot library. It is not a standalone server — it is embedded into a Spring Boot application via auto-configuration, intercepting HTTP requests through Spring MVC's `HandlerMapping` / `HandlerAdapter` extension points.

### Core Design Goals

1. **Framework, not server**: Embedded as a library dependency; no separate deployment
2. **Protocol-agnostic**: One entry point, any backend protocol (HTTP, RPC, in-process bean)
3. **Pluggable**: Storage, protocols, and security are SPI-based; new implementations require no core changes
4. **Hot-reloadable**: Route configuration can change without restarting
5. **Observable**: Integrated OpenTelemetry tracing, structured logging, typed exceptions

### Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 4.1 |
| Spring Integration | `HandlerMapping`, `HandlerAdapter`, `@AutoConfiguration` |
| Template Engine | Groovy (compiled, cached) |
| HTTP Proxy | Spring `RestClient` |
| RPC Proxy | Apache Dubbo `GenericService` |
| Database | MyBatis-Flex + Flyway (migrations) |
| File Storage | OpenCSV (routes), filesystem (templates) |
| Tracing | OpenTelemetry (Tracer, TextMapPropagator) |
| Code Quality | Spotless, Checkstyle, PMD, SpotBugs, OWASP |

---

## 2. Module Architecture

The project is organized as a Maven multi-module project with strict dependency direction: **facade ← core ← plugins ← starter**.

```
                     ┌──────────────────────┐
                     │  loadup-gateway-     │
                     │  starter             │
                     │  (AutoConfiguration) │
                     └──────────┬───────────┘
                                │ assembles
              ┌─────────────────┼─────────────────┐
              │                 │                 │
    ┌─────────▼────────┐ ┌─────▼──────┐ ┌────────▼─────────┐
    │ loadup-gateway-  │ │  plugins/  │ │  plugins/        │
    │ core             │ │  proxy-*   │ │  repository-*    │
    │ (request engine) │ │  (HTTP,    │ │  (FILE, DATABASE)│
    └────────┬─────────┘ │  RPC, Bean)│ └────────┬─────────┘
             │           └─────┬──────┘          │
             │                 │                 │
             └─────────┬───────┘                 │
                       │                         │
              ┌────────▼─────────────────────────▼──┐
              │  loadup-gateway-facade              │
              │  (SPI interfaces, models, config)   │
              └─────────────────────────────────────┘
```

### Module Dependency Graph

```
loadup-gateway-starter
  ├── loadup-gateway-core
  ├── plugins/proxy-http-plugin
  ├── plugins/proxy-rpc-plugin
  ├── plugins/proxy-springbean-plugin
  ├── plugins/repository-file-plugin
  └── plugins/repository-database-plugin

loadup-gateway-core
  └── loadup-gateway-facade

plugins/* (all plugins)
  └── loadup-gateway-facade

loadup-gateway-facade
  └── (no internal module dependencies)
```

**Key rule**: The facade module has zero internal dependencies. It defines all shared contracts. Core and plugins both depend on facade, but never on each other.

### Module Responsibilities

#### `loadup-gateway-facade` — Shared API Contract

- **SPI interfaces**: `GatewayPlugin`, `ProxyProcessor`, `RepositoryPlugin`, `SecurityStrategy`
- **Data models**: `GatewayRequest`, `GatewayResponse`, `RouteConfig`, `PluginConfig`, `RouteStructure`
- **Context object**: `GatewayContext` — the per-request state bucket passed through the entire pipeline
- **Configuration**: `GatewayProperties` — Spring Boot `@ConfigurationProperties` with full type hierarchy
- **Constants**: `GatewayConstants` — protocol types, storage types, HTTP methods, content types, property keys
- **Exception hierarchy**: `GatewayException` + typed subclasses + `GatewayExceptionFactory`
- **Utilities**: `JsonUtils`, `CommonUtils`

#### `loadup-gateway-core` — Processing Engine

- **Action chain**: 8 `GatewayAction` implementations forming a Chain of Responsibility
- **Route resolution**: `RouteResolver` with in-memory cache and double-buffered refresh
- **Template engine**: `TemplateEngine` using Groovy with compiled script caching
- **Plugin management**: `PluginManager` (protocol dispatch), `SecurityStrategyManager` (strategy registration)
- **Security strategies**: `DefaultSecurityStrategy` (JWT), `SignatureSecurityStrategy` (HMAC-SHA256), `InternalSecurityStrategy` (IP whitelist)
- **Spring MVC integration**: `GatewayHandlerMapping` (extends `AbstractHandlerMapping`), `GatewayHandlerAdapter` (implements `HandlerAdapter`)

#### `loadup-gateway-starter` — Auto-Configuration

- `GatewayAutoConfiguration`: Single class defining all beans explicitly (no component scanning)
- Conditionally creates beans based on classpath presence (`@ConditionalOnClass`, `@ConditionalOnProperty`)
- Assembles the action chain in order: Exception → Tracing → Route → Security → RequestTemplate → Proxy → ResponseTemplate → ResponseWrapper

#### Plugins — Protocol Adapters & Storage Backends

| Plugin | Type | Protocol/Storage |
|--------|------|------------------|
| `proxy-http-plugin` | Proxy | HTTP/HTTPS via Spring RestClient |
| `proxy-rpc-plugin` | Proxy | Dubbo RPC via GenericService |
| `proxy-springbean-plugin` | Proxy | Spring Bean method invocation |
| `repository-file-plugin` | Repository | CSV files + filesystem templates |
| `repository-database-plugin` | Repository | MySQL/PostgreSQL via MyBatis-Flex |

---

## 3. Request Processing Pipeline

### 3.1 Entry Points

The gateway intercepts requests through Spring MVC's standard extension points:

1. **`GatewayHandlerMapping`** (extends `AbstractHandlerMapping`, `Ordered.HIGHEST_PRECEDENCE`)
   - Spring calls `getHandlerInternal(request)` for every request
   - Looks up the route from the `RepositoryPlugin` by request URI
   - Returns a `GatewayHandler` (with pre-resolved `RouteConfig`) if a route matches, or `null` to let Spring fall through to normal controllers

2. **`GatewayHandlerAdapter`** (implements `HandlerAdapter`, `Ordered.HIGHEST_PRECEDENCE`)
   - Spring calls `supports(handler)` → returns `true` for `GatewayHandler` instances
   - `handle(request, response, handler)` is the main processing method

### 3.2 GatewayHandlerAdapter.handle() Flow

```
handle(HttpServletRequest, HttpServletResponse, Object handler)
    │
    ├─ 1. Build GatewayContext
    │     ├─ buildGatewayRequest(HttpServletRequest)
    │     │     ├─ Extract all headers
    │     │     ├─ Parse query parameters
    │     │     ├─ Read request body (reader.lines())
    │     │     └─ Set requestId from TraceUtil.getTracerId()
    │     └─ Pre-populate context.route from GatewayHandler.routeConfig
    │
    ├─ 2. Dispatch to Action Chain
    │     └─ actionDispatcher.dispatch(context)
    │
    └─ 3. Write HTTP Response
          ├─ Set status code
          ├─ Set response headers (skip content-length/transfer-encoding)
          ├─ Set content-type with charset detection
          └─ Write body + flush
```

### 3.3 Action Chain Order & Purpose

The `ActionDispatcher` creates a `DefaultGatewayActionChain` that executes actions sequentially. Each action calls `chain.proceed(context)` to pass control to the next action.

```
┌──────────────────────────────────────────────────────────────────────┐
│ 1. ExceptionAction                                                    │
│    Wraps entire chain in try/catch. Catches GatewayException and      │
│    unknown exceptions, builds unified error responses.                │
│    DOES NOT call chain.proceed() on exception — terminates chain.     │
├──────────────────────────────────────────────────────────────────────┤
│ 2. TracingAction (conditional — requires Tracer on classpath)         │
│    Extracts parent span context, creates SERVER span, propagates      │
│    context to downstream headers. Wraps chain.proceed() in scope.     │
│    Records HTTP status on span after chain completes.                 │
├──────────────────────────────────────────────────────────────────────┤
│ 3. RouteAction                                                        │
│    Checks if route is already resolved (from GatewayHandler).         │
│    If not, calls routeResolver.resolve(request) as fallback.          │
│    Throws RouteException if no route found.                           │
├──────────────────────────────────────────────────────────────────────┤
│ 4. SecurityAction                                                     │
│    Reads route.securityCode. Skips if blank or "OFF".                 │
│    Looks up SecurityStrategy by code in SecurityStrategyManager.      │
│    Calls strategy.process(context).                                   │
│    Throws GatewayException (401/403) on failure.                      │
├──────────────────────────────────────────────────────────────────────┤
│ 5. RequestTemplateAction                                              │
│    If route has requestTemplate, runs it through TemplateEngine.      │
│    Template receives `request` and `log` as bound variables.          │
│    Replaces context.request with the template's return value.         │
├──────────────────────────────────────────────────────────────────────┤
│ 6. ProxyAction                                                        │
│    Calls pluginManager.executeProxy(request, route).                  │
│    PluginManager dispatches to correct ProxyProcessor by protocol.    │
│    Sets context.response with the GatewayResponse from the proxy.     │
├──────────────────────────────────────────────────────────────────────┤
│ 7. ResponseTemplateAction                                             │
│    Calls chain.proceed() FIRST (ProxyAction must execute before).     │
│    Then: if route has responseTemplate, runs it through TemplateEngine│
│    Template receives `response` and `log` as bound variables.         │
│    Replaces context.response with the template's return value.        │
├──────────────────────────────────────────────────────────────────────┤
│ 8. ResponseWrapperAction                                              │
│    Calls chain.proceed() FIRST.                                       │
│    Then: checks wrapResponse (route-level → global config).           │
│    If wrapping enabled: parses response body as JSON, wraps in        │
│    { result, data, meta } format, updates Content-Type headers.       │
└──────────────────────────────────────────────────────────────────────┘
```

**Important ordering nuance**: `ResponseTemplateAction` and `ResponseWrapperAction` call `chain.proceed()` *before* their own logic. This is intentional — the ProxyAction must execute first to produce a response. These are effectively post-processing hooks.

### 3.4 Chain Implementation

`DefaultGatewayActionChain` uses an `AtomicInteger` index to track position:

```java
public void proceed(GatewayContext context) {
    if (index.get() < actions.size()) {
        GatewayAction action = actions.get(index.getAndIncrement());
        action.execute(context, this);  // 'this' is the chain itself
    }
}
```

Each action receives `this` (the chain) and calls `chain.proceed(context)` to advance. If an action does not call `proceed()`, the chain terminates (used by `ExceptionAction` to stop processing on error).

---

## 4. Component Reference

### 4.1 RouteResolver

**Purpose**: Maintain an in-memory route cache and provide route lookup by path+method.

**Key design decisions**:
- Uses `volatile ConcurrentHashMap` with atomic reference swap for cache refresh — readers always see either the old complete map or the new one, never a partially-populated map
- Cache key: `{METHOD}:{PATH}` (e.g., `POST:/api/user/login`)
- On cache miss, falls back to `repositoryPlugin.getRouteByPath()`, then updates cache
- Refresh happens on `@PostConstruct`; periodic refresh is commented out (TODO)

**Thread safety**:
- `volatile` reference ensures visibility of the cache swap across threads
- `ConcurrentHashMap` ensures safe concurrent reads during the swap
- Cache writes (`put()`) are on the current reference; concurrent reads on a different reference are safe

### 4.2 PluginManager

**Purpose**: Registry of `ProxyProcessor` instances, dispatched by protocol string.

**Architecture**:
- Collects all `ProxyProcessor` beans via constructor injection
- Builds a `Map<String, ProxyProcessor>` keyed by `getSupportedProtocol()`
- `executeProxy()` looks up the processor for `route.getProtocol()` and calls `processor.proxy()`
- Duplicate protocols log a warning; last registration wins

### 4.3 TemplateEngine

**Purpose**: Execute Groovy scripts to transform requests and responses.

**Caching**: Scripts are compiled once and cached by script text (not filename) in a `ConcurrentHashMap<String, Script>`. Using text as the key avoids filename collisions when different routes reference different templates.

**Binding**: Request templates get `request` (GatewayRequest) + `log` (SLF4J). Response templates get `response` (GatewayResponse) + `log`.

**Error handling**: Template execution failures log a warning and return the original object unchanged — templates are best-effort transformations, not validation gates. However, `RequestTemplateAction` and `ResponseTemplateAction` wrap template exceptions as `TemplateException`.

### 4.4 SecurityStrategyManager

**Purpose**: Registry of `SecurityStrategy` instances, dispatched by strategy code.

**Built-in initial set**: "OFF" strategy (no-op) is always registered even if no strategy beans are present.

**Registration**: Strategies are injected via constructor. Duplicate codes log a warning; last registration wins.

### 4.5 GatewayContext

**Purpose**: Mutable request-scoped state container passed through the entire pipeline.

**Fields**:
- `request` (GatewayRequest) — the incoming request, potentially modified by templates
- `response` (GatewayResponse) — set by ProxyAction, potentially modified by response template/wrapper
- `route` (RouteConfig) — the matched route configuration
- `originalRequest` / `originalResponse` — raw servlet objects (for actions that need them)
- `attributes` (ConcurrentHashMap) — arbitrary key-value store for cross-action data sharing
- `exception` (Throwable) — captured exception for error reporting

### 4.6 RouteConfig

**Purpose**: Immutable configuration for a single route, parsed from storage.

**Design**: Manual builder pattern (not Lombok's `@Builder`) with all parsing, defaulting, and ID generation done at `build()` time. The resulting object is effectively immutable (defensive copy of `properties` map, no setters, final fields).

**Target parsing**: The `target` string is parsed at construction time to determine protocol, target URL, bean name, and method name based on URI scheme (`http://`, `bean://`, `rpc://`).

**Route ID generation**: `"route-" + abs(hash(path + ":" + method))` — deterministic, not suitable for production (collision risk), but functional.

---

## 5. SPI Contracts

### 5.1 GatewayPlugin (Base Interface)

```java
public interface GatewayPlugin {
    String getName();        // Human-readable name
    String getType();        // "PROXY" or "REPOSITORY"
    String getVersion();     // Semantic version
    int getPriority();       // Lower = higher priority
    void initialize();       // Lifecycle: called on startup
    void destroy();          // Lifecycle: called on shutdown
}
```

All plugins implement this interface. Priority is informational — in the current implementation, only one plugin of each type is active.

### 5.2 ProxyProcessor (extends GatewayPlugin)

```java
public interface ProxyProcessor extends GatewayPlugin {
    GatewayResponse proxy(GatewayRequest request, RouteConfig route) throws Exception;
    String getSupportedProtocol();  // "HTTP", "RPC", "BEAN" — must match GatewayConstants.Protocol
}
```

**Contract**:
- Receives the (potentially template-transformed) `GatewayRequest` and the matched `RouteConfig`
- Returns a fully-populated `GatewayResponse` (statusCode, body, headers, contentType)
- Must not throw checked exceptions that aren't `GatewayException` — wrap or rethrow as RuntimeException
- The protocol string must exactly match what `RouteConfig.getProtocol()` returns

### 5.3 RepositoryPlugin (extends GatewayPlugin)

```java
public interface RepositoryPlugin extends GatewayPlugin {
    Optional<RouteConfig> getRoute(String routeId) throws Exception;
    Optional<RouteConfig> getRouteByPath(String path, String method) throws Exception;
    List<RouteConfig> getAllRoutes() throws Exception;
    Optional<String> getTemplate(String templateId, String templateType) throws Exception;
    String getSupportedStorageType();  // "FILE" or "DATABASE"
    RouteConfig convertToRouteConfig(RouteStructure structure) throws Exception;
}
```

**Contract**:
- `getAllRoutes()` is the primary method — called on cache refresh
- `getRouteByPath()` is the fallback — called on cache miss
- `getTemplate()` maps a template ID + type to its Groovy script content
- `convertToRouteConfig()` transforms storage-specific entity objects to the canonical `RouteConfig`
- Template types are "REQUEST" or "RESPONSE" (from `GatewayConstants.Template`)

### 5.4 SecurityStrategy

```java
public interface SecurityStrategy {
    String getCode();        // "default", "signature", "internal", "OFF", or custom
    void process(GatewayContext context);  // Throws on failure
}
```

**Contract**:
- `process()` must throw a RuntimeException (typically `GatewayException`) on authentication/authorization failure
- On success, should populate `context.getRequest().getAttributes()` with user info
- Should also inject user identity headers (`X-User-Id`, etc.) for downstream services
- Strategy code is case-sensitive; "OFF" is special-cased in `SecurityAction` to skip processing

---

## 6. Data Flow Diagrams

### 6.1 Happy Path: HTTP Proxy

```
Client                  Gateway                     Backend
  │                        │                           │
  │  POST /api/user/login  │                           │
  │───────────────────────►│                           │
  │                        │                           │
  │                        │ GatewayHandlerMapping     │
  │                        │ → finds route "login"     │
  │                        │                           │
  │                        │ GatewayHandlerAdapter     │
  │                        │ → builds GatewayContext   │
  │                        │                           │
  │                        │ ExceptionAction           │
  │                        │ → try {                   │
  │                        │                           │
  │                        │ TracingAction             │
  │                        │ → creates span            │
  │                        │ → propagates traceparent  │
  │                        │                           │
  │                        │ RouteAction               │
  │                        │ → route already resolved  │
  │                        │                           │
  │                        │ SecurityAction            │
  │                        │ → validates JWT           │
  │                        │ → extracts user info      │
  │                        │                           │
  │                        │ RequestTemplateAction     │
  │                        │ → runs Groovy script      │
  │                        │ → transforms body         │
  │                        │                           │
  │                        │ ProxyAction               │
  │                        │ → PluginManager           │
  │                        │ → HttpProxyProcessor      │
  │                        │───────────────────────────►│
  │                        │                           │ POST /api/login
  │                        │                           │ (with transformed body)
  │                        │                           │
  │                        │◄───────────────────────────│
  │                        │     200 OK { user, token }│
  │                        │                           │
  │                        │ ResponseTemplateAction    │
  │                        │ → runs Groovy script      │
  │                        │ → removes sensitive fields│
  │                        │                           │
  │                        │ ResponseWrapperAction     │
  │                        │ → wraps in {result,       │
  │                        │    data, meta}            │
  │                        │                           │
  │                        │ } catch → ends span       │
  │                        │                           │
  │                        │ writeResponse()           │
  │◄───────────────────────│                           │
  │  200 {result, data,    │                           │
  │        meta}           │                           │
```

### 6.2 Error Path: Route Not Found

```
Client                  Gateway
  │                        │
  │  GET /api/nonexistent   │
  │───────────────────────►│
  │                        │
  │                        │ ExceptionAction → try {
  │                        │   RouteAction → no route found
  │                        │   → throws RouteException("Route not found: /api/nonexistent")
  │                        │ } catch (GatewayException e) {
  │                        │   handleGatewayException()
  │                        │   → errorType=ROUTING → HTTP 404
  │                        │   → buildErrorResponse()
  │                        │ }
  │                        │
  │                        │ writeResponse()
  │◄───────────────────────│
  │  404 {result: {code:   │
  │  "NOT_FOUND", status:  │
  │  "FAIL", message:      │
  │  "Route not found..."},│
  │  data: null, meta: {}} │
```

### 6.3 Security Failure Path

```
Client                  Gateway
  │                        │
  │  POST /api/secure       │
  │  (no Auth header)       │
  │───────────────────────►│
  │                        │
  │                        │ ExceptionAction → try {
  │                        │   RouteAction → found
  │                        │   SecurityAction → securityCode="default"
  │                        │   → DefaultSecurityStrategy.process()
  │                        │   → no Authorization header
  │                        │   → throws GatewayException("Missing authorization header")
  │                        │   → errorType=SECURITY
  │                        │ } catch → HTTP 500
  │                        │   (currently maps SECURITY to 500)
  │                        │
  │◄───────────────────────│
  │  500 {result: {code:   │
  │  "SECURITY_UNAUTHORIZED",│
  │  ...}}                 │
```

### 6.4 Bean Proxy Flow (In-Process)

```
Client                  Gateway                     Spring Container
  │                        │                           │
  │  GET /api/internal/     │                           │
  │  stats                  │                           │
  │───────────────────────►│                           │
  │                        │                           │
  │                        │ ... (routing, security)   │
  │                        │                           │
  │                        │ ProxyAction               │
  │                        │ → PluginManager           │
  │                        │ → protocol="BEAN"          │
  │                        │ → SpringBeanProxyProcessor│
  │                        │                           │
  │                        │  applicationContext       │
  │                        │  .getBean("statsService") │
  │                        │───────────────────────────►│
  │                        │◄───────────────────────────│
  │                        │   bean instance           │
  │                        │                           │
  │                        │  setupUserContext()       │
  │                        │  → UserContext.set(user)  │
  │                        │                           │
  │                        │  method.invoke(bean, args)│
  │                        │───────────────────────────►│
  │                        │◄───────────────────────────│
  │                        │   return value            │
  │                        │                           │
  │                        │  clearUserContext()       │
  │                        │                           │
  │                        │  serialize result to JSON │
  │                        │                           │
  │                        │ ... (response processing) │
  │                        │                           │
  │◄───────────────────────│                           │
  │  200 {result, data,    │                           │
  │        meta}           │                           │
```

---

## 7. Design Decisions

### 7.1 Manual Builder for RouteConfig Instead of Lombok @Builder

**Decision**: RouteConfig uses a hand-written `RouteConfigBuilder` instead of Lombok's `@Builder`.

**Rationale**: The `build()` method performs complex parsing (target protocol extraction, property parsing, route ID generation) that must happen atomically at construction time. A hand-written builder gives full control over validation, defaulting, and defensive copying. The resulting object is truly immutable — all fields are `final`, the properties map is defensively copied and wrapped in `Collections.unmodifiableMap()`.

### 7.2 Atomic Reference Swap for Route Cache

**Decision**: Route cache refresh builds a new `ConcurrentHashMap`, then atomically swaps the reference.

**Rationale**: The naive approach — `cache.clear()` followed by `cache.putAll()` — creates a gap where concurrent requests see an empty or partially-populated cache, causing a thundering herd of database/filesystem lookups. The double-buffering approach (build → swap) ensures readers always see either the old complete cache or the new complete cache.

### 7.3 Pre-resolved RouteConfig in GatewayHandlerMapping

**Decision**: `GatewayHandlerMapping` returns a `GatewayHandler` with the `RouteConfig` already resolved, and `RouteAction` checks for this pre-populated value before doing its own lookup.

**Rationale**: Avoids redundant database/filesystem lookups. The mapping step already queries the repository to determine if a handler exists; storing the result avoids `RouteAction` doing the same query again. This is a performance optimization for the common path.

### 7.4 Reflection-Based SecurityStrategy Instantiation

**Decision**: `GatewayAutoConfiguration` instantiates security strategies via `Class.forName()` + reflection rather than direct constructor calls.

**Rationale**: The security strategy classes live in `loadup-gateway-core` but the auto-configuration lives in `loadup-gateway-starter`. Since the starter has a compile-time dependency on core, direct instantiation would work. The reflection approach appears to be future-proofing for a scenario where strategies might be loaded from optional modules. This should be simplified to direct instantiation when the module boundaries stabilize.

### 7.5 ExceptionAction Wrapping the Entire Chain

**Decision**: `ExceptionAction` is placed first in the chain and wraps all subsequent actions in try/catch.

**Rationale**: All actions (including `TracingAction`, which also wraps in try/catch) execute inside `ExceptionAction`'s try block. This ensures that *any* exception thrown anywhere in the pipeline is caught, converted to a structured error response, and returned to the client. Without this, uncaught exceptions would bubble up to the servlet container and produce unstructured 500 responses.

### 7.6 ResponseTemplateAction calls chain.proceed() First

**Decision**: Unlike most actions that process then proceed, `ResponseTemplateAction` calls `chain.proceed()` first, then applies template processing.

**Rationale**: The response doesn't exist until `ProxyAction` executes. `ProxyAction` is later in the chain. So `ResponseTemplateAction` must let the chain run forward (past `ProxyAction`), then apply its transformation as a post-processing step. Same pattern for `ResponseWrapperAction`.

### 7.7 No Component Scanning

**Decision**: All beans are explicitly defined in `GatewayAutoConfiguration` using `@Bean` methods. No `@ComponentScan`, no `@Component` on action classes.

**Rationale**: Explicit bean definitions make the dependency graph visible in one place. It avoids surprises from classpath scanning picking up unintended beans. The action chain assembly in `actionDispatcher()` shows the complete pipeline order at a glance.

### 7.8 HTTP Proxy: No Retry Logic in Plugin

**Decision**: The HTTP proxy plugin does not implement retry logic, despite `RouteConfig` having `retryCount`.

**Rationale**: The `retryCount` property is parsed but not currently consumed by any component. Retry logic is identified as a roadmap item. It would be implemented either in a dedicated `RetryAction` in the chain, or directly in the proxy plugins with configurable backoff strategies.

---

## 8. Thread Safety & Concurrency

### 8.1 Shared Mutable State

| Component | Shared State | Strategy |
|-----------|-------------|----------|
| `RouteResolver` | `volatile ConcurrentHashMap` | Atomic reference swap on refresh; ConcurrentHashMap for concurrent reads |
| `PluginManager` | `ConcurrentHashMap<String, ProxyProcessor>` | Write-once in `@PostConstruct`, then read-only |
| `SecurityStrategyManager` | `ConcurrentHashMap<String, SecurityStrategy>` | Write-once in constructor, then read-only |
| `TemplateEngine` | `ConcurrentHashMap<String, Script>` | `computeIfAbsent()` is atomic; Script instances are immutable after compilation |
| `RpcProxyProcessor` | `ConcurrentHashMap<String, GenericService>` | `computeIfAbsent()` is atomic |
| `GatewayContext` | `ConcurrentHashMap<String, Object>` attributes | ConcurrentHashMap for safe concurrent access |

### 8.2 Per-Request Isolation

- Each request gets its own `GatewayContext` instance (created in `GatewayHandlerAdapter.buildGatewayContext()`)
- `GatewayRequest` and `GatewayResponse` are mutable (Lombok `@Data`) but are local to a single request context
- The `DefaultGatewayActionChain` is created per-request in `ActionDispatcher.dispatch()`
- All actions are stateless singletons — they receive the context as a parameter

### 8.3 Potential Issues

- **RouteConfig.getProperties()** returns an unmodifiable view of a copy — safe for concurrent access
- **Spring Bean Proxy** uses `@Resource` for `ApplicationContext` — standard Spring thread-safety applies
- **Security strategies** accessing `RequestContextHolder` for the original servlet request — this is thread-safe in Spring MVC (request-scoped thread-local)

---

## 9. Extending the Gateway

### 9.1 Adding a New Protocol

1. Create a new Maven module under `plugins/`
2. Implement `ProxyProcessor` — key method is `proxy(GatewayRequest, RouteConfig)`
3. Create an auto-configuration class with `@Bean` method for your processor
4. Register in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
5. Add a protocol constant to `GatewayConstants.Protocol` (or use an arbitrary string)
6. The processor will be auto-discovered by `PluginManager` via constructor injection of `List<ProxyProcessor>`
7. Configure routes with targets starting with your protocol scheme (e.g., `grpc://...`)

### 9.2 Adding a New Storage Backend

1. Create a new Maven module under `plugins/`
2. Implement `RepositoryPlugin` — `getAllRoutes()`, `getRouteByPath()`, `getTemplate()`, `convertToRouteConfig()`
3. Create an auto-configuration class with `@Bean` method + `@ConditionalOnProperty` for storage type
4. Register in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
5. Add a storage type constant to `GatewayConstants.Storage`
6. The `RouteResolver` constructor receives a single `RepositoryPlugin` — only one storage backend is active
7. Selection is controlled by `loadup.gateway.storage.type` property + `@ConditionalOnProperty`

### 9.3 Adding a New Security Strategy

1. Implement `SecurityStrategy` — `getCode()` returns a unique code, `process()` validates and throws on failure
2. Register as a Spring bean (either via `@Component` or in an auto-configuration `@Bean` method)
3. The strategy is auto-discovered by `SecurityStrategyManager` via constructor injection of `List<SecurityStrategy>`
4. Reference the strategy code in route configuration: `securityCode: my-strategy`

### 9.4 Adding a New Action

1. Implement `GatewayAction` — `execute(GatewayContext, GatewayActionChain)`
2. Create a `@Bean` method in `GatewayAutoConfiguration`
3. Insert the action into the `actionDispatcher()` bean's chain at the appropriate position
4. Consider whether your action should process before or after calling `chain.proceed()`

---

## Appendix A: Key Class Diagram

```
┌──────────────────────┐
│   GatewayProperties  │◄── @ConfigurationProperties("loadup.gateway")
│   (facade/config)    │
├──────────────────────┤
│ - enabled            │
│ - security           │──── SecurityConfig { secret, header, prefix }
│ - storage            │──── Storage { type, file, database }
│ - proxyPlugins       │──── ProxyPlugins { bean, http, rpc }
│ - response           │──── ResponseProperties { wrap, wrapResult, wrapMeta }
└──────────────────────┘

┌──────────────────────┐       ┌──────────────────────┐
│   GatewayContext     │       │   GatewayRequest     │
│   (facade/context)   │       │   (facade/model)     │
├──────────────────────┤       ├──────────────────────┤
│ - request ◄──────────┼───────│ - requestId          │
│ - response ◄─────────┼───┐   │ - path, method       │
│ - route ◄────────────┼─┐ │   │ - headers            │
│ - originalRequest    │ │ │   │ - queryParameters    │
│ - originalResponse   │ │ │   │ - body, contentType  │
│ - attributes (Map)   │ │ │   │ - clientIp, userAgent│
│ - exception          │ │ │   │ - attributes (Map)   │
└──────────────────────┘ │ │   └──────────────────────┘
                          │ │
                          │ │   ┌──────────────────────┐
                          │ │   │   GatewayResponse    │
                          │ │   │   (facade/model)     │
                          │ │   ├──────────────────────┤
                          │ └───│ - requestId          │
                          │     │ - statusCode         │
                          │     │ - headers, body      │
                          │     │ - contentType        │
                          │     │ - processingTime     │
                          │     │ - errorMessage       │
                          │     └──────────────────────┘
                          │
                          │     ┌──────────────────────┐
                          │     │   RouteConfig        │
                          │     │   (facade/model)     │
                          │     ├──────────────────────┤
                          └─────│ - routeId, routeName │
                                │ - path, method       │
                                │ - protocol           │
                                │ - target, targetUrl  │
                                │ - targetBean, targetMethod│
                                │ - requestTemplate    │
                                │ - responseTemplate   │
                                │ - securityCode       │
                                │ - enabled            │
                                │ - properties (Map)   │
                                │ - parsedTimeout      │
                                │ - parsedRetryCount   │
                                │ - parsedWrapResponse │
                                └──────────────────────┘

┌──────────────────────────────────────────────────────┐
│                  SPI Interfaces                       │
│                  (facade/spi)                         │
├──────────────────────────────────────────────────────┤
│ «interface» GatewayPlugin                             │
│   + getName(): String                                │
│   + getType(): String                                │
│   + getVersion(): String                             │
│   + getPriority(): int                               │
│   + initialize()                                     │
│   + destroy()                                        │
│                                                      │
│ «interface» ProxyProcessor extends GatewayPlugin      │
│   + proxy(GatewayRequest, RouteConfig): GatewayResponse│
│   + getSupportedProtocol(): String                   │
│                                                      │
│ «interface» RepositoryPlugin extends GatewayPlugin    │
│   + getRoute(String): Optional<RouteConfig>          │
│   + getRouteByPath(String, String): Optional<RouteConfig>│
│   + getAllRoutes(): List<RouteConfig>                │
│   + getTemplate(String, String): Optional<String>    │
│   + getSupportedStorageType(): String                │
│   + convertToRouteConfig(RouteStructure): RouteConfig│
│                                                      │
│ «interface» SecurityStrategy                          │
│   + getCode(): String                                │
│   + process(GatewayContext)                           │
└──────────────────────────────────────────────────────┘
```

## Appendix B: Exception Type → HTTP Status Mapping

| ErrorType | HTTP Status | Scenario |
|-----------|-------------|----------|
| `ROUTING` | 404 | Route not found, route disabled |
| `VALIDATION` | 400 | Missing required param, invalid format |
| `SYSTEM` | 500 | Internal error, configuration error |
| `NETWORK` | 500 | Connection refused, timeout, DNS failure |
| `SECURITY` | 500 | (Note: should be 401/403; see roadmap) |
| `TEMPLATE` | 500 | Groovy script execution error |
| `PROXY` | 500 | Backend invocation failure |
| `PLUGIN` | 500 | Plugin not found, plugin execution error |
| `BUSINESS` | 500 | Downstream business logic error |
| `CONFIGURATION` | 500 | Invalid configuration |
| `SERIALIZATION` | 500 | JSON parse/serialize error |
| `STORAGE` | 500 | Database/file read error |
| `AUTHORIZATION` | 500 | (Note: not yet used by any strategy) |
| `RATE_LIMIT` | 500 | (Note: rate limiting not yet implemented) |
| `TIMEOUT` | 500 | (Note: timeout handling not yet implemented) |
| `UNKNOWN` | 500 | Unexpected/unclassified error |

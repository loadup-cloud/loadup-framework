# LoadUp Gateway — Architecture

## Overview

LoadUp Gateway is an **embedded multi-protocol API gateway** for Spring Boot MVC
applications. Instead of a self-built servlet engine, it is a **thin adapter over
Spring Cloud Gateway Server MVC (SCG Server MVC)**: route definitions (YAML or JDBC)
are compiled into a Spring MVC `RouterFunction`, and each route runs through a fixed
pipeline of `HandlerFilterFunction`s before reaching the proxy handler.

Because the engine is a standard Spring MVC `RouterFunction` bean, gateway routes and
user-written `@RestController`s coexist in the same application. The project explicitly
does **not** use WebFlux.

```
HTTP Request
  → RouterFunctionMapping (Spring MVC, auto-registered)
    → RouteFunctionRegistry.route(request)   (atomic RouterFunction snapshot)
      → compiled route: method + path predicate → sets ROUTE_CONFIG attribute
      → filter pipeline (outermost first):
          GatewayExceptionHandler
            → TracingHandlerFilterFunction     (OpenTelemetry, optional)
              → SecurityHandlerFilterFunction  (JWT / HMAC / internal / OFF)
                → RateLimitHandlerFilterFunction
                  → CircuitBreakerHandlerFilterFunction
                    → ResponseWrapperHandlerFilterFunction  ({result, data, meta})
                      → ProxyHandlerFunction   (HTTP / BEAN / RPC dispatch)
```

## Module Layout

```
loadup-gateway/
├── loadup-gateway-facade/            SPI + models + config (zero internal deps)
│   ├── spi/RouteStore.java           Route definition storage SPI
│   ├── spi/ProxyProcessor.java       Backend protocol SPI
│   ├── spi/SecurityStrategy.java     Auth strategy SPI
│   ├── model/RouteDefinition.java    YAML-friendly route config
│   ├── model/RouteConfig.java        Compiled runtime route
│   ├── model/GatewayRequest.java     Inbound request model
│   ├── model/GatewayResponse.java    Outbound response model (body = JSON document)
│   ├── event/RouteStoreRefreshedEvent.java
│   └── config/GatewayProperties.java
├── loadup-gateway-webmvc/            SCG Server MVC adapter (the engine)
│   ├── router/RouteFunctionRegistry.java   Atomic RouterFunction snapshot, hot reload
│   ├── router/RouteFunctionCompiler.java   RouteConfig list → RouterFunction
│   ├── filter/*HandlerFilterFunction.java  Fixed pipeline filters
│   ├── proxy/ProxyHandlerFunction.java     Terminal handler → ProxyProcessor
│   ├── security/*.java                     JWT / HMAC / internal / OFF strategies
│   ├── support/GatewayRequestFactory.java  ServerRequest → GatewayRequest
│   └── autoconfigure/GatewayWebMvcAutoConfiguration.java
├── loadup-gateway-starter/           Auto-configuration (default YamlRouteStore)
└── plugins/
    ├── proxy-http-plugin/            HTTP proxy
    ├── proxy-rpc-plugin/             Dubbo RPC (GenericService)
    ├── proxy-springbean-plugin/      Spring Bean invocation
    ├── repository-yaml-plugin/       YAML file store + WatchService hot reload
    └── repository-database-plugin/   JDBC store
```

## Engine Design

### Route lifecycle

1. `RouteStore.loadAll()` returns `RouteDefinition`s (YAML file / JDBC table).
2. `RouteFunctionRegistry.refresh()` filters disabled routes, converts each definition
   with `RouteConfigConverter`, and compiles them into an immutable `RouterFunction`
   via `RouteFunctionCompiler`.
3. The compiled function is published atomically through an `AtomicReference`
   snapshot; in-flight requests keep the previous snapshot.
4. Refresh is triggered at startup (`@PostConstruct`) and on every
   `RouteStoreRefreshedEvent` (YAML file watcher, DB admin updates).

An empty route table is compiled to `request -> Optional.empty()` — a zero-route
`RouterFunctions.route().build()` throws in Spring 7, so the registry never uses it.

### Filter pipeline

The pipeline is fixed per route (order above). Per-route behavior is expressed
through `RouteConfig` attributes rather than per-route filter lists:

| Concern | Filter | Route / global switch |
|---------|--------|----------------------|
| Errors | `GatewayExceptionHandler` | always |
| Tracing | `TracingHandlerFilterFunction` | `loadup.tracer.enabled`, OTel `Tracer` bean |
| Security | `SecurityHandlerFilterFunction` | `securityCode` (`OFF` / `default` / `signature` / `internal`) |
| Rate limit | `RateLimitHandlerFilterFunction` | route properties (token bucket per route + IP) |
| Circuit breaker | `CircuitBreakerHandlerFilterFunction` | route properties |
| Response wrapping | `ResponseWrapperHandlerFilterFunction` | route `wrapResponse` or `loadup.gateway.response.wrap` |
| Proxy | `ProxyHandlerFunction` | `backend.protocol` → `ProxyProcessor` |

### Proxy protocols

`ProxyProcessor` is the facade SPI; each protocol plugin registers one:

| Protocol | Processor | Target |
|----------|-----------|--------|
| `HTTP` | `HttpProxyProcessor` | `backend.url`, forwarded with hop-by-hop headers stripped |
| `BEAN` | `SpringBeanProxyProcessor` | `backend.beanName` + `backend.methodName`, args resolved from JSON body |
| `RPC` | `RpcProxyProcessor` | `backend.url` = `interface:method:version`, Dubbo `GenericService` |

The terminal `ProxyHandlerFunction` stores the `GatewayResponse` in the
`GatewayAttributes.PROXY_RESPONSE` request attribute so post filters (response
wrapper) can rewrite the body, then converts it to a `ServerResponse`.

## Response Contract

- Successful backend results are wrapped in `{"result": {...}, "data": <body>,
  "meta": {"requestId", "timestamp"}}` when wrapping is enabled.
- `GatewayResponse.body` is always a **JSON document**. Bean and RPC processors
  serialize through Jackson's `ObjectMapper` (not `JsonUtil.toJson`), so String
  results are quoted — a bare `hello` would not be valid JSON.
- Responses with HTTP status >= 400 are left untouched: the exception handler
  produces the unified error envelope.
- Hop-by-hop headers (`connection`, `transfer-encoding`, `content-length`, ...)
  are stripped when converting the backend response.

## Hot Reload

`YamlRouteStore` watches the config file with `WatchService` and publishes
`RouteStoreRefreshedEvent`; `DatabaseRouteStore` publishes the same event from its
admin CRUD. `RouteFunctionRegistry` listens and recompiles atomically. The route
store is selected by `@ConditionalOnMissingBean(RouteStore.class)` — adding a
`DatabaseRouteStore` bean overrides the default YAML store.

## YAML Parsing Note

SnakeYAML follows YAML 1.1 and would parse unquoted `OFF` / `ON` as booleans,
corrupting `securityCode: OFF`. `YamlRouteStore` uses a `StrictBooleanResolver`
that only treats `true` / `false` as booleans, so the route DSL accepts unquoted
`OFF`.

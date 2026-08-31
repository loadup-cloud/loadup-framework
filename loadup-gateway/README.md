# LoadUp Gateway

Embedded multi-protocol API gateway for Spring Boot, built on **Spring Cloud Gateway Server MVC** — distributed as a library, not a server. Routes are declared in YAML / DB and dispatched through a `RouterFunction`, so gateway routes and regular `@RestController`s coexist in one MVC application.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 4.1](https://img.shields.io/badge/Spring_Boot-4.1.0-green.svg)](https://spring.io/projects/spring-boot)

## What it does

- Embeds an API gateway in your Spring Boot MVC app — no separate process
- Declarative routes (YAML default, JDBC optional) without any `@RestController`
- Backend protocols: `http://` forward, `bean://` in-process Spring bean call, `rpc://` Dubbo GenericService
- Fixed pipeline per route: exception → tracing → security → rate limit → circuit breaker → response wrapper → proxy
- Hot reload: route stores publish `RouteStoreRefreshedEvent`, the compiled `RouterFunction` snapshot swaps atomically

## Quick Start

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-gateway-starter</artifactId>
    <version>0.0.2-SNAPSHOT</version>
</dependency>
```

Create `gateway-routes.yml`:

```yaml
routes:
  - id: hello
    path: /api/hello
    method: GET
    backend:
      protocol: bean
      beanName: helloService
      methodName: sayHello
    securityCode: OFF
```

Configure it in `application.yml`:

```yaml
loadup:
  gateway:
    enabled: true
    storage:
      file:
        base-path: classpath:gateway-routes.yml
    response:
      wrap: true        # wrap backend results in {result, data, meta}
    security:
      secret: "<your-jwt-secret>"
      header: Authorization
      prefix: "Bearer "
      app-secrets:
        my-app: "my-secret"
```

## Route Configuration

### Maven coordinates

- Starter: `loadup-gateway-starter` (engine + default YAML store)
- Protocol plugins (optional): `loadup-gateway-proxy-http-plugin`, `loadup-gateway-proxy-springbean-plugin`, `loadup-gateway-proxy-rpc-plugin`
- Storage plugins (optional): `loadup-gateway-repository-database-plugin`

```yaml
routes:
  - id: user-detail
    path: /api/users/{id}
    method: GET
    backend:
      protocol: bean
      beanName: userService
      methodName: findById
    securityCode: default
    wrapResponse: false
    timeout: 5000
  - id: payment-api
    path: /api/payment/**
    method: POST
    backend:
      protocol: http
      url: http://payment-service:8080
    securityCode: signature
    wrapResponse: false
```

| Field | Description |
|-------|-------------|
| `id` | Unique route identifier |
| `path` | URL pattern (Spring `PathPattern`, e.g. `/api/users/{id}`, `/api/**`) |
| `method` | HTTP method |
| `backend.protocol` | `http`, `bean`, or `rpc` |
| `backend.url` | Target URL (http/rpc) |
| `backend.beanName` / `methodName` | Bean target (bean protocol) |
| `securityCode` | `OFF`, `default` (JWT), `signature` (HMAC), `internal` — unquoted `OFF` works |
| `timeout` | Route-level timeout in ms |
| `wrapResponse` | Override global `{result, data, meta}` wrapping |
| `enabled` | Set to `false` to disable a route without deleting it |

## Notes

- Gateway responses are wrapped into `{result, data, meta}`; responses with HTTP status >= 400 are left untouched (formatted by the exception handler)
- `GatewayResponse.body` is a JSON document — bean/RPC backends serialize results through Jackson, strings are quoted correctly
- See [ARCHITECTURE.md](ARCHITECTURE.md) for the engine design (SCG Server MVC, atomic `RouterFunction` snapshots, filter order)

## License

Apache-2.0 — [LoadUp Cloud](https://github.com/loadup-cloud)

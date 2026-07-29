# LoadUp Gateway

Embedded multi-protocol API gateway for Spring Boot — distributed as a library, not a server.

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 4.1](https://img.shields.io/badge/Spring_Boot-4.1.0-green.svg)](https://spring.io/projects/spring-boot)

## Quick Start

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-gateway-starter</artifactId>
    <version>0.0.2-SNAPSHOT</version>
</dependency>
```

Create `gateway-routes.yml` in your project root:

```yaml
routes:
  - id: hello
    path: /api/hello
    method: GET
    backend:
      protocol: bean
      beanName: helloService
      methodName: sayHello
    filters: []
```

Start your Spring Boot app — `GET /api/hello` is now proxied to `helloService.sayHello()`.

## Features

- **Embedded**: Drop the starter dependency into any Spring Boot app, no separate process
- **Multi-protocol**: HTTP, Dubbo RPC, Spring Bean invocation — per-route
- **Named filter chains**: Each route declares its own filter pipeline in YAML
- **Hot reload**: Edit `gateway-routes.yml`, routes update in seconds, no restart
- **Dual storage**: YAML file (default) or JDBC database (admin CRUD)
- **Built-in filters**: rate limiting, circuit breaker, JWT auth, HMAC signature, body parsing, response wrapping, OpenTelemetry tracing
- **Bounded caches**: Caffeine with size + TTL eviction — no memory leaks

## Route Configuration

```yaml
routes:
  - id: payment-api
    path: /api/payment/**
    method: POST
    backend:
      protocol: http
      url: http://payment-service:8080
    filters:
      - name: body-parser
      - name: rate-limit
        props: { capacity: 100, refillRate: 10 }
      - name: security
      - name: circuit-breaker
        props: { failureThreshold: 3 }
    responseFilters:
      - name: response-wrapper
    securityCode: default
    timeout: 5000

  - id: health-check
    path: /api/health
    method: GET
    backend:
      protocol: bean
      beanName: healthService
      methodName: check
    filters: []
    wrapResponse: false
```

| Field | Description |
|-------|-------------|
| `id` | Unique route identifier |
| `path` | URL pattern (supports Ant-style: `/api/user/{id}`, `/api/**`) |
| `method` | HTTP method |
| `backend.protocol` | `http`, `bean`, or `rpc` |
| `backend.url` | Target URL (http/rpc) |
| `backend.beanName` / `methodName` | Bean target (bean protocol) |
| `filters` | Ordered request-phase filter names |
| `responseFilters` | Ordered response-phase filter names |
| `securityCode` | `OFF`, `default` (JWT), `signature` (HMAC), `internal` |
| `timeout` | Route-level timeout in ms |
| `wrapResponse` | Override global `{result, data, meta}` wrapping |

## Available Filters

| Name | Phase | Description |
|------|-------|-------------|
| `body-parser` | request | Parse JSON / form body into `parsedBody` attribute |
| `rate-limit` | request | Token-bucket per (route, IP); config: `capacity`, `refillRate` |
| `security` | request | JWT / HMAC signature / internal IP; reads `securityCode` |
| `circuit-breaker` | request | CLOSED → OPEN → HALF_OPEN; config: `failureThreshold`, `openTimeout` |
| `response-wrapper` | response | Wrap in `{result, data, meta}`; respects per-route `wrapResponse` |

## Configuration Properties

```yaml
loadup:
  gateway:
    enabled: true
    storage:
      type: FILE              # FILE (YAML) or DATABASE (JDBC)
    security:
      secret: "<your-jwt-secret>"
      header: Authorization
      prefix: "Bearer "
      app-secrets:            # HMAC signature keys
        my-app: "my-secret"
    response:
      wrap: true
```

## Protocol Backends

| Protocol | Target format | Example |
|----------|--------------|---------|
| HTTP | `http://host:port/path` | `http://user-service:8080/api/users` |
| Bean | `bean://beanName:methodName` | `bean://userService:findById` |
| RPC | `rpc://interface:method:version` | `rpc://com.example.OrderApi:create:1.0.0` |

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) for full details. Key points:

- **GatewayFilter** — named, per-route, replaces hardcoded GatewayAction chain
- **RouteStore** — SPI for YAML / JDBC storage, replaces RepositoryPlugin
- **Caffeine** — all bounded maps use size + TTL eviction
- **Direct constructor injection** — no `@Resource`, no `Class.forName()` reflection
- **Unified error format** — single ExceptionFilter, single `{result, data, meta}` JSON

## License

GPL-3.0 — [LoadUp Cloud](https://github.com/loadup-cloud)

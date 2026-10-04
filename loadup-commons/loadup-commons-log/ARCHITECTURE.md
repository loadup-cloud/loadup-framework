# LoadUp Common Log — Architecture

## 1. Boundary

`loadup-commons-log` owns the logging contract shared by applications and framework components:

- default `logging.pattern.console` for Spring Boot applications;
- stable MDC keys for `traceId`, `spanId`, and `requestId`;
- a small `LogContext` utility for framework integrations.

It does not select a logging implementation, ship a Logback configuration, or implement tracing.

## 2. Startup flow

```
SpringApplication
      |
      v
LoadupLogEnvironmentPostProcessor
      |
      +-- add lowest-precedence logging.pattern.console default
      v
LoadupLogAutoConfiguration
      |
      +-- bind loadup.log.* properties
      v
Application logging backend
```

The post processor uses a low-precedence property source, so normal application configuration wins.

## 3. Trace integration

`loadup-components-observability` uses Spring Boot's Micrometer Tracing integration, which populates the
standard `traceId` and `spanId` MDC keys. The log module only defines their names and output patterns:

```
loadup-commons-log  <-  MVC application  ->  loadup-components-observability
```

The log module neither creates spans nor depends on a tracer SDK.

## 4. LogUtil

`LogUtil` provides two equivalent styles:

- `LogUtil.getLogger(MyService.class)` for a reusable logger field;
- direct `LogUtil.info(...)` / `debug(...)` / `warn(...)` / `error(...)` calls for development diagnostics.

Both styles delegate to SLF4J parameterized logging. Direct calls resolve the calling class with the JDK
`StackWalker`, so the emitted logger name remains useful for filtering and searching.

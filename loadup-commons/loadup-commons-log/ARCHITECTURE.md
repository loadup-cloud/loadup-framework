# LoadUp Common Log — Architecture

## 1. Boundary

`loadup-commons-log` owns the logging contract shared by applications and framework components:

- default `logging.pattern.console` / `logging.pattern.file` for Spring Boot applications;
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
      +-- add lowest-precedence logging.pattern.console/file defaults
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

- `LogUtil.info(MyService.class, ...)` and other explicit-source methods for application logs;
- direct `LogUtil.info(...)` / `debug(...)` / `warn(...)` / `error(...)` calls for development diagnostics.

Both styles delegate to SLF4J parameterized logging. Direct calls resolve the calling class with the JDK
`StackWalker`, so the emitted logger name remains useful for filtering and searching.

## 统一入口与输出

框架源码的日志打印统一走 `LogUtil` 显式来源类重载，保留 SLF4J 的级别、参数化和末尾 Throwable 语义。第三方日志适配可通过 `LogUtil.getLogger` 获得原生 Logger。调用入口不创建 Trace 或 MeterRegistry，也不修改 MDC 的 traceId/spanId。

EnvironmentPostProcessor 同时提供 console/file 默认 pattern，file 未单独配置时沿用 LoadUp console pattern。标准 `logging.pattern.*` 属性优先于组件默认值。JSON 使用 Boot `logging.structured.format.*`，启动器不再维护重复的 Logback 配置。

# LoadUp Common Log

统一日志输出默认格式，并约定 Micrometer Tracing 使用的 MDC 键。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-commons-log</artifactId>
</dependency>
```

## 配置

```yaml
loadup:
  log:
    enabled: true
    include-trace-context: true
    console-pattern: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n%wEx"
```

模块通过 Spring Boot `EnvironmentPostProcessor` 设置 `logging.pattern.console` 默认值；集成方显式配置的
`logging.pattern.console` 具有更高优先级。`LogContext` 统一使用 `traceId`、`spanId` 和 `requestId` MDC 键。

引入 `loadup-components-observability` 后，Spring Boot 的 Micrometer Tracing 将当前 Span 的 `traceId` / `spanId` 写入 MDC，并负责上下文传播。

## LogUtil

```java
LogUtil.info("Created order id={}", orderId);
LogUtil.error(OrderService.class, "Failed to create order id={}", orderId, exception);

// Use this form for a long-lived service logger.
private static final Logger log = LogUtil.getLogger(OrderService.class);
```

`LogUtil` 保留 SLF4J 参数化日志写法；直接调用静态日志方法时，会使用调用方类名作为 logger 名称。

## 能力矩阵

| 能力 | 支持 |
|------|------|
| 统一 console 日志格式默认值 | ✓ |
| 应用配置覆盖默认 pattern | ✓ |
| traceId / spanId MDC 约定 | ✓ |
| requestId MDC 辅助 API | ✓ |
| JSON 编码器绑定 | ✗（由集成方日志后端配置） |

该模块不绑定具体日志实现；日志后端仍由 Spring Boot / 集成方选择。

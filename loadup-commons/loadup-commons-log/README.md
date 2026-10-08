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
    file-pattern: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n%wEx"
    console-pattern: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n%wEx"
```

模块通过 Spring Boot `EnvironmentPostProcessor` 设置 `logging.pattern.console` 与 `logging.pattern.file` 默认值；集成方显式配置的
`logging.pattern.console` / `logging.pattern.file` 具有更高优先级。`LogContext` 统一使用 `traceId`、`spanId` 和 `requestId` MDC 键。

引入 `loadup-components-observability` 后，Spring Boot 的 Micrometer Tracing 将当前 Span 的 `traceId` / `spanId` 写入 MDC，并负责上下文传播。

## LogUtil

```java
LogUtil.info(OrderService.class, "Created order id={}", orderId);
LogUtil.error(OrderService.class, "Failed to create order id={}", orderId, exception);

// Only obtain a raw logger when a third-party adapter requires it.
Logger adapterLogger = LogUtil.getLogger(OrderService.class);
```

`LogUtil` 保留 SLF4J 参数化日志写法，异常作为最后参数。框架日志打印统一使用 `LogUtil.info/warn/error(Source.class, ...)` 等显式来源类方法，避免生产路径中的 StackWalker；省略来源类的方法仅用于开发诊断。第三方 SDK 要求 Logger 时使用 `LogUtil.getLogger`，不直接创建 LoggerFactory。

## 能力矩阵

| 能力 | 支持 |
|------|------|
| 统一 console/file 文本格式默认值 | ✓ |
| 应用配置覆盖默认 pattern | ✓ |
| traceId / spanId MDC 约定 | ✓ |
| requestId MDC 辅助 API | ✓ |
| JSON 编码器绑定 | ✗（由集成方日志后端配置） |

该模块不绑定具体日志实现；日志后端仍由 Spring Boot / 集成方选择。

## 敏感日志

输出 DTO 的 `@Masked` 只保护 MVC JSON 响应，不保护日志、`JsonUtil`、`toString()` 或异常文字。需要记录个人字段时显式引入 `loadup-commons-masking`：

```java
LogUtil.info(ContactService.class, "Contact mobile={}", Masking.mask(mobile, MaskType.PHONE));
```

避免记录完整请求、响应和实体。默认日志格式不执行全局正则替换；密码、令牌、密钥与签名凭证应直接省略，不以脱敏后的值作为审计证据。

## 结构化日志

采用 Spring Boot 标准编码器配置：

```yaml
logging:
  structured:
    format:
      console: ecs
      file: ecs
```

消费工程按需启用。启动器的 `json` profile 使用同样配置；默认使用统一文本格式。该组件不打包 Logback XML 或创建独立日志后端。文件输出仍需配置 `logging.file.name` 或 `logging.file.path`，本组件不会自动创建文件。应用自定义 `logging.config` 时须自行保持日志字段和编码器契约。

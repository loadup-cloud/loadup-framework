# LoadUp Common :: Tracer

基于 **OpenTelemetry** 的分布式链路追踪薄集成（MVC 模式，无 WebFlux 依赖）：
`@Traced` 注解 + `TraceUtil` 编程式 API + HTTP Filter 自动追踪 + 异步线程 Context 传播。
facade 直接采用 OpenTelemetry 标准 API（`Tracer` / `Span` / `Context`），不自创平行接口。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-commons-tracer</artifactId>
</dependency>
```

## 使用

```java
@Traced(name = "OrderService.create", includeParameters = true)
public Order createOrder(OrderDTO dto) { ... }

// 编程式
Span span = TraceUtil.createSpan("PaymentGateway.charge");
try { ... } finally { span.end(); }
```

日志通过 `loadup-commons-log` 自动关联 `traceId` / `spanId`（MDC），无需额外配置。

Java 包名已迁移为 `io.github.loadup.common.tracer`，Maven 坐标为 `loadup-commons-tracer`。

## 配置

```yaml
spring:
  application:
    name: my-service

loadup:
  tracer:
    enabled: true                    # 总开关，默认 true
    enable-web-tracing: true         # HTTP SERVER span
    enable-async-tracing: true       # @Async 线程 Context 传播
    sampler-ratio: 1.0               # 采样率
    attributes:                      # 附加 Resource 属性
      environment: production
    exporters:                       # 可多选，composite fan-out
      - type: otlp                   # logging | noop | otlp | zipkin
        endpoint: http://otel-collector:4318/v1/traces
    batch:
      max-queue-size: 2048
      max-export-batch-size: 512
      schedule-delay-millis: 5000
```

## 能力矩阵

| 能力 | 支持 |
|------|------|
| `@Traced` 方法级追踪（AOP） | ✓ |
| `TraceUtil` 编程式 Span | ✓ |
| HTTP 请求追踪（Servlet Filter，W3C 传播） | ✓ |
| 异步线程 Context + MDC 传播 | ✓ |
| MDC `traceId` / `spanId` 自动注入 | ✓ |
| 导出器 SPI 扩展（logging / noop / otlp / zipkin） | ✓ |
| 多导出器并行输出 | ✓ |
| 采样（全量 / 关闭 / 比例） | ✓ |
| 兜底降级链（configured → logging → noop） | ✓ |
| WebFlux / 非 Servlet 环境 | ✗（项目为 MVC 模式） |

> 版本：OpenTelemetry 1.62（BOM 统一管理），Java 21 / Spring Boot 4.1。

## 部署拓扑

- OTLP / Zipkin 导出器需要可访问的 Collector / Zipkin 实例；无配置时自动降级为
  logging 导出器，业务启动不受影响。
- 自定义导出器：实现 `SpanExporterProvider` SPI 并在 `META-INF/services` 注册即可。

详见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

## 许可证

Apache License 2.0 (Apache-2.0)

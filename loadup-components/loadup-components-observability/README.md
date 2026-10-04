# Observability

基于 Spring Boot Actuator、Micrometer Observation 与 OpenTelemetry 的统一观测接入。Spring Boot 创建 HTTP Span、指标注册表和上下文传播；本组件只补充 `traceId` 响应头与业务 API 结果计数。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-observability</artifactId>
</dependency>
```

需要 Prometheus 抓取时，应用另加 `io.micrometer:micrometer-registry-prometheus` 并暴露 `prometheus` 端点。OpenTelemetry starter 也带来 OTLP 导出能力；本地没有 Collector 时可以关闭 Trace 和 Metrics 的 OTLP 导出，Span、MDC 和响应追踪头仍然生效。

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
    export:
      otlp:
        enabled: false
  otlp:
    metrics:
      export:
        enabled: false
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

使用标准 `ObservationRegistry` 记录需要耗时与 Trace 的业务操作，使用 `MeterRegistry` 记录计数和状态量。`loadup.api.responses` 按 `outcome=success|failure` 统计 JSON API 结果，不依赖统一 HTTP 200 的状态码，也不把用户 ID 或 traceId 加入指标标签。

使用 Boot 自动配置的 `@Async` 执行器时，设置 `spring.task.execution.propagate-context=true` 传播当前 Observation。自定义 `AsyncTaskExecutor` 需配置 Spring 的 `ContextPropagatingTaskDecorator`。

## 能力矩阵

| 能力 | 提供方 |
|------|--------|
| HTTP、JVM 等基础指标 | Spring Boot Actuator |
| HTTP Span、MDC 与传播 | Micrometer Tracing + OpenTelemetry |
| `traceId` 响应头 | 本组件 |
| `loadup.api.responses` 业务结果计数 | 本组件与 Web MVC / Resource Server |
| Prometheus 或 OTLP 指标导出 | 应用选择相应 Micrometer Registry |

内部边界见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

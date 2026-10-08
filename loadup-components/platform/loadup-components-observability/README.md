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

内部边界见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 统一观测契约

组件传递引入 `loadup-commons-log`。追踪关联统一为 Micrometer 的 `traceId` / `spanId`，日志输出使用 `LogUtil`；不创建第二套追踪或指标注册表。

EnvironmentPostProcessor 通过标准 `management.metrics.tags.application` 提供 `${spring.application.name:application}` 默认值，适用于 Boot 管理的全部指标。消费工程的显式 `management.metrics.tags.application` 具有更高优先级；优先设置稳定的 `spring.application.name`。

| 观测 | 名称与维度 |
|---|---|
| HTTP/JVM 基础指标 | Boot/Micrometer 原生名称，保留标准维度 |
| JSON API 结果 | `loadup.api.responses`，`outcome=success/failure` |
| 命名出站调用（含 KMS） | `loadup.http.calls`，配置中的 client、operation 与 `outcome=success/failure` |
| Outbox 投递与状态 | `loadup.outbox.delivery` 的 success/failure；pending、failed、oldest.age 状态量 |
| Outbox handler Trace | `loadup.outbox.handle` Observation |
| Resilience4j | 官方 Micrometer binder 的标准名称和配置实例维度 |

HTTP 非 2xx 与传输失败均计为 failure；标准 HTTP 客户端观测仍保留协议状态分类。组件从容器注入共享 `MeterRegistry` / `ObservationRegistry`，未启用观测的独立消费工程可不提供可选 Registry。禁止使用全局静态 Metrics、另建生产 Registry、用户/租户/订单/traceId/完整 URI 等高基数标签；新的业务耗时与 Trace 优先使用标准 Observation。

## 业务上下文传播

传递引入 [commons-context](../../../loadup-commons/loadup-commons-context/README.md)，要求 JDK 25+。自动提供 `LoadUpContextTaskDecorator` Bean，在包装时捕获不可变业务上下文，并通过 ScopedValue 包裹整个任务。Boot 4.1 的执行器会组合多个 TaskDecorator，已有自定义装饰器不会使该装饰器被跳过；显式提供同类型 Bean 可替换默认实现。

开启 `spring.task.execution.propagate-context=true` 后，Boot 的标准 Micrometer 装饰器负责 Observation/Trace，LoadUp 装饰器负责业务元数据。只开启虚拟线程不会自动传播。自定义执行器显式组合：

```java
executor.setTaskDecorator(new CompositeTaskDecorator(List.of(
    new LoadUpContextTaskDecorator(),
    new ContextPropagatingTaskDecorator()
)));
```

多个自定义装饰器应合并配置，不要连续 setTaskDecorator 覆盖前一个。业务数据不进入 Micrometer ThreadLocalAccessor：ScopedValue 需要动态作用域，不能采用 set/reset 适配。任意 Micrometer snapshot 或第三方线程池不会自动捕获业务上下文，须安装本装饰器或显式 ContextHolder.wrap。空 context 也绑定，退出后恢复调用前状态；值对象保持轻量不可变，身份继续使用 Spring Security 标准传播。

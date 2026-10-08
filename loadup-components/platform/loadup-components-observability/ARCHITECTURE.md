# Observability 架构

## 边界

该模块是 Spring Boot 观测能力的薄集成。它不创建 OpenTelemetry SDK、不创建 SERVER Span、不维护自定义 Context 栈，也不实现指标或 Trace 导出器 SPI。追踪、指标、日志关联使用 Spring Boot 管理的同一套 Micrometer Observation、Micrometer Tracing 和 OpenTelemetry 实例。

## 调用路径

```text
Servlet 请求
  → Boot ServerHttpObservationFilter（HTTP 指标 + SERVER Span）
  → TraceResponseHeaderFilter（读取当前 Span，写 traceId）
  → Security / Spring MVC
  → ApiResponseAdvice 或 Security 错误处理（记录业务结果）

业务代码 → ObservationRegistry / MeterRegistry → 应用配置的 Registry 与导出器
```

`TraceResponseHeaderFilter` 的注册顺序为 `Ordered.HIGHEST_PRECEDENCE + 2`，位于 Boot 4 的 ServerHttpObservationFilter（`+1`）之后、Security Filter Chain 之前。因此认证错误也可取得当前 Span 的 trace ID；Filter 自身不会再创建 Span。非 Servlet 应用仍可使用 Actuator 指标和业务结果计数。

`ApiResultMetrics` 预注册两个固定标签组合，不使用路径参数、用户 ID、错误消息或 traceId 作为标签。Spring MVC 的成功和错误报文由 `ApiResponseAdvice` 记录；在 MVC 之前写出的认证与授权错误由 Resource Server 记录。此指标统计响应次数，不替代 `http.server.requests` 的延迟分布。

## 依赖与配置

- `spring-boot-starter-actuator`：ObservationRegistry、MeterRegistry 和基础指标。
- `spring-boot-starter-opentelemetry`：Micrometer Tracing 与 OpenTelemetry SDK/OTLP 自动配置。没有 Collector 时显式关闭 OTLP Trace 和 Metrics 导出。
- `spring-web` 与 Servlet API 仅供可选 Servlet 适配编译；应用按需添加 Prometheus 或 OTLP 指标 Registry。
- 采样、导出端点、资源属性和指标标签统一使用 Spring Boot 的 `management.*` 配置，不另设 `loadup.tracer.*` 或 `loadup.metrics.*`。
- 异步上下文传播使用 Boot 的 `spring.task.execution.propagate-context`；自定义执行器由应用设置 `ContextPropagatingTaskDecorator`，不修改其他模块的线程池 Bean。

## 默认配置与组件集成

`ObservabilityEnvironmentPostProcessor` 以最低优先级属性源补充 `management.metrics.tags.application`，不替换消费工程的显式设置；由 Boot 自己将该标签装配到 Registry。输出日志默认值由传递引入的 commons-log 管理。

HTTP、KMS、Outbox 和 Resilience4j 使用容器中的共享 Micrometer 实例，导出由 Boot 统一负责。HTTP/KMS 的命名操作计时、Outbox 的投递计时与状态量只采用有界配置/结果维度；自定义 outcome 统一 success/failure，官方原生指标保留标准名称和标签。不创建静态全局 Registry 或新的导出 API。

配置优先级用例位于 `ObservabilityDefaultsTest`；源码测试不能替代消费工程中的导出、Trace/MDC 和异步传播验收。

## ScopedValue 上下文组合

业务上下文由 commons-context 的私有 ScopedValue 管理，`LoadUpContextTaskDecorator` 捕获不可变 ExecutionContext 并包裹任务动态执行。自动配置提供同类型缺失时的默认 Bean，Boot 4.1 原生收集并组合多个 TaskDecorator；不会因用户已有其他装饰器跳过业务传播。标准 `spring.task.execution.propagate-context` 控制 Boot Micrometer 装饰器，独立负责 Observation/Trace/MDC，框架不重复捕获它们。

移除旧 ThreadLocalAccessor/ServiceLoader 注册：set/reset 协议不能安全安装 ScopedValue 动态作用域。自定义执行器要显式配置组合；第三方直接调用 Micrometer snapshot 不会传播 LoadUp 业务数据。身份和 Trace 权威来源不变。测试覆盖异常恢复、空绑定、虚拟线程和 Boot 原生装饰器组合，未执行运行验证。

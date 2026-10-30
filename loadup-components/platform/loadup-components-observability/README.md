# Observability

基于 Spring Boot Actuator、Micrometer Observation 与 OpenTelemetry 的统一观测接入。Spring Boot 创建 HTTP
Span、指标注册表和上下文传播；本组件补充 `traceId` 响应头、业务 API 结果计数及注解驱动的 Service 摘要日志。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-observability</artifactId>
</dependency>
```

需要 Prometheus 抓取时，应用另加 `io.micrometer:micrometer-registry-prometheus` 并暴露 `prometheus` 端点。OpenTelemetry
starter 也带来 OTLP 导出能力；本地没有 Collector 时可以关闭 Trace 和 Metrics 的 OTLP 导出，Span、MDC 和响应追踪头仍然生效。

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

使用标准 `ObservationRegistry` 记录需要耗时与 Trace 的业务操作，使用 `MeterRegistry` 记录计数和状态量。
`loadup.api.responses` 按 `outcome=success|failure` 统计 JSON API 结果，不依赖统一 HTTP 200 的状态码，也不把用户 ID 或
traceId 加入指标标签。

使用 Boot 自动配置的 `@Async` 执行器时，设置 `spring.task.execution.propagate-context=true` 传播当前 Observation。自定义
`AsyncTaskExecutor` 需配置 Spring 的 `ContextPropagatingTaskDecorator`。

## 能力矩阵

| 能力                                          | 提供方                             |
|-----------------------------------------------|------------------------------------|
| HTTP、JVM 等基础指标                          | Spring Boot Actuator               |
| HTTP Span、MDC 与传播                         | Micrometer Tracing + OpenTelemetry |
| `traceId` 响应头                              | 本组件                             |
| `loadup.api.responses` 业务结果计数           | 本组件与 Web MVC / Resource Server |
| `@ServiceDigest` 类/方法摘要与 Stopwatch 耗时 | 本组件与 commons-log               |
| Prometheus 或 OTLP 指标导出                   | 应用选择相应 Micrometer Registry   |

内部边界见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 统一观测契约

组件传递引入 `loadup-commons-log`。追踪关联统一为 Micrometer 的 `traceId` / `spanId`，日志输出使用 `LogUtil`
；不创建第二套追踪或指标注册表。

EnvironmentPostProcessor 通过标准 `management.metrics.tags.application` 提供 `${spring.application.name:application}`
默认值，适用于 Boot 管理的全部指标。消费工程的显式 `management.metrics.tags.application` 具有更高优先级；优先设置稳定的
`spring.application.name`。

| 观测                   | 名称与维度                                                                      |
|------------------------|---------------------------------------------------------------------------------|
| HTTP/JVM 基础指标      | Boot/Micrometer 原生名称，保留标准维度                                          |
| JSON API 结果          | `loadup.api.responses`，`outcome=success/failure`                               |
| 命名出站调用（含 KMS） | `loadup.http.calls`，配置中的 client、operation 与 `outcome=success/failure`    |
| 分布式锁               | `loadup.lock.acquire`、`loadup.lock.hold`；有限 outcome/reason；释放失败计数    |
| Outbox 投递与状态      | `loadup.outbox.delivery` 的 success/failure；pending、failed、oldest.age 状态量 |
| Outbox handler Trace   | `loadup.outbox.handle` Observation                                              |
| Resilience4j           | 官方 Micrometer binder 的标准名称和配置实例维度                                 |

HTTP 非 2xx 与传输失败均计为 failure；标准 HTTP 客户端观测仍保留协议状态分类。组件从容器注入共享 `MeterRegistry` /
`ObservationRegistry`，未启用观测的独立消费工程可不提供可选 Registry。禁止使用全局静态 Metrics、另建生产
Registry、用户/租户/订单/traceId/完整 URI 等高基数标签；新的业务耗时与 Trace 优先使用标准 Observation。

## 业务上下文传播

传递引入 [commons-context](../../../loadup-commons/loadup-commons-context/README.md)，要求 JDK 25+。自动提供
`LoadUpContextTaskDecorator` Bean，在包装时捕获不可变业务上下文，并通过 ScopedValue 包裹整个任务。Boot 4.1 的执行器会组合多个
TaskDecorator，已有自定义装饰器不会使该装饰器被跳过；显式提供同类型 Bean 可替换默认实现。

开启 `spring.task.execution.propagate-context=true` 后，Boot 的标准 Micrometer 装饰器负责 Observation/Trace，LoadUp
装饰器负责业务元数据。只开启虚拟线程不会自动传播。自定义执行器显式组合：

```java
executor.setTaskDecorator(new CompositeTaskDecorator(List.of(
    new LoadUpContextTaskDecorator(),
    new ContextPropagatingTaskDecorator()
)));
```

多个自定义装饰器应合并配置，不要连续 setTaskDecorator 覆盖前一个。业务数据不进入 Micrometer
ThreadLocalAccessor：ScopedValue 需要动态作用域，不能采用 set/reset 适配。任意 Micrometer snapshot
或第三方线程池不会自动捕获业务上下文，须安装本装饰器或显式 ContextHolder.wrap。空 context 也绑定，退出后恢复调用前状态；值对象保持轻量不可变，身份继续使用
Spring Security 标准传播。

## Service 摘要日志

通过 `io.github.loadup.commons.log.annotation.ServiceDigest` 显式选择类或方法。注解定义在 commons-log，不依赖
AOP；引入本组件后自动装配拦截器。无需修改方法体或套用 ServiceTemplate。

```java
import io.github.loadup.commons.log.annotation.ServiceDigest;
import org.springframework.stereotype.Service;

@Service
@ServiceDigest
public class OrderService {
    public void confirm() {
        // Execute business logic.
    }
}
```

类上的注解覆盖其 public 方法；只想记录部分方法时，将注解放到对应方法：

```java
@Service
public class MerchantService {
    @ServiceDigest
    public void approve() {
        // Execute approval logic.
    }

    public boolean isReady() {
        return true;
    }
}
```

`approve` 生成摘要，`isReady` 不生成。未标记的 Service 不受拦截；构造函数、static、Object 方法及非 public 方法不生成摘要。支持继承及组合注解。

每次经过 Spring 代理的方法调用输出一条 INFO 日志，来源为
`io.github.loadup.components.observability.digest.ServiceDigestInterceptor`：

```text
service_digest service=com.example.OrderService method=public void com.example.OrderService.confirm() outcome=success code=SUCCESS elapsedMs=3 exceptionType=none
```

| 字段          | 含义                                                       |
|---------------|------------------------------------------------------------|
| service       | 实际目标类名，不使用代理类名                               |
| method        | 完整方法签名，区分重载                                     |
| outcome       | success / failure                                          |
| code          | SUCCESS、CommonException 的 ResultCode 或 SYS_ERROR        |
| elapsedMs     | Guava Stopwatch 测量的调用耗时，单位毫秒，向下取整，可为 0 |
| exceptionType | 异常类名；成功时为 none                                    |

traceId/spanId 沿用 commons-log 的 MDC 格式，由 Micrometer 管理。摘要不访问或序列化入参、返回值、异常消息和异常链；失败摘要仍使用
INFO，由业务异常处理层负责需要的错误诊断。日志后端抛出 RuntimeException 时保留原始业务结果及异常。

默认启用注解支持，可整体关闭：

```yaml
loadup:
  observability:
    service-digest:
      enabled: false
```

仅调整日志输出也可以配置标准 `logging.level.io.github.loadup.components.observability.digest=OFF`。
`spring.aop.auto=false` 会关闭 Boot 的自动代理。

### 调用边界

- 对象必须由 Spring 管理，调用需经过代理。类内 `this.method()` 不会再次拦截；跨 Bean 调用分别记录各自的方法摘要。
- 默认使用 CGLIB，目标类和被记录的方法应可继承/覆盖；JDK 接口代理可使用标准 `spring.aop.proxy-target-class=false`
  ，方法需通过接口调用。
- 摘要 advisor 顺序为 `HIGHEST_PRECEDENCE + 100`，位于默认事务和方法授权 advisor
  外侧；方法自己的事务提交失败也会记录为失败。加入外层事务的方法成功返回，并不代表最外层事务已提交。
- 耗时以被代理的方法返回/抛出为结束点；返回 Future、Publisher 或流不代表异步工作或流消费已经完成。框架不等待、不订阅、不包装返回值。异步工作需要在实际执行入口设置合适的代理调用边界。

验证包含 JDK/CGLIB 代理、类/方法/继承/组合注解、虚拟线程、异常保留、日志失败隔离以及 Boot 自动发现、真实 Micrometer MDC
和事务提交失败。事务测试使用测试事务管理器验证代理顺序，不将其视为真实数据库验收。

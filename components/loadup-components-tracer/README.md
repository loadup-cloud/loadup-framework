# LoadUp Tracer Component

LoadUp Tracer 组件基于 OpenTelemetry 为 Spring Boot 3 应用提供便捷的分布式链路追踪能力。

## 功能特性

- 🚀 **开箱即用**：自动配置，零侵入集成
- 🎯 **注解驱动**：使用 `@Traced` 注解轻松追踪方法执行
- 🌐 **HTTP 请求追踪**：自动追踪所有 HTTP 请求和响应
- ⚡ **异步任务支持**：自动传播追踪上下文到异步任务
- 🔧 **灵活配置**：丰富的配置选项满足不同场景需求
- 📊 **标准协议**：基于 OpenTelemetry 标准，兼容主流后端（Jaeger、Zipkin、Grafana Tempo 等）

## 快速开始

### 1. 添加依赖

在项目的 `pom.xml` 中添加依赖：

```xml

<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-tracer</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置应用

在 `application.yml` 中添加配置：

```yaml
spring:
  application:
    name: your-service-name

loadup:
  tracer:
    enabled: true
    enable-web-tracing: true
    enable-async-tracing: true
    # 如果使用 OTLP 导出器，配置端点
    otlp-endpoint: http://localhost:4317
```

### 3. 启动应用

无需额外代码，组件会自动配置并开始追踪。

## 使用方式

### 方法级追踪

使用 `@Traced` 注解追踪方法执行：

```java
import com.github.loadup.components.tracer.annotation.Traced;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Traced(name = "UserService.createUser")
    public User createUser(UserDTO userDTO) {
        // 业务逻辑
        return user;
    }

    // 包含方法参数和返回值
    @Traced(
            name = "UserService.findUser",
            includeParameters = true,
            includeResult = true
    )
    public User findUserById(Long id) {
        // 业务逻辑
        return user;
    }

    // 添加自定义属性
    @Traced(
            name = "UserService.updateUser",
            attributes = {"operation=update", "module=user"}
    )
    public void updateUser(User user) {
        // 业务逻辑
    }
}
```

### 类级追踪

在类上使用 `@Traced` 注解追踪所有公共方法：

```java

@Service
@Traced
public class OrderService {

    public Order createOrder(OrderDTO orderDTO) {
        // 所有方法都会被自动追踪
        return order;
    }

    public Order getOrder(Long id) {
        return order;
    }
}
```

### 编程式追踪

使用 `TraceUtil` 工具类手动创建 Span：

```java
import com.github.loadup.components.tracer.TraceUtil;
import io.opentelemetry.api.trace.Span;

@Service
public class PaymentService {

    public void processPayment(Payment payment) {
        Span span = TraceUtil.createSpan("PaymentService.processPayment");

        try {
            // 添加自定义属性
            span.setAttribute("payment.id", payment.getId());
            span.setAttribute("payment.amount", payment.getAmount().toString());

            // 业务逻辑
            doPayment(payment);

        } catch (Exception e) {
            // 记录异常
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
```

### 获取 TraceId

在日志中使用 TraceId：

```java
import com.github.loadup.components.tracer.TraceUtil;

@RestController
public class UserController {

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        String traceId = TraceUtil.getTracerId();
        log.info("Processing request with traceId: {}", traceId);

        // 或者直接记录到 MDC（组件会自动处理）
        TraceUtil.logTraceId(TraceUtil.getSpan());

        return userService.getUser(id);
    }
}
```

### 异步任务追踪

组件自动支持异步任务的追踪上下文传播：

```java
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Async
    @Traced(name = "NotificationService.sendEmail")
    public void sendEmail(String to, String subject, String content) {
        // 追踪上下文会自动传播到异步线程
        // 业务逻辑
    }
}
```

### HTTP 请求追踪

所有 HTTP 请求会自动被追踪，包括：

- HTTP 方法和 URL
- 请求/响应头（可选）
- 请求参数（可选）
- 响应状态码
- 执行时间

追踪上下文会自动通过 HTTP 头在服务间传播。

## 配置选项

### 完整配置示例

```yaml
loadup:
  tracer:
    # 是否启用追踪器
    enabled: true

    # 是否启用 Web 请求追踪
    enable-web-tracing: true

    # 是否启用异步任务追踪
    enable-async-tracing: true

    # OTLP 导出器端点
    otlp-endpoint: http://localhost:4317

    # 是否在 Span 中包含请求头
    include-headers: false

    # 是否在 Span 中包含请求参数
    include-parameters: false

    # 排除追踪的 URL 模式（逗号分隔）
    exclude-patterns: /actuator/**,/health,/metrics,/swagger-ui/**

# OpenTelemetry 配置
otel:
  exporter:
    otlp:
      endpoint: ${loadup.tracer.otlp-endpoint:}
```

### 配置说明

| 配置项                                  | 类型      | 默认值                           | 说明               |
|--------------------------------------|---------|-------------------------------|------------------|
| `loadup.tracer.enabled`              | Boolean | true                          | 是否启用追踪器          |
| `loadup.tracer.enable-web-tracing`   | Boolean | true                          | 是否启用 Web 请求追踪    |
| `loadup.tracer.enable-async-tracing` | Boolean | true                          | 是否启用异步任务追踪       |
| `loadup.tracer.otlp-endpoint`        | String  | -                             | OTLP 导出器端点地址     |
| `loadup.tracer.include-headers`      | Boolean | false                         | 是否在 Span 中包含请求头  |
| `loadup.tracer.include-parameters`   | Boolean | false                         | 是否在 Span 中包含请求参数 |
| `loadup.tracer.exclude-patterns`     | String  | /actuator/**,/health,/metrics | 排除追踪的 URL 模式     |

## 与追踪后端集成

### Jaeger

1. 启动 Jaeger（使用 Docker）：

```bash
docker run -d --name jaeger \
  -p 4317:4317 \
  -p 16686:16686 \
  jaegertracing/all-in-one:latest
```

2. 配置应用：

```yaml
loadup:
  tracer:
    otlp-endpoint: http://localhost:4317
```

3. 访问 Jaeger UI：http://localhost:16686

### Zipkin

1. 启动 Zipkin：

```bash
docker run -d --name zipkin -p 9411:9411 openzipkin/zipkin
```

2. 配置应用使用 Zipkin 导出器（需要额外依赖）

### Grafana Tempo

配置 OTLP 端点指向 Tempo：

```yaml
loadup:
  tracer:
    otlp-endpoint: http://tempo-host:4317
```

## 最佳实践

### 1. Span 命名规范

使用清晰的命名规范：

- 格式：`ClassName.methodName` 或 `Component.operation`
- 示例：`UserService.createUser`、`PaymentGateway.processPayment`

### 2. 添加业务属性

为 Span 添加有意义的业务属性：

```java

@Traced(name = "OrderService.createOrder")
public Order createOrder(OrderDTO dto) {
    Span span = TraceUtil.getSpan();
    span.setAttribute("order.type", dto.getType());
    span.setAttribute("order.amount", dto.getTotalAmount().toString());
    span.setAttribute("customer.id", dto.getCustomerId().toString());

    // 业务逻辑
    return order;
}
```

### 3. 敏感信息处理

避免在追踪中包含敏感信息：

- 不要设置 `include-headers: true`（可能包含认证信息）
- 谨慎使用 `include-parameters: true`
- 手动添加属性时过滤敏感字段

### 4. 性能考虑

- 默认使用批量处理器，不会显著影响性能
- 生产环境建议使用异步导出器
- 根据实际情况调整采样率

### 5. 排除健康检查

排除不需要追踪的端点：

```yaml
loadup:
  tracer:
    exclude-patterns: /actuator/**,/health,/metrics,/favicon.ico
```

## 故障排查

### 查看追踪日志

组件使用 LoggingSpanExporter，可以在应用日志中看到追踪信息：

```yaml
logging:
  level:
    io.opentelemetry: DEBUG
```

### 验证追踪上下文

检查 HTTP 响应头是否包含追踪信息：

```bash
curl -v http://localhost:8080/api/users/1
```

响应头应该包含：

```
traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01
```

### 常见问题

**Q: 为什么没有看到追踪数据？**

A: 检查以下几点：

1. 确认 `loadup.tracer.enabled=true`
2. 确认 OTLP 端点配置正确且可访问
3. 检查日志中是否有错误信息

**Q: 如何禁用 Web 追踪但保留方法追踪？**

A: 设置：

```yaml
loadup:
  tracer:
    enable-web-tracing: false
```

**Q: 如何自定义异步线程池配置？**

A: 创建自己的 AsyncConfigurer 并使用 TracingTaskDecorator：

```java

@Configuration
public class CustomAsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 自定义配置
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setTaskDecorator(new TracingTaskDecorator());
        executor.initialize();
        return executor;
    }
}
```

## API 参考

### TraceUtil 工具类

| 方法                                        | 说明                |
|-------------------------------------------|-------------------|
| `getTracer()`                             | 获取 Tracer 实例      |
| `getSpan()`                               | 获取当前 Span         |
| `createSpan(String name)`                 | 创建新的 Span         |
| `createSpan(String name, Context parent)` | 创建带父上下文的 Span     |
| `getTracerId()`                           | 获取当前 TraceId      |
| `logTraceId(Span span)`                   | 将 TraceId 记录到 MDC |
| `clearTraceId()`                          | 清除 MDC 中的 TraceId |

### @Traced 注解

| 属性                  | 类型       | 默认值   | 说明                  |
|---------------------|----------|-------|---------------------|
| `name`              | String   | ""    | Span 名称（默认使用方法名）    |
| `attributes`        | String[] | {}    | 自定义属性（格式：key=value） |
| `includeParameters` | boolean  | false | 是否包含方法参数            |
| `includeResult`     | boolean  | false | 是否包含返回值             |

## 技术栈

- Spring Boot 3.1+
- OpenTelemetry 1.57.0
- Java 17+

## 版本要求

- JDK: 17+
- Spring Boot: 3.0.0+
- Maven: 3.6+

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！

## 更新日志

### 1.0.0-SNAPSHOT

- 初始版本
- 支持方法级追踪（@Traced 注解）
- 支持 HTTP 请求自动追踪
- 支持异步任务追踪上下文传播
- 支持 OpenTelemetry 标准协议
- 支持多种追踪后端（Jaeger、Zipkin 等）


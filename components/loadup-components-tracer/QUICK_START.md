# LoadUp Tracer - 快速开始指南

本指南将帮助你在 5 分钟内集成 LoadUp Tracer 组件。

## 前置要求

- JDK 17+
- Spring Boot 3.0+
- Maven 3.6+

## 第一步：添加依赖

在你的 `pom.xml` 中添加依赖：

```xml

<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-tracer</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 第二步：配置应用

在 `application.yml` 中添加最小配置：

```yaml
spring:
  application:
    name: my-service  # 必需：你的服务名称

loadup:
  tracer:
    enabled: true
```

**就这样！** 现在你的应用已经具备基础的追踪能力了。

## 第三步：使用追踪

### 方式一：注解方式（推荐）

```java
import com.github.loadup.components.tracer.annotation.Traced;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Traced(name = "UserService.createUser")
    public User createUser(UserDTO userDTO) {
        // 你的业务代码
        return user;
    }
}
```

### 方式二：编程方式

```java
import com.github.loadup.components.tracer.TraceUtil;
import io.opentelemetry.api.trace.Span;

@Service
public class OrderService {

    public Order createOrder(OrderDTO orderDTO) {
        Span span = TraceUtil.createSpan("OrderService.createOrder");
        try {
            span.setAttribute("order.type", orderDTO.getType());
            // 你的业务代码
            return order;
        } finally {
            span.end();
        }
    }
}
```

## 第四步：查看追踪数据

### 选项 A：查看日志（无需额外工具）

启动应用后，追踪数据会自动输出到控制台日志：

```
INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'UserService.createUser' : 
  4bf92f3577b34da6a3ce929d0e0e4736 00f067aa0ba902b7 INTERNAL [tracer: my-service:] {}
```

### 选项 B：使用 Jaeger（推荐用于生产）

1. **启动 Jaeger**（使用 Docker）：

```bash
docker run -d --name jaeger \
  -p 4317:4317 \
  -p 16686:16686 \
  jaegertracing/all-in-one:latest
```

2. **更新配置**：

```yaml
loadup:
  tracer:
    otlp-endpoint: http://localhost:4317
```

3. **访问 Jaeger UI**：

打开浏览器访问：http://localhost:16686

## 常用配置

```yaml
loadup:
  tracer:
    enabled: true                          # 启用追踪
    enable-web-tracing: true              # 自动追踪 HTTP 请求
    enable-async-tracing: true            # 支持异步任务追踪
    include-parameters: true              # 在 Span 中包含请求参数
    exclude-patterns: /actuator/**,/health # 排除不需要追踪的 URL
```

## 高级用法

### 获取 TraceId

```java
import com.github.loadup.components.tracer.TraceUtil;

String traceId = TraceUtil.getTracerId();
log.

info("Current TraceId: {}",traceId);
```

### 添加自定义属性

```java

@Traced(
        name = "PaymentService.process",
        attributes = {"type=payment", "priority=high"}
)
public void processPayment(Payment payment) {
    // 业务代码
}
```

### 包含方法参数和返回值

```java

@Traced(
        name = "UserService.findUser",
        includeParameters = true,
        includeResult = true
)
public User findUserById(Long id) {
    return userRepository.findById(id);
}
```

## 下一步

- 查看 [README.md](README.md) 了解完整功能
- 配置生产环境的追踪后端
- 探索更多高级特性

## 故障排查

**问题：没有看到追踪数据？**

检查以下几点：

1. 确认配置中 `loadup.tracer.enabled=true`
2. 检查日志级别：
   ```yaml
   logging:
     level:
       io.opentelemetry: DEBUG
   ```
3. 如果使用 OTLP 端点，确认端点可访问

**问题：追踪数据太多？**

使用排除模式：

```yaml
loadup:
  tracer:
    exclude-patterns: /actuator/**,/health,/metrics,/static/**
```

## 帮助

如有问题，请查阅 [README.md](README.md) 或提交 Issue。


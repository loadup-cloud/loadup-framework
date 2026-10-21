# RPC Dubbo binder

引入 `loadup-components-rpc-binder-dubbo`，使用原生 `dubbo.*` 配置。当前沿用 BOM 的 Dubbo 3.3.6，尚未完成与 Spring Boot 4.1.1 的运行兼容性验证。

## 服务端

共享契约 jar 定义 Java 接口，例如 `GreetingService`。在服务端实现接口并标记 `org.apache.dubbo.config.annotation.DubboService`：

```java
@DubboService
public class GreetingServiceImpl implements GreetingService {
    @Override
    public String greet(String name) {
        return "Hello " + name;
    }
}
```

配置应用名、扫描路径、协议与注册中心。以下是不依赖注册中心的本地开发示例：

```yaml
dubbo:
  application:
    name: greeting-provider
    qos-enable: false
  scan:
    base-packages: com.example.rpc
  protocol:
    name: dubbo
    port: 20880
  registry:
    address: N/A
```

将 `com.example.rpc` 替换为实现类所在包。使用注册中心时由消费工程选择并引入对应客户端依赖，再替换 registry 配置。

## 客户端

使用原生 `ReferenceConfig` 创建 Spring Bean，保留构造器注入：

```java
@Bean(destroyMethod = "destroy")
ReferenceConfig<GreetingService> greetingReference() {
    var reference = new ReferenceConfig<GreetingService>();
    reference.setInterface(GreetingService.class);
    reference.setUrl("dubbo://127.0.0.1:20880");
    reference.setTimeout(3000);
    reference.setRetries(0);
    return reference;
}

@Bean
GreetingService greetingService(ReferenceConfig<GreetingService> reference) {
    return reference.get();
}
```

客户端同样配置 `dubbo.application.name`。示例中的 `@Bean` 方法放在应用的 `@Configuration` 中；`GreetingService` 来自共享契约。也可按 Dubbo 官方约定使用 `@DubboReference`。写操作是否重试应由应用根据幂等性明确决定，binder 不覆盖 Dubbo 默认策略。

原生配置参考：[Dubbo Spring Boot](https://dubbo.apache.org/en/overview/mannual/java-sdk/reference-manual/config/spring/spring-boot/)。

# RPC gRPC binder

引入 `loadup-components-rpc-binder-grpc`，复用 Spring Boot 4.1 的官方 gRPC client/server starters。使用 `spring.grpc.*` 原生配置；不引入 net.devh 注解或另一套 gRPC starter。

## 服务契约

在消费工程的契约模块维护 `.proto`，配置 Protobuf 与 gRPC Java 代码生成，并让客户端、服务端依赖生成的契约 jar。binder 不替消费工程选择 Protobuf 编译插件。

```protobuf
syntax = "proto3";
package greeting;
option java_package = "com.example.rpc.proto";
option java_multiple_files = true;

service Greeting {
  rpc Greet (GreetingRequest) returns (GreetingReply);
}
message GreetingRequest { string name = 1; }
message GreetingReply { string message = 1; }
```

## 服务端

继承生成的 `GreetingGrpc.GreetingImplBase`，将实现注册为 Spring Bean：

```java
@Bean
BindableService greetingService() {
    return new GreetingGrpc.GreetingImplBase() {
        @Override
        public void greet(GreetingRequest request, StreamObserver<GreetingReply> response) {
            response.onNext(GreetingReply.newBuilder()
                    .setMessage("Hello " + request.getName()).build());
            response.onCompleted();
        }
    };
}
```

`BindableService` 来自 `io.grpc`，`StreamObserver` 来自 `io.grpc.stub`。服务端端口、TLS、拦截器等使用原生 Spring Boot 配置，网络资源由 starter 管理。

## 客户端

使用 `org.springframework.grpc.client.GrpcChannelFactory` 创建 channel，注入生成的 stub：

```java
@Bean
GreetingGrpc.GreetingBlockingStub greetingStub(
        GrpcChannelFactory channels,
        @Value("${loadup.rpc.clients.user.channel}") String channelName) {
    return GreetingGrpc.newBlockingStub(channels.createChannel(channelName));
}
```

`@Value` 来自 `org.springframework.beans.factory.annotation.Value`。channel 名称从配置读取；Java 不拼接主机名、Namespace 或端口，也不直接创建 ManagedChannel。多个客户端可改用消费应用的 `@ConfigurationProperties`，目标地址仍由原生 `spring.grpc.*` 绑定。

以下示例假设 Greeting 服务由 `user-service` 提供。调用方 `application.yml` 固定使用同 Namespace 的 Headless Service DNS：

```yaml
loadup:
  rpc:
    clients:
      user:
        channel: user-service
spring:
  grpc:
    client:
      channel:
        user-service:
          target: dns:///user-service-headless:50051
          default:
            load-balancing-policy: round_robin
```

提供方在自己的 `application.yml` 配置 `spring.application.name: user-service` 和 `spring.grpc.server.port: 50051`。纯客户端应用另设 `spring.grpc.server.enabled: false`；同时提供 RPC 的应用保留服务端。

开发、测试、生产使用相同服务名和端口，调用双方位于各环境的同一 Namespace，沿用相同镜像及上述地址。Headless DNS 返回 Pod IP，`round_robin` 由 gRPC 客户端在解析结果间分配请求；DNS 本身不负责负载均衡。完整清单、发布顺序与验收见 [Kubernetes RPC 配置与发布规范](KUBERNETES.md)。

IDE 在集群外运行时，可通过 `application-local.yml` 将同一 channel 的 target 覆盖为 `static://localhost:50051`，结合端口转发；不修改主配置或重新打包。生产传输安全使用原生 SSL Bundle 与服务身份配置。

调用时为每次请求设置 deadline：

```java
var reply = greetingStub.withDeadlineAfter(3, TimeUnit.SECONDS)
        .greet(GreetingRequest.newBuilder().setName("LoadUp").build());
```

示例中的 `@Bean` 方法放在应用的 `@Configuration` 中。纯客户端应用应通过原生服务端开关禁用监听；不要在业务代码手动关闭 starter 管理的共享 channel。流式调用、取消、状态码、metadata 和凭证使用 gRPC 原生 API。

参考：[Spring Boot gRPC](https://docs.spring.io/spring-boot/reference/io/grpc.html)。

## 可选通用调用与认证支持

原生 starter 用法保持不变。消费工程需要统一 unary 调用、Bean Validation 与资源服务器认证时，显式导入 `io.github.loadup.components.rpc.grpc.GrpcSupportConfiguration`，并在 BindableService 注册时绑定 `RpcJwtInterceptor`；认证/校验依赖由消费方显式引入 `loadup-components-resource-server` 和 Validator。binder 中这两项依赖为 optional，不强制原生 starter 用户启用 JWT。

| 支持 | 类 |
| --- | --- |
| 类型化方法与 starter channel 调用 | RpcOperation / GrpcTransport |
| Spring channel 名与 deadline 绑定 | RpcClientProperties（loadup.rpc.clients） |
| JWT 验证、权限转换、逐回调身份隔离 | RpcJwtInterceptor |
| 请求校验、固定枚举错误码 trailer | RpcServerSupport |
| 标准 Timestamp / 精确十进制转换 | GrpcScalarMapper |

`GrpcTransport` 逐次附加 bearer、设置配置的 deadline；不缓存凭证、不自动重试、不接管 channel 生命周期。资源服务器签名/issuer/audience/token_use 继续验证，业务权限由应用负责。业务异常使用 FAILED_PRECONDITION 和固定 `loadup-result-code` trailer，未知异常不包含载荷或凭证。

业务 proto、方法与 MapStruct mapper 由各提供方维护，不集中放入框架。MapStruct 使用 `hasX()` 保持 optional，oneof 分支显式转换，repeated 通过 builder adders 写入；不得修改不可变 getter 集合。GrpcScalarMapper 在 Java 边界使用 `ZoneId.systemDefault()`，不设置全局时区。示例用 `@Value`，多客户端也可绑定 `RpcClientProperties`，channel/目标/deadline/TLS 均从配置读取。

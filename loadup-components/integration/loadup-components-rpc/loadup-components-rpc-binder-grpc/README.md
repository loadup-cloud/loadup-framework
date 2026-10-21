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
GreetingGrpc.GreetingBlockingStub greetingStub(GrpcChannelFactory channels) {
    return GreetingGrpc.newBlockingStub(channels.createChannel("greeting"));
}
```

本地开发可使用以下明文连接配置（生产 TLS 使用原生 SSL Bundle 配置）：

```yaml
spring:
  grpc:
    server:
      port: 9090
    client:
      channel:
        greeting:
          target: static://localhost:9090
```

调用时为每次请求设置 deadline：

```java
var reply = greetingStub.withDeadlineAfter(3, TimeUnit.SECONDS)
        .greet(GreetingRequest.newBuilder().setName("LoadUp").build());
```

示例中的 `@Bean` 方法放在应用的 `@Configuration` 中。纯客户端应用应通过原生服务端开关禁用监听；不要在业务代码手动关闭 starter 管理的共享 channel。流式调用、取消、状态码、metadata 和凭证使用 gRPC 原生 API。

参考：[Spring Boot gRPC](https://docs.spring.io/spring-boot/reference/io/grpc.html)。

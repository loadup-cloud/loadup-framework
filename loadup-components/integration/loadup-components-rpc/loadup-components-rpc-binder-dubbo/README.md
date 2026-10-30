# RPC Dubbo binder

应用间调用统一使用 Dubbo Triple 的 **Protobuf 纯 IDL 模式**。服务契约由提供方的 `*-client` 模块用 `.proto` 定义，
`dubbo-maven-plugin` 生成 protobuf 消息和 `DubboXxxTriple` 存根；不生成 gRPC `BindableService`，也不使用 Java
接口 + Wrapper 序列化模式。BOM 固定 Dubbo 3.3.6。

## 契约模块（`*-client`）

```text
<service>-client/
  src/main/proto/*.proto
  src/main/java/.../rpc/<Service>RpcMapper.java   # Wire <-> client DTO
```

POM 使用：

```xml
<plugin>
  <groupId>org.apache.dubbo</groupId>
  <artifactId>dubbo-maven-plugin</artifactId>
  <version>${dubbo.version}</version>
  <executions><execution><goals><goal>compile</goal></goals></execution></executions>
</plugin>
```

生成物位于 `target/generated-sources/protobuf/java`：protobuf 消息、`<Service>Rpc` 接口、
`Dubbo<Service>RpcTriple` 存根与 `ImplBase`。`java_package` 决定 Java 包，`package` 决定 Triple 服务名。

## 服务端

实现生成的 `ImplBase`，只重写业务方法，并标注 `@DubboService`：

```java
@DubboService(interfaceClass = CustomerRpc.class)
public class CustomerRpcService extends DubboCustomerRpcTriple.CustomerRpcImplBase {
    @Override
    public WireMerchantEligibilityDTO authorize(WireMerchantActionAuthorizeCommand request) {
        return CustomerRpcMapper.INSTANCE.toWireMerchantEligibilityDTO(
                facade.authorize(CustomerRpcMapper.INSTANCE.fromWireMerchantActionAuthorizeCommand(request)));
    }
}
```

业务 Facade 仍留在 `*-app`；`*-adapter` 只做 Wire↔DTO 转换和委托。一个 proto service 对应一个 `@DubboService`
类；多个 proto service 各自暴露。

应用配置：

```yaml
dubbo:
  application:
    name: customer
    qos-enable: false
  scan:
    base-packages: io.github.loadup.pay.customer
  protocol:
    name: tri
    port: 50051
    triple:
      max-body-size: 33554432
      max-response-body-size: 33554432
  registry:
    address: N/A
```

Protobuf 消息命中 Dubbo 的 IDL 分支（`needWrapper=false`），因此不产生 `TripleRequestWrapper` 信封，也不需要
`serialization` / `serialize-check-status` / `check-serializable` 配置。

## 客户端

在 `*-infrastructure` 引用生成的存根接口；字段类型必须是生成的 `<Service>Rpc`：

```java
@DubboReference(url = "tri://customer-headless:50051")
private CustomerRpc customerRpc;

public MerchantEligibilityDTO authorizeNewOrder(String tenant, String payee) {
    return RpcTokenContext.call(actor.customerAccessToken(), () ->
            CustomerRpcMapper.INSTANCE.fromWireMerchantEligibilityDTO(customerRpc.authorize(
                    CustomerRpcMapper.INSTANCE.toWireMerchantActionAuthorizeCommand(command))));
}
```

`@DubboReference` 只支持字段和 setter 注入；超时、重试由各应用的 `dubbo.consumer.*` 统一配置，写调用保持
`retries: 0`，长调用在注解上单独设置 `timeout`。

## 身份与错误

- 消费端用 `RpcTokenContext.call(token, ...)` 把访问令牌写入 Dubbo client attachment，调用结束即移除，不缓存凭证。
- 服务端 `RpcIdentityProviderFilter` 从 attachment 取回令牌，解析 claims 重建 `Jwt` 与 `SecurityContext`，调用结束后恢复
  原上下文。签名、issuer、audience 由入口资源服务器验证一次；内部跳转只校验 `exp`，因此 RPC 端口必须只对同 Namespace 开放。
- 业务拒绝由 `CommonException` 转为携带 `loadup-result-code=` 的 Dubbo `RpcException`。消费端用
  `RpcExceptionCodes.resultCode(failure)` 读取，并只映射自己的白名单；未知错误一律按依赖失败处理，不输出远端文案或载荷。
- 令牌缺失或过期返回 UNAUTHENTICATED；参数校验失败返回 VALIDATION_EXCEPTION。两者在消费端都不是可持久化的业务拒绝。

## 序列化与兼容性

跨进程只传 protobuf 消息；字段号、`reserved`、`optional` presence 和未知枚举拒绝语义与传统 IDL 一致。
MapStruct 只负责 Wire↔client DTO 转换，`RpcScalarMapper` 处理 `Timestamp`/`OffsetDateTime`/`BigDecimal`。

Dubbo 3.3.6 面向 Spring Boot 2.7/3.x 发布，本仓库运行在 Spring Boot 4.1.1。编译期已验证，运行时仍需在真实环境完成
启动、JWT、protobuf 编解码和滚动发布验收。原生配置参考：
[Dubbo Triple IDL](https://dubbo.apache.org/en/overview/mannual/java-sdk/tasks/protocols/triple/idl/)。

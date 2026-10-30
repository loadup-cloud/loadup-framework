# RPC

RPC 通讯组件提供 Dubbo Triple binder 与 Protobuf IDL 标量辅助模块。应用间契约使用 `.proto` 定义，由
`dubbo-maven-plugin` 生成 protobuf 消息和 Dubbo 存根；不生成 gRPC `BindableService`，也不使用 Java 接口 + Wrapper
序列化模式。

组件采用依赖型薄集成：binder 是 jar，不包含重复的 AutoConfiguration。引入依赖后，由原生 Dubbo starter 自动装配。
没有 `RpcTemplate`、`RpcProvider` 或 `loadup.rpc.binder-type`。

## 模块

```text
rpc (pom)
├── rpc-binder-dubbo → Dubbo starter + 身份/错误过滤器
└── rpc-proto        → Wire mapper 共享的 Timestamp/BigDecimal 标量转换
```

## 引入

消费工程先导入 `loadup-dependencies` BOM，再引入 binder；业务 `*-client` 模块引入 `rpc-proto`：

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-rpc-binder-dubbo</artifactId>
</dependency>
```

`loadup-components-rpc` 仅聚合模块，不作为业务运行时依赖。Dubbo 版本沿用 LoadUp BOM 中的 `dubbo.version`。

## 能力矩阵

| 能力              | Dubbo binder                                                             |
|-------------------|--------------------------------------------------------------------------|
| 客户端            | 原生 `@DubboReference`（字段或 setter 注入）引用生成的 `<Service>Rpc`    |
| 服务端            | 原生 `@DubboService` 实现生成的 `Dubbo<Service>Triple.<Service>ImplBase` |
| 服务契约          | Protobuf `.proto` + 生成存根 + Wire↔DTO mapper                           |
| 配置              | `dubbo.*`                                                                |
| 注册发现          | Dubbo 原生能力；无注册中心时使用 `registry.address: N/A` + 显式 URL      |
| 超时、TLS、过滤器 | 原生配置与扩展点                                                         |
| 流式调用          | 支持 protobuf streaming，当前业务只使用 unary                            |
| 重试              | 保留 Dubbo 原生语义，应用显式选择                                        |
| 身份上下文透传    | `RpcTokenContext` + `RpcIdentityProviderFilter`                          |
| 业务错误码透传    | `RpcExceptionCodes`                                                      |
| 运行验证          | 未执行                                                                   |

## 身份与错误

- 消费方通过 `RpcTokenContext.call(token, ...)` 把访问令牌写入 Dubbo client attachment，调用结束即移除。
- 提供方 `RpcIdentityProviderFilter`（`@Activate(group = PROVIDER)`）从 attachment 取回令牌，重建 `Jwt` 和
  `SecurityContext`，并恢复原上下文。它不重复验证签名；入口资源服务器负责签名、issuer 和 audience。
- 业务拒绝由 provider filter 转换为 `RpcException(BIZ_EXCEPTION, "loadup-result-code=<CODE>")`。消费方使用
  `RpcExceptionCodes.resultCode(failure)` 读取，并按本地白名单映射；未知错误按依赖失败处理。
- filter 通过 optional `Validator` 做参数校验兜底；业务服务仍应自行校验命令。

组件不自动推断租户，也不把 RPC attachment 中的自报字段当作身份。身份传播必须显式调用 `RpcTokenContext`，
调用方仍需校验租户、受众和权限。

## 序列化

Protobuf 消息命中 Triple 的 IDL 分支，不产生 Wrapper 信封。业务应用不需要配置 `serialization`、
`serialize-check-status` 或 `check-serializable`。详见
[Dubbo binder](loadup-components-rpc-binder-dubbo/README.md)。

## 本地验证

消费工程需要分别验证服务启动、真实请求、超时与关闭行为；选择注册中心和 TLS 后再做对应验收。Dubbo 当前固定为
3.3.6，其与本仓库 Spring Boot 4.1.1 的实际运行兼容性尚未验证，不能将依赖接入视为兼容性验收通过。

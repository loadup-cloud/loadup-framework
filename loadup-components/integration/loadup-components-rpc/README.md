# RPC

RPC 通讯组件提供 Dubbo、gRPC 两个可按需引入的 binder，覆盖原生客户端与服务端。业务使用 Dubbo Java 接口或 gRPC Protobuf 生成接口，配置、生命周期、异常、流式调用和扩展点均由底层框架负责。

组件采用依赖型薄集成：binder 是 jar，不包含重复的 AutoConfiguration。引入依赖后，由原生 starter 自动装配。没有 `RpcTemplate`、`RpcProvider` 或 `loadup.rpc.binder-type`；切换协议需要迁移服务契约，不能仅修改一个配置值。两个 binder 可同时引入，此时应分别配置监听端口和服务。

## 引入

消费工程先导入 `loadup-dependencies` BOM，再选择需要的 binder：

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-rpc-binder-dubbo</artifactId>
</dependency>
```

或：

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-rpc-binder-grpc</artifactId>
</dependency>
```

`loadup-components-rpc` 仅聚合模块，不作为业务运行时依赖。Dubbo 版本沿用 LoadUp BOM 中的 `dubbo.version`；gRPC starter 及其依赖沿用 Spring Boot BOM，不另行覆盖版本。

## 能力矩阵

| 能力 | Dubbo binder | gRPC binder |
|---|---|---|
| 客户端 | 原生 `@DubboReference` | 原生 channel factory 与生成 stub |
| 服务端 | 原生 `@DubboService` | Spring 管理的 `BindableService` Bean |
| 服务契约 | Java 接口与可序列化 DTO | `.proto` 与生成代码 |
| 配置 | `dubbo.*` | `spring.grpc.*` |
| 注册发现 | Dubbo 原生能力，注册中心依赖由应用选择 | 原生 channel 地址与 resolver，依赖由应用选择 |
| 超时、TLS、拦截器 | 原生配置与扩展点 | 原生配置与扩展点 |
| 流式调用 | 取决于所用 Dubbo 协议和契约 | 原生 gRPC streaming |
| 重试 | 保留 Dubbo 原生语义，应用显式选择 | 保留 gRPC 原生语义，应用显式选择 |
| 统一 JSON 响应 / MVC 映射 | 不介入 | 不介入 |
| LoadUp 身份上下文透传 | 未提供 | 未提供 |
| 运行验证 | 未执行 | 未执行 |

组件不自动传递租户、用户、权限或凭证，不把 RPC metadata 当作可信业务身份。身份传播、认证和授权需要由消费工程明确设计。

详细示例见 [Dubbo binder](loadup-components-rpc-binder-dubbo/README.md) 和 [gRPC binder](loadup-components-rpc-binder-grpc/README.md)。设计边界见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 本地验证

按仓库约定未执行构建或测试。消费工程需要分别验证服务启动、真实请求、超时与关闭行为；选择注册中心和 TLS 后再做对应验收。Dubbo 当前固定为 3.3.6，其与本仓库 Spring Boot 4.1.1 的实际运行兼容性尚未验证，不能将依赖接入视为兼容性验收通过。

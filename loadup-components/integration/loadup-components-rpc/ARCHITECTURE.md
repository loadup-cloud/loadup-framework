# RPC 设计

## 职责与依赖

RPC 属于 integration 分类。聚合 POM 下保留 Dubbo binder 与 Protobuf 标量辅助模块，父 POM 直接指向根 loadup-parent；
对外版本由 loadup-dependencies 管理。

```text
rpc (pom)
├── rpc-binder-dubbo → Dubbo starter + 身份/错误过滤器
└── rpc-proto        → Wire mapper 的 Timestamp/Instant/BigDecimal 转换
```

本组件是原生 starter 的依赖入口，不维护额外运行时。Dubbo 的导出、引用、注册发现和资源关闭由 Dubbo 管理；
protobuf 代码生成由消费工程的 `dubbo-maven-plugin` 完成。

## 为什么没有统一 API

Dubbo 的 IDL 存根已经表达契约，不需要再包一层 `RpcTemplate`。Binder 只提供跨服务调用共同需要、且不能下沉到
业务模块的三件事：把调用者令牌放进 Dubbo attachment、在提供方重建安全上下文、把业务结果码编码为可识别的
Dubbo 异常。`.proto`、Wire↔DTO mapper 和业务权限仍归各业务服务。

## 契约边界

Protobuf 只用于跨进程传输；业务 client 仍维护纯 Java Facade、Command/Query/DTO。`*-client` 模块持有 `.proto` 和
MapStruct Wire mapper，`*-app` 不依赖 protobuf，`*-adapter` 实现生成的 `ImplBase` 并委托 app。这样 IDL 的
`optional`/`oneof`/`reserved` 语义只停留在传输边界，不污染 domain。

## 配置所有权

目标、TLS、负载均衡、超时、重试与底层网络参数沿用原生 `dubbo.*` 配置。注册中心由消费工程选择；LoadUp Pay
使用 `registry.address: N/A` 加显式 `tri://<service>-headless:50051`。组件不启动独立注册中心，不自动生成业务服务。

## 身份边界

`RpcTokenContext` 只负责把调用方提供的令牌写入 client attachment，并在调用结束后移除；它不读取
`SecurityContext`，也不推断租户。`RpcIdentityProviderFilter` 只解析 claims 与 `exp`，不重复验证签名——
签名、issuer、audience 必须在入口验证一次。因此使用该 binder 的 RPC 端口只能对同 Namespace 开放。

## 错误边界

provider filter 把 `CommonException` 转换为 `RpcException(BIZ_EXCEPTION, "loadup-result-code=<CODE>")`；
未知异常保持不透明。消费方通过 `RpcExceptionCodes.resultCode` 读取并做本地白名单映射，不能把远端描述直接
返回给调用者。

## 验收边界

依赖型 binder 的静态检查包括聚合入口、父 POM 相对路径、BOM 声明和依赖版本来源。运行验收由真实消费工程
覆盖客户端与服务端、JWT 透传、protobuf 编解码、异常、超时和资源关闭。未运行的检查不得视为通过。
Dubbo 3.3.6 与 Spring Boot 4.1.1 的运行时兼容性仍需真实启动验收。

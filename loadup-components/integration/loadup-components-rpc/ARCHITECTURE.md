# RPC 设计

## 职责与依赖

RPC 属于 integration 分类。聚合 POM 下有两个独立 binder，父 POM 均直接指向根 loadup-parent；对外版本由 loadup-dependencies 管理。

```text
rpc (pom)
├── rpc-binder-dubbo → Spring Boot starter + Dubbo starter
└── rpc-binder-grpc  → Spring Boot gRPC client/server starters
```

本组件是原生 starter 的依赖入口，不维护额外运行时。gRPC 的服务、channel 与网络资源由官方 starter 管理；Dubbo 的导出、引用、注册发现和资源关闭由 Dubbo 管理。

## 为什么没有统一 API

Dubbo 的 Java 接口代理与 gRPC 的生成 stub、MethodDescriptor、streaming 和 metadata 不是可透明替换的 Provider。遵循仓库“底层 OSS + 薄集成、优先标准接口”的原则，当前不建立共同 Provider/Template，也不采用只允许单后端的 Mode A 选择器。各自服务契约保持原生，应用按需选用或共存。

## 配置所有权

仅使用原生配置命名空间，不复制注册中心、连接、超时、TLS、重试或线程池配置。注册中心客户端、Protobuf 编译插件、业务 proto 与生成代码属于消费工程。组件不启动独立注册中心，不自动生成业务服务，不依赖其他业务模块。

## 验收边界

依赖型 binder 的静态检查包括聚合入口、父 POM 相对路径、BOM 声明和依赖版本来源。运行验收由真实消费工程分别覆盖客户端与服务端、异常、deadline、资源关闭；两种协议共存、注册中心和 TLS 按实际部署配置单独验收。未运行的检查不得视为通过。

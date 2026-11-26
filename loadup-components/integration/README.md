# 应用集成组件接入

消费应用导入 [Framework BOM](../../loadup-framework-bom/README.md)，再按需求选择下表中的工件。此目录的聚合 POM 用于组织 Maven 子模块。

| 组件 | 适用场景 | 接入要点 |
| --- | --- | --- |
| [HTTP 客户端](loadup-components-http/README.md) | 命名客户端与 Spring HTTP Service Clients | 为提供方配置基础地址、超时、连接资源及 TLS。 |
| [RPC](loadup-components-rpc/README.md) | Protobuf 协议与可选 Dubbo 适配 | 按服务契约选择 proto 与 Dubbo 工件。 |

[分类架构](ARCHITECTURE.md) · [全部组件](../README.md)

# 应用集成组件架构

## 职责与依赖

HTTP 和 RPC 是独立的应用边界。接口契约由提供方定义，宿主负责服务地址、出站身份和调用失败后的业务恢复。Pay 当前内部调用走 HTTP，Framework 的 RPC 仍可供其他消费者选择。

宿主直接依赖所需的组件工件。目录对应 Maven reactor 的聚合层，运行时依赖由各组件 POM 声明。

## 组件设计

- [HTTP 客户端](loadup-components-http/ARCHITECTURE.md)
- [RPC](loadup-components-rpc/ARCHITECTURE.md)

[接入说明](README.md)

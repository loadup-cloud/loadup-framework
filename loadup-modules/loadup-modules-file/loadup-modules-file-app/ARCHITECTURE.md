# LoadUp File App Architecture

## 职责

应用用例编排、Spring Bean 装配与客户端 DTO 映射。

依赖 client/domain/infrastructure，服务通过构造器依赖 Gateway。负责校验、租户范围、事务与外部组件调用，返回客户端 DTO；生成的 MapStruct Spring 实现显式 Import，无条件组件扫描。

整体职责、事务和失败语义见 [业务架构](../ARCHITECTURE.md)，接入见 [README.md](README.md)。

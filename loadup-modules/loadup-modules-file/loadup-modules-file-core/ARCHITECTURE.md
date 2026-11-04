# LoadUp File Core Architecture

## 职责

在同一 JAR 内保留 `domain`、`app` 和 `infrastructure` 包边界：

- `domain`：纯 Java 领域模型与 Gateway 端口，不依赖 Spring 或 ORM 注解。
- `app`：用例编排、客户端 DTO 映射与自动装配；服务通过构造器依赖 Gateway。
- `infrastructure`：DO、Mapper、GatewayImpl、Converter、Flyway 迁移与持久化自动装配。

合并 Maven 模块不改变包职责，只把总是一起消费的持久化实现与用例编排放进同一发布单元。MapStruct 生成的 Spring 实现显式 Import，
无条件组件扫描。

整体职责、事务和失败语义见 [业务架构](../ARCHITECTURE.md)，接入见 [README.md](README.md)。

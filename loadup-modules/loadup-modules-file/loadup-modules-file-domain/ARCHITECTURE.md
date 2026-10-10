# LoadUp File Domain Architecture

## 职责

纯 Java 领域模型与持久化 Gateway 端口。

只依赖 JDK，模型采用不可变 record，Gateway 返回领域模型，不使用 client DTO、数据库映射注解或 Spring Bean。

整体职责、事务和失败语义见 [业务架构](../ARCHITECTURE.md)，接入见 [README.md](README.md)。

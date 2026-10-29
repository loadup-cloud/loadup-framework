# LoadUp Audit Infrastructure Architecture

## 职责

JDBC Gateway 默认实现、Flyway 迁移及持久化自动装配。

依赖 domain 的端口与模型。默认适配使用 Spring JDBC 和 Converter；自动配置只创建 Gateway，不创建应用服务。Flyway 初始脚本位于本层。

整体职责、事务和失败语义见 [业务架构](../ARCHITECTURE.md)，接入见 [README.md](README.md)。

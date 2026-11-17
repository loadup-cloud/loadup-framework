# OAuth Core 架构

实现签发、资源端验证、可选 JDBC 存储和数据库客户端生命周期管理；按角色分包和独立自动配置，详见[组件架构](../ARCHITECTURE.md)。

`OAuthClientManager` 使用标准 `RegisteredClient` 字段作为输入。它生成并仅一次返回明文凭证，存储端只收到密码散列。授权更新保留注册 ID、client ID 和凭证；停用及凭证到期由协议仓储在查找时执行过滤。每次签发还会核对当前 grant、scope 与凭证。管理写入使用单条记录的 JDBC 行锁和本地事务；自动装配要求单一 `DataSourceTransactionManager`（包括 `JdbcTransactionManager`）、同库 JDBC 与共享 Clock。没有新增数据库列或公开 HTTP 管理 API。

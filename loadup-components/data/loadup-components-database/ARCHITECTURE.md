# Database 组件架构

## 持久化边界

组件为 Spring Data JDBC 和 Spring JDBC 提供持久化约定、共享事务、固定 schema、Flyway 装配以及 ID 和序列能力。宿主选择数据源、驱动和部署迁移。简单单表查询和写入可使用 Repository 与 `JdbcAggregateTemplate`，复杂筛选、加锁、条件更新与批处理使用参数化 JDBC。

```mermaid
flowchart TD
    Gateway["Infrastructure Gateway"] --> Repository["Spring Data JDBC"]
    Gateway --> JDBC["Spring JdbcClient"]
    Gateway --> ID["IdGenerator"]
    Repository --> Tx["Spring 事务连接"]
    JDBC --> Tx
    Config["loadup.database.schema"] --> Pool["Hikari 固定物理连接 schema"]
    Config --> Flyway["Flyway defaultSchema 与 schemas"]
    Config --> Verifier["启动验证当前 schema 与搜索路径"]
    Pool --> Tx
    Tx --> Schema["固定 PostgreSQL schema"]
    Flyway --> Schema
```

`BaseDO` 维护公共字段。应用显式分配主键。审计时间使用共享业务 Clock 及其时区。

| 字段 | 列 | 类型 | 行为 |
| --- | --- | --- | --- |
| `id` | `id` | `VARCHAR(64)` | 显式赋值，Spring Data `@Id` 映射 |
| `createdAt` | `created_at` | `TIMESTAMP(6)` | 插入时写入 |
| `updatedAt` | `updated_at` | `TIMESTAMP(6)` | 插入与更新时写入 |

## 事务

宿主按模块配置 Repository 扫描。两种访问路径共用 DataSource 和 Spring JDBC 事务管理器。业务 Gateway 显式设置 ID 和审计时间。需要逻辑删除的表声明自己的业务状态和 SQL 条件。

## 固定 schema 与迁移归属

`FixedSchemaEnvironmentPostProcessor` 对齐连接池的 schema，拒绝不一致的显式 schema 和可改变连接初始状态的配置。`FixedSchemaVerifier` 在 Worker 等后台执行器开始前验证 `current_schema()` 与完整 `search_path`。请求和后台任务共用固定数据源，部署身份与连接目标分别配置。

数据库角色负责实际访问边界。迁移与运行账号按部署分权，跨 schema 权限需在目标数据库验收。非关系存储按部署配置自己的命名空间。

`LoadUpFlywayAutoConfiguration` 将相同 schema 应用于 `defaultSchema` 和 `schemas`。应用选择明确的迁移位置，每个应用的所选脚本共享一个历史记录。脚本使用当前 schema 下的未限定表名，已发布版本保留原内容。

## 序列分配

`BusinessIdGenerator` 在 Sequence 可用时提供默认应用 ID。Sequence 通过 Spring JDBC 以独立的 `REQUIRES_NEW` 事务预留区间，按 schema 内唯一 `scope_key` 更新分配上界，业务回滚后号码仍然有效。

每个作用域持有一个活跃区间和最多一个备用区间，剩余 20% 时开始预取。热路径用原子操作领取号段中的号码，并发冷启动共享一次分配。初始化和死锁等短暂故障在新事务中有限重试并加入抖动。预取失败保留当前区间，失败冷却抑制重复分配请求。队列拒绝、超时、容量耗尽、最大值不一致和作用域接纳失败通过带 `DatabaseResultCodeEnum` 的 `CommonException` 报告。

作用域达到上限时可清理过往日期的缓存。未用号段丢弃，已在运行的分配可能提交但对新的缓存条目不可见。常驻与当前日期作用域保留，超出的请求返回错误。关闭应用会停止预取并结束未完成的等待。

[接入、配置与业务 ID 格式](README.md)

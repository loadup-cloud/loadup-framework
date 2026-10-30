# LoadUp Contract Test Architecture

## 验证分层

领域测试不启动 Spring，验证确定性算法。ContractCodecTest 检查 Jackson3 多态存储与摘要规则。ContractPersistenceIT 使用
SpringBootTest、Testify 和真实 PostgreSQL Testcontainers，不用 MockBean 替代数据库。

IT 为每个场景创建独立租户，测试数据不依赖用户账号或本地业务库。MerchantFactsProvider 测试 Bean 只在测试 Application
中提供固定可信资料，不进入发布 jar。并发线程通过 TenantUtil.callWithTenant 显式绑定上下文。

## 证据边界

测试源码存在不代表已通过；定向运行结果见 README。多进程退出/恢复、HTTP 权限、部署主库路由、迁移组合和真实商户事实仍须消费工程验收。

## 测试装配约定

使用标准 Boot 自动装配，显式声明可选 Flyway starter 及 PostgreSQL 扩展，通过真实迁移创建业务表和 Sequence 表。持久化框架配置由
database 独占，测试配置不覆盖 `mybatis-flex.global-config`。测试类不固定 profile，允许 CI 切换配置；关闭容器复用以隔离迁移历史和其他测试遗留的
schema。

# LoadUp Contract Test Architecture

## 验证分层

领域测试不启动 Spring，验证确定性算法。ContractCodecTest 检查 Jackson3 多态存储与摘要规则。ContractPersistenceIT 使用 SpringBootTest、Testify 和真实 MySQL Testcontainers，不用 MockBean 替代数据库。

IT 为每个场景创建独立租户，测试数据不依赖用户账号或本地业务库。MerchantFactsProvider 测试 Bean 只在测试 Application 中提供固定可信资料，不进入发布 jar。并发线程通过 TenantUtil.callWithTenant 显式绑定上下文。

## 证据边界

测试源码存在不代表已编译或通过；本轮未运行。多进程退出/恢复、HTTP 权限、部署主库路由、迁移组合和真实商户事实仍须消费工程验收。

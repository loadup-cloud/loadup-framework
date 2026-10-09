# Contract Test 架构

测试通过领域公开入口执行，覆盖 SalesPlanCompiler → ContractSigner → ContractRuntimeResolver，不依赖数据库替身或框架自动装配。关键验证是越权值无法签约、未知条件不能放行、快照不受上游修改影响以及运行区间不会重叠。

测试模块只以 test scope 依赖 domain、JUnit Jupiter 和 AssertJ，parent 指向根 loadup-parent。没有生产代码。

持久化阶段需要 Testify + MySQL Testcontainers 覆盖事务回滚、幂等冲突、租户隔离、并发区间更新及同事务 Outbox；页面/HTTP 阶段需要认证身份、POST 报文、响应包装和 OpenAPI 验证。上述尚未实现或执行。

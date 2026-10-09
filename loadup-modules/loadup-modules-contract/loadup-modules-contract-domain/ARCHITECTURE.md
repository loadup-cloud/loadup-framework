# Contract Domain 架构

详细领域决策见 [合约详细设计](../ARCHITECTURE.md)。本 jar 无 Spring、ORM、Jackson 或缓存依赖。

## 内部结构

- model：不可变 ProductVersion、BundleVersion、SalesPlanVersion、MerchantContractRevision 与 MerchantContract 运行视图。
- TypedValue/ParameterDefinition/OverridePolicy：规范化标量及分层覆盖约束。
- ConfigurationResolver：默认填充、权限检查、来源记录和最终必填校验。
- Condition/ConditionEvaluator：有界结构化条件与 MATCH/NO_MATCH/INDETERMINATE。
- SalesPlanCompiler：展开组合、检查协商范围和配置引用。
- ContractSigner：检查资格和选择，形成固定条款。
- ContractRuntimeResolver：基于当前可信合约视图和事实做纯判断。
- SnapshotDigest：包内摘要实现，带长度前缀与稳定 map 顺序，不依赖 Java toString。

服务型纯函数与值对象集中在 domain.model，形成一套内聚的阶段一领域模型；后续增加 Gateway 放 domain.gateway，事务编排放 app.service，不向领域对象添加 Spring 注解。

## 不变量

集合防御性复制；已发布方案只能通过 compiler 构建；字段类型不隐式转换；商户权限必须明确授权且不能扩大产品约束。规则缺失事实不放行。固定签约配置不重新读取当前产品默认值。

当前运行视图校验区间，但不提供修订安排的并发写入。暂停状态来自消费方的权威读取。不可变条款与未来可审计安排的分离需要持久化阶段完成，不能用此 jar 保证数据库幂等或多进程一致性。

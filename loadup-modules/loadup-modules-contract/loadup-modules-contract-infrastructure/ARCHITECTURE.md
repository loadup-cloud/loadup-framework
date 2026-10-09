# LoadUp Contract Infrastructure Architecture

## 职责与边界

MySQL 合约持久化与租户 Gateway。完整业务不变量见 [合约设计](../ARCHITECTURE.md)。

## 内部设计

Gateway 实现 domain 接口；DO 继承 BaseDO，包含内容字段但不在 toString 输出完整 JSON。保存和状态更新有 rowVersion/generation 条件；唯一约束保护编码版本、商户范围和幂等键。原子 header/revision 写入需由 app 事务包围，不要直接调用写 Gateway 绕过事务。

## 关键契约

三个表存储目录版本、合约头和不可变修订。Mapper 仅继承 BaseMapper；QueryWrapper 显式 tenant/deleted 约束，MapStruct 使用共享 Spring 配置。Flyway V20261009000001 自动提供结构。

## 依赖和扩展

遵守 client/domain → infrastructure → app → 可选 web 方向；领域不依赖 Spring、JSON 或数据库。所有转换使用共享 MapStruct 配置；不引入第二套继承或条件判断算法。

v1 只支持单合约范围、初次签约 revision=1。后续修订需增加独立生效安排和历史；审批/审计/Outbox 按业务事务边界接入。当前不使用缓存，主库状态决定暂停/终止，避免异步事件造成旧状态放行。

## 验证

ContractCodecTest 验证内部存储方言、摘要及边界；ContractPersistenceIT 使用真实 MySQL 验证数据库约束、事务、幂等和状态；源码已写入但未运行。HTTP/权限和真实消费工程部署验收仍待执行。

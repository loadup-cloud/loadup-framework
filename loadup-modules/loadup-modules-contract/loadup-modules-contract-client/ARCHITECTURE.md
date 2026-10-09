# LoadUp Contract Client Architecture

## 职责与边界

对外数据契约与可信商户事实 SPI。完整业务不变量见 [合约设计](../ARCHITECTURE.md)。

## 内部设计

类型契约可被业务调用方依赖。MerchantFactsProvider 由消费工程实现；不引入默认数据、HTTP 调用或数据库查询。泛型目录 definition 在 app 按 kind 校验，client 只表达报文。

## 关键契约

CatalogDefinitions、Command/Query/DTO 为不可变 record。所有 JSON 值与数值配置通过显式类型和字符串表达，不依赖领域模型或 ORM。ContractError 复用全局 ResultCode。

## 依赖和扩展

遵守 client/domain → infrastructure → app → 可选 web 方向；领域不依赖 Spring、JSON 或数据库。所有转换使用共享 MapStruct 配置；不引入第二套继承或条件判断算法。

v1 只支持单合约范围、初次签约 revision=1。后续修订需增加独立生效安排和历史；审批/审计/Outbox 按业务事务边界接入。当前不使用缓存，主库状态决定暂停/终止，避免异步事件造成旧状态放行。

## 验证

ContractCodecTest 验证内部存储方言、摘要及边界；ContractPersistenceIT 使用真实 MySQL 验证数据库约束、事务、幂等和状态；源码已写入但未运行。HTTP/权限和真实消费工程部署验收仍待执行。

# LoadUp Contract Web Architecture

## 职责与边界

可选 Spring MVC 管理 API。完整业务不变量见 [合约设计](../ARCHITECTURE.md)。

## 内部设计

Web 只做 HTTP 参数、当前 Authentication 操作者、权限与服务调用适配；不保存数据、不编译规则。TenantUtil 作用域必须由消费工程可信身份入口建立；API 权限与商户归属校验不可由客户端代替。

## 关键契约

Controller 路径 /contract，WebMVC 默认添加 /api。全部 JSON 操作为 POST，@Valid + @PreAuthorize + SpringDoc，响应由全局 WebMVC result/data 包装。loadup.contract.web.enabled 可关闭装配。

## 依赖和扩展

遵守 client/domain → infrastructure → app → 可选 web 方向；领域不依赖 Spring、JSON 或数据库。所有转换使用共享 MapStruct 配置；不引入第二套继承或条件判断算法。

v1 只支持单合约范围、初次签约 revision=1。后续修订需增加独立生效安排和历史；审批/审计/Outbox 按业务事务边界接入。当前不使用缓存，主库状态决定暂停/终止，避免异步事件造成旧状态放行。

## 验证

ContractCodecTest 验证内部存储方言、摘要及边界；ContractPersistenceIT 使用真实 MySQL 验证数据库约束、事务、幂等和状态；源码已写入但未运行。HTTP/权限和真实消费工程部署验收仍待执行。

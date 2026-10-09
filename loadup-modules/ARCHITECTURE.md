# 业务模块架构

业务模块位于 `commons → components → modules → 消费工程` 的第三层；业务模块之间避免横向依赖。当前业务能力包括 [UPMS](loadup-modules-upms/README.md)、[审计中心](loadup-modules-audit/README.md)、[数据字典](loadup-modules-dictionary/README.md)、[文件资源](loadup-modules-file/README.md)、[站内通知](loadup-modules-notification/README.md) 和 [导入导出任务](loadup-modules-transfer/README.md)。导入导出任务以文件资源作为明确的下层业务能力，复用其访问控制与 DFS 生命周期。

UPMS 按 COLA 分层：`client` 暴露 DTO/Command/Query，`domain` 保存纯领域模型与网关接口，`infrastructure` 实现持久化，`app` 编排用例，`web` 提供可选 Controller，`authserver` 提供可选 SAS 适配。领域层不得引入 Spring MVC 或 ORM 注解；持久化对象继承 `BaseDO`，映射由 MapStruct Spring 组件完成。

应用通过 Controller 暴露 HTTP 接口。UPMS 提供用户与权限数据，不负责独立令牌签发；SAS、Resource Server 和方法授权分别由独立组件承担。跨模块待办见根目录 `ROADMAP.md`。

合约模块采用相同 COLA 目标分层，当前仅交付纯 Java domain 与测试；产品到合约的固定版本、参数覆盖和条件模型见 [合约详细设计](loadup-modules-contract/ARCHITECTURE.md)。持久化、编排和可选 Web 适配在后续阶段提供。

## 分层与调用路径

```text
loadup-modules
  ├─ loadup-modules-contract → 纯领域核心（阶段一）
  ├─ loadup-modules-upms
  ├─ loadup-modules-audit + loadup-modules-audit-web
  ├─ loadup-modules-dictionary + loadup-modules-dictionary-web
  ├─ loadup-modules-file + loadup-modules-file-web → DFS
  ├─ loadup-modules-notification + loadup-modules-notification-web → Gotone
  └─ loadup-modules-transfer + loadup-modules-transfer-web → RetryTask + 文件资源
```

聚合 POM 组织模块与版本，运行时依赖由子模块决定。

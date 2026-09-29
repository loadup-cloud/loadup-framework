# 业务模块架构

业务模块位于 `commons → components → modules → 消费工程` 的第三层；业务模块之间避免横向依赖。当前发布的业务能力是 [UPMS](loadup-modules-upms/README.md)。

UPMS 按 COLA 分层：`client` 暴露 DTO/Command/Query，`domain` 保存纯领域模型与网关接口，`infrastructure` 实现持久化，`app` 编排用例，`web` 提供可选 Controller，`authserver` 提供可选 SAS 适配。领域层不得引入 Spring MVC 或 ORM 注解；持久化对象继承 `BaseDO`，映射由 MapStruct Spring 组件完成。

应用通过 Controller 暴露 HTTP 接口。UPMS 提供用户与权限数据，不负责独立令牌签发；SAS、Resource Server 和方法授权分别由独立组件承担。跨模块待办见根目录 `ROADMAP.md`。

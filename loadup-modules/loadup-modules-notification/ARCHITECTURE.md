# In-App Notifications 架构

## 职责

站内通知是可持久化、可读取的业务收件箱；Gotone 负责渠道选择、模板与发送编排。`InAppChannelProvider` 实现 Gotone `NotificationChannelProvider`，并调用 `InboxService` 落库；业务也可直接调用 `InboxService`。模块不依赖 UPMS，收件人 ID 是业务身份标识。

```text
业务调用 → InboxService → InboxGateway → notification_inbox
Gotone NotificationService → IN_APP provider ─┘
Web Controller → 当前认证用户的收件箱 ────┘
```

## 数据与一致性

一位收件人对应一条 `notification_inbox` 记录，保存租户、收件人、发送者、分类、标题、正文、应用内链接、已读时间和归档时间。`(tenant_id, recipient_id, request_key)` 为非空 key 提供幂等约束；不提供 key 时允许重复投递。批量投递由一个 JDBC 事务包裹，单次最多 100 人；Gotone 渠道返回逐收件人状态。

读取、已读及归档操作均加租户和收件人谓词。归档是软删除，不影响其他收件人的副本。首版不自动清理历史消息；留存与批量投递扩展见根目录 Roadmap。

## 边界

Web 层仅允许超级管理员发布消息，普通用户只能操作自己的收件箱；业务系统内部使用服务时，负责收件人身份和租户归属校验。通知正文作为文本保存，前端应进行 HTML 转义。`actionUrl` 限制为站内路径，避免发布任意外部跳转。

接入见 [README.md](README.md)。

## COLA 职责与装配

```text
web → app → domain
       ↓       ↑
     client  infrastructure → MySQL / Flyway
```

- client 定义不可变请求和 DTO，domain 保留领域模型与 `InboxGateway` 端口；应用边界由 `NotificationDTOConverter` 以 MapStruct Spring 模式映射。
- `NotificationPersistenceAutoConfiguration` 归 infrastructure，按 DataSource 和 `loadup.modules.notification.enabled` 提供默认 Gateway，消费者可覆盖接口 Bean。
- app 在持久化装配之后按 Gateway 及所需组件条件创建 `InboxService`。生成的 converter 通过显式 `@Import` 注册，避免 REGISTER_BEAN 阶段条件与组件扫描冲突。
- web 只负责路由、可信身份/租户、方法授权和 HTTP 投影；请求契约归 client。全局响应、Jackson 与 `/api` 前缀由 WebMVC 组件处理。
- 仓储使用 MyBatis-Flex、Tables 常量与 Spring MapStruct；保留租户、锁、幂等及状态条件更新语义。Flyway 历史脚本保持原样，标准字段通过新增迁移补齐。
- 所有子模块 parent 指向根 `loadup-parent`，所有内部依赖坐标由 BOM 管理。

配置契约迁移到 `loadup.modules.notification.*`，不保留旧前缀别名。自动装配、覆盖默认 Gateway 和关闭功能的回归源码见 `NotificationAutoConfigurationTest`，本次未执行；真实数据库与 HTTP 验收仍需由消费工程完成。

## 公共边界与映射约束

Facade 是 client 的业务契约，应用服务直接实现；Controller 和跨模块消费者依赖 Facade。domain 保留业务状态、规则与 Gateway，表示层字段转换交给 Spring 管理的 MapStruct。共享配置固定 Spring 模式、构造器注入和目标字段严格校验。

仓储依赖 database 的固定 UUID、审计时间、逻辑删除规则，显式声明空 BaseMapper，并通过模块生成的 Tables 表达查询。字典删除与文件引用解绑明确使用物理删除；文件状态、通知归档和任务生命周期是业务状态，独立于 BaseDO 的 deleted。

所有数据对象的诊断文本使用 commons-json；诊断序列化与真实 API JSON 分离，避免因 HTTP 脱敏设置改变日志中的凭证披露规则。

## Client 入参与分页边界

client.command 表达业务写动作，client.query 表达只读条件；Controller 和 Facade 共用入参契约。模块专用 PageDTO 已移除，app 将领域分页对象映射为 commons-dto 的 PageDTO；Web 输出 PageResponse，避免分页结构随模块变化。领域 Gateway 的分页返回值不携带 HTTP envelope。

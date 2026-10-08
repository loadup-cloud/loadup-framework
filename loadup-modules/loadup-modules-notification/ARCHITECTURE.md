# In-App Notifications 架构

## 职责

站内通知是可持久化、可读取的业务收件箱；Gotone 负责渠道选择、模板与发送编排。`InAppChannelProvider` 实现 Gotone `NotificationChannelProvider`，并调用 `InboxService` 落库；业务也可直接调用 `InboxService`。模块不依赖 UPMS，收件人 ID 是业务身份标识。

```text
业务调用 → InboxService → InboxRepository → notification_inbox
Gotone NotificationService → IN_APP provider ─┘
Web Controller → 当前认证用户的收件箱 ────┘
```

## 数据与一致性

一位收件人对应一条 `notification_inbox` 记录，保存租户、收件人、发送者、分类、标题、正文、应用内链接、已读时间和归档时间。`(tenant_id, recipient_id, request_key)` 为非空 key 提供幂等约束；不提供 key 时允许重复投递。批量投递由一个 JDBC 事务包裹，单次最多 100 人；Gotone 渠道返回逐收件人状态。

读取、已读及归档操作均加租户和收件人谓词。归档是软删除，不影响其他收件人的副本。首版不自动清理历史消息；留存与批量投递扩展见根目录 Roadmap。

## 边界

Web 层仅允许超级管理员发布消息，普通用户只能操作自己的收件箱；业务系统内部使用服务时，负责收件人身份和租户归属校验。通知正文作为文本保存，前端应进行 HTML 转义。`actionUrl` 限制为站内路径，避免发布任意外部跳转。

接入见 [README.md](README.md)。

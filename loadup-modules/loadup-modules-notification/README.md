# In-App Notifications

持久化站内收件箱。业务可直接调用 `InboxService` 投递，也可把它作为 Gotone 的 `IN_APP` 渠道，与邮件、短信等渠道共享发送入口。消息按租户及收件人隔离，支持分页、未读计数、已读和归档。

## 引入

```xml
<dependency><groupId>io.github.loadup-cloud</groupId><artifactId>loadup-modules-notification</artifactId></dependency>
```

应用需要 MySQL `DataSource`；Flyway 执行 `V20261003000001__create_notification_inbox.sql`。设置 `loadup.notification.enabled: false` 可关闭自动装配。需要 API 时另引入 `loadup-modules-notification-web`；需要使用 Gotone 统一发送入口时引入 `loadup-components-gotone-engine`。

## 服务 API

`InboxService.publish(tenantId, senderId, recipients, category, title, body, actionUrl, requestKey)` 每次最多投递 100 个收件人，返回新插入的消息数。`requestKey` 可选；同一租户、收件人和 key 重复投递只保留一条。`actionUrl` 仅接受应用内以 `/` 开头的路径。`list`、`unreadCount`、`markRead`、`markAllRead` 和 `archive` 均以收件人 ID 为边界。调用方负责保证收件人属于当前租户。

Gotone `IN_APP` 渠道使用 `NotificationRequest.channels = ["IN_APP"]`，接收者为用户 ID。`templateParams` 提供 `tenantId`、`title`、`body`、`category`，可选 `senderId`、`actionUrl`、`requestKey`；已配置 Gotone 模板时渲染结果作为消息正文，否则使用 `body`。业务重试时应复用相同 `requestKey`，以避免重复入站通知。没有 `tenantId` 时按单租户 `__default__` 处理。

Web 接口见 [Web 适配](../loadup-modules-notification-web/README.md)，设计见 [ARCHITECTURE.md](ARCHITECTURE.md)。

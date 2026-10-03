# In-App Notifications Web 架构

`NotificationController` 是 `InboxService` 的可选 MVC 适配。自动配置仅在核心服务存在时注册它；WebMVC 统一处理 JSON 包装和异常。

发布操作要求 `ROLE_SUPER_ADMIN`，发送人 ID 来自认证主体；收件箱操作只使用认证主体的 `userId` 与可信 `TenantUtil` 租户上下文。Controller 不接受读取其他用户消息的请求参数。业务系统需要更细粒度的发布授权时，应直接调用服务或扩展自身 Controller。

接入见 [README.md](./README.md)。

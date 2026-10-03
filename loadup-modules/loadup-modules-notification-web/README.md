# In-App Notifications Web

引入 `loadup-modules-notification-web` 后，在站内通知服务可用时注册以下 `/api/notifications` 接口。设置 `loadup.notification.web.enabled: false` 可关闭 HTTP 适配。

| 方法 | 路径 | 作用 | 权限 |
| --- | --- | --- | --- |
| POST | `/api/notifications/publish` | 向指定用户发布通知 | `ROLE_SUPER_ADMIN` |
| POST | `/api/notifications/list` | JSON 分页查询本人消息，可传 `unreadOnly` | 登录用户 |
| POST | `/api/notifications/unread-count` | JSON `{}` 查询本人未读数 | 登录用户 |
| POST | `/api/notifications/read` | JSON `{ "id": "..." }` 标记本人消息已读 | 登录用户 |
| POST | `/api/notifications/read-all` | JSON `{}` 全部标记已读 | 登录用户 |
| POST | `/api/notifications/archive` | JSON `{ "id": "..." }` 归档本人消息 | 登录用户 |

JSON 响应复用 WebMVC 的 `{result, data}` 契约。收件人从认证主体获取，不接受客户端指定读取用户 ID。发布请求包含 `recipients`、`category`、`title`、`body`，可选 `actionUrl` 与 `requestKey`。

设计见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

# 应用间 OAuth2 客户端

共享 jar，使用 Spring Security 标准 `AuthorizedClientServiceOAuth2AuthorizedClientManager`，支持请求线程与后台任务。业务调用端只依赖 `ServiceAccessTokens`，不接触密码、缓存或 OAuth 响应解析。

| 能力 | 契约 |
| --- | --- |
| 调用入口 | `token(service, audience)`，仅接受显式注册的调用方与目标 audience 组合 |
| 取令牌 | 标准 client_credentials + client_secret_basic，显式申请配置的 scope，不接受任意调用方或 audience |
| 缓存 | 每个注册独立保存；到期前 30 秒重新获取，同一注册串行续取 |
| 超时 | 连接 2 秒、读取 5 秒；配置上限 30 秒，不跟随重定向 |
| 失败 | 隐藏响应正文/凭证；不能获取令牌时终止当前调用，无旧文件回退，无自动重试业务写操作 |
| 信任 | 接收方通过 JWKS 验证 JWT 并检查权限，客户端缓存不代替资源端验签 |

`loadup.security.service-auth` 包含 token-uri、client-secret、registrations（每条配置显式 scopes）；默认只允许 HTTPS，开发 HTTP 必须显式开启 allow-insecure-http。注册项的身份字段可省略：`service-id` 默认取 `spring.application.name`，`audience` 默认注册键，`client-id` 默认 `<service-id>.default.<audience>`；仅在特殊环境显式覆盖。Pay 配置见 OAuth server 和调用方 application.yml。

默认 client-id 的 `.default.` 是固定命名段，不参与租户选择；已有数据库 client-id 保持不变。

缓存键不使用当前登录用户或租户上下文；每条注册使用独立的缓存项和续取锁。用户/商户的委托 token 仍由原有 Actor 路径处理。

已补缓存、分区、提前续取、授权失败及 HTTP 限制的测试源码；未运行测试。

Spring 装配使用 `loadup-components-runtime` 提供的应用 JSON mapper / Clock；消费者通过参数注入，不创建默认兜底实例。独立测试需显式提供所需 Bean。

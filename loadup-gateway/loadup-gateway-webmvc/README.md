# LoadUp Gateway WebMVC Engine

基于 **Spring Cloud Gateway Server MVC** 的嵌入式网关引擎：路由编译为 `RouterFunction`，
与 `@RestController` 共存；安全层为标准 Spring Security **OAuth2 资源服务器**（Nimbus）。

## 安全配置

```yaml
loadup:
  gateway:
    security:
      enabled: true
      # 三选一（优先级从高到低）：
      jwk-set-uri: https://sso.example.com/realms/loadup/protocol/openid-connect/certs
      issuer-uri: https://sso.example.com/realms/loadup
      secret: loadup-gateway-secret-key-must-be-long-enough-32bytes   # HS256（默认）
      app-secrets:
        my-app: my-signing-secret     # signature 策略的 appId → secret
```

## 路由安全字段

```yaml
routes:
  - id: admin-api
    path: /api/admin/**
    securityCode: default        # OFF | default | signature | internal
    authorize: hasRole('ADMIN')  # 完整 SpEL，或权限列表简写：user:list,user:delete
```

- `authorize` 缺省 = 认证通过即放行；简写（无括号/空白）编译为 `hasAnyAuthority(...)`。
- 401（未认证/坏 token，`SECURITY`）与 403（权限不足，`AUTHORIZATION`）均为统一 JSON。
- `signature` 只验签（`X-App-Id / X-Timestamp / X-Nonce / X-Signature`，时间窗 300s），
  复用 `loadup-components-signature`，不产生用户身份。

## 资源服务器扩展点

实现 `ResourceServerBinder`（默认 `nimbus`）可替换 JWT 校验后端（如 Sa-Token），并配置
`loadup.gateway.security.enabled=true` 让网关使用你的 decoder。

## 认证链共存

网关安全链固定 `@Order(SecurityFilterProperties.DEFAULT_FILTER_ORDER)`；与
`loadup-components-authserver`（SAS）同进程时，SAS 的 `/oauth2/**` 链 order 更低且带
matcher，二者自动共存。应用自定义安全链时可用 `loadup.gateway.security.enabled=false`
关闭网关默认链。

## 能力矩阵

| 能力 | 支持 |
|------|------|
| Bearer JWT 校验（HS256 / JWKS / issuer discovery） | ✓ |
| claims → `LoadUpUser` + authorities（roles/permissions） | ✓ |
| 路由级 `authorize`（SpEL / 权限简写） | ✓ |
| 401 / 403 统一 JSON | ✓ |
| 签名策略（复用 signature 组件） | ✓ |
| 内网白名单 `internal` | ✓ |
| Sa-Token 等其他资源服务器 | 扩展点（`ResourceServerBinder`） |

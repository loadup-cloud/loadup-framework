# UPMS

用户、角色、权限和部门业务模块。UPMS 校验凭证，提供角色继承的 RBAC 授权及基于资源所属人、部门的数据范围判定；令牌签发由 AuthServer 完成。

账号安全自助接口由 `loadup-modules-upms-web` 提供：`GET /api/account/security/overview` 查看本人状态，`GET /api/account/security/logins` 查看本人登录记录，`POST /api/account/security/password` 使用旧密码修改本人密码。用户 ID 从认证主体取得，不接受请求指定其他账号。新密码至少 8 字符、最多 72 个 UTF-8 字节且必须不同于旧密码。管理员的锁定与解锁接口仍在 `/api/upms/user/**`。

登录失败达到配置阈值后临时锁定；到期后的下一次登录会自动解锁。管理员手动锁定不会自动解锁。HTTP 登录记录使用服务端观察到的 IP，不信任客户端传入的转发头。当前 JWT 为无状态令牌，修改密码或锁定账号不会立即撤销已签发令牌；已有令牌可用至其到期，后续撤销机制见 Roadmap。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-modules-upms-app</artifactId>
</dependency>
```

前后端分离的用户名密码登录需引入 `loadup-modules-upms-authserver` 与 `loadup-components-authserver-binder-sas`，通过 `POST /api/auth/login` 获取 JWT。可设置 `loadup.security.auth-server.protocol-endpoints-enabled: false` 关闭 OAuth2 协议端点。

## 配置

```yaml
loadup:
  upms:
    security:
      login:
        enable-failure-tracking: true
        max-fail-attempts: 5
        lock-duration: 30
```

`AuthenticationService.login` 是内部身份校验接口；HTTP 登录由 authserver 适配器完成。需要预置用户注册与 RBAC HTTP 接口时，再引入 `loadup-modules-upms-web`；也可以直接在应用 Controller 中调用 UPMS 服务。

业务代码注入 `AccessCheckService`，调用 `check(new AccessCheckCommand(userId, permissionCode, ownerUserId, departmentId))`。结果默认拒绝；仅当有效角色授予该权限，且该角色的数据范围匹配资源属性时放行。`@PreAuthorize` 可校验静态权限，资源级 ABAC 判定须显式调用此 API。

| 能力 | 所属模块 |
|------|----------|
| 用户凭证校验与登录策略 | `loadup-modules-upms-app` |
| 角色与权限数据 | `loadup-modules-upms-domain`、`-infrastructure` |
| RBAC + 资源属性判定 API | `loadup-modules-upms-client`、`-app` |
| 可选 HTTP Controller | `loadup-modules-upms-web` |
| SAS 用户认证适配 | `loadup-modules-upms-authserver` |
| 令牌签发与刷新 | `loadup-components-authserver-binder-sas` |
| Bearer 验签 | `loadup-components-resource-server` |
| 方法级授权 | `loadup-components-authorization` |

# UPMS

用户、角色、权限和部门业务模块。UPMS 校验用户凭证、记录登录状态并提供 RBAC 数据；令牌签发由 AuthServer 完成。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-modules-upms-app</artifactId>
</dependency>
```

使用内嵌 SAS 授权码登录时，再引入 `loadup-modules-upms-authserver` 与 `loadup-components-authserver-binder-sas`。后者管理 OAuth2 客户端、访问令牌和刷新令牌。

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

`AuthenticationService.login` 是内部身份校验接口，不作为 HTTP 登录或令牌签发接口。需要预置用户注册与 RBAC HTTP 接口时，再引入 `loadup-modules-upms-web`；也可以直接在应用 Controller 中调用 UPMS 服务。核心业务模块不依赖 Gateway。

| 能力 | 所属模块 |
|------|----------|
| 用户凭证校验与登录策略 | `loadup-modules-upms-app` |
| 角色与权限数据 | `loadup-modules-upms-domain`、`-infrastructure` |
| 可选 HTTP Controller | `loadup-modules-upms-web` |
| SAS 用户认证适配 | `loadup-modules-upms-authserver` |
| 令牌签发与刷新 | `loadup-components-authserver-binder-sas` |
| Bearer 验签 | `loadup-components-resource-server` |
| 方法级授权 | `loadup-components-authorization` |

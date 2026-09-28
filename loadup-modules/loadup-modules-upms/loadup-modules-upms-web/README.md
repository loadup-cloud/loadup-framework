# UPMS Web 适配

提供用户注册、用户、角色、权限和部门的 Spring MVC Controller。业务服务仍由 `loadup-modules-upms-app` 提供；无需 Gateway。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-modules-upms-web</artifactId>
</dependency>
```

模块自动引入 UPMS app 和方法授权组件。Controller 只声明 `/auth/register`、`/upms/**`，Web 适配器统一添加 `/api` 前缀。所有 UPMS 响应均使用 HTTP 200 JSON，包含 `result` 与 `data`；分页另有 `pageInfo`。异常、无匹配路径和认证拒绝由对应 Web、Resource Server 层转为业务错误码。用户名密码登录由 `loadup-modules-upms-authserver` 提供 `/api/auth/login`；在 `loadup.security.resource-server.permit-all` 中放行登录与注册。

## 配置

本模块没有专属配置键。UPMS 登录策略使用 `loadup.upms.security.login`；公开路径使用 `loadup.security.resource-server.permit-all`。

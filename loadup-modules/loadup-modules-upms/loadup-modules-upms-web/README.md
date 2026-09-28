# UPMS Web 适配

提供用户注册、用户、角色、权限和部门的 Spring MVC Controller。业务服务仍由 `loadup-modules-upms-app` 提供；无需 Gateway。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-modules-upms-web</artifactId>
</dependency>
```

模块自动引入 UPMS app、方法授权组件和 `loadup-components-webmvc`。Controller 只声明 `/auth/register`、`/upms/**`，Web 适配器统一添加 `/api` 前缀。`loadup-components-webmvc` 对所有 `/api/**` Controller 统一处理 `result`、`data` 和错误报文；认证拒绝由 Resource Server 处理。用户名密码登录由 `loadup-modules-upms-authserver` 提供 `/api/auth/login`；在 `loadup.security.resource-server.permit-all` 中放行登录与注册。

## 配置

本模块没有专属配置键。UPMS 登录策略使用 `loadup.upms.security.login`；公开路径使用 `loadup.security.resource-server.permit-all`。

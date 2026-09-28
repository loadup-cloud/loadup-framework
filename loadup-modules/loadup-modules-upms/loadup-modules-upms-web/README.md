# UPMS Web 适配

提供用户注册、用户、角色、权限和部门的 Spring MVC Controller。业务服务仍由 `loadup-modules-upms-app` 提供；无需 Gateway。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-modules-upms-web</artifactId>
</dependency>
```

模块自动引入 UPMS app 和方法授权组件，并注册 `/api/auth/register`、`/api/upms/**` 接口。认证链由应用或 `loadup-components-resource-server` 配置；仅 `/api/auth/register` 应按需公开，其余接口默认需要认证。

## 配置

本模块没有专属配置键。UPMS 登录策略使用 `loadup.upms.security.login`；公开路径使用 `loadup.security.resource-server.permit-all`。

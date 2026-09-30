# UPMS Web 适配

提供用户注册、用户、角色、权限和部门的 Spring MVC Controller。业务服务仍由 `loadup-modules-upms-app` 提供。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-modules-upms-web</artifactId>
</dependency>
```

模块自动引入 UPMS app、方法授权组件和 `loadup-components-webmvc`。Controller 只声明 `/auth/register`、`/upms/**`，Web 适配器统一添加 `/api` 前缀。`loadup-components-webmvc` 对所有 `/api/**` Controller 统一处理 `result`、`data` 和错误报文；认证拒绝由 Resource Server 处理。用户名密码登录由 `loadup-modules-upms-authserver` 提供 `/api/auth/login`；在 `loadup.security.resource-server.permit-all` 中放行登录与注册。

## HTTP 接口

接口统一使用 POST，请求体为 JSON。路径如下：

| 能力 | 路径与操作 |
|---|---|
| 用户注册 | `/api/auth/register` |
| 用户 | `/api/upms/user/{create,update,delete,detail,list,change-password,lock,unlock}` |
| 角色 | `/api/upms/role/{create,update,delete,detail,list,tree,assign-to-user,remove-from-user,assign-permissions}` |
| 权限 | `/api/upms/permission/{create,update,delete,detail,tree,user-menu}` |
| 部门 | `/api/upms/department/{create,update,delete,detail,tree,move}` |

引入可选的 `loadup-components-springdoc` 后，在 `/scalar` 浏览这些 Controller 的 OpenAPI 文档，或从 `/v3/api-docs` 获取 JSON。接口返回的 `result`、`data` 以实际 HTTP 报文为准；SpringDoc 默认从 Controller 返回类型生成模型。

各 Controller 使用 `@Tag` 分组、`@Operation` 说明操作；公开的注册接口覆盖全局 Bearer 要求。模块只依赖 Swagger 注解，页面仍由集成方按需引入。

## 配置

本模块没有专属配置键。UPMS 登录策略使用 `loadup.upms.security.login`；公开路径使用 `loadup.security.resource-server.permit-all`。

## 接入步骤

接入 Spring MVC 时引入 web 与所需 app/infra；需要登录签发时增加 upms-authserver。

## 自动装配

- [`UpmsWebAutoConfiguration`](src/main/java/io/github/loadup/modules/upms/web/UpmsWebAutoConfiguration.java)

设计边界与装配路径见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

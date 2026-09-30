# LoadUp UPMS Authorization Server Adapter

UPMS 与授权服务器之间的可选认证适配模块。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-upms-authserver</artifactId>
</dependency>
```

## 登录接口

引入本模块和 `loadup-components-authserver-binder-sas` 后，前端调用 `POST /api/auth/login`，以 JSON 提交 `{"username":"admin","password":"..."}`。成功响应的 `data` 包含 `accessToken`、`tokenType`、`expiresIn`；后续受保护请求携带 `Authorization: Bearer <accessToken>`。失败响应同样使用 `result`、`data` 报文。设置 `loadup.security.auth-server.protocol-endpoints-enabled: false` 可关闭不需要的 OAuth2 协议端点。

引入可选的 `loadup-components-springdoc` 后，可在 `/scalar` 查阅登录接口。Controller 的 `@Tag`、`@Operation` 和字段说明会显示在页面中；登录接口通过 `@SecurityRequirements` 声明公开访问。该接口还需在 `loadup.security.resource-server.permit-all` 中放行；实际访问规则以 Spring Security 配置为准。

## 自动装配

- [`UpmsAuthServerAutoConfiguration`](src/main/java/io/github/loadup/modules/upms/authserver/UpmsAuthServerAutoConfiguration.java)

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

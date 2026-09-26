# Gateway JWT Security

可选 Spring Security OAuth2 资源服务器。仅在引入本模块并设置 `loadup.gateway.security.enabled=true` 时注册匹配 `/api/**` 的安全链；应用也可自行提供 `SecurityFilterChain`。

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-gateway-security-jwt</artifactId>
</dependency>
```

支持 `loadup.gateway.security.secret`、`issuer-uri` 或 `jwk-set-uri`；路由的 `access.type` 决定是否要求身份及权限。入口 HMAC 签名由 gateway-webmvc 的 `access.signature` 独立处理。

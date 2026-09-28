# LoadUp Resource Server

通用 JWT 资源服务器组件。Controller 应用和嵌入式 Gateway 共用验签、claims 转换与 Spring Security 身份；不依赖 Gateway，也不签发令牌。

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-resource-server</artifactId>
</dependency>
```

```yaml
loadup:
  security:
    resource-server:
      enabled: true
      issuer-uri: https://identity.example.com
      jwk-set-uri: https://identity.example.com/oauth2/jwks
      audience: loadup-api
      permit-all: [/api/auth/register, /api/auth/login]
```

启用时默认注册 `/api/**` 安全链：只有 `permit-all` 路径允许匿名，其余请求需要有效 JWT。认证失败和权限不足返回 HTTP 200 JSON，报文有 `result`、`data: null`，由 `result.code` 区分错误。更改公开路径名单需要刷新应用配置。要自行定义安全链，设置 `default-security-filter-chain: false`。组件创建的 `JwtDecoder` 校验签名，安全链校验 issuer、过期时间、audience，默认还要求 `token_use=access`；应用提供自己的 `JwtDecoder` 时仍需保证签名可信。关闭本地 AuthServer 协议端点时，可省略 `jwk-set-uri`，由本地公钥解码器验签。外部 IdP 未提供 `token_use` 时可设置 `access-token-use-claim: ""`，并确保其刷新令牌不能作为同 audience 的访问令牌。权限转换为 Spring Security authorities，业务方法可用 `@PreAuthorize`。

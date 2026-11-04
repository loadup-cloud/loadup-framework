# LoadUp Resource Server

通用 JWT 资源服务器组件。Spring MVC Controller 应用使用它完成验签、claims 转换与 Spring Security 身份装配；本组件不签发令牌。

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

启用时默认注册 `/api/**` 安全链：只有 `permit-all` 路径允许匿名，其余请求需要有效 JWT。认证失败和权限不足返回 HTTP 200
JSON，报文有 `result`、`data: null`，由 `result.code` 区分错误。更改公开路径名单需要刷新应用配置。要自行定义安全链，设置
`default-security-filter-chain: false`。组件创建的 `JwtDecoder` 校验签名，安全链校验 issuer、过期时间、audience，默认还要求
`token_use=access`；应用提供自己的 `JwtDecoder` 时仍需保证签名可信。省略 `jwk-set-uri` 时，解码器通过 `issuer-uri` 元数据发现
JWK 地址；须保证发现端点可访问。若没有发现端点，需显式配置 `jwk-set-uri`、`public-key-location` 或提供自定义 `JwtDecoder`。外部 IdP 未提供
`token_use` 时可设置 `access-token-use-claim: ""`，并确保其刷新令牌不能作为同 audience 的访问令牌。权限转换为 Spring
Security authorities，业务方法可用 `@PreAuthorize`。

## 本地公钥验签

固定 RSA 公钥可直接使用 Spring Security 的 `NimbusJwtDecoder.withPublicKey` 验签，无需部署 JWT/JWKS 服务：

```yaml
loadup:
  security:
    resource-server:
      enabled: true
      issuer-uri: https://identity.example.com/dev
      public-key-location: file:/var/run/loadup/secrets/jwt-public.pem
      audience: payment
```

公钥采用 X.509 PEM（BEGIN PUBLIC KEY），支持 Spring Resource 的 file:/classpath: 位置。
public-key-location 与 jwk-set-uri 不能同时设置；公钥不可读或格式错误时启动失败，不回退到网络发现。
此模式 issuer-uri 只用于 claims 比较，不访问该地址；签名、有效期、issuer、audience 和 token_use 校验均保留。
公钥在启动时读取，换钥需要滚动应用；需要自动多密钥轮换时使用 JWKS 模式。
签名私钥不分发到应用，不同服务继续使用各自身份、受众和权限的访问令牌。

| 验签方式 | 配置 | 外部依赖 |
| --- | --- | --- |
| 静态 RSA 公钥 | public-key-location | 无网络发现；本地公钥文件 |
| 显式 JWKS | jwk-set-uri | 访问 JWKS 地址 |
| Issuer 发现 | 仅 issuer-uri | 访问元数据和 JWKS |

参考 [Spring Security：JWT Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)。

JWT 认证结果使用 JwtAuthenticationToken 派生类型保留 LoadUpUser 主体；经过 ProviderManager 默认凭证清理后，业务适配器仍可读取已验签 JWT。此行为已补回归源码，未运行测试。

Spring 装配使用 `loadup-components-runtime` 提供的应用 JSON mapper / Clock；消费者通过参数注入，不创建默认兜底实例。独立测试需显式提供所需 Bean。

# AuthServer

内嵌 Spring Authorization Server，统一负责访问令牌和刷新令牌的签发。UPMS 仅校验用户凭证。外部身份提供商签发的令牌直接由 Resource Server 验证，不需要 AuthServer binder。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-authserver-binder-sas</artifactId>
</dependency>
```

需要 UPMS 账号登录时，额外引入 `loadup-modules-upms-authserver`。该适配器把 UPMS 密码校验接入 SAS 的授权码登录流程。

## 配置

```yaml
loadup:
  components:
    authserver:
      issuer: https://auth.example.com
      audience: loadup-api
      jwk:
        rsa-private-key-base64: ${AUTH_RSA_PRIVATE_KEY_BASE64}
      clients:
        - client-id: web-app
          client-secret: ${AUTH_CLIENT_SECRET}
          grant-types: [authorization_code, refresh_token]
          redirect-uris: [https://app.example.com/login/oauth2/code/loadup]
          scopes: [openid, profile]
```

开发环境可省略 RSA 私钥，此时会生成临时密钥，重启后旧令牌失效。客户端密钥以 BCrypt 存储于内存注册表；生产环境应从安全配置来源提供密钥，并为多实例部署提供持久化 `RegisteredClientRepository`、`OAuth2AuthorizationService`。授权码登录由 `/oauth2/authorize` 和 `/login` 完成；令牌和 JWK 分别由 `/oauth2/token`、`/oauth2/jwks` 提供。

访问令牌携带 `token_use=access`、`aud`、`username`、`roles`、`permissions`。UPMS 登录的 `sub` 为用户 ID。Resource Server 独立校验 issuer、audience、签名和 token_use。

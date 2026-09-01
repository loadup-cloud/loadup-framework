# LoadUp Components :: AuthServer

OAuth2 授权服务器组件（Mode A 单后端选择）：**签发**带 claims 的 JWT。签发与校验解耦——
gateway 只做资源服务器校验，不依赖本组件。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-authserver-binder-sas</artifactId>
</dependency>
```

Keycloak issuer-only 场景换成 `loadup-components-authserver-binder-keycloak`。

## 配置

```yaml
loadup:
  components:
    authserver:
      binder-type: sas            # sas（默认） | keycloak
      issuer: http://localhost:8080
      clients:
        - client-id: loadup-app
          client-secret: change-me
          grant-types: [client_credentials, refresh_token]
          scopes: [openid]
      jwk:
        rsa-private-key: <PKCS#8 Base64，可选；缺省启动时生成临时密钥>
```

Keycloak 模式：

```yaml
loadup:
  components:
    authserver:
      binder-type: keycloak
      issuer: https://sso.example.com/realms/loadup
      jwk-set-uri: https://sso.example.com/realms/loadup/protocol/openid-connect/certs
```

## 行为

- SAS binder 启动即暴露标准 OAuth2 端点（`/oauth2/authorize`、`/oauth2/token`、`/oauth2/jwks`）。
- `LoadUpJwtTokenCustomizer`（标准 `OAuth2TokenCustomizer`）把 roles / permissions / username
  写入 JWT，与 gateway claims 契约一致（sub=userId / roles / permissions 自包含）。
- Keycloak binder 只做 issuer 对接，装配标准 `NimbusJwtDecoder`，不做 admin API。
- gateway 侧通过 `ResourceServerBinder` SPI 消费：`jwk-set-uri` > `issuer-uri` > `secret`。

## 能力矩阵

| 能力 | SAS binder | Keycloak binder |
|------|-----------|-----------------|
| `/oauth2/token` 签发 | ✓ | ✗（外部 IdP） |
| 客户端注册（yml） | ✓ | ✗ |
| roles/permissions claims | ✓ | 由 IdP 配置提供 |
| issuer / jwk-set 校验装配 | ✓（自验） | ✓ |
| admin API / 客户端管理 | ✗ | ✗（只作为 issuer） |

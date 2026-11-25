# LoadUp OAuth Server

统一 OAuth2 授权服务器、JWT 资源服务器与可选的标准 JDBC 存储集成。

| 能力 | 开关或接口 | 约束 |
| --- | --- | --- |
| KMS 签名与公钥发布 | auth-server.enabled | KMS 保存私钥，组件只发布公钥 |
| OAuth2/OIDC 协议 | auth-server.protocol-endpoints-enabled | 应用选择通用协议或专用链 |
| 资源端验证 | resource-server.enabled | 签名、时效、issuer、audience、token_use |
| 客户端数据库管理 | RegisteredClientRepository | 不执行启动配置同步 |
| 授权及同意持久化 | oauth.persistence.enabled | Spring 标准 JDBC 实现 |
| 业务 claims | OAuth2TokenCustomizer | 应用负责身份与授权语义 |
| 客户端生命周期 | OAuthClientManager | 标准注册字段、策略校验、停用与凭证轮换 |

引入 `loadup-components-oauth-server-core` 使用实现；只需身份契约的消费者引入 `loadup-components-oauth-server-api`。
开关完整前缀为 `loadup.security`。资源服务只需要：

```yaml
loadup:
  security:
    resource-server:
      enabled: true
      issuer-uri: https://issuer.example
      audience: orders
```

独立授权服务还需启用 `auth-server.enabled`，配置 issuer、audience、KMS signing key；启用 JDBC 时显式引入 JDBC/Flyway/数据库驱动并加入 `classpath:db/loadup-oauth`。
协议链不读取 YAML 客户端目录。管理方通过 `OAuthClientManager` 创建和更新注册；它使用 Spring `RegisteredClient` 作为授权字段输入，并负责生成和散列客户端凭证。不要直接将此管理对象暴露为未经保护的 HTTP API；嵌入它的管理边界必须认证并授权调用者。

### 签名密钥轮换

配置 `loadup.security.auth-server.signing.active-key.name` 即可按 alias 签发 JWT。省略 `version` 或设为 `0` 时，签发端每次读取当前公钥，JWT 的 `kid` 为 `alias.公钥指纹`。JWKS 通过 KMS binder 查询并发布该 alias 的当前及历史公钥。显式正整数版本沿用 `alias-vN`，升级前签发的此类 JWT 可通过 `signing.verification-keys` 保留原来的公钥引用。

每个 binder 从后端读取仍受信任的历史公钥，OAuth Server 可以在重启后重新发布 JWKS。保留旧密钥的验签权限或公钥至少到旧 JWT 到期。

详见 [架构](ARCHITECTURE.md)、[API](loadup-components-oauth-server-api/README.md)、[Core](loadup-components-oauth-server-core/README.md)。

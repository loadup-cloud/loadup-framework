# OAuth Server 架构

## 发布边界

组件只有 `loadup-components-oauth-server-api` 和 `loadup-components-oauth-server-core` 两个发布模块。
API 保留轻量身份接口 `LoadUpSubject` 与可选的客户端元数据键 `OAuthClientSettings`，不创建 Bean，不依赖 SAS 或 KMS。
Core 包含授权端签发、资源端验证、标准 JDBC 持久化、配置绑定及全部测试。
`authserver` / `resourceserver` Java 包表达不同协议角色；模块合并不合并安全链或令牌信任范围。

## 独立装配

- `loadup.security.auth-server.enabled=true`：启用 KMS 签发、JWKS/settings；缺省关闭。
- `loadup.security.auth-server.protocol-endpoints-enabled=true`：在签发启用基础上提供通用 OAuth2/OIDC 协议链；关闭可由应用提供专用协议链，或只使用 encoder。
- `loadup.security.resource-server.enabled=true`：只启用资源端 JWT 校验；不要求 KMS、客户端库或授权数据库。
- `loadup.security.oauth.persistence.enabled=true`：存在 JDBC 时提供 Spring 标准的客户端、授权、同意记录存储；缺省关闭。

Core 直接依赖 Spring Security OAuth2 库，不引入 Boot Authorization Server starter，避免仅引入组件就触发 Boot 默认授权服务装配。
JDBC 和 Boot SQL 依赖可选；应用主动引入 JDBC starter。协议签发与资源端同时启用时，资源端 decoder 负责 issuer/audience/token_use；本地签发 decoder 不覆盖它。同一应用同时签发和验签、且 issuer 相同且未显式指定外部公钥/JWKS 时，资源端直接使用本地公钥，但仍完整校验 issuer、audience、时效和用途。

## 标准接口与数据所有权

客户端使用 `RegisteredClientRepository` / `RegisteredClient` 管理；授权和同意使用 `OAuth2AuthorizationService` / `OAuth2AuthorizationConsentService`。
实现分别为 Spring 的 JDBC 实现，允许通过标准 Bean 替换。不另造协议 CRUD、注册目录或同步 SPI。
`OAuthClientSettings.ENABLED` 与 `AUDIENCE` 是可选元数据键，具体授权策略负责解释；标准存储本身不推导业务权限或自动禁用记录。
业务身份和 claims 通过标准 `OAuth2TokenCustomizer<JwtEncodingContext>` 定制。框架不强制租户字段，也不依赖商户、Pay 权限或用户目录。

数据库是客户端授权的唯一持久化来源。应用启动不导入配置、不覆盖客户端、不轮换凭证、不清理目录，因此没有启动注册同步锁。
密钥散列、grant、scope、redirect URI、有效期等使用标准模型；管理方负责散列明文密钥和审批授权。
修改数据库授权只影响后续签发；已签发 JWT 的本地验证不提供即时撤销。

## 数据库迁移

Core 提供 PostgreSQL 协议表迁移位置 `classpath:db/loadup-oauth`，包含客户端、授权和同意表。
迁移资源放在默认 db/migration 扫描目录之外，由应用显式纳入 Flyway locations，不自动执行初始化 SQL。原始注册/授权迁移内容及版本描述保持不变，以保留既有 Flyway 校验值。
Pay 特定的旧 settings 清理由 Pay 自身迁移负责。

## 测试

全部单元和集成测试位于 core/src/test，旧独立测试模块与资源端测试均迁入此处。
已有的 KMS 边界测试及测试说明保留。数据库集成验证、签发/验签验证需在本地运行，静态检查不能代替它们。

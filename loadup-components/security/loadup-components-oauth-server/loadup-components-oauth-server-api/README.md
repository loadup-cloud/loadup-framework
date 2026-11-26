# OAuth Server API 接入

需要共享主体接口、OAuth 客户端管理 DTO 或路径契约的模块可在导入 Framework BOM 后引入此工件。

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-oauth-server-api</artifactId>
</dependency>
```

业务身份实现可用 `LoadUpSubject.subjectId()` 暴露统一主体标识。客户端管理接入方按需复用 `OAuthClientCreateRequest`、`OAuthClientDetails` 和 `OAuthClientCredentialResponse` 等契约。运行时启用签发、验签或 JDBC 存储时，按 [Core 接入说明](../loadup-components-oauth-server-core/README.md) 配置宿主。

[API 架构](ARCHITECTURE.md)

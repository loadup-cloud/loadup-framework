# OAuth Server API

| 契约 | 用途 |
| --- | --- |
| LoadUpSubject | 稳定的主体 ID，与显示用户名区分 |
| OAuthClientSettings | enabled、audience 可选客户端元数据键 |
| OAuthClientAuthorization / CreateRequest / ExpiryRequest | 客户端管理请求及字段校验 |
| OAuthClientDetails / CredentialResponse | 不含散列的管理详情；创建和轮换返回的凭证 |
| OAuthClientManagementPaths | 内部管理 HTTP 路径契约 |

API 使用 Jakarta Validation 和公共 JSON 脱敏能力，无自动配置、KMS、JDBC 或授权服务器实现依赖。标准 OAuth 存储及令牌接口直接使用 Spring 类型，不另作包装。
配置绑定和默认 claim 实现属于 core，参见 [架构](../ARCHITECTURE.md)。

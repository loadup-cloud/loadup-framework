# OAuth Server API

| 契约 | 用途 |
| --- | --- |
| LoadUpSubject | 稳定的主体 ID，与显示用户名区分 |
| OAuthClientSettings | enabled、audience 可选客户端元数据键 |

API 无自动配置、KMS、JDBC 或授权服务器实现依赖。标准 OAuth 存储及令牌接口直接使用 Spring 类型，不另作包装。
配置绑定和默认 claim 实现属于 core，参见 [架构](../ARCHITECTURE.md)。

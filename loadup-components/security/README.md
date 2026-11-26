# 安全与身份组件接入

消费应用导入 [Framework BOM](../../loadup-framework-bom/README.md)，再按需求选择下表中的工件。此目录的聚合 POM 用于组织 Maven 子模块。

| 组件 | 适用场景 | 接入要点 |
| --- | --- | --- |
| [OAuth2 与 JWT](loadup-components-oauth-server/README.md) | 签发、资源端验证、可选 JDBC 持久化 | 按服务角色启用签发、协议端点或资源端校验。 |
| [方法授权](loadup-components-authorization/README.md) | 业务方法权限判断 | 启用应用的认证链，再配置授权规则。 |
| [服务身份](loadup-components-service-auth/README.md) | 内部调用的服务令牌与身份校验 | 为每个应用配置对应的客户端身份与 scope。 |
| [密钥与机密管理](loadup-components-kms/README.md) | 密钥操作、别名以及后端支持的机密操作 | 选择 binder，并在宿主配置后端与密钥。 |
| [报文签名](loadup-components-signature/README.md) | 对接方报文签名和验签 | 按双方约定配置算法、签名字段和时间窗。 |
| [验证码](loadup-components-captcha/README.md) | 验证码生成与校验 | 选择 NanoCaptcha 或 Tianai binder。 |

[分类架构](ARCHITECTURE.md) · [全部组件](../README.md)

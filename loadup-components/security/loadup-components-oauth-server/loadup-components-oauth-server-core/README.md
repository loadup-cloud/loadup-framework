# OAuth Server Core

| 能力 | 实现 |
| --- | --- |
| 签名 | KmsTemplate + JwtEncoder |
| 授权端 | Spring Security OAuth2 Authorization Server |
| 资源端 | Spring Security JWT Resource Server |
| 客户端、授权、同意存储 | Spring JDBC 标准接口及实现 |
| 回归测试 | src/test，含原 SAS、资源端及 KMS 边界用例 |

各能力独立启用，默认不创建授权端或 JDBC 存储。配置、依赖与数据库归属见[组件架构](../ARCHITECTURE.md)。
原独立测试模块说明保存在 [TESTING.md](TESTING.md)，其中历史运行方式不再适用；统一在本模块运行测试。

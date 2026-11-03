# LoadUp Components AuthServer Test

验证 AuthServer API 与 SAS/KMS 装配的测试模块，不作为生产依赖。

| 范围 | 用例 |
| --- | --- |
| 配置绑定 | issuer、audience、活动密钥与保留公钥、客户端配置 |
| JWT 编码 | Spring 类型化 RS256 头转换、标准 JSON 头、时间声明、实际 RSA 签名与验签 |
| 持久化与轮换 | 相同 PKCS12 跨上下文重启；新密钥签发、旧公钥保留与退役 |
| 失败边界 | 缺少 KMS/alias/文件/密码、错误密码、错误算法/密钥版本、KMS 失败及错误返回身份 |
| 协议集成 | OAuth2 token、浏览器授权重定向、仅含公钥的 JWKS |

配置与协议测试使用测试 JDK 的 keytool，在临时目录生成真实 PKCS12。集成测试通过 DynamicPropertySource 注入文件位置和合成密码，明确启用 kms-local 并指定密钥 alias；不依赖本机或部署目录的密钥，也不恢复临时密钥生成的生产回退逻辑。

合成凭证只用于测试。TestKeyStores 仅属于测试源码，密钥文件随临时目录清理；keytool 不输出私钥、密码或生成日志。测试失败不能通过降低签名、权限或受众断言处理。

本批次只完成源码修复和 IDE 静态检查，未执行单元/集成测试。运行使用 mvnd，遵循 clean 和指定模块范围；先安装匹配组件。`*IntegrationTest` 由 failsafe 管理，普通 `test` 阶段不运行。

示例（由本地构建者选择执行）：

```bash
mvnd clean verify -pl loadup-components/security/loadup-components-authserver/loadup-components-authserver-test -am \
  -Dtest=SasSigningAndClientTest,KmsSigningFailureTest,SasAuthServerAutoConfigurationTest,LoadUpAuthServerPropertiesTest,LoadUpJwtTokenCustomizerTest,JwtClaimsBoundaryTest \
  -Dit.test=SasAuthServerIntegrationTest \
  -Dsurefire.failIfNoSpecifiedTests=false -Dfailsafe.failIfNoSpecifiedTests=false \
  -Dskip.unit.tests=false -Dskip.integration.tests=false
```

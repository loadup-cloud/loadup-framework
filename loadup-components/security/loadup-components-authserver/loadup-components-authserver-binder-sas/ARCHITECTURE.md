# AuthServer SAS binder 架构

## 依赖与职责

依赖 authserver-api、kms-api、Spring Security Authorization Server 和 Commons Lang。
不依赖 kms-local/OpenBao 实现，集成应用按需选择 KMS binder。应用仍使用标准 JwtEncoder、JwtDecoder 和 OAuth2TokenCustomizer。

## 调用路径

```text
OAuth2 / UPMS 签发
  → JwtEncoder (KmsJwtEncoder)
    → Nimbus SignedJWT + JWSSigner
      → KmsTemplate.sign(明确名称/版本, RSA_SHA256_PKCS1, JOSE签名输入)
```

KmsSigningKeys 在启动时校验 active-key 和 verification-keys，仅通过 KMS 获取元数据与公钥。
公钥构建 RS256 JWK，kid 为名称-v版本；ImmutableJWKSet 只持有公钥，标准 SAS JWKS 端点与本地 JwtDecoder 共用该集合。
KmsJwtEncoder 将时间 claims 转换为 JOSE NumericDate，Nimbus 负责标准 JWT 序列化；不拼装私有协议，不读取私钥。
签名算法只接受 RS256；请求中的 kid 必须匹配活动密钥，拒绝非标准 critical/b64 头和不匹配的 KMS 返回身份。

## 装配

SasAuthServerAutoConfiguration 排在两个 KMS binder 自动配置之后、Boot 授权服务器和资源服务器默认配置之前。
KMS 或配置缺失时启动失败；不存在 Base64 私钥加载和临时密钥生成分支。
协议端点通过 protocol-endpoints-enabled 选择；关闭端点仍可使用 KMS 签发和公钥解码。
密钥配置/公钥集合在启动时固定，轮换按先发布公钥、再切签名、最后退出旧公钥的顺序滚动。

配置示例、版本迁移与未执行验证见 [AuthServer README](../README.md)。

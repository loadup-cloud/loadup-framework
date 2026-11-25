# KMS Aliyun binder 架构

`AliyunKmsAutoConfiguration` 通过 `loadup.kms.enabled` 和 `loadup.kms.binder=aliyun` 装配 `AliyunKmsTemplate`。应用提供自己的 `KmsTemplate` 时，默认装配退让。默认 SDK 客户端通过 KMS Java SDK V2.0 的 `Config.setCredential` 接收显式配置的凭据客户端。ECS/ECI 实例角色由凭据客户端获取并刷新临时凭据，AK/SK 在创建 SDK 客户端时从部署环境读取。消费者依赖 KMS API。

业务 alias 直接进入 `DescribeKey`、`ListKeyVersions` 和密码操作的 `KeyId` 字段。RSA 的版本来自云端查询，版本结果存在歧义或请求期间 alias 改绑时，binder 拒绝不一致的响应。AES 的实际版本取自 `Encrypt` 响应，允许同一 CMK 在请求期间轮换。

签名与密文携带实际 CMK 和版本标识。验签按调用方指定的可信 alias 查找具有对应标签的已启用密钥，并校验指纹和真实密钥版本。公钥列举使用同一标签范围。解密将密文与相同的加密上下文传给 KMS，检查响应密钥身份后返回明文。请求使用 HTTPS、配置化超时和关闭自动重试的 SDK 调用。

协议参见阿里云 [AsymmetricSign](https://www.alibabacloud.com/help/en/kms/key-management-service/developer-reference/api-kms-2016-01-20-asymmetricsign)、[Encrypt](https://www.alibabacloud.com/help/en/kms/key-management-service/developer-reference/api-kms-2016-01-20-encrypt) 和 [Decrypt](https://www.alibabacloud.com/help/en/kms/key-management-service/developer-reference/api-kms-2016-01-20-decrypt)。

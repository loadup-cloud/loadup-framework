# KMS Local binder

坐标：io.github.loadup-cloud:loadup-components-kms-binder-local，版本由 LoadUp BOM 管理；传递引入 kms-api。
显式设置 loadup.kms.enabled=true，binder 默认为 local。已有 KmsTemplate Bean 时整套 local 装配退让。

## 能力矩阵

| 能力 | 契约 |
| --- | --- |
| 装配 | 应用提供 LocalKmsKeyStore，或显式配置文件加载 |
| 文件加载 | Spring Resource 位置、可配置 KeyStore 类型；默认 PKCS12，只读加载 |
| 密码 | 从指定的 Spring Environment 属性名读取；条目密码默认与仓库密码相同 |
| 加解密 | AES-GCM；保留 local:v1: 格式 |
| 签名验签 | RSA SHA-256 PKCS1/PSS；公钥和元数据读取 |
| 密钥版本 | alias 对应名称，仅版本 1；不支持隐式创建、自动轮换或私钥导出 |

## 文件配置

```yaml
loadup:
  kms:
    enabled: true
    binder: local
    local:
      key-store-location: file:/etc/loadup/keys/authserver.p12
      key-store-type: PKCS12
      key-store-password-env: KMS_KEYSTORE_PASSWORD
      # key-password-env: KMS_KEY_PASSWORD
```

密码值由受控 Secret/环境提供，不放入普通 YAML；key-password-env 未指定时使用仓库密码。
缺少位置、密码或文件，文件损坏或密码错误时启动失败，不创建文件、不生成密钥，也不回写 KeyStore。
应用提供 LocalKmsKeyStore Bean 时不读取上述文件或密码，保留代码装配入口。
密钥的生成、导入和持久化由集成方负责；RSA 私钥条目需证书链，AES 使用 SecretKeyEntry。

AuthServer 可通过 kms-api 复用签名和公钥读取，详见 [AuthServer 接入与轮换](../../loadup-components-oauth-server/README.md)。
详细原生 KeyStore 示例及密文边界见 [KMS 接入](../README.md#local-binder)。

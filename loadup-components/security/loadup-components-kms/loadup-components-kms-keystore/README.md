# KMS KeyStore

`loadup-components-kms-keystore` 提供基于已加载 Java KeyStore 的 `KmsTemplate` 实现。Local 和 Database binder 均通过
Maven 传递引入该模块。

## 能力矩阵

| 能力       | 契约                                                    |
|------------|---------------------------------------------------------|
| 签名与验签 | RSA SHA-256 PKCS#1 v1.5 和 RSA-PSS                      |
| 加解密     | AES-GCM，密文使用 `local:v1:` 格式                      |
| 密钥查询   | KeyStore alias 对应密钥名，支持版本 1、公钥和元数据查询 |
| 密钥加载   | 调用方提供已加载的 KeyStore 和条目密码                  |

接入配置见 [Local binder](../loadup-components-kms-binder-local/README.md)
或 [Database binder](../loadup-components-kms-binder-database/README.md)。

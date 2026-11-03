# KMS Local binder

坐标：io.github.loadup-cloud:loadup-components-kms-binder-local，版本由 LoadUp BOM 管理。

基于 Java KeyStore 的本地实现，传递引入 kms-api。显式设置 loadup.kms.enabled=true，binder 默认为 local。
应用提供 LocalKmsKeyStore Bean，持有已加载的 KeyStore 和条目密码；密钥的加载、生成和持久化由应用负责。
RSA 私钥条目需证书链，AES 使用 SecretKeyEntry。详细示例和约束见 [KMS 接入](../README.md#local-binder)。

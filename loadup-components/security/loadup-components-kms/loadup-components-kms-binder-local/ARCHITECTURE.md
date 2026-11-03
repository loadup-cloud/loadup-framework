# KMS Local binder 架构

LocalKmsAutoConfiguration 在显式启用 KMS 后装配，已有 KmsTemplate 时整套退让；各本地 Bean 仅在选择 local 时创建。
LocalKmsProperties 绑定 loadup.kms.local，默认 PKCS12，通过 ResourceLoader 只读加载既有 KeyStore。
密码从指定的 Spring Environment 属性名获取；临时 char[] 在加载结束时清空，LocalKmsKeyStore 保留自身的防御性副本。
应用自定义 LocalKmsKeyStore 时跳过文件与密码读取。无配置或加载失败时终止装配，不生成临时替代密钥。

LocalKmsTemplate 支持 RSA SHA-256 PKCS1/PSS、AES-GCM、公钥和元数据；KeyStore alias 对应名称，只支持版本 1。
不提供轮换或管理 Bean。local:v1: 密文保留原后端格式；不导出私钥。
AuthServer 仅通过 kms-api 使用本组件，由集成应用选择 local binder；SAS 不依赖 local 的实现类。

统一契约与边界见 [KMS 架构](../ARCHITECTURE.md)。本批次完成源码与静态检查，未执行构建或运行验收。

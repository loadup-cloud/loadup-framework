# KMS KeyStore 架构

`KeyStoreKmsTemplate` 使用 JCA 完成密钥条目查找、RSA 签名验签、AES-GCM 加解密及元数据查询。依赖方向为 Local 或 Database
binder → KeyStore 模块 → KMS API。

两个 binder 分别装配自己的 `KmsTemplate`。Local 从 Spring Resource 或应用提供的 KeyStore Bean 取得密钥，Database 从
PostgreSQL 读取并校验 PKCS#12。共享实现接收已打开的 KeyStore，并持有条目密码的防御性副本。各 binder 管理自己的配置、凭证来源和加载生命周期。

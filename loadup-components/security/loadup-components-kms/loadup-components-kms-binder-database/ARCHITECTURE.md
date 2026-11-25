# KMS Database binder 架构

PostgreSQL 的 `kms_key_file` 表保存受密码保护的 PKCS#12 原始文件、文件 ID、审计时间和停用标记。文件 ID 由部署方分配。主键与单条插入语句保证并发导入同一 ID 时只有一次成功；停用保留记录和 ID。部署通过数据源选择固定 schema。

`DatabaseKmsAutoConfiguration` 在启用 KMS 并选择 database 时加载。已有业务 `KmsTemplate` Bean 时跳过文件和密码读取。默认只创建业务模板。启用管理开关后可注入 `DatabaseKmsKeyFiles`，首次部署也可用独立管理数据源构造该对象。

启动时按 ID 查询有效文件，在内存中用部署提供的密码打开 KeyStore 并校验密钥条目，再交给 [共享 KeyStore 模块](../loadup-components-kms-keystore/ARCHITECTURE.md) 执行密码操作。文件不写入磁盘，数据库不保存密码。读取或导入产生的临时文件字节数组在使用后清零。数据库和 JCA 失败使用 KMS 错误分类，异常不带 SQL 参数或文件内容。

每个应用实例持有启动时的快照。后续密码操作不查询数据库。文件切换和停用的生效时机、历史别名保留要求见 [接入说明](README.md)。

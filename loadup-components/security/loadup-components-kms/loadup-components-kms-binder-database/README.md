# KMS Database binder

引入 BOM 管理的 `io.github.loadup-cloud:loadup-components-kms-binder-database`，并提供 PostgreSQL JDBC 驱动和指向应用 schema 的数据源。应用按文件 ID 读取一份 PKCS#12，业务代码照常注入 `KmsTemplate`。

## 能力矩阵

| 能力 | 行为 |
| --- | --- |
| 文件管理 | `DatabaseKmsKeyFiles.importFile` 校验后保存文件，单个文件最大 4 MiB。文件 ID 在停用后仍保留。 |
| 停用 | `retireFile` 逻辑停用指定文件，后续加载失败。已启动的实例继续使用内存中的密钥。 |
| 业务操作 | RSA SHA-256 PKCS#1/PSS 签名、验签及公钥读取；AES-GCM 加解密。 |
| 密钥引用 | KeyStore 别名对应密钥名称，版本为 1。使用相同密钥时可读取 local binder 生成的 `local:v1:` 密文。 |

## 首次导入

在目标 schema 执行组件提供的 [PostgreSQL 建表脚本](src/main/resources/db/schema/kms_key_file.sql)。准备一份受密码保护的 PKCS#12，RSA 支持 2048、3072 和 4096 位，私钥条目须附证书链。AES 使用 256 位 SecretKeyEntry。文件中的密钥条目共用一个条目密码，推荐使用小写字母、数字、下划线和连字符命名别名。

在有写权限的管理进程中执行以下代码。示例中的 `adminDataSource` 指向目标 schema，两个 `char[]` 密码来自部署的 Secret，两个密码相同时可传入同一数组。

```java
var files = new DatabaseKmsKeyFiles(adminDataSource);
try (var input = Files.newInputStream(Path.of("/run/secrets/payments.p12"))) {
    files.importFile("payments-2026", input, storePassword, keyPassword);
} finally {
    Arrays.fill(storePassword, '\0');
    Arrays.fill(keyPassword, '\0');
}
```

文件 ID 支持 1 至 64 个字母、数字、下划线或连字符。首次导入须在业务应用启动前完成。以相同 ID 再次导入会失败并保留原记录。

## 业务应用配置

```yaml
loadup:
  kms:
    enabled: true
    binder: database
    database:
      file-id: payments-2026
      key-store-password-env: KMS_KEYSTORE_PASSWORD
      # key-password-env: KMS_KEY_PASSWORD
```

运行身份只需对 `kms_key_file` 有 SELECT 权限。密码配置填写 Spring Environment 属性名。条目密码默认使用文件密码。缺少指定文件或密码、文件无法解析、条目密码错误会导致启动失败。

需要在管理应用中注入 `DatabaseKmsKeyFiles` 时，设置 `loadup.kms.management-enabled=true`，并给管理数据源授予 INSERT 和更新停用标记、更新时间的权限。在管理流程中调用 `files.retireFile("payments-2026")`，首次停用返回 true。

## 更换文件

以新 ID 导入文件，更新 `file-id` 并重启所有使用该文件的实例。文件 ID 用于选取 Keystore，业务密文和签名里记录的是密钥别名和版本。同名 RSA 签名密钥轮换时，在新 PKCS#12 中用原 alias 保存新私钥，并用 `<alias>.history.<label>` 的受信证书条目保留旧公钥。应用重新加载新文件后可按公钥指纹验签或发布历史公钥。处理旧密文时仍需保留对应的原加密密钥条目。迁移完成后可停用旧文件。紧急撤销时需同时终止或重启持有旧密钥的实例。

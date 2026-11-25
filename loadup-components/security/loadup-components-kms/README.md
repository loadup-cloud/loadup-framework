# LoadUp Kms

提供 local、database、OpenBao Transit 和 Aliyun 四种 KMS binder，默认选择 local。应用传入密钥引用与待处理字节，由所选后端执行加解密或签名。local 使用应用提供的 KeyStore，database 从 PostgreSQL 加载指定的 PKCS#12；OpenBao 后端的私钥与对称密钥留在远端。适用于商户请求签名、通知签名及敏感字段加密。

## 能力矩阵

| 能力     | 首版契约                                                                              |
|----------|---------------------------------------------------------------------------------------|
| 后端     | API + local/database/OpenBao/Aliyun 独立 binder；OpenBao 复用 LoadUp HTTP                                           |
| OpenBao 密钥生成 | RSA 2048/3072/4096、AES-256-GCM；新建请求关闭导出与明文备份                           |
| 加解密   | 字节输入；密文保留密钥名称、版本及后端包装串                                      |
| 字符串凭据 | 四种 binder 均支持按名称创建、更新和读取。Local 写入密文文件，Database 写入 PostgreSQL，远程 binder 使用原生凭据存储 |
| 签名验签 | SHA-256 + RSA PKCS#1 v1.5 / RSA-PSS；输入为原始字节，组件不做参数排序                 |
| 签名输出 | 原始签名字节及 Base64；保存名称、版本、算法以支持历史验签                             |
| OpenBao 管理操作 | 创建、轮换、最低版本配置、读取公钥、导入公钥或已经按 BYOK 包装的密钥                  |
| OpenBao 凭证 | 环境变量或自定义 Provider；每次请求重新读取；管理身份独立                             |
| 网络     | HTTPS、SSL Bundle、超时、连接池、请求/响应大小限制；无自动重试和重定向                |
| 可观测性 | 复用 HTTP 的计时与 Boot RestClient 观测，固定客户端与操作名                           |
| 暂不提供 | SM2/SM4、证书生命周期、登录续租、批量接口、AAD/派生密钥、导出/备份/删除接口、管理页面 |

KMS 默认关闭。local 需要应用提供 KeyStore，database 需要已导入的密钥文件，OpenBao 和 Aliyun 需要已有的远程服务。OpenBao 的部署、初始化和解封由运维负责。导出关闭是服务端策略，不等同于
HSM 硬件保护。既有密钥的策略由运维检查，创建请求不会重写已有密钥的安全属性。

## Database 接入

引入 `loadup-components-kms-binder-database`，设置 `loadup.kms.enabled=true`、`loadup.kms.binder=database` 和 `loadup.kms.database.file-id`。应用启动时读取指定密钥文件。建表、导入、切换和停用见 [Database binder 接入说明](loadup-components-kms-binder-database/README.md)。

## Aliyun 接入

引入 `loadup-components-kms-binder-aliyun` 并设置 `loadup.kms.binder=aliyun`。使用阿里云 KMS Java SDK V2.0 的区域共享网关，支持 ECS/ECI 实例 RAM Role 或 AK/SK 凭据，业务代码使用 KMS alias，签名和密文携带原生密钥版本。配置与能力矩阵见 [Aliyun binder](loadup-components-kms-binder-aliyun/README.md)。

## OpenBao 接入

先引入 `loadup-framework-bom` BOM，再添加：

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-kms-binder-openbao</artifactId>
</dependency>
```

该组件传递引入 HTTP、RestClient 和 Jackson 3。KMS 使用自己管理的连接池，不需要定义 `loadup.http.clients`；全局 HTTP
客户端可正常共存。

## 配置

```yaml
loadup:
  kms:
    enabled: true
    binder: openbao
    endpoint: https://openbao.internal:8200
    mount: transit
    token-env: OPENBAO_TOKEN
    management-enabled: false
    management-token-env: OPENBAO_MANAGEMENT_TOKEN
    connect-timeout: 3s
    read-timeout: 10s
    max-input-bytes: 65536
    # namespace: payments
    # ssl-bundle: openbao
```

`endpoint` 只接受服务原点，不能含账号密码、查询参数或 `/v1` 路径。`mount` 可为 `payments/transit`；`namespace`
是可选请求头，只有服务端已配置相应命名空间时使用。生产使用 HTTPS；本地调试 HTTP 必须显式设置 `allow-insecure-http: true`
。SSL Bundle 沿用 Spring Boot 的 CA 与客户端证书配置。

`max-input-bytes` 限制原始输入，默认 64 KiB、最大 1 MiB，网络响应也有上限。RSA 加密受单块 OAEP 长度限制，不能用于大报文；一般业务数据使用
AES，组件不做 RSA 分块。默认连接池每个身份独立，凭证不是普通配置中的字符串。

默认从进程环境读取 `OPENBAO_TOKEN`。组件不会启动时访问服务；未设置凭证时首次调用返回 `AUTHENTICATION`。生产可注入
`KmsTokenProvider`，由集成方完成 AppRole/Kubernetes 登录、缓存和续租：

```java
@Bean
KmsTokenProvider kmsTokenProvider(MyOpenBaoSession session) {
    return session::currentToken;
}
```

管理接口只有 `management-enabled: true` 时创建，使用 `KmsManagementTokenProvider` 或独立的 `OPENBAO_MANAGEMENT_TOKEN`
。不要将管理令牌授权给业务请求。

## 业务操作

构造器注入 `KmsTemplate`：

```java
byte[] canonical = canonicalRequest.getBytes(StandardCharsets.UTF_8);
KmsSignature signature = kms.sign(
        new KmsKeyRef("merchant-signing", 1),
        KmsSignatureAlgorithm.RSA_SHA256_PKCS1,
        canonical);
String channelSignature = signature.base64();
boolean valid = kms.verify(new KmsKeyRef("merchant-signing", 1), signature, canonical);
String publicPem = kms.publicKey(new KmsKeyRef("merchant-signing", 1));

KmsCiphertext ciphertext = kms.encrypt(KmsKeyRef.latest("sensitive-fields"), plaintext);
byte[] original = kms.decrypt(KmsKeyRef.latest("sensitive-fields"), ciphertext);
```

`version = 0`（`latest`）在新签名/加密时读取最新版本并固定到这次操作。支付渠道应使用渠道已注册公钥对应的明确版本；先登记新公钥、验证成功后再切换签名版本。历史签名和密文需要保存它们自身的版本。

`verify` 的 `expectedKey` 校验调用方期望的名称；传入明确版本还会检查版本一致。收到外部 Base64 签名时，使用已认证商户绑定的密钥信息构造
`new KmsSignature(name, version, algorithm, Base64.getDecoder().decode(externalSignature))`，不要从未经认证的输入决定密钥引用。内容不匹配返回
`false`；服务不可用、拒绝访问、协议错误抛异常，不伪装成验签失败。

PSS 使用 SHA-256、MGF1-SHA256、32 字节 salt；PKCS#1 输出等价于 Java `SHA256withRSA`。组件不提供渠道规范化、证书解析、nonce
防重放或商户授权，这些由上层协议负责。Transit 密文不等同于微信等渠道要求的特定 AES 报文格式。

## 字符串凭据管理

选择 binder，启用 `loadup.kms.enabled=true` 和 `loadup.kms.secrets-enabled=true`，再注入 `KmsSecretManager`。
凭据名称由业务传入。Local 和 Database 共用 `loadup.kms.secret-key-alias` 指定专用 AES 条目，OpenBao 可另设 `loadup.kms.secret-mount`。

```java
secrets.create("payments-api-token", initialValue);
secrets.update("payments-api-token", rotatedValue);
String currentValue = secrets.get("payments-api-token");
```

`create` 保留已有凭据，`update` 替换已有凭据的当前值，`get` 直接读取当前值。远程后端管理各自的历史版本。名称接受 1 到 128 个英文字母、数字、
下划线或连字符，值为 1 到 30,720 个 UTF-8 字节。调用方从受控来源提供明文，并避免把返回值写入日志。

- **Aliyun** 复用现有 Java V2.0 SDK 和 RAM Role 或 AK/SK。创建通用文本凭据时使用服务端默认加密配置，已有凭据沿用原来的加密密钥。
  读取授予 `kms:GetSecretValue`，受控写入流程按需授予 `kms:CreateSecret` 和 `kms:PutSecretValue`，自管密钥还需相应的密钥权限。
  [Aliyun 凭据授权](https://www.alibabacloud.com/help/en/kms/key-management-service/security-and-compliance/authorization-information)
- **OpenBao** 先启用 KV v2，默认挂载点为 `secret`，凭据保存为 `value` 字段。读取身份需要 `<mount>/data/<name>` 的 `read`，
  创建需要 `create`，更新需要同一路径的 `patch` 和 `<mount>/metadata/<name>` 的 `read`。更新用当前版本执行比较并写入，遇到并发变更会失败。
  [OpenBao KV v2](https://openbao.org/docs/secrets/kv/kv-v2/)
- **Local** 将 AES-GCM 密文写入 `loadup.kms.local.secret-directory`，应用重启后用同一个目录和 AES 密钥读取。
  [Local 接入](loadup-components-kms-binder-local/README.md#字符串凭据)
- **Database** 将 AES-GCM 密文写入应用 schema 的 `kms_secret` 表，应用重启后用同一个 schema 和 AES 密钥读取。
  [Database 接入](loadup-components-kms-binder-database/README.md#字符串凭据)

## 密钥管理与导入

构造器注入可选 `KmsKeyManager`，管理功能应只暴露给受授权的管理流程：

```java
manager.create("merchant-signing", KmsKeyType.RSA_2048);
manager.create("sensitive-fields", KmsKeyType.AES_256_GCM);
manager.rotate("sensitive-fields");
manager.setMinimumVersions("sensitive-fields", 2, 1);
```

最低版本配置可能使旧密文无法解密或旧签名无法验签；提高最低解密版本前先完成数据迁移并确认历史保留要求。超时后的创建/轮换状态可能已经生效，应查询
OpenBao 状态，不能盲目重试。

已有渠道密钥可导入，但首版不接受明文私钥：

1. `wrappingPublicKey()` 获取 OpenBao wrapping 公钥。
2. 在受控离线工具中按 OpenBao BYOK 格式（SHA-256 OAEP + AES-KWP）包装私钥/对称密钥。
3. 调用 `importWrappedKey(name, type, wrappedBytes)`；这里传入的是完整的包装二进制，不能是明文 DER/PEM，组件不替调用方执行包装。
4. `importPublicKey(name, type, pem)` 导入 `BEGIN PUBLIC KEY` 格式的 RSA 公钥，只能执行该公钥支持的操作。

导入请求显式禁用 OpenBao 内部轮换，避免误替换渠道注册密钥。新增导入版本、重包装旧密文及完整 BYOK 工具暂未封装。

## OpenBao 权限与部署准备

运维先启用 Transit 挂载，建议全局关闭加密接口自动创建密钥：

```shell
bao secrets enable transit
bao write transit/config/keys disable_upsert=true
```

按已绑定密钥逐一授予运行权限，示例策略：

```hcl
path "transit/keys/merchant-signing" { capabilities = ["read"] }
path "transit/sign/merchant-signing/sha2-256" { capabilities = ["update"] }
path "transit/verify/merchant-signing/sha2-256" { capabilities = ["update"] }
path "transit/keys/sensitive-fields" { capabilities = ["read"] }
path "transit/encrypt/sensitive-fields" { capabilities = ["update"] }
path "transit/decrypt/sensitive-fields" { capabilities = ["update"] }
```

运行身份不授予密钥创建、配置、轮换、导出或备份权限，加密路径只授予 `update`。管理身份按管理范围授予 `keys/{name}` 的
`create/update`、`keys/{name}/rotate`、`config`、`import` 的 `update` 和 `wrapping_key` 的 `read`。修改挂载名时同步修改策略路径；使用真实
OpenBao 验证 ACL 匹配后再接入业务。

运维负责持久存储、解封、HA、备份恢复、审计设备与访问策略；应用不得记录请求体、令牌、明文及签名字节。商户到密钥的映射和管理操作审批/审计归上层业务，不由组件建表。

## 失败语义与验证

`KmsException.getCode()` 提供 `AUTHENTICATION`、`ACCESS_DENIED`、`NOT_FOUND`、`INVALID_REQUEST`、`UNAVAILABLE`、`TRANSPORT`、
`INVALID_RESPONSE`；异常不保留远端报文或底层异常，以免泄漏密码材料。服务失败不会降级到本地私钥。

已编写本地协议夹具测试及自动装配测试，涵盖独立身份、凭证变更、密钥版本、JCA 签名互操作、异常脱敏、不重试及输入限制；它们不代表真实
OpenBao 已验证。遵循仓库约定，本次不执行测试。用户可定向运行：

```shell
mvn clean -pl loadup-components/security/loadup-components-kms -am test \
  -Dtest=OpenBaoKmsTest,KmsAutoConfigurationTest \
  -Dsurefire.failIfNoSpecifiedTests=false -Dspotbugs.skip=true -Dspotless.skip=true
```

真实服务验收仍需覆盖密钥轮换、历史数据、BYOK、公钥导入、封印/重启、ACL、续租和故障恢复。设计见
[ARCHITECTURE](ARCHITECTURE.md)，协议依据为 [OpenBao Transit API](https://openbao.org/docs/api/secret/transit/)。

## Local binder

引入 `loadup-components-kms-binder-local` 依赖，设置 `loadup.kms.enabled=true`（`loadup.kms.binder` 默认为 `local`），可由应用提供 `LocalKmsKeyStore` Bean，或使用 [local binder 的文件加载配置](loadup-components-kms-binder-local/README.md)。Bean 包含已加载的 Java KeyStore 和各密钥条目共用的密码；集成方负责生成、导入和持久化 KeyStore，加载器只读打开既有文件。RSA 私钥条目需要证书链，AES 密钥使用 SecretKeyEntry。可用 Java 的 KeyPairGenerator 和 KeyGenerator 生成密钥，RSA 条目还需配套证书。

本地实现支持 RSA SHA-256 签名和验签、AES-GCM 加解密以及公钥和元数据查询。别名对应 KMS 密钥名，目前只支持版本 1，不提供密钥轮换或管理接口。本地密文使用 `local:v1:` 前缀，与 OpenBao 密文不可互换；切换后端前须保留原有后端以解密历史数据。不要把密码或密钥写入普通配置文件。

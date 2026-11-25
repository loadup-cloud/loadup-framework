# KMS API

坐标：io.github.loadup-cloud:loadup-components-kms-api，版本由 LoadUp BOM 管理。

提供 KmsTemplate、KmsKeyRef、KmsSignature、KmsCiphertext、元数据和管理接口契约。仅依赖 commons-error/commons-json，不装配后端或发起网络调用。
运行时选择一个 binder；默认推荐 local，需要远程 Transit 时选 OpenBao。构造器注入 KmsTemplate，使用 sign/verify/encrypt/decrypt/publicKey/metadata。

名称、版本、算法和密文字节属于契约；不要从不可信请求决定密钥引用。密文由产生它的 binder 解密。

`new KmsKeyRef(name)` 与 `KmsKeyRef.latest(name)` 等价，默认版本为 0。使用云端原生版本的 binder 可通过结果的 `keyRef()` 返回实际密钥版本，用于之后按原密钥验签或解密。

`currentSigningKey(alias)` 返回实际签名版本及公钥，`verificationKeys(alias)` 列出该 alias 仍受信任的公钥。按指纹调用 `verify` 时由业务提供可信 alias，binder 从后端筛选匹配的版本。

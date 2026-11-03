# KMS API

坐标：io.github.loadup-cloud:loadup-components-kms-api，版本由 LoadUp BOM 管理。

提供 KmsTemplate、KmsKeyRef、KmsSignature、KmsCiphertext、元数据和管理接口契约。仅依赖 commons-error/commons-json，不装配后端或发起网络调用。
运行时选择一个 binder；默认推荐 local，需要远程 Transit 时选 OpenBao。构造器注入 KmsTemplate，使用 sign/verify/encrypt/decrypt/publicKey/metadata。

名称、版本、算法和密文字节属于契约；不要从不可信请求决定密钥引用。两种 binder 的密文不能互换。

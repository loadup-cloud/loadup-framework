# KMS OpenBao binder 架构

OpenBaoKmsTemplate 将标准 API 转为 Transit 请求，私钥和对称密钥留在远端后端；组件不提供密钥导出。
独立连接池共享 LoadUp HTTP 的地址策略、TLS、大小限制和观测；无自动重试/重定向。凭证按请求读取。
OpenBao 密文与 local 密文不可交换；切换后端前明确历史密文和签名的保留方案。

统一契约与边界见 [KMS 架构](../ARCHITECTURE.md)。本文按当前源码整理，不代表构建或运行验收。

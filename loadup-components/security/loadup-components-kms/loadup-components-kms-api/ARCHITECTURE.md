# KMS API 架构

API 与实现按 Maven 模块分开，业务只依赖 API；binder 负责算法、密钥存储/网络和自动装配。
KmsKeyManager 是独立管理契约，不表示所有 binder 都提供管理 Bean；本地 binder 当前仅提供运行操作。

统一契约与边界见 [KMS 架构](../ARCHITECTURE.md)。本文按当前源码整理，不代表构建或运行验收。

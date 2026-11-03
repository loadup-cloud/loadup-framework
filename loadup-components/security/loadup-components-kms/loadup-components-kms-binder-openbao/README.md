# KMS OpenBao binder

坐标：io.github.loadup-cloud:loadup-components-kms-binder-openbao，版本由 LoadUp BOM 管理。

通过既有 HTTP 组件访问独立 OpenBao Transit，传递引入 kms-api。显式选择 loadup.kms.binder=openbao 并启用 KMS。
endpoint、SSL Bundle、凭证 Provider、输入上限及可选管理身份见 [KMS OpenBao 接入](../README.md#openbao-接入)。
组件不部署、初始化或解封 OpenBao。管理身份与业务运行身份分开，管理 Bean 仅在显式启用时创建。

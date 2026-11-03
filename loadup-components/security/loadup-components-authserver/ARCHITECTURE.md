# LoadUp Components AuthServer 架构

## 职责与边界

OAuth2 授权服务器与令牌签发的聚合模块，负责组织下列子模块。

## 子模块关系

此 POM 聚合以下模块，具体实现和配置由各子模块负责：

- [`loadup-components-authserver-api`](loadup-components-authserver-api/ARCHITECTURE.md)
- [`loadup-components-authserver-binder-sas`](loadup-components-authserver-binder-sas/ARCHITECTURE.md)
- [`loadup-components-authserver-test`](loadup-components-authserver-test/ARCHITECTURE.md)

## 分层与调用路径

API 定义主体、认证契约与 KMS 密钥引用配置；SAS binder 通过 kms-api 委托 RSA 签名，并仅发布公钥 JWKS 和可选 OAuth2 协议端点。
集成应用选择 kms-local 等 binder，AuthServer 不持有私钥对象，不再使用独立的 Base64 私钥或临时生成路径。

```text
loadup-components-authserver
  └─ loadup-components-authserver-api
  └─ loadup-components-authserver-binder-sas
  └─ loadup-components-authserver-test
```

聚合 POM 组织模块与版本，运行时依赖由子模块决定。

## 设计取舍

聚合层只表达模块组合和依赖方向，实际能力由选定的子模块提供。

集成方式与配置示例见 [README.md](README.md)。

# OAuth Server API 架构

## 契约边界

`LoadUpSubject` 统一主体标识，`OAuthClientSettings` 定义客户端启用状态等元数据键。`OAuthClientManagementPaths` 与管理请求、授权视图和凭证响应定义适配层可复用的协议契约。API 发布独立轻量 JAR，签发、认证、事务、密码编码与存储实现归 Core 和宿主应用。

管理适配器用授权视图返回标准客户端信息。创建和轮换时通过专用凭证响应交付新 secret，调用方按敏感字段处理该结果。宿主负责管理入口的认证、授权和审计。

[API 接入](README.md) · [OAuth Server 总体设计](../ARCHITECTURE.md) · [Core 实现](../loadup-components-oauth-server-core/ARCHITECTURE.md)

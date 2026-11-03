# 可选 Dubbo binder 架构

本模块封装可选 Triple 运行装配、令牌 attachment 和业务错误码映射，不替代默认 HTTP 客户端。
IDL/DTO/mapper 由选用该协议的集成方契约模块拥有；app 保持纯业务 Facade，适配器负责转换与委托。

详细配置与信任边界集中在 [README](README.md#身份与错误)。其内部 exp-only 解析依赖已验证入口与受控网络，
不能用于公开入口，也不能沿用到 HTTP Resource Server。Pay 已移除本协议，示例不代表 Pay 当前源码。
选择 binder 不授权部署注册中心或执行金融请求；运行验收独立完成。

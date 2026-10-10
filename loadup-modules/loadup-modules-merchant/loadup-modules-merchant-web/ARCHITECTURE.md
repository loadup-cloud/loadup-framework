# LoadUp Merchant Web Architecture

## 职责

可选管理 API。

## 设计与依赖

Controller 只暴露 POST /merchants 下 create/update/page/detail/status，WebMVC 添加 /api 前缀、JSON 报文和掩码，方法权限 merchant:read/write/manage，SpringDoc 提供文档。

详细职责边界见 [模块架构](../ARCHITECTURE.md)。client/domain 是公开类型与纯业务模型；infrastructure/app/web 承担持久化、编排与 HTTP。contract 适配只依赖公开查询接口，不建立两个核心模块之间的实现依赖。

## 不变量与验证

租户隔离、编码唯一/不可变、版本条件更新、状态、私有字段更新规则在适当层保证。商户启用不表示资质认证，基本事实不含私人信息。单测与 MySQL 组合 IT 源码已写，未运行；Boot 装配、实际前端和鉴权需消费工程验收。

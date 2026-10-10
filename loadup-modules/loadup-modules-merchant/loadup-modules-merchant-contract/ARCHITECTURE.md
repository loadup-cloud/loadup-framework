# LoadUp Merchant Contract Architecture

## 职责

可选合约事实适配。

## 设计与依赖

只依赖 merchant-client、contract-client 与 Boot 装配，将 MerchantLookup 查询结果转换为 MerchantFactsProvider。检查 tenant/id，仅输出基本事实，已存在 Provider 时退让。

详细职责边界见 [模块架构](../ARCHITECTURE.md)。client/domain 是公开类型与纯业务模型；infrastructure/app/web 承担持久化、编排与 HTTP。contract 适配只依赖公开查询接口，不建立两个核心模块之间的实现依赖。

## 不变量与验证

租户隔离、编码唯一/不可变、版本条件更新、状态、私有字段更新规则在适当层保证。商户启用不表示资质认证，基本事实不含私人信息。单测与 MySQL 组合 IT 源码已写，未运行；Boot 装配、实际前端和鉴权需消费工程验收。

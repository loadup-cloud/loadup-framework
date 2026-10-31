# LoadUp Merchant App Architecture

## 职责

资料管理编排。

## 设计与依赖

MerchantService 事务编排创建/更新/查询/启停；MerchantQueryService 提供可信基本 Profile。共享 Clock、事务管理器与
DataSource；显式 Import 装配，不与 REGISTER_BEAN 条件混用扫描。

详细职责边界见 [模块架构](../ARCHITECTURE.md)。client/domain 是公开类型与纯业务模型；infrastructure/app/web 承担持久化、编排与
HTTP。contract 适配只依赖公开查询接口，不建立两个核心模块之间的实现依赖。

## 不变量与验证

租户身份校验、编码唯一/不可变、版本条件更新、状态、私有字段更新规则在适当层保证。关系型数据当前只使用默认 schema，未提供租户间数据库隔离；详见[数据库 README](../../../loadup-components/data/loadup-components-database/README.md)。商户启用不表示资质认证，基本事实不含私人信息。单测与
MySQL 组合 IT 源码已写，未运行；Boot 装配、实际前端和鉴权需消费工程验收。

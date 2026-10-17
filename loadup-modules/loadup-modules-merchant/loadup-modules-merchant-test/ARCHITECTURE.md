# LoadUp Merchant Test Architecture

## 职责

商户和合约验证源码。

## 设计与依赖

MerchantBasicInfoTest 与 MerchantFactsTest 检查领域语义和适配；MerchantContractIT 使用 Testify/MySQL Testcontainers 检查资料隔离与合约真实集成。三套 yml 支持本地与 CI 定向验证。

详细职责边界见 [模块架构](../ARCHITECTURE.md)。client/domain 是公开类型与纯业务模型；infrastructure/app/web 承担持久化、编排与 HTTP。contract 适配只依赖公开查询接口，不建立两个核心模块之间的实现依赖。

## 不变量与验证

租户隔离、编码唯一/不可变、版本条件更新、状态、私有字段更新规则在适当层保证。商户启用不表示资质认证，基本事实不含私人信息。MySQL 组合 IT 的运行结果见 README；实际前端和鉴权需消费工程验收。

## 测试装配约定

使用标准 Boot 自动装配，显式声明可选 Flyway starter 及 MySQL 扩展，通过真实迁移创建业务表和 Sequence 表。持久化框架配置由 database 独占，测试配置不覆盖 `mybatis-flex.global-config`。测试类不固定 profile，允许 CI 切换配置；关闭容器复用以隔离迁移历史和其他测试遗留的 schema。

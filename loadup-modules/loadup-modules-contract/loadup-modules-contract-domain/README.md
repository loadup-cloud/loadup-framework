# LoadUp Contract Domain

纯 Java 合约领域核心。Maven 坐标 `io.github.loadup-cloud:loadup-modules-contract-domain`，版本由 LoadUp BOM 管理，Java 25，无运行三方依赖。

## 接入

完整实例和约束见 [Contract README](../README.md)。公开类型位于 `io.github.loadup.modules.contract.domain.model`。通过 SalesPlanCompiler 发布方案，通过 ContractSigner 构建签约修订，通过 ContractRuntimeResolver 查询指定产品项。

## 能力矩阵

| 能力 | 契约 |
|---|---|
| 值和参数 | 四种规范化标量，严格类型与覆盖约束 |
| 条件 | 三值逻辑，预算限制，未知配置引用拒绝 |
| 配置 | 确定继承与字段来源 |
| 方案/签约 | 固定版本、选择校验、不变快照 |
| 运行判断 | 时间、管理状态、使用规则、拒绝原因 |
| 存储/HTTP | 不提供，后续由 infrastructure/app/web 接入 |

## 验证状态

测试位于 sibling test 模块，目前未编译或执行。领域边界见 [ARCHITECTURE](ARCHITECTURE.md)。

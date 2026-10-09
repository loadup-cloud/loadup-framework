# LoadUp Contract Test

合约领域测试模块，非生产运行依赖。坐标 `io.github.loadup-cloud:loadup-modules-contract-test`，只通过 Maven 聚合进行验证。

## 能力矩阵

| 测试 | 场景 |
|---|---|
| ContractFlowTest | 产品到签约的完整解析、协商权限、来源、历史快照、时间、暂停、依赖和互斥 |
| ConditionEvaluatorTest | 未知事实、三值逻辑、类型、数值比较、区间、预算和配置引用 |
| 数据库/HTTP 集成 | 后续阶段补充，当前未提供 |

## 执行

见 [主模块 README 的定向命令](../README.md#定向验证)。测试源码已编写，但未编译或执行，不代表验证通过。

application.yml 激活 test，application-test.yml/application-ci.yml 为后续真实数据库集成预留。当前测试不启动 Spring 或数据库，不使用 MockBean。

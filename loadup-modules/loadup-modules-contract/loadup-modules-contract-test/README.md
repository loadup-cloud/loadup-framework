# LoadUp Contract Test

合约模块验证源码，不作为生产依赖。parent 为根 loadup-parent。

## 能力矩阵

| 测试 | 覆盖 |
|---|---|
| ContractFlowTest / ConditionEvaluatorTest | 参数继承、选择、条件三值、时间及快照 |
| ContractAutoConfigurationTest | 自动配置注册、启用/禁用与数据源候选条件 |
| ContractCodecTest | Jackson3 多态存储往返、摘要排序、未知字段和大小限制 |
| ContractPersistenceIT | 真实 MySQL 租户引用、发布不变、乐观锁、幂等并发和生命周期 |

按项目约定尚未运行。集成测试引入 Testify + Testcontainers，需要 Docker；application.yml 激活 test，application-test.yml/application-ci.yml 均使用容器注入数据库。由用户定向执行带 clean 的构建，详见 [主 README](../README.md)。

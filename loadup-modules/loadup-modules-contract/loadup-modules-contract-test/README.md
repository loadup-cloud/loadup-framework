# LoadUp Contract Test

合约模块验证源码，不作为生产依赖。parent 为根 loadup-parent。

## 能力矩阵

| 测试 | 覆盖 |
|---|---|
| ContractFlowTest / ConditionEvaluatorTest | 参数继承、选择、条件三值、时间及快照 |
| ContractAutoConfigurationTest | 自动配置注册、启用/禁用与数据源候选条件 |
| ContractCodecTest | Jackson3 多态存储往返、摘要排序、未知字段和大小限制 |
| ContractPersistenceIT | 真实 MySQL 租户引用、发布不变、乐观锁、幂等并发和生命周期 |

集成测试引入 Testify + Testcontainers，需要 Docker；配置与执行方式见下文和 [主 README](../README.md)。

## 运行与数据库初始化

- `application.yml` 默认激活 `test`；CI 使用 `-Dspring.profiles.active=ci`，测试类不固定 profile。
- 公共配置启用 `loadup.database.multi-tenant.enabled/required`，使租户上下文与持久化过滤一致；各场景使用独立租户。
- IT 显式关闭容器复用，从空 MySQL 数据库执行迁移，避免其他测试留下的表和 Flyway 历史污染。
- 测试模块显式引入 test scope 的 `spring-boot-starter-flyway` 和 `flyway-mysql`；database 的可选 Flyway 依赖不会自动传递给消费方。使用真实的业务及 Sequence 迁移，不手动复制建表脚本。
- 不设置 `mybatis-flex.global-config`，包括 `print-banner` 等诊断选项；database 统一管理持久化约定。

从仓库根目录执行（`verify` 才会运行 Failsafe IT）：

```bash
mvnd -B -ntp -T 1 clean verify -pl :loadup-dependencies,:loadup-modules-contract-test -am \
  -Dtest=PersistenceConventionGuardTest -Dsurefire.failIfNoSpecifiedTests=false \
  -Dit.test=ContractPersistenceIT -Dfailsafe.failIfNoSpecifiedTests=false \
  -Dspring.profiles.active=ci -Dskip.spotbugs=true
```

该命令选择上述 IT 和 database 的配置保护单测，跳过其他测试及 SpotBugs；不能代表完整质量门禁。

## 运行记录

2026-10-17，包含 BOM、database、商户与合约测试及其依赖的定向 `mvnd clean verify --fail-at-end` 成功。使用 `ci` profile 和非复用 MySQL 容器，本模块 5 项 IT、另一业务模块 IT 以及 database 的 5 项配置保护单测全部通过；相关 Spotless、License、Enforcer 校验通过。未执行其他单测、SpotBugs、完整 reactor 或远程 CI；本轮未另跑 `test` profile。

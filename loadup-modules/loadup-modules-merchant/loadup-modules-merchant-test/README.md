# LoadUp Merchant Test

商户和合约验证源码。接入细节见 [商户 README](../README.md)。

## Maven

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-merchant-test</artifactId>
</dependency>
```

版本由 LoadUp BOM 管理；test 仅限 test scope，不作为生产依赖。

## 能力矩阵

| 能力 | 契约 |
|---|---|
| 职责 | 商户和合约验证源码 |
| 当前实现 | MerchantBasicInfoTest 与 MerchantFactsTest 检查领域语义和适配；MerchantContractIT 使用 Testify/MySQL Testcontainers 检查资料隔离与合约真实集成。使用三套 yml 进行定向验证。 |
| 验证状态 | 验证结果见下方运行记录 |

## 接入约束

使用可信租户作用域，编码创建后不变；合约 merchantId 为返回 id。地址/登记号/联系方式输出脱敏，修改时 null 保留、空串清空，不写回掩码。Merchant app 与 contract 适配需同时引入；自定义 Provider 可替换默认适配。配置、权限、示例和限制见主 README。设计见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 运行与数据库初始化

- `application.yml` 默认激活 `test`；CI 使用 `-Dspring.profiles.active=ci`，测试类不固定 profile。
- 公共配置启用 `loadup.database.multi-tenant.enabled/required`，使租户上下文与持久化过滤一致；各场景使用独立租户。
- IT 显式关闭容器复用，从空 MySQL 数据库执行迁移，避免其他测试留下的表和 Flyway 历史污染。
- 测试模块显式引入 test scope 的 `spring-boot-starter-flyway` 和 `flyway-mysql`；database 的可选 Flyway 依赖不会自动传递给消费方。使用真实的业务及 Sequence 迁移，不手动复制建表脚本。
- 使用 PostgreSQL 初始迁移；数据库与事务配置由测试应用提供。

从仓库根目录执行（`verify` 才会运行 Failsafe IT）：

```bash
mvnd -B -ntp -T 1 clean verify -pl :loadup-dependencies,:loadup-modules-merchant-test -am \
  -Dtest=PersistenceConventionGuardTest -Dsurefire.failIfNoSpecifiedTests=false \
  -Dit.test=MerchantContractIT -Dfailsafe.failIfNoSpecifiedTests=false \
  -Dspring.profiles.active=ci -Dskip.spotbugs=true
```

该命令选择上述 IT 和 database 的配置保护单测，跳过其他测试及 SpotBugs；不能代表完整质量门禁。

## 运行记录

2026-10-17，包含 BOM、database、商户与合约测试及其依赖的定向 `mvnd clean verify --fail-at-end` 成功。使用 `ci` profile 和非复用 MySQL 容器，本模块 2 项 IT、另一业务模块 IT 以及 database 的 5 项配置保护单测全部通过；相关 Spotless、License、Enforcer 校验通过。未执行其他单测、SpotBugs、完整 reactor 或远程 CI；本轮未另跑 `test` profile。

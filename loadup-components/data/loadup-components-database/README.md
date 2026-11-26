# Database 组件接入手册

提供 MyBatis-Flex 和 Spring JDBC 集成、PostgreSQL 固定 schema、事务及业务 ID 分配。宿主负责数据源、驱动和业务迁移。

## Maven 依赖

导入 Framework BOM 后添加运行时组件。需要生成 Mapper 的实体模块另行配置 [Database 处理器](../loadup-components-database-processor/README.md)。

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-database</artifactId>
</dependency>
```

组件引入 MyBatis-Flex Spring Boot starter，注册框架 Mapper，使用共享 Spring `Clock`，并在单数据源下提供 JDBC 事务管理器。在同一 Spring 事务内，MyBatis-Flex 和 Spring JDBC 使用同一连接，Mapper 可读取未提交的 JDBC 写入，回滚覆盖两种访问方式。

## 固定 schema 与迁移

单个部署使用一个租户和一套固定 schema。`loadup.database.schema` 同时配置连接池和 Flyway。校验规则为 `[a-z_][a-z0-9_]{0,62}`，`public` 和 `pg_` 保留。

```yaml
loadup:
  database:
    schema: customer_a_payment
spring:
  datasource:
    url: ${PAYMENT_JDBC_URL}
    username: ${PAYMENT_JDBC_USERNAME}
    password: ${PAYMENT_JDBC_PASSWORD}
```

- Hikari `schema` 或 JDBC URL 的 `currentSchema` 如有显式配置，必须与目标一致。启动拒绝非空的 Hikari `connection-init-sql` 和 JDBC `options`，包括 Hikari 提供的驱动属性。
- 启动校验 `current_schema()` 和完整 `search_path`。搜索路径只能包含配置的 schema，应用运行账号需获得相应权限。
- 启用 Flyway 时添加 Flyway 和 PostgreSQL 适配工件。通过 `loadup.flyway.locations` 纳入应用自有与获准的模块目录，例如 `classpath:db/migration/pay/admin` 或 `classpath:db/migration/framework/upms`。通用迁移根也会发现依赖 JAR 的匹配资源。
- 组件表模板位于 `db/schema`。在空 schema 初始化时将需要的模板纳入应用迁移。已发布的迁移版本保持内容不变，升级追加新版本。

Task、Scheduler 和 Transfer 使用同一数据源。部署身份通过 `DeploymentIdentity` 表达，锁、AsyncResult 和 DFS 使用各自配置的部署命名空间。

## 持久化接入

1. 在 infrastructure 中定义带 `com.mybatisflex.annotation.Table` 的 DO，继承 `io.github.loadup.components.database.dataobject.BaseDO`。基类包含 id、createdAt、updatedAt 和 deleted，Flex 主键策略为 `KeyType.None`。
2. 在实体模块父 POM 配置编译器处理器。Framework BOM 管理处理器版本，编译器选项由消费工程维护。按处理器文档显式选择 LoadUp、MapStruct 和 Spring Boot 元数据处理器。
3. 用生成的 Mapper、`ServiceImpl<Mapper, DO>` 和 `QueryWrapper` 实现简单筛选、分页和逻辑删除。复杂筛选、`FOR UPDATE`、`IN` 列表及显式列查询通过 infrastructure Gateway 中的 `JdbcClient` 和命名参数完成。可参考 `LoginLogGatewayImpl` 和 `InboxGatewayImpl`。
4. 显式 SQL 自行设置 ID、审计时间和 Boolean `deleted`，查询显式过滤已删除记录。业务表按部署 schema 隔离。MyBatis-Flex 的读写、计数和 `deleteById` 使用统一逻辑删除配置。分页缺省每页 20 条，上限 100。

## 业务 ID 与序列

启用 Sequence 时，默认 `IdGenerator` 为 `BusinessIdGenerator`。持久化写入显式申请 ID。消费应用可以提供单一 `IdGenerator` Bean（如 `UuidV7IdGenerator`）替换默认实现。关闭 `loadup.database.sequence.enabled` 时必须提供替代实现。显式设置的 ID 保留。

Sequence 默认开启。使用前将 `db/schema/sequence_allocation.sql` 作为应用自有版本化迁移纳入 Flyway。每个编码站点应有一个权威分配数据库。故障切换或恢复时保留已提交的分配上界，限制序列行被删除或重置。

```yaml
loadup:
  database:
    sequence:
      enabled: true
      step: 10000
      max-scopes: 1024
      prefetch-threads: 2
      queue-capacity: 256
      wait-timeout: 3s
    business-id:
      data-version: 0
      system-version: 1
      event-code: "00000000"
      extension-code: ""
      site-id: 1
      shard: 0
```

默认元数据为数据版本 0、系统版本 1、事件码 `00000000`、空扩展、站点 000 和分片 000。通用持久化 ID 使用 `00000000`，显式业务生成可使用已登记的事件码。使用默认作用域的实体共享其每日八位容量。独立的发号数据库需明确站点所有权。

注入 `BusinessIdGenerator` 后可复用不可变的命令元数据。

```java
BusinessIdCommand command = new BusinessIdCommand(
        0, 1, "11000000", "", 1, 440);
String businessId = generator.generate(command);
String[] businessIds = generator.generateBatch(command, 100);
BusinessIdParts parts = formatter.parse(businessId);
```

编码顺序为日期（8）、数据版本（1）、系统版本（1）、事件码（8）、扩展（0 至 32）、站点（3）、分片（3）、序列（8）。总长度为 32 至 64，数字保留前导零。每次调用从共享 `Clock` 的时区取一次日期。日期、版本、事件及站点决定分配作用域，分片和扩展共享计数。每个作用域每日最多分配 99,999,999 个号码，含未用完的保留号码。

`shard` 是 000 至 999 的逻辑桶，调用方按稳定路由规则传入，例如按客户路由的版本化散列。需要基于序号路由时组合数值 Sequence 和格式器。分片字段用于业务标识和路由。

需要纯数字时注入 `Sequence`。

```java
SequenceScope scope = new SequenceScope("payment", LocalDate.now(clock), 99_999_999L);
long number = sequence.next(scope);
long[] batch = sequence.nextBatch(scope, 100);
```

热点调用可复用已计算缓存键的 `SequenceScope`。日期为空表示持久作用域。唯一性在单个作用域内成立，最大值首次建立后固定。`nextBatch` 支持 1 至 10,000，可跨不连续区间，失败时不返回部分批次，已消耗号码仍保留。业务幂等复用已保存的 ID。

## 容量与接入检查

正常发号使用内存区间，冷启动和补段需要数据库连接。预取、缓存、独立事务、失败冷却与淘汰策略见 [数据库架构](ARCHITECTURE.md#序列分配)。号码允许跳号，业务排序使用自己的顺序字段。

存在共享 `MeterRegistry` 时，组件记录 `loadup.database.sequence.issued`、`loadup.database.sequence.refill` 和 `loadup.database.sequence.failures`。失败指标使用低基数的 `reason` 枚举。部署验收检查固定 schema、数据库权限、冷启动与补段延迟，并演练分配数据库切换和恢复。

需要验证该组件时可执行以下定向测试，数据库 IT 需要 Docker 和 PostgreSQL 容器。

```shell
mvnd clean test -pl loadup-components/data/loadup-components-database -am \
  -Dtest=BusinessIdTest,SegmentSequenceTest,DatabaseSequenceIT \
  -Dsurefire.failIfNoSpecifiedTests=false -Dspotless.skip=true -Dspotbugs.skip=true
```

[数据库架构](ARCHITECTURE.md)

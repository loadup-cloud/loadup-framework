# LoadUp Components Database

MyBatis-Flex and Spring JDBC support for persistent IDs, PostgreSQL transactions, sequence allocation and one fixed
business schema per deployment.

## Maven

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-database</artifactId>
</dependency>
```

The component uses the shared Spring `Clock` and supplies a JDBC transaction manager for a single data source, the
fixed schema target and sequence/ID configuration. It brings `mybatis-flex-spring-boot4-starter`, registers framework
mappers and configures the shared Flex conventions. Applications configure their own data source and PostgreSQL driver.

With the default single-DataSource setup, MyBatis-Flex mappers and Spring JDBC share the connection bound to the
Spring transaction. A mapper can read JDBC writes before commit, and one rollback covers both APIs. The component
configures this automatically.

MyBatis-Flex is the default ORM for simple aggregate reads. Annotate infrastructure data objects with
`com.mybatisflex.annotation.Table`; the annotation processor generates a `Tables` class, per-entity `TableDef` classes
and a `@Mapper` interface under the module's `infrastructure.table` and `infrastructure.mapper` packages. Gateway
implementations extend `ServiceImpl<Mapper, DO>` (and therefore `IService<DO>`) so standard reads, paging and logical
deletion come from the framework; complex conditional writes remain explicit parameterized SQL.

The component applies the logical-delete convention through a `MyBatisFlexCustomizer`: the `deleted` column is the
logic-delete flag, normal rows use `false`, deleted rows use `true`, and mapper reads, updates, counts and `deleteById`
automatically add or apply that condition. Paging defaults are 20 items per page with a maximum of 100.

Explicit SQL must still set IDs, audit timestamps and `deleted`, and must filter `deleted` rows itself. Business tables
do not carry a `tenant_id` column: a deployment serves one tenant and every table lives in that deployment's schema.
PostgreSQL schemas use boolean `deleted`.

## Fixed schema per deployment

One deployment serves one tenant. `loadup.database.schema` is the only application source of the target schema; the
connection pool, transaction manager and Flyway all use it. Schema names are validated against
`[a-z_][a-z0-9_]{0,62}`, and the shared `public` schema plus the reserved `pg_` prefix are rejected.

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

Startup behavior:

- `FixedSchemaEnvironmentPostProcessor` applies the schema as the pool's physical-connection setting. An explicit Hikari
  `schema` or JDBC `currentSchema` must equal it. Any non-empty Hikari `connection-init-sql` or JDBC `options` value is
  rejected, including driver properties supplied through Hikari; arbitrary SQL is not parsed to infer its effects.
- `FixedSchemaVerifier` checks both `current_schema()` and the complete `search_path` on a connection before workers,
  schedulers or scanners start. `search_path` must contain only the configured schema. A missing schema does not fall
  back to `public`.
- Flyway uses the same value as `defaultSchema`/`schemas`, so the migration target cannot drift from the runtime target.
- There is no tenant map, no `default-tenant-id`, no per-request `SET search_path` and no routing `DataSource`. Data
  isolation is enforced by the fixed connection configuration and database role privileges, not by request headers.

Background workers (Task, Scheduler, Transfer) run without any tenant context against the same
application data source. Rows do not store a tenant.

Non-relational resources use deployment-fixed namespaces: lock and async-result keys use their configured namespace,
DFS object paths use the configured bucket/prefix, and internal service calls use `DeploymentIdentity`.

## Repository query style

Use generated MyBatis-Flex mappers and `QueryWrapper` for simple aggregates (one or two fixed predicates), paging and
logical deletion. Use `JdbcClient` in the infrastructure gateway when a query has three or more predicates, optional
filters, `FOR UPDATE`, `IN` lists, or requires explicit column lists and locking semantics. `JdbcClient` keeps SQL in
the infrastructure layer with named parameters and clear locking/pagination semantics. Current examples:
`LoginLogGatewayImpl` and `InboxGatewayImpl`.

Generated artifacts use the [LoadUp database processor](../loadup-components-database-processor/README.md).
It includes the official MyBatis-Flex processor as a transitive build dependency and owns the single generation
configuration. Entity modules do not keep mybatis-flex.config files or run synchronization scripts. The root POMs
select LoadUp's processor entry explicitly, alongside MapStruct and Spring Boot configuration metadata processing,
so the upstream processor is not also discovered and executed.

Import the framework BOM for processor versions. BOM import does not inherit compiler-plugin configuration;
independent repositories configure the compiler plugin in their own parent POM. Runtime database configuration
and annotation processing remain separate dependencies.

Flyway support is optional; consumers using migrations must include Flyway and its PostgreSQL database module. Migration
scripts assume a fresh PostgreSQL schema.

`classpath:db/migration` scans matching locations across the classpath, including dependency JARs; it does not
isolate the application. Configure `loadup.flyway.locations` with the application namespace and only approved
framework module namespaces, such as `classpath:db/migration/pay/admin` and
`classpath:db/migration/framework/upms`. Component templates remain under `db/schema`; freeze imported initial
snapshots and append reviewed migrations for upgrades instead of overwriting existing versions.

## Business IDs and database sequences

`BusinessIdGenerator` is the default `IdGenerator` when Sequence is available. Persistence code explicitly requests an
ID when needed; `BaseDO` and JDBC inserts do not automatically assign one. Sequence is enabled by default. Apply its
migration before using the default generator.

Sequence persistence uses Spring JDBC and requires a suitable DataSource and transaction manager. A consumer-provided
`IdGenerator` takes precedence over the default generator.

Default generator metadata uses data version 0, system version 1, event code `00000000`, no extension, site 000 and
shard 000. Reserve `00000000` for generic persistence IDs; explicit business generation may use registered event codes.
All entities using the same default scope share its eight-digit daily capacity. Configure stable site ownership when
independent databases issue IDs.

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

Apply the `db/schema/sequence_allocation.sql` template before enabling traffic. Copy it into the integrating
application's own `classpath:db/migration` history (for example via `scripts/gen-migrations.py`), retaining all other
module locations. The component does not create tables on the request path. Use one authoritative allocation database
for each encoded site, preserve committed allocation maxima during failover/restoration, and restrict deletion/reset of
allocation rows. The migration and allocation SQL target PostgreSQL.

Inject `IdGenerator` when a persistence operation needs an application-generated ID. Explicit SQL is responsible for
setting its ID and audit columns.

An application may supply one `IdGenerator` bean (for example `UuidV7IdGenerator`) to replace the default.
`loadup.database.sequence.enabled=false` requires such an alternative because the default generator depends on Sequence.
Existing explicitly supplied IDs, including UUIDs assigned by application services, are preserved.

Inject `BusinessIdGenerator` and reuse immutable metadata where practical:

```java
BusinessIdCommand command = new BusinessIdCommand(
        0, 1, "11000000", "", 1, 440);
String businessId = generator.generate(command);
String[] businessIds = generator.generateBatch(command, 100);
BusinessIdParts parts = formatter.parse(businessId);
```

Encoding: date (8) + dataVersion (1) + systemVersion (1) + eventCode (8) + extension (0..32) + site (3) + shard (3) +
sequence (8). Length is 32..64; numeric fields preserve leading zeros. Date uses the shared `Clock` and its local zone,
captured once per generation call. Business sequence scope is date + versions + event + site, shared across
shards/extensions. It allows at most 99,999,999 allocated numbers per scope per day, including unused reservations.

`shard` is a logical bucket from 000 to 999. The application registers event codes and chooses a stable routing policy,
for example customer affinity via a versioned hash. Supply the resolved bucket in the command. For sequence-derived
routing, use the numeric Sequence and formatter together; do not invent a second random bucket or assume 1,000 physical
tables. A shard field does not provide authorization or tenant isolation.

For numeric sequences, inject `Sequence`:

```java
SequenceScope scope = new SequenceScope("payment", LocalDate.now(clock), 99_999_999L);
long number = sequence.next(scope);
long[] batch = sequence.nextBatch(scope, 100);
```

Reuse `SequenceScope` for hot numeric loops; its cache key is precomputed. A null date creates a persistent scope.
Numbers are unique within a scope; different scopes can repeat numbers. Maximum is persisted on first initialization and
cannot subsequently change. `nextBatch` accepts 1..10,000 and may cross noncontiguous segments. Failure returns no
partial batch; consumed values are not reclaimed. Business idempotency must reuse an already-persisted ID.

Each scope has an active range and at most one standby range. Prefetch begins at 20% remaining capacity. Warm issuance
uses atomic range claims without database access; concurrent cold callers share one allocation task. Database
reservations commit independently of business transactions. Transient initialization/deadlock failures retry in fresh
transactions with bounded jitter; failed prefetch preserves the current range. On allocation failure a short cooldown
avoids repeated database attempts. Queue rejection, timeout, exhausted capacity, conflicting maxima and scope admission
failures use `DatabaseResultCodeEnum` through `CommonException`.

Scope admission is bounded. At capacity, past-date entries may be discarded; their unused numbers are lost and
already-running reservations may finish without becoming visible in the new cache entry. Persistent/current-date scopes
remain protected from automatic eviction; additional scopes fail explicitly. Shutdown stops prefetch workers and fails
outstanding waits. Size the pool for independent allocation transactions and distinguish cold/refill latency from warm
issuance latency.

Shared Boot `MeterRegistry`, when present, receives `loadup.database.sequence.issued`, `loadup.database.sequence.refill`
and `loadup.database.sequence.failures` (bounded `reason` enum labels for allocation failures). No raw
scope/date/customer IDs are metric tags. Throughput is deployment-dependent; gaps are permitted and global chronological
ordering is not guaranteed.

Run focused validation using Maven Daemon:

```shell
mvnd clean test -pl loadup-components/data/loadup-components-database -am \
  -Dtest=BusinessIdTest,SegmentSequenceTest,DatabaseSequenceIT \
  -Dsurefire.failIfNoSpecifiedTests=false -Dspotless.skip=true -Dspotbugs.skip=true
```

The IT requires Docker and uses real PostgreSQL through the framework Testcontainers annotation. Consumer
failover/restore rehearsals and P95/P99 capacity measurements are separate production acceptance work.

## 持久化基类

使用本组件的 `io.github.loadup.components.database.dataobject.BaseDO`。它保留 id、createdAt、updatedAt、deleted
字段以及 MyBatis-Flex `@Id(keyType = KeyType.None)` 元数据；标识仍由应用显式提供，迁移不改变 JSON、数据库列或逻辑删除行为。

Deployment identity is separate from database schema selection. The database target has no `DatabaseTenant` or
`TenantContext` request contract; the only schema property is `loadup.database.schema`.

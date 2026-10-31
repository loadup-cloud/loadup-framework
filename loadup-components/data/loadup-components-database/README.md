# LoadUp Components Database

MyBatis-Flex and Spring JDBC support for persistent IDs, PostgreSQL transactions, sequence allocation and
schema-per-tenant isolation.

## Maven

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-database</artifactId>
</dependency>
```

The component uses the shared Spring `Clock` and supplies a JDBC transaction manager for a single data source,
`DatabaseTenant` and sequence/ID configuration. It brings `mybatis-flex-spring-boot4-starter`, registers framework
mappers and configures the shared Flex conventions. Applications configure their own data source and PostgreSQL driver.

MyBatis-Flex is the default ORM for simple aggregate reads. Annotate infrastructure data objects with
`com.mybatisflex.annotation.Table`; the annotation processor generates a `Tables` class, per-entity `TableDef` classes
and a `@Mapper` interface under the module's `infrastructure.table` and `infrastructure.mapper` packages. Gateway
implementations extend `ServiceImpl<Mapper, DO>` (and therefore `IService<DO>`) so standard reads, paging and logical
deletion come from the framework; complex conditional writes remain explicit parameterized SQL.

The component applies the logical-delete convention through a `MyBatisFlexCustomizer`: the `deleted` column is the
logic-delete flag, normal rows use `false`, deleted rows use `true`, and mapper reads, updates, counts and `deleteById`
automatically add or apply that condition. Paging defaults are 20 items per page with a maximum of 100.

Explicit SQL must still set IDs, audit timestamps and `deleted`, and must filter `deleted` rows itself. Business tables
do not carry a `tenant_id` column. Schema-per-tenant is the isolation target, but runtime routing is not implemented:
`TenantFilter` currently binds the configured default tenant and the application serves only the configured default
schema. Do not treat `multi-tenant.enabled` as enabling per-tenant datasource routing. PostgreSQL schemas use boolean
`deleted`.

## Schema-per-tenant isolation

Tenant identity selects the PostgreSQL schema; it is not stored in every row. `DatabaseTenant.currentTenantId()`
resolves the trusted tenant and `DatabaseTenant.currentSchema()` returns the validated schema
(`loadup.database.multi-tenant.default-schema`, default `public`). Schema names are validated against
`[a-z_][a-z0-9_]{0,62}` before they can reach a connection.

```yaml
loadup:
  database:
    multi-tenant:
      enabled: false          # single default tenant in the current phase
      required: true          # fail closed when enabled and context is missing
      default-tenant-id: __default__
      default-schema: public
```

Current runtime uses one default schema. The planned routing layer is an `AbstractRoutingDataSource` whose
`determineCurrentLookupKey()` returns `TenantContext.tenantId()`:

- Schema mode: each tenant maps to a per-tenant `DataSource` pointing at the same database with a different
  `currentSchema`/Hikari `schema` (one pool per tenant, so `search_path` cannot leak between tenants).
- Database mode: the same routing key maps to an independent `DataSource`.
- The tenant must be bound before the transaction starts (`TenantFilter` for web requests, the worker entry point for
  background jobs).
- Until routing is implemented, the application serves the default schema only. Do not deploy multi-tenant traffic
  before fail-closed routing and per-tenant Flyway orchestration are in place.

Background workers (Outbox, DelayedTask, Transfer) run per tenant/schema. Rows no longer store a tenant; a worker scans
only the schema it was started for.

Non-relational resources still need their own tenant namespaces: Redis lock/async-result keys, DFS object paths and RPC
identity continue to use `TenantUtil`.

## Repository query style

Use generated MyBatis-Flex mappers and `QueryWrapper` for simple aggregates (one or two fixed predicates), paging and
logical deletion. Use `JdbcClient` in the infrastructure gateway when a query has three or more predicates, optional
filters, `FOR UPDATE`, `IN` lists, or requires explicit column lists and locking semantics. `JdbcClient` keeps SQL in
the infrastructure layer with named parameters and clear locking/pagination semantics. Current examples:
`LoginLogGatewayImpl` and `InboxGatewayImpl`.

Generated artifacts and their packages are controlled by `mybatis-flex.config`:
`processor.mapper.generateEnable=true`, `processor.mapper.annotation=true`,
`processor.mapper.package=${entityPackage.parent}.mapper`,
`processor.tableDef.package=${entityPackage.parent}.table` and
`processor.allInTables.package=${entityPackage.parent}.table`. Keep a copy of this file next to each module's `pom.xml`;
the processor resolves the project root differently for shallow and nested module layouts.

Consumers that compile their own `@Table` entities add the processor to the compiler plugin:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>com.mybatis-flex</groupId>
                <artifactId>mybatis-flex-processor</artifactId>
                <version>1.11.8</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

Flyway support is optional; consumers using migrations must include Flyway and its PostgreSQL database module. Migration
scripts assume a fresh PostgreSQL schema.

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

Apply `classpath:db/migration/V2026101201__sequence_allocation.sql` before enabling traffic. Use the integrating
application's migration procedure or add `classpath:db/migration` to its existing Flyway locations, retaining all other
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
  -Dsurefire.failIfNoSpecifiedTests=false -Dskip.spotless=true -Dskip.spotbugs=true
```

The IT requires Docker and uses real PostgreSQL through the framework Testcontainers annotation. Consumer
failover/restore rehearsals and P95/P99 capacity measurements are separate production acceptance work.

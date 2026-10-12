# LoadUp Components Database

MyBatis-Flex integration for common persistent fields, audit timestamps, business identifiers, logical deletion, and tenant isolation.

## Maven

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-database</artifactId>
</dependency>
```

This module brings the JDBC stack (`spring-boot-starter-jdbc`, so `DataSourceAutoConfiguration` and a
default Hikari pool); the application still supplies the JDBC driver and the `spring.datasource.*`
properties. This module does not select a database vendor.

Define each data object as `@Table(...) class XxxDO extends BaseDO`. `BaseDO` supplies `id`, `createdAt`, `updatedAt`, `tenantId`, and integer `deleted` (`0` normal, `1` deleted). The database processor generates `XxxDOMapper extends BaseMapper<XxxDO>` with MyBatis `@Mapper`, module-local `Tables` and TableDef sources. Do not declare these Mapper interfaces manually. Generated files remain under `target/`.

## Compile-time generation

Infrastructure modules enable the database-owned processor separately from the runtime dependency. The BOM manages its version. A provided dependency establishes Maven reactor ordering and keeps the processor out of deployment dependencies:

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-database-processor</artifactId>
    <scope>provided</scope>
    <optional>true</optional>
</dependency>
```

Configure `maven-compiler-plugin` annotation processing (external consumers must also include MapStruct/Boot paths if used):

```xml
<configuration>
    <annotationProcessorPaths combine.children="append">
        <path>
            <groupId>com.mybatis-flex</groupId>
            <artifactId>mybatis-flex-processor</artifactId>
            <version>${mybatis.flex.version}</version>
        </path>
        <path>
            <groupId>io.github.loadup-cloud</groupId>
            <artifactId>loadup-components-database-processor</artifactId>
            <version>${loadup.framework.version}</version>
        </path>
    </annotationProcessorPaths>
    <annotationProcessors>
        <annotationProcessor>org.mapstruct.ap.MappingProcessor</annotationProcessor>
        <annotationProcessor>org.springframework.boot.configurationprocessor.ConfigurationMetadataAnnotationProcessor</annotationProcessor>
        <annotationProcessor>io.github.loadup.components.database.processor.LoadUpMyBatisFlexProcessor</annotationProcessor>
    </annotationProcessors>
</configuration>
```

External projects must define the example version properties to match the selected BOM; importing a BOM does not import its Maven properties or plugin configuration.

The explicit processor list avoids running upstream Flex a second time through service discovery. Both Flex paths are explicit so compilation works before the processor artifact is installed into the local repository. Build using `mvn clean ...` to avoid generating the same sources twice from stale output. Import `infrastructure.mapper.XxxDOMapper` and inject it into repositories; scan the generated mapper package with `@MapperScan`. MapStruct remains an independent processor for Spring-managed converters.

Fixed options live in `DatabaseAptConfiguration` Java code. No project `mybatis-flex.config` or application property is needed; ancestor config files cannot change these options. The generated config in compiler output is an upstream implementation bridge, not an application configuration file.

## Configuration

```yaml
loadup:
  database:
    multi-tenant:
      enabled: true
      required: true
      default-tenant-id: __default__ # used when enabled=false
      ignore-tables: [sys_tenant, sys_config]
      request: {header-name: X-Tenant-Id, parameter-name: tenantId}
```

When `enabled=false` (the default), the request filter binds `default-tenant-id` for every HTTP request and the database insert listener writes the same ID into `BaseDO.tenantId`, ignoring request-supplied tenant values. The default ID is `__default__` and can be overridden. When `enabled=true`, the filter uses the configured request source; missing tenant context is rejected when `required=true` rather than falling back to the single-tenant ID.

Use `TenantUtil.runWithTenant(...)` or `callWithTenant(...)` for non-HTTP jobs that need a tenant context. Tenant bindings are read-only within a JDK 25 ScopedValue scope; there is no set/clear API. The request filter saves immutable metadata under `ExecutionContext.class.getName()` and binds it around REQUEST/ASYNC/ERROR dispatch. Header/parameter/subdomain extraction remains a configurable convenience; authenticated tenant authorization must be enforced by the integrating application.

## Capability matrix

| Capability | MyBatis-Flex |
|---|---|
| CRUD, QueryWrapper, generated MyBatis Mapper, Tables and TableDef | ✓ |
| Audit timestamps and default business primary keys | ✓ |
| Business IDs up to 64 characters, formatting and parsing | ✓ |
| Scope-based database segments, local batches and asynchronous prefetch | ✓ |
| Integer logical deletion | ✓ |
| Tenant SQL isolation and request propagation | ✓ |

## Fixed framework conventions

Local-time audit timestamps, `deleted = 0/1` and the `tenant_id` column are framework-owned defaults. These audit/deletion conventions cannot be switched off or renamed through application properties. The ID strategy is selected through a single IdGenerator bean and defaults to BusinessIdGenerator. Extra `MyBatisFlexCustomizer` beans fail startup. DataSource settings, tenant activation and trusted tenant resolution remain deployment concerns. All mapped business tables must contain the five BaseDO fields; apply the module migrations before enabling the adapters.

Use generated `Tables` constants in repositories; do not concatenate SQL or sort expressions. MapStruct converters use the shared Spring configuration and constructor injection. `UpdateEntity.of` expresses partial updates, including intentional null assignments.

## Business IDs and database sequences

`BusinessIdGenerator` is the default `IdGenerator` for MyBatis-Flex and BaseDO insert listeners. Missing IDs automatically receive a business ID; explicit IDs remain unchanged. Sequence is enabled by default. Apply the sequence migration before any insert that relies on automatic ID generation. Allocation metadata explicitly uses UUID to avoid recursively requesting a sequence.

Default automatic metadata uses data version 0, system version 1, event code `00000000`, no extension, site 000 and shard 000. Reserve `00000000` for generic persistence IDs; explicit business generation may use registered event codes. All entities using the same default scope share its eight-digit daily capacity. Configure stable site ownership when independent databases issue IDs.

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

Apply `classpath:db/migration/loadup-sequence/V2026101201__sequence_allocation.sql` before enabling traffic. Use the integrating application's migration procedure or add `classpath:db/migration/loadup-sequence` to its existing Flyway locations, retaining all other module locations. The component does not create tables on the request path. Use one authoritative allocation database for each encoded site, preserve committed allocation maxima during failover/restoration, and restrict deletion/reset of allocation rows. The migration uses MySQL syntax.

Inject `IdGenerator` for the configured automatic strategy. `IdGenerator` also implements MyBatis-Flex's generator contract; `generate(entity, keyColumn)` defaults to `generate()`. Flex registers it under `IdGenerator.KEY` through a lazy provider to avoid a SqlSessionFactory → generator → Sequence → Mapper initialization cycle. No ID is generated while constructing the SqlSessionFactory.

An application may supply one `IdGenerator` bean (for example `UuidV7IdGenerator`) to replace the default; do not replace the global Flex customizer. `loadup.database.sequence.enabled=false` requires such an alternative because the default generator depends on Sequence. Existing explicitly supplied IDs, including UUIDs assigned by application services, are preserved.

Inject `BusinessIdGenerator` and reuse immutable metadata where practical:

```java
BusinessIdCommand command = new BusinessIdCommand(
        0, 1, "11000000", "", 1, 440);
String businessId = generator.generate(command);
String[] businessIds = generator.generateBatch(command, 100);
BusinessIdParts parts = formatter.parse(businessId);
```

Encoding: date(8) + dataVersion(1) + systemVersion(1) + eventCode(8) + extension(0..32) + site(3) + shard(3) + sequence(8). Length is 32..64; numeric fields preserve leading zeros. Date uses `databaseClock` and its local zone, captured once per generation call. Business sequence scope is date + versions + event + site, shared across shards/extensions. It allows at most 99,999,999 allocated numbers per scope per day, including unused reservations.

`shard` is a logical bucket from 000 to 999. The application registers event codes and chooses a stable routing policy, for example customer affinity via a versioned hash. Supply the resolved bucket in the command. For sequence-derived routing, use the numeric Sequence and formatter together; do not invent a second random bucket or assume 1,000 physical tables. A shard field does not provide authorization or tenant isolation.

For numeric sequences, inject `Sequence`:

```java
SequenceScope scope = new SequenceScope("payment", LocalDate.now(clock), 99_999_999L);
long number = sequence.next(scope);
long[] batch = sequence.nextBatch(scope, 100);
```

Reuse `SequenceScope` for hot numeric loops; its cache key is precomputed. A null date creates a persistent scope. Numbers are unique within a scope; different scopes can repeat numbers. Maximum is persisted on first initialization and cannot subsequently change. `nextBatch` accepts 1..10,000 and may cross noncontiguous segments. Failure returns no partial batch; consumed values are not reclaimed. Business idempotency must reuse an already-persisted ID.

Each scope has an active range and at most one standby range. Prefetch begins at 20% remaining capacity. Warm issuance uses atomic range claims without database access; concurrent cold callers share one allocation task. Database reservations commit independently of business transactions. Transient initialization/deadlock failures retry in fresh transactions with bounded jitter; failed prefetch preserves the current range. On allocation failure a short cooldown avoids repeated database attempts. Queue rejection, timeout, exhausted capacity, conflicting maxima and scope admission failures use `DatabaseResultCodeEnum` through `CommonException`.

Scope admission is bounded. At capacity, past-date entries may be discarded; their unused numbers are lost and already-running reservations may finish without becoming visible in the new cache entry. Persistent/current-date scopes remain protected from automatic eviction; additional scopes fail explicitly. Shutdown stops prefetch workers and fails outstanding waits. Size the pool for independent allocation transactions and distinguish cold/refill latency from warm issuance latency.

Shared Boot `MeterRegistry`, when present, receives `loadup.database.sequence.issued`, `loadup.database.sequence.refill` and `loadup.database.sequence.failures` (bounded `reason` enum labels for allocation failures). No raw scope/date/customer IDs are metric tags. Throughput is deployment-dependent; gaps are permitted and global chronological ordering is not guaranteed.

Run focused validation using Maven Daemon:

```shell
mvnd clean test -pl loadup-components/data/loadup-components-database -am \
  -Dtest=BusinessIdTest,SegmentSequenceTest,DatabaseSequenceIT \
  -Dsurefire.failIfNoSpecifiedTests=false -Dskip.spotless=true -Dskip.spotbugs=true
```

The IT requires Docker and uses real MySQL through the framework Testcontainers annotation. Consumer failover/restore rehearsals and P95/P99 capacity measurements are separate production acceptance work.

# Database Component Architecture

## Scope

`loadup-components-database` is the runtime integration for MyBatis-Flex and Spring JDBC. It owns persistence
conventions, generated mapper/table definitions, ID/sequence allocation, Flyway defaults and one fixed schema target.
Applications own datasource selection, JDBC drivers and deployment migrations.

Spring Data JDBC is not part of the component. Simple aggregate reads use generated MyBatis-Flex mappers and
`ServiceImpl`/`IService`; complex queries, locks, conditional writes and batches use parameterized Spring JDBC.

## Runtime flow

```text
DO extends BaseDO
      │
      ├─ generated Mapper / ServiceImpl(IService) ── simple reads, paging, logical delete
      ├─ QueryWrapper ───────────────────────────── fixed predicates and ordering
      ├─ JdbcClient gateway ─────────────────────── complex/paginated/locking SQL
      ├─ IdGenerator ────────────────────────────── application-assigned business IDs
      └─ explicit audit fields ──────────────────── createdAt / updatedAt / deleted
```

`BaseDO` lives in `loadup-components-database` as
`io.github.loadup.components.database.dataobject.BaseDO` and is shared by persistence adapters. Its `id` is marked with
`@Id(keyType = KeyType.None)`, so MyBatis-Flex never generates or replaces an application-assigned identifier. Business
tables do not contain `tenant_id`. A deployment serves one tenant through a single fixed schema; runtime tenant
routing and per-request schema switching are not part of the contract.

## Common fields

| Java field  | Database column | Type           | Behavior                                                                             |
|-------------|-----------------|----------------|--------------------------------------------------------------------------------------|
| `id`        | `id`            | `VARCHAR(64)`  | Assigned explicitly or by `IdGenerator`; Flex key type `None`                        |
| `createdAt` | `created_at`    | `TIMESTAMP(6)` | Set on insert                                                                        |
| `updatedAt` | `updated_at`    | `TIMESTAMP(6)` | Set on insert and every update                                                       |
| `deleted`   | `deleted`       | `BOOLEAN`      | `FALSE` normal, `TRUE` logically deleted; configured through `MyBatisFlexCustomizer` |

Timestamps use the framework clock and the JVM system default time zone.

## Generated sources

`loadup-components-database-processor` delegates generation to the official MyBatis-Flex processor in entity modules. For each `@Table` entity it generates:

- `{entityPackage.parent}.mapper.XxxMapper` annotated with `@Mapper` and extending `BaseMapper<XxxDO>`;
- `{entityPackage.parent}.table.XxxTableDef` and an aggregate `Tables` class with type-safe `QueryColumn` constants.

The database auto-configuration scans `io.github.loadup` for `BaseMapper` subtypes so framework mappers are visible
outside the consuming application's auto-configuration packages. Mapper beans are lazy; a mapper is only materialized
when a gateway injects it and a single `DataSource` exists.

`MyBatisFlexCustomizer` installs the shared conventions on the Flex global configuration: logical-delete column/values,
banner suppression and pagination defaults. Entity classes without a `deleted` column are unaffected.

Because gateways mix mapper reads with explicit JDBC writes in the same transaction, the MyBatis local cache scope is
`STATEMENT`. A session-scoped cache would return stale entities after an explicit JDBC update that MyBatis cannot
observe.

## APT build policy ownership

Runtime customization stays in MyBatisFlexAutoConfiguration. The sibling database-processor module owns the
single configuration resource under src/main/resources/META-INF/loadup/database/. Its official Flex processor
dependency is transitive on the processor path; business POMs declare only the LoadUp database processor for Flex.

LoadUp writes the fixed policy into the current compilation's CLASS_OUTPUT before initializing the upstream
generator. stopBubbling=true prevents ancestor or module-local policies from changing generation. Configuration
load/write failures abort compilation. The same mechanism handles main and test compilation outputs, without
source-tree copies, sync scripts or Maven resource download steps.

The parent compiler configuration explicitly selects LoadUp, MapStruct and Spring Boot metadata processors.
This is necessary because the upstream JAR also registers its own processor through SPI; automatic discovery
would run both generators. The processor module disables annotation processing while compiling itself and never
depends on the runtime database component. Framework entity modules declare it provided/optional as a reactor
dependency, so -am and parallel builds order the processor before its consumers without exporting it at runtime.

## Fixed schema isolation

`loadup.database.schema` is the sole schema target. It configures the Hikari pool's physical schema and Flyway's
`defaultSchema`/`schemas`; there is no `DatabaseTenant`, `TenantContext`, default schema fallback, default tenant ID,
`AbstractRoutingDataSource`, or per-request `SET search_path` contract. Older tenant-routing descriptions in previous versions of this document are obsolete and do not describe current behavior.

An explicitly configured Hikari `schema` and JDBC URL `currentSchema` must match `loadup.database.schema`. Any
non-empty Hikari `connection-init-sql` or JDBC `options` value is rejected, including Hikari driver properties. The
implementation does not parse arbitrary SQL. At startup `FixedSchemaVerifier` checks that `current_schema()` equals the
target and the complete `search_path` contains only that schema, before workers or scanners can start.

```text
application configuration: loadup.database.schema
        │
        ├─ Hikari physical connection schema
        ├─ Flyway defaultSchema / schemas
        └─ startup verifier: current_schema + complete search_path
```

This startup check verifies the configured target; it does not prove cross-schema privilege isolation. Runtime roles
must be denied access to other schemas and kept separate from migration accounts. Those grants and account boundaries
require deployment acceptance against the actual database. Do not treat documentation or startup verification as
proof of that operational isolation.

Non-relational resources use deployment-fixed namespaces: lock and async-result keys use their configured namespace,
DFS object paths use the configured bucket/prefix, and internal service calls use `DeploymentIdentity`.

## Repository query style

Generated MyBatis-Flex mappers and `QueryWrapper` are used for simple aggregates (one or two fixed predicates), paging
and logical deletion. `JdbcClient` is used in infrastructure gateways for complex queries: three or more predicates,
optional filters, `FOR UPDATE`, `IN` lists, or explicit column lists. `JdbcClient` keeps SQL in infrastructure with
named parameters and clear locking/pagination semantics.

Mappers expose tenant-free, single-tenant signatures. Gateways and facades do not accept tenant identifiers. Logical
deletion is applied by the Flex global configuration for mapper operations and must be written explicitly in raw SQL.

## Flyway

Flyway support is optional; consumers using migrations include Flyway and its PostgreSQL module. Migration scripts are
schema-agnostic (no schema-qualified names) and assume a fresh schema. `LoadUpFlywayAutoConfiguration` applies the
same fixed schema configured by `loadup.database.schema` to `defaultSchema` and `schemas`; runtime and migration targets
must remain aligned. Applications must select explicit migration namespaces: their own directory
and approved `db/migration/framework/<module>` locations. The generic classpath migration root also discovers
dependency JAR resources and is not an isolation mechanism. One application retains one Flyway history across
its selected locations; published versioned migrations are immutable.

## ID generation and sequences

`IdGenerator` supplies application-assigned IDs; `BusinessIdGenerator` is the default when Sequence is available.
Sequence persistence uses Spring JDBC and an independent `REQUIRES_NEW` transaction. Allocation rows are per schema and
use a unique `scope_key`; business rollback does not reclaim issued numbers. See README for encoding, capacity and
operational rules.

## Background workers

Task and Transfer workers scan the configured fixed schema through the application's shared data source. A worker row
has no tenant column, and background work runs without HTTP or tenant context.

Deployment identity is separate from database schema selection. The database target has no `DatabaseTenant` or
`TenantContext` request contract; the only schema property is `loadup.database.schema`.

# Database Component Architecture

## Scope

`loadup-components-database` is a single-jar integration for MyBatis-Flex and Spring JDBC. It owns persistence
conventions, generated mapper/table definitions, ID/sequence allocation, Flyway defaults and schema-per-tenant routing
seams. Applications own datasource selection, JDBC drivers and deployment migrations.

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

`BaseDO` lives in `loadup-commons-dto` and is shared by framework modules. Its `id` is marked with
`@Id(keyType = KeyType.None)`, so MyBatis-Flex never generates or replaces an application-assigned identifier. Business
tables do not contain `tenant_id`. Schema-per-tenant is the target isolation boundary, but runtime datasource routing
and per-tenant Flyway orchestration are not implemented; the current application serves the configured default schema.

## Common fields

| Java field  | Database column | Type           | Behavior                                                                             |
|-------------|-----------------|----------------|--------------------------------------------------------------------------------------|
| `id`        | `id`            | `VARCHAR(64)`  | Assigned explicitly or by `IdGenerator`; Flex key type `None`                        |
| `createdAt` | `created_at`    | `TIMESTAMP(6)` | Set on insert                                                                        |
| `updatedAt` | `updated_at`    | `TIMESTAMP(6)` | Set on insert and every update                                                       |
| `deleted`   | `deleted`       | `BOOLEAN`      | `FALSE` normal, `TRUE` logically deleted; configured through `MyBatisFlexCustomizer` |

Timestamps use the framework clock and the JVM system default time zone.

## Generated sources

`mybatis-flex-processor` runs as an annotation processor in every module. For each `@Table` entity it generates:

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

## Schema-per-tenant isolation

Tenant isolation is physical: each tenant owns a PostgreSQL schema. `TenantContext`/`DatabaseTenant` resolve the trusted
tenant and its schema; business code, DTOs, facades, gateways and repositories never accept or expose a tenant
parameter.

```text
request / worker entry
        │
        ▼
TenantFilter / worker bootstrap ── binds TenantContext
        │
        ▼
AbstractRoutingDataSource (planned) ── determineCurrentLookupKey() = TenantContext.tenantId()
        │
        ├─ schema mode: per-tenant DataSource (same DB, different currentSchema)
        └─ database mode: independent DataSource
        │
        ▼
single-tenant repository / explicit SQL (no tenant predicate)
```

Design rules:

- `loadup.database.multi-tenant.default-schema` defaults to `public`; `DatabaseTenant.currentSchema()` validates
  `[a-z_][a-z0-9_]{0,62}` before any connection is selected.
- `enabled=false` always resolves the configured default tenant/schema. `enabled=true, required=true` fails closed when
  the execution context has no tenant.
- The current phase serves only the default schema; routing, tenant registry lookup, per-tenant pools and per-tenant
  Flyway are deliberately deferred.
- Schema mode uses one pool per tenant so pooled `search_path` state cannot leak between tenants. Database mode is an
  alternative target for the same routing key.
- The tenant must be bound before the transaction starts. `TenantFilter` binds the default tenant in the current phase;
  background workers bind their schema at the worker entry point.
- Per-tenant Flyway: run the same schema-agnostic migrations with `defaultSchema`/`schemas` set to the tenant schema. A
  control-plane tenant registry maps tenant codes to schema names; business tables never carry a tenant column.

Non-relational resources are outside schema isolation: Redis lock/async-result keys, DFS object paths and RPC identity
continue to use `TenantUtil`/`MDCUtils`.

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
configured default schema from `loadup.database.multi-tenant.default-schema`; per-tenant execution is a
deployment/routing responsibility.

## ID generation and sequences

`IdGenerator` supplies application-assigned IDs; `BusinessIdGenerator` is the default when Sequence is available.
Sequence persistence uses Spring JDBC and an independent `REQUIRES_NEW` transaction. Allocation rows are per schema and
use a unique `scope_key`; business rollback does not reclaim issued numbers. See README for encoding, capacity and
operational rules.

## Background workers

Outbox, DelayedTask and Transfer workers scan only the schema they run in. A worker row has no tenant column;
tenant/schema binding happens at the worker entry point. Deploy one worker per tenant schema (or a dispatcher that
iterates schemas and switches context) when multiple tenants are active.

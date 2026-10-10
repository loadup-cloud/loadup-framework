# Database Component Architecture

## Scope

The component is a single-jar, thin MyBatis-Flex integration. It owns persistence conventions and auto-configuration; applications own datasource selection, JDBC drivers, table schemas, and repository code.

## Runtime flow

```text
DO extends BaseDO
      │
      ├─ MyBatis-Flex key generator ── random / UUID v4 / UUID v7 / Snowflake
      ├─ BaseEntityListener ─────────── createdAt / updatedAt / tenantId / deleted
      ├─ logic-delete metadata ──────── deleted = 0 / 1
      └─ tenant metadata ────────────── tenant_id predicate + insert value
```

`BaseDO` remains in `loadup-commons-dto` because it is the common persistence contract consumed by components and modules. The database component supplies all MyBatis-Flex behavior around that contract.

## Common fields

| Java field | Database column | Type | Behavior |
|---|---|---|---|
| `id` | `id` | `VARCHAR(64)` | Generated only when blank |
| `tenantId` | `tenant_id` | `VARCHAR(64)` | Filled and filtered when tenancy is enabled |
| `createdAt` | `created_at` | `DATETIME` | Filled once on insert |
| `updatedAt` | `updated_at` | `DATETIME` | Filled on insert and every entity update |
| `deleted` | `deleted` | `TINYINT` | `0` normal, `1` deleted |

Timestamps use a UTC `Clock` bean. Applications can replace the bean for deterministic tests or another time source.

## ID generation

`DatabaseIdGenerator` implements both the LoadUp `IdGenerator` contract and MyBatis-Flex `IKeyGenerator`. Auto-configuration registers it under `loadupId` and sets it as the global strategy only when automatic generation is enabled. An explicit entity `@Id` strategy still has higher priority.

- `random`: compact alphanumeric value, 1–64 characters.
- `uuid-v4`: random RFC 4122 UUID.
- `uuid-v7`: timestamp-ordered RFC 9562 UUID, suitable for B-tree indexes.
- `snowflake`: decimal 64-bit ID using 5-bit datacenter and worker identifiers.

## Logical deletion

When enabled, the configured column and integer values are applied to Flex global metadata. Delete operations become updates and normal queries add the normal-value predicate. Physical maintenance operations can use `LogicDeleteManager.execWithoutLogicDelete` explicitly.

## Multi-tenancy

The Flex tenant factory receives the current table name, so ignored tables return no tenant predicate. When multi-tenancy is enabled, all other tables resolve the tenant from `TenantUtil`. With `required=true`, missing context fails closed rather than issuing an unscoped query. The single-tenant default is not used as a fallback in this mode.

The servlet filter always propagates request context. With multi-tenancy disabled, it binds the configured `default-tenant-id` and ignores headers, parameters and subdomains. The insert listener also assigns this ID to every `BaseDO`. With multi-tenancy enabled, header lookup is enabled by default; query-parameter lookup is enabled by configuring a name, and subdomain lookup is opt-in. Authentication and authorization layers remain responsible for validating that the caller may act as the supplied tenant.

`TenantUtil` reads immutable ScopedValue bindings. Executor integrations use LoadUpContextTaskDecorator or ContextHolder.wrap; runWithTenant/callWithTenant establish callback scopes and automatically restore previous bindings.

## Code generation

The repository root `mybatis-flex.config` is the single source for annotation processing. It generates uppercase TableDef properties, one module-local `Tables` class, and mapper interfaces annotated with `@Mapper`. Generated sources stay under `target/generated-sources/annotations` and must not be committed.

## Schema contract

Every business table uses the five standard fields shown in `schema.sql`. Tenant-enabled query patterns should normally add a composite index beginning with `tenant_id` and `deleted`; business-selective columns can follow them.

## 租户绑定生命周期

TenantUtil 基于 commons-context 的不可变 ExecutionContext 与 JDK 25 ScopedValue。TenantFilter 显式派生租户上下文、写入同名请求属性并包裹 FilterChain；REQUEST/ASYNC/ERROR 分派受支持，异常/结束由 JDK 恢复此前绑定，不再 set/clear。单租户模式固定使用 `default-tenant-id`；多租户模式解析请求来源，客户端租户提取配置不替代认证授权，集成方必须验证可访问租户。

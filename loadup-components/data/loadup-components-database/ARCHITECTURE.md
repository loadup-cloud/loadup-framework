# Database Component Architecture

## Scope

The component is a single-jar, thin MyBatis-Flex integration. It owns persistence conventions and auto-configuration; applications own datasource selection, JDBC drivers, table schemas, and repository code.

## Runtime flow

```text
DO extends BaseDO
      │
      ├─ MyBatis-Flex key generator ── fixed UUID v4
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

Timestamps use the framework UTC clock. Unit tests may construct a listener with a deterministic clock.

## ID generation

`DatabaseIdGenerator` implements LoadUp and MyBatis-Flex generator contracts with UUID v4. Missing identifiers are filled before insertion; explicit business identifiers are preserved.

## Logical deletion and ownership

The framework always configures `deleted`, normal value `0` and deleted value `1`. Physical lifecycle operations such as dictionary removal or reference unlinking explicitly use `LogicDeleteManager.execWithoutLogicDelete`; default repository queries retain the normal-row filter.

A bean-definition guard rejects additional MyBatisFlexCustomizer beans before singleton initialization. Fixed defaults are applied after Boot property binding. This protects normal Spring integration; MyBatis-Flex exposes mutable static APIs, so direct calls to mutate globals remain prohibited rather than technically immutable. Applications own deployment connection settings and trusted tenant resolution, not audit listeners or global CRUD conventions.

## Multi-tenancy

The Flex tenant factory receives the current table name, so ignored tables return no tenant predicate. When multi-tenancy is enabled, all other tables resolve the tenant from `TenantUtil`. With `required=true`, missing context fails closed rather than issuing an unscoped query. The single-tenant default is not used as a fallback in this mode.

The servlet filter always propagates request context. With multi-tenancy disabled, it binds the configured `default-tenant-id` and ignores headers, parameters and subdomains. The insert listener also assigns this ID to every `BaseDO`. With multi-tenancy enabled, header lookup is enabled by default; query-parameter lookup is enabled by configuring a name, and subdomain lookup is opt-in. Authentication and authorization layers remain responsible for validating that the caller may act as the supplied tenant.

`TenantUtil` reads immutable ScopedValue bindings. Executor integrations use LoadUpContextTaskDecorator or ContextHolder.wrap; runWithTenant/callWithTenant establish callback scopes and automatically restore previous bindings.

## Code generation

The repository root `mybatis-flex.config` is the single source for annotation processing. It generates uppercase TableDef properties, one module-local `Tables` class, while Mapper interfaces are explicitly declared. Generated sources stay under `target/generated-sources/annotations` and must not be committed.

## Schema contract

Every business table uses the five standard fields shown in `schema.sql`. Tenant-enabled query patterns should normally add a composite index beginning with `tenant_id` and `deleted`; business-selective columns can follow them.

## 租户绑定生命周期

TenantUtil 基于 commons-context 的不可变 ExecutionContext 与 JDK 25 ScopedValue。TenantFilter 显式派生租户上下文、写入同名请求属性并包裹 FilterChain；REQUEST/ASYNC/ERROR 分派受支持，异常/结束由 JDK 恢复此前绑定，不再 set/clear。单租户模式固定使用 `default-tenant-id`；多租户模式解析请求来源，客户端租户提取配置不替代认证授权，集成方必须验证可访问租户。

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

Timestamps use the framework clock using the JVM system default time zone. Unit tests may construct a listener with a deterministic clock.

## ID generation

`DatabaseIdGenerator` implements LoadUp and MyBatis-Flex generator contracts with UUID v4. Missing identifiers are filled before insertion; explicit business identifiers are preserved.

## Business IDs and database sequences

Implemented 2026-10-12 using MyBatis-Flex and independent Spring transactions. The automatic primary-key generator remains UUID v4. Sequence auto-configuration is opt-in via loadup.database.sequence.enabled; deployment migrations and site/routing policies remain integration responsibilities.

### Existing implementations

| Candidate | Relevant capability | Integration assessment |
|---|---|---|
| [Meituan Leaf](https://github.com/Meituan-Dianping/Leaf) | Apache-2.0; database segment allocation by business tag; separate core and server modules | Preferred reference and first reuse candidate. Evaluate the segment core independently of the server and Snowflake path. |
| [Didi TinyId](https://github.com/didi/tinyid) | Apache-2.0; ID service and Java client | Repository archived on 2026-04-15; an adoption requires accepting maintenance responsibility and its service deployment model. |
| [ShardingSphere key generators](https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/keygen/) | Built-in UUID/Snowflake and custom generator SPI | Suitable when ShardingSphere is already required; introducing it solely for database sequences adds unnecessary infrastructure. |
| [MySQL sequence counter example](https://dev.mysql.com/doc/refman/8.4/en/information-functions.html) | Atomic counter update with connection-local LAST_INSERT_ID | A database primitive rather than an SDK; connection affinity and transaction ownership must be explicit. |

Leaf is not assumed to be a verified Java 25 / Spring Boot 4 dependency. Its [core POM](https://github.com/Meituan-Dianping/Leaf/blob/master/leaf-core/pom.xml) declares legacy dependency coordinates, including the old MySQL connector, and exposes MyBatis and other dependencies. Its [segment implementation](https://github.com/Meituan-Dianping/Leaf/blob/master/leaf-core/src/main/java/com/sankuai/inf/leaf/segment/SegmentIDGenImpl.java) owns background refresh and buffer lifecycles that require extra consideration for date-scoped sequences. TinyId's Java client obtains ranges from a separate HTTP server rather than directly using a local DataSource; its [server POM](https://github.com/didi/tinyid/blob/master/tinyid-server/pom.xml) uses Boot 1.5.9. Based on these constraints, the recommended implementation is a small MyBatis-Flex segment allocator using the established segment approach; direct Leaf core reuse remains an option if dependency and lifecycle evaluation proves simpler. Any copied upstream source must retain Apache-2.0 attribution and notices. No standalone ID service is required for the first embedded implementation.

### Responsibility and public contracts

- `Sequence.next(scope)` supplies a positive numeric value unique within a documented scope. `nextBatch(scope, count)` returns a bounded primitive `long[]` of unique numbers from local committed ranges, with a maximum batch count of 10,000. A batch may cross ranges and need not be numerically contiguous; a failed call returns no partial batch and may consume numbers. The internal allocator reserves committed database ranges independently of the public batch count. A scope contains a registered name and optional business date. Different scopes may issue the same number.
- `BusinessIdGenerator.generate(command)` owns validation, local-date selection, sequence scope, routing buckets and formatting. Callers supply business metadata, not a raw sequence value.
- A pure formatter/parser supports fixed vectors and decoding, but does not promise uniqueness when callers supply arbitrary numbers.
- Existing entities without event/customer metadata continue to use the generic ID generator. Business numbers may be explicitly assigned as IDs where the schema and lifecycle permit, or stored in a separately indexed business-number column. A global replacement requires a separate entity-to-event policy.

### Encoding contract

```text
date(8) + dataVersion(1) + systemVersion(1) + eventCode(8)
        + extension(0..32) + site(3) + shard(3) + sequence(8)
```

The fixed portion contains 32 characters. A 32-character extension therefore produces at most 64 characters and fits the existing VARCHAR(64) schema contract. Customer and random buckets are replaced by one three-digit logical shard field. No primary-key schema expansion is required; never truncate an identifier.

| Field | Validation and meaning |
|---|---|
| Date | Eight ASCII digits, calendar date from an injected Clock; default JVM local zone. Capture once for formatting and allocation. Use a common deployment zone across replicas. |
| Versions | One ASCII decimal digit each, 0..9; changes define an explicit format/routing contract. |
| Event code | Exactly eight ASCII digits; centrally registered and preserved as a string to retain leading zeros. |
| Extension | Optional bounded ASCII alphanumeric value; exclude credentials and sensitive user data. Fixed tail length is 14, so variable extension boundaries remain parsable. |
| Site | Three ASCII digits, 000..999; stable deployment identity, not pod IP or a random startup value. Independent allocation databases must use distinct sites or share one allocator. |
| Shard | 000..999. Use a specified deterministic hash of the customer identifier when customer affinity is required; an event with no such requirement may derive a bucket from its sequence modulo 1,000 to distribute writes. Select and register one policy per event/version, preserve it across replicas and retries, and never encode the raw customer identifier. This is a logical routing hint, not a uniqueness source or a requirement to create 1,000 physical tables. Changing the physical bucket mapping requires a separate routing/migration contract. |
| Sequence | Zero-padded eight digits, 1..99,999,999. Exhaustion fails explicitly; truncation, modulo wrap and silent fallback are forbidden. |

The business scope is `(date, dataVersion, systemVersion, eventCode, site)`, shared across shards and extensions. This makes the limit 99,999,999 allocated numbers per scope per day, including unused reservations. A namespace dimension that resets sequences independently must be encoded in the ID or share an allocation scope; an invisible tenant namespace can otherwise produce duplicate IDs. Business retries reuse the persisted ID through the application's idempotency contract. The generic Sequence is unique within its own scope; the complete encoded business ID is unique across these encoded scopes.

### Allocation and persistence

The allocation table includes the standard BaseDO fields plus a unique `scope_key`, `max_allocated` and immutable `max_value`. Segment step is a deployment setting, not persisted per scope. Sequence state is shared infrastructure: its repository must deliberately bypass business-tenant filtering and forbid logical deletion/reset that could recreate an already-used scope. A CHECK constraint and allocator validation reject logically deleted/corrupt rows; deployment permissions must restrict physical deletion and manual counter reset.

1. Resolve the scope, then obtain a row lock in a short independent `REQUIRES_NEW` transaction using a dedicated allocator bean/transaction template.
2. Reserve a bounded range, update the maximum, and commit before publishing the range to any caller. Concurrent first-row creation uses a unique constraint; retry an initialization conflict in a fresh transaction.
3. Serve the committed range with an atomic in-memory cursor. Provide an active range and at most one standby range per scope. Trigger asynchronous prefetch when active capacity drops below 20%, with a single in-flight refill per scope. Publish the standby range only after its database transaction commits. A CAS-based range claim must respect the inclusive upper bound before returning any number.
4. Cap the final range at the configured maximum. Reject exhaustion with a database ResultCode; never issue a value beyond the eight-digit space.
5. Create a new scope for a new date. Never reset an old row. Old dates revisited after clock changes continue from their persisted maximum, subject to bounded cache admission.

Repositories use MyBatis-Flex BaseMapper, generated Tables and QueryWrapper. No JdbcTemplate or handwritten SQL is introduced. The allocation table's operational metadata ID must use the generic generator to avoid sequence recursion. A REQUIRES_NEW allocator needs connection-pool headroom when called inside existing business transactions.

The guarantee is scope uniqueness, with gaps permitted. Business rollback and process death do not reclaim issued or reserved numbers. Parallel segments do not provide global chronological ordering or gapless accounting numbers. Cached ranges remain usable during a database outage until exhausted; then allocation fails explicitly. Cache eviction, restarts and unused reservations also consume capacity. At scope admission capacity, past-date states are removed; old references only consume their previously committed ranges and cannot be republished into the new state. If no past-date entry is available, admission fails. Bound active scopes and observe waste before choosing the default segment size.

### Throughput and batch allocation

Initial defaults are a 10,000-number segment, 20% remaining-capacity prefetch trigger, and two committed ranges per active scope at most. Step size is a deployment capacity setting, not an ID-format field. Reduce it for low-volume scopes to avoid wasting the eight-digit space; evaluate increases using observed refill latency, throughput and restart waste. Automatic adaptive sizing is deferred until measurements justify it.

Steady-state `next` performs a scope-cache lookup and an atomic cursor claim, without database access or a process-wide lock. `nextBatch` claims multiple local numbers using a bounded CAS operation per range rather than repeatedly calling `next`; crossing a range uses another committed range. Public batches are bounded to protect memory. Neither issuance nor range transitions may reuse numbers or race a cache eviction.

Cold-start requests share one initialization task per scope. Prefetch uses a component-owned bounded executor, bounded queue and explicit shutdown. If no committed capacity remains, callers await a shared refill only up to a configured deadline; queue rejection and database failures are reported with stable database ResultCodes. A failed prefetch does not invalidate the remaining active range. A 250ms failure cooldown bounds repeated allocation attempts. Capacity exhaustion is terminal for that scope and must not create a retry storm. First-call cold start and refill latency are measured separately from the warm issuance path.

Scope caching must have a bounded active-scope count, lifecycle-safe eviction and expiry appropriate to the date dimension. In-flight reservations and old cursors cannot be republished after eviction. Registered hot scopes may be warmed before traffic; do not scan every possible event/site combination or allocate new-day ranges for inactive scopes. Caller cancellation after commit wastes a range rather than reclaiming it.

As an illustrative calculation, one instance issuing 100,000 numbers/second with a step of 10,000 needs about ten database range transactions/second in steady state. This is arithmetic, not a benchmark or promised throughput. Different scopes lock different rows; instances sharing one hot scope still serialize range reservations at the database. Performance acceptance measures single hot-scope contention, multiple scopes, multiple JVMs, virtual threads, batch sizes, P95/P99 latency, refill latency and database transaction rate in the actual deployment. Database capacity and the eight-digit daily limit remain independent constraints.

Database failover and backup restoration must preserve committed allocation maxima. Restoring an older allocation table can reissue numbers even if the allocator itself is correct; recovery requires an established high-water mark or a new encoded site/version. Unique business-number constraints provide a final safeguard.

### Verification and remaining acceptance

The implementation uses generated `SequenceAllocationDOMapper` and Tables from the framework processor. The MySQL migration is isolated under `db/migration/loadup-sequence`; deployments explicitly include that location or apply the migration externally. The component deliberately retains UUID allocation-row primary keys to prevent recursion. Trusted tenant binding and scoped MyBatis-Flex tenant/logical-delete bypass keep allocation shared without changing the caller's identity. A deleted/corrupt allocation row fails explicitly.

Unit tests cover fixed encoding vectors, maximum length, leading zeros, invalid dates and values, local-zone batch dates, virtual-thread single/batch concurrency, restart waste, range boundaries, cache admission, timeouts, prefetch failure and metrics. Real MySQL tests cover concurrent first creation, committed disjoint ranges, tenant independence, caller rollback, final-range truncation and maximum conflicts. Leaf dependencies were not introduced; source-compatible upstream reuse was not required.

2026-10-12 verification passed 36 focused tests: 16 new unit tests, four real MySQL ITs and 16 existing database regressions. The final monotonic-clock cooldown adjustment was followed by another passing run of the 16 new unit tests. License and Spotless checks passed. This is correctness evidence, not a production throughput benchmark.

Consumer acceptance still requires representative multi-JVM P95/P99 and capacity measurements, executor/pool sizing, event registry and routing policy integration, site ownership, and database failover/restore rehearsals. Eight-digit scope capacity includes prefetch/restart waste and is separate from throughput.

## Logical deletion and ownership

The framework always configures `deleted`, normal value `0` and deleted value `1`. Physical lifecycle operations such as dictionary removal or reference unlinking explicitly use `LogicDeleteManager.execWithoutLogicDelete`; default repository queries retain the normal-row filter.

A bean-definition guard rejects additional MyBatisFlexCustomizer beans before singleton initialization. Fixed defaults are applied after Boot property binding. This protects normal Spring integration; MyBatis-Flex exposes mutable static APIs, so direct calls to mutate globals remain prohibited rather than technically immutable. Applications own deployment connection settings and trusted tenant resolution, not audit listeners or global CRUD conventions.

## Multi-tenancy

The Flex tenant factory receives the current table name, so ignored tables return no tenant predicate. When multi-tenancy is enabled, all other tables resolve the tenant from `TenantUtil`. With `required=true`, missing context fails closed rather than issuing an unscoped query. The single-tenant default is not used as a fallback in this mode.

The servlet filter always propagates request context. With multi-tenancy disabled, it binds the configured `default-tenant-id` and ignores headers, parameters and subdomains. The insert listener also assigns this ID to every `BaseDO`. With multi-tenancy enabled, header lookup is enabled by default; query-parameter lookup is enabled by configuring a name, and subdomain lookup is opt-in. Authentication and authorization layers remain responsible for validating that the caller may act as the supplied tenant.

`TenantUtil` reads immutable ScopedValue bindings. Executor integrations use LoadUpContextTaskDecorator or ContextHolder.wrap; runWithTenant/callWithTenant establish callback scopes and automatically restore previous bindings.

## Code generation

Generation belongs to the database component's `loadup-components-database-processor` build-time jar. `DatabaseAptConfiguration` fixes the options in Java; `LoadUpMyBatisFlexProcessor` delegates source generation to the upstream MyBatis-Flex processor. It generates uppercase TableDef properties, module-local `Tables`, and `XxxDOMapper extends BaseMapper<XxxDO>` annotated with MyBatis `@Mapper`. DOs in `.dataobject` produce interfaces in the sibling `.mapper` package. Generated sources stay under `target/generated-sources/annotations` and must not be committed.

Upstream 1.11.8 reads generation configuration only from files. Before initializing the delegate, the wrapper writes its fixed options into `CLASS_OUTPUT/mybatis-flex.config` and enables `stopBubbling`; parent configuration is ignored. This bridge avoids reflection, copied upstream generator code and runtime Spring configuration for a compile-time concern. The compile test verifies that disabling generation or changing packages in a parent config cannot override the defaults.

The processor jar is an independent reactor module beneath database; the runtime database artifact remains a jar. Infrastructure modules declare it as optional/provided and explicitly select the wrapper alongside MapStruct and Boot processors. The upstream processor path supplies the delegate but is not selected independently, preventing duplicate generation. The wrapper is not a dependency of the runtime database artifact. Empty hand-written persistence Mapper interfaces are removed; MapStruct converter contracts remain unchanged.

## Schema contract

Every business table uses the five standard fields shown in `schema.sql`. Tenant-enabled query patterns should normally add a composite index beginning with `tenant_id` and `deleted`; business-selective columns can follow them.

## 租户绑定生命周期

TenantUtil 基于 commons-context 的不可变 ExecutionContext 与 JDK 25 ScopedValue。TenantFilter 显式派生租户上下文、写入同名请求属性并包裹 FilterChain；REQUEST/ASYNC/ERROR 分派受支持，异常/结束由 JDK 恢复此前绑定，不再 set/clear。单租户模式固定使用 `default-tenant-id`；多租户模式解析请求来源，客户端租户提取配置不替代认证授权，集成方必须验证可访问租户。

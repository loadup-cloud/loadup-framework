# Global Unique Component Architecture

## Scope

The component is a single-jar idempotency primitive. It owns claim semantics and the
`global_unique` schema; the database component provides IDs, a clock and tenant configuration.

## Runtime flow

```text
business transaction
      │
      └─ GlobalUniqueTemplate.claim
              │
              ├─ allocate ID and populate audit fields
              ├─ insert with ON CONFLICT (biz_type, unique_key) DO NOTHING
              └─ PostgreSQL unique index decides first claim or replay
```

Only a conflict on the specified business key is treated as a replay. Other constraint failures
propagate to the caller. An inactive row that still owns the key is an integrity error.

## Uniqueness contract

The current database constraint is:

```text
UNIQUE (biz_type, unique_key)
```

The API retains a tenant-scoped identity contract, but the persisted row and unique index contain no
tenant column. Runtime datasource routing is not implemented and the application currently serves
only the default schema. Therefore the current database behavior is unique within that schema, not
isolated per tenant. Do not enable multi-tenant operation until the routing and migration lifecycle
described by the [database component](../../data/loadup-components-database/README.md) is implemented.

`bizType` and `uniqueKey` are trimmed and required. Their maximum lengths are 64 and 255
characters. `bizId` and `requestData` are diagnostic data and do not participate in uniqueness.

## Transaction boundary

The template does not start a transaction. Callers that need claim rollback after business failure
must invoke `claim` inside the same Spring transaction as their business writes. A claim made
without an outer transaction commits as a standalone idempotency record.

## Persistence

Spring JDBC uses the Spring-managed connection and an explicit soft-delete predicate. The PostgreSQL
initial migration contains the standard columns, including a Boolean deletion flag; it does not
store or filter a `tenant_id` column.

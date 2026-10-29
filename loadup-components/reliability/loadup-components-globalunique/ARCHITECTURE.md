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
              ├─ resolve tenant scope
              ├─ allocate ID and populate audit fields
              ├─ insert with ON CONFLICT (tenant_id, biz_type, unique_key) DO NOTHING
              └─ PostgreSQL unique index decides first claim or replay
```

Only a conflict on the specified business key is treated as a replay. Other constraint failures
propagate to the caller. An inactive row that still owns the key is an integrity error.

## Uniqueness contract

The database constraint is:

```text
UNIQUE (tenant_id, biz_type, unique_key)
```

Tenant-enabled applications use the tenant bound in `TenantUtil`. When tenant support is disabled,
the database component's `default-tenant-id` scopes claims. When tenant support is enabled but
context is optional and absent, the reserved `__loadup_global__` scope keeps the non-null unique
constraint deterministic.

`bizType` and `uniqueKey` are trimmed and required. Their maximum lengths are 64 and 255
characters. `bizId` and `requestData` are diagnostic data and do not participate in uniqueness.

## Transaction boundary

The template does not start a transaction. Callers that need claim rollback after business failure
must invoke `claim` inside the same Spring transaction as their business writes. A claim made
without an outer transaction commits as a standalone idempotency record.

## Persistence

Spring JDBC uses the Spring-managed connection and explicit tenant and soft-delete predicates.
The PostgreSQL initial migration contains the standard columns, including a Boolean deletion flag.

# Global Unique Component Architecture

## Scope

The component is a single-jar idempotency primitive. It owns claim semantics and the
`global_unique` schema; the database component owns MyBatis-Flex, IDs, audit fields, logical
deletion, and tenant context propagation.

## Runtime flow

```text
business transaction
      │
      └─ GlobalUniqueTemplate.claim
              │
              ├─ resolve tenant scope
              ├─ GlobalUniqueMapper.insert(GlobalUniqueDO)
              ├─ database listener fills id / timestamps / tenant / deleted
              └─ MySQL unique index decides first claim or replay
```

`DuplicateKeyException` is the only duplicate signal consumed by the template. SQL error messages
are never parsed. Other persistence errors propagate to the caller.

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

`GlobalUniqueDO extends BaseDO`; its mapper only extends MyBatis-Flex `BaseMapper`. Query code uses
the generated `GlobalUniqueDOTableDef`, and generated sources remain under `target/`. The migration
targets the project database baseline, MySQL 8, and contains the five standard columns.

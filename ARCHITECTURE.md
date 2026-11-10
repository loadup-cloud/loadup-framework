# LoadUp Framework Architecture

This repository publishes reusable framework modules for applications that consume the
`loadup-framework-bom` BOM. The default root Maven reactor aggregates the BOM, commons,
and technical components. Reusable business modules and Testify live in sibling repositories. It does not contain a runnable
application. The `loadup-coverage` report aggregator is included only by the optional
`component-coverage` profile.

## Dependency direction

```text
loadup-framework-bom (BOM)
          ↑
    loadup-commons
          ↑
   loadup-components
          ↑
     loadup-modules
          ↑
  consuming application

loadup-testify is test support for consuming applications and framework module tests.
```

Commons contains shared context, data contracts, errors, JSON, logging, masking, and utilities.
Components provide technical capabilities; category directories organize source but do not add
Maven coordinates. Providers with interchangeable implementations are separated into API and
binder artifacts. Business modules expose client contracts and can provide optional `*-web`
Spring MVC adapters. Their application services do not depend on HTTP clients or web response
types. Module-specific extension points and configuration are documented in that module's
`README.md` and `ARCHITECTURE.md`.

## Persistence and tenant boundary

Small business modules publish `client`, `core`, and optional `web` artifacts; `core` keeps the
`domain`, `app`, and `infrastructure` package boundaries in one JAR. Modules that need independent
layer publication or separate application integration (UPMS authserver) keep the full
client/domain/infrastructure/app/web/test split. Persistence models and mappers belong in
infrastructure; domain objects remain free of Spring
and ORM annotations. The database component uses MyBatis-Flex generated mappers for simple
queries and logical deletion, with explicit `JdbcClient` SQL for complex filters, locking, and
bulk operations.

Relational tenant isolation is designed around PostgreSQL schemas. Business tables do not carry
a `tenant_id` column. `DatabaseTenant` validates the schema derived from the current tenant and
defaults to the configured `public` schema. At present, `TenantFilter` binds only the configured
default tenant, and runtime data-source routing plus per-tenant Flyway orchestration are not
implemented. The application therefore serves the default schema only; the schema-per-tenant
model must not be treated as active multi-tenant request routing. Redis keys, DFS paths, and RPC
identity still require their own tenant namespaces.

See the [database component README](loadup-components/data/loadup-components-database/README.md)
for current configuration and persistence details.

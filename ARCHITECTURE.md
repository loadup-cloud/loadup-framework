# LoadUp Framework Architecture

## Static architecture baseline

The 2026-11-15 [phase 0 baseline](../loadup-pay/design/PHASE0_BASELINE.md) records current declared dependencies, ownership and assembly exceptions; [all source POM declarations](../loadup-pay/design/PHASE0_DEPENDENCIES.md) distinguish compile, runtime, test, provided and optional. This is a static audit, not build or runtime acceptance. Stage 1B now separates Web context binding from database parsing at source level: WebMVC owns DeploymentScopeFilter, runtime assembles DeploymentDataScope, and database no longer declares a Web starter. Builds and tests have not run.

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
Spring MVC adapters. Business Facades and domain contracts are kept separate from HTTP response envelopes.
There is a current adapter-placement exception: UPMS app's GitHubOAuthProvider directly owns RestTemplate token/userinfo
calls. This is recorded as E15 in the phase 0 baseline, not presented as an already isolated infrastructure adapter.
Module-specific extension points and configuration are documented in that module's
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
a `tenant_id` column. `DatabaseTenant` resolves and validates the schema name. With multi-tenancy disabled it returns
the configured default tenant and schema (normally `public`). When enabled, a nonblank context tenant is validated
directly as the schema string; it is not specially remapped to the default schema. This helper does not route
connections: WebMVC `DeploymentScopeFilter` binds only the configured deployment tenant, and the application
data source and Flyway setup use the configured default schema. Per-tenant data-source routing and
Flyway orchestration are not implemented, so schema-per-tenant is not active request isolation.
Redis keys, DFS paths, and RPC identity still require their own tenant namespaces.

See the [database component README](loadup-components/data/loadup-components-database/README.md)
for current configuration and persistence details.

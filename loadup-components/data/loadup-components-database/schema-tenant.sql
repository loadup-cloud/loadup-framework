-- Control-plane tenant registry for schema-per-tenant isolation.
-- Keep this table in a shared control schema, never inside a tenant schema.
-- Business tables do not carry tenant columns; schema_name is the routing target.

CREATE TABLE IF NOT EXISTS loadup_tenant_registry
(
    id          VARCHAR(64) COLLATE "C"        NOT NULL PRIMARY KEY,
    tenant_code VARCHAR(64) COLLATE "C"        NOT NULL,
    schema_name VARCHAR(63) COLLATE "C"        NOT NULL,
    status      SMALLINT                       NOT NULL DEFAULT 1,
    created_at  TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    deleted     BOOLEAN                        NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_loadup_tenant_schema_name CHECK (schema_name ~ '^[a-z_][a-z0-9_]{0,62}$')
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_loadup_tenant_code ON loadup_tenant_registry (tenant_code) WHERE deleted = FALSE;
CREATE UNIQUE INDEX IF NOT EXISTS uk_loadup_tenant_schema ON loadup_tenant_registry (schema_name) WHERE deleted = FALSE;

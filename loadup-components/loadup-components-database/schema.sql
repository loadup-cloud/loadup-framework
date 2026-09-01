-- Reference schema for a tenant-aware business table.
-- The component does not run this file automatically; applications own migrations.

CREATE TABLE app_example
(
    id         VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id  VARCHAR(64)  NULL,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME     NOT NULL,
    deleted    TINYINT      NOT NULL DEFAULT 0,
    name       VARCHAR(128) NOT NULL,
    KEY idx_app_example_tenant_deleted (tenant_id, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

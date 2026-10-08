-- Reference schema for the tenant registry.
-- Add this table to an application migration when tenant administration is required.

CREATE TABLE sys_tenant
(
    id         VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id  VARCHAR(64)  NULL,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME     NOT NULL,
    deleted    TINYINT      NOT NULL DEFAULT 0,
    tenant_code VARCHAR(50)  NOT NULL UNIQUE,
    tenant_name VARCHAR(100) NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    KEY idx_sys_tenant_deleted (deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

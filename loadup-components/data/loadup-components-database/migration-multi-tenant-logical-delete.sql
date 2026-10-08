-- Reference migration for an existing application table.
-- Replace app_example with the reviewed target table before execution.

ALTER TABLE app_example
    ADD COLUMN tenant_id VARCHAR(64) NULL,
    ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0;

CREATE INDEX idx_app_example_tenant_deleted
    ON app_example (tenant_id, deleted);

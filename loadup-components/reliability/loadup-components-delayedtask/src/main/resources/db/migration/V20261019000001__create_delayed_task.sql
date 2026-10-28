-- PostgreSQL initial schema.

CREATE TABLE delayed_task (
    id VARCHAR(64) COLLATE "C" NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(64) COLLATE "C" NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    task_type VARCHAR(64) COLLATE "C" NOT NULL,
    biz_id VARCHAR(128) COLLATE "C" NOT NULL,
    execute_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    next_execute_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    status VARCHAR(16) COLLATE "C" NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL,
    lease_until TIMESTAMP(6) WITHOUT TIME ZONE,
    claim_token VARCHAR(64) COLLATE "C",
    last_error VARCHAR(256) COLLATE "C",
    CHECK (deleted IN (0, 1))
);

CREATE UNIQUE INDEX uk_delayed_business ON delayed_task (tenant_id, task_type, biz_id);

CREATE INDEX idx_delayed_due ON delayed_task (status, next_execute_at, id);

CREATE INDEX idx_delayed_lease ON delayed_task (status, lease_until, id);

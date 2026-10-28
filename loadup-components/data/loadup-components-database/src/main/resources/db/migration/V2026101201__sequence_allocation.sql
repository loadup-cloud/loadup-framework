-- PostgreSQL initial schema.

CREATE TABLE loadup_sequence_allocation (
    id VARCHAR(64) COLLATE "C" NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(64) COLLATE "C",
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    scope_key VARCHAR(160) COLLATE "C" NOT NULL,
    max_allocated BIGINT NOT NULL,
    max_value BIGINT NOT NULL,
    CONSTRAINT chk_loadup_sequence_bounds CHECK (max_allocated >= 0 AND max_allocated <= max_value AND max_value > 0),
    CONSTRAINT chk_loadup_sequence_deleted CHECK (deleted = 0),
    CHECK (deleted IN (0, 1))
);

CREATE UNIQUE INDEX uk_loadup_sequence_scope ON loadup_sequence_allocation (scope_key);

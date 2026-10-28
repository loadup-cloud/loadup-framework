-- PostgreSQL initial schema.

CREATE TABLE global_unique (
    id           VARCHAR(64) COLLATE "C"  NOT NULL,
    tenant_id    VARCHAR(64) COLLATE "C"  NOT NULL,
    unique_key   VARCHAR(255) COLLATE "C" NOT NULL,
    biz_type     VARCHAR(64) COLLATE "C"  NOT NULL,
    biz_id       VARCHAR(100) COLLATE "C"          DEFAULT NULL,
    request_data TEXT                  DEFAULT NULL,
    created_at   TIMESTAMP(6) WITHOUT TIME ZONE     NOT NULL,
    updated_at   TIMESTAMP(6) WITHOUT TIME ZONE     NOT NULL,
    deleted      SMALLINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CHECK (deleted IN (0, 1))
);

CREATE UNIQUE INDEX uk_global_unique_scope ON global_unique (tenant_id, biz_type, unique_key);

CREATE INDEX idx_global_unique_biz ON global_unique (tenant_id, deleted, biz_type, biz_id);

CREATE INDEX idx_global_unique_created_at ON global_unique (created_at);

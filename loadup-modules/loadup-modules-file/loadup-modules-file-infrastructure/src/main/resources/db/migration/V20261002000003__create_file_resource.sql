-- PostgreSQL initial schema.

CREATE TABLE file_resource (
    id             VARCHAR(64) COLLATE "C"  NOT NULL PRIMARY KEY,
    tenant_id      VARCHAR(64) COLLATE "C"  NOT NULL,
    owner_id       VARCHAR(64) COLLATE "C"  NOT NULL,
    storage_id     VARCHAR(512) COLLATE "C" NOT NULL,
    filename       VARCHAR(255) COLLATE "C" NOT NULL,
    content_type   VARCHAR(255) COLLATE "C" NOT NULL,
    content_length BIGINT       NOT NULL,
    provider       VARCHAR(64) COLLATE "C"  NOT NULL,
    state          VARCHAR(32) COLLATE "C"  NOT NULL,
    created_at     TIMESTAMP(6) WITHOUT TIME ZONE  NOT NULL,
    updated_at     TIMESTAMP(6) WITHOUT TIME ZONE  NOT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CHECK (deleted IN (0, 1))
);

CREATE UNIQUE INDEX uk_file_resource_storage ON file_resource (provider, storage_id);

CREATE INDEX idx_file_resource_owner ON file_resource (tenant_id, owner_id, state, created_at);

CREATE INDEX idx_file_resource_cleanup ON file_resource (state, updated_at);

CREATE TABLE file_resource_reference (
    id             VARCHAR(64) COLLATE "C"  NOT NULL PRIMARY KEY,
    tenant_id      VARCHAR(64) COLLATE "C"  NOT NULL,
    file_id        VARCHAR(64) COLLATE "C"  NOT NULL,
    reference_type VARCHAR(128) COLLATE "C" NOT NULL,
    reference_id   VARCHAR(128) COLLATE "C" NOT NULL,
    created_at     TIMESTAMP(6) WITHOUT TIME ZONE  NOT NULL,
    CONSTRAINT fk_file_resource_reference_file FOREIGN KEY (file_id) REFERENCES file_resource (id),
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CHECK (deleted IN (0, 1))
);

CREATE UNIQUE INDEX uk_file_resource_reference ON file_resource_reference (tenant_id, file_id, reference_type, reference_id);

CREATE INDEX idx_file_resource_reference_target ON file_resource_reference (tenant_id, reference_type, reference_id);

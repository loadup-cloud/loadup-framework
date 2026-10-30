-- PostgreSQL initial schema.

CREATE TABLE file_resource
(
    id             VARCHAR(64) COLLATE "C"        NOT NULL PRIMARY KEY,

    owner_id       VARCHAR(64) COLLATE "C"        NOT NULL,
    storage_id     VARCHAR(512) COLLATE "C"       NOT NULL,
    filename       VARCHAR(255) COLLATE "C"       NOT NULL,
    content_type   VARCHAR(255) COLLATE "C"       NOT NULL,
    content_length BIGINT                         NOT NULL,
    provider       VARCHAR(64) COLLATE "C"        NOT NULL,
    state          VARCHAR(32) COLLATE "C"        NOT NULL,
    created_at     TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    deleted        BOOLEAN                        NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX uk_file_resource_storage ON file_resource (provider, storage_id);

CREATE INDEX idx_file_resource_owner ON file_resource (owner_id, state, created_at);

CREATE INDEX idx_file_resource_cleanup ON file_resource (state, updated_at);

CREATE TABLE file_resource_reference
(
    id             VARCHAR(64) COLLATE "C"        NOT NULL PRIMARY KEY,

    file_id        VARCHAR(64) COLLATE "C"        NOT NULL,
    reference_type VARCHAR(128) COLLATE "C"       NOT NULL,
    reference_id   VARCHAR(128) COLLATE "C"       NOT NULL,
    created_at     TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    deleted        BOOLEAN                        NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX uk_file_resource_reference ON file_resource_reference (file_id, reference_type, reference_id);

CREATE INDEX idx_file_resource_reference_target ON file_resource_reference (reference_type, reference_id);

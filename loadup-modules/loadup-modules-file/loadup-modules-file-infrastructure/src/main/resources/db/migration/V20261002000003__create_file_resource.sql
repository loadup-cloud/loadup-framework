CREATE TABLE IF NOT EXISTS file_resource
(
    id             VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id      VARCHAR(64)  NOT NULL,
    owner_id       VARCHAR(64)  NOT NULL,
    storage_id     VARCHAR(512) NOT NULL,
    filename       VARCHAR(255) NOT NULL,
    content_type   VARCHAR(255) NOT NULL,
    content_length BIGINT       NOT NULL,
    provider       VARCHAR(64)  NOT NULL,
    state          VARCHAR(32)  NOT NULL,
    created_at     DATETIME(6)  NOT NULL,
    updated_at     DATETIME(6)  NOT NULL,
    UNIQUE KEY uk_file_resource_storage (provider, storage_id),
    KEY idx_file_resource_owner (tenant_id, owner_id, state, created_at),
    KEY idx_file_resource_cleanup (state, updated_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS file_resource_reference
(
    id             VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id      VARCHAR(64)  NOT NULL,
    file_id        VARCHAR(64)  NOT NULL,
    reference_type VARCHAR(128) NOT NULL,
    reference_id   VARCHAR(128) NOT NULL,
    created_at     DATETIME(6)  NOT NULL,
    UNIQUE KEY uk_file_resource_reference (tenant_id, file_id, reference_type, reference_id),
    KEY idx_file_resource_reference_target (tenant_id, reference_type, reference_id),
    CONSTRAINT fk_file_resource_reference_file FOREIGN KEY (file_id) REFERENCES file_resource (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

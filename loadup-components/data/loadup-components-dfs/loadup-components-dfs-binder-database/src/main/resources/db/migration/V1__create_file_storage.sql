CREATE TABLE IF NOT EXISTS dfs_file (
    id         VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id  VARCHAR(64),
    filename   VARCHAR(255) NOT NULL,
    file_size  BIGINT       NOT NULL DEFAULT 0,
    content_type VARCHAR(128),
    content    LONGBLOB NOT NULL,
    metadata_json TEXT,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME     NOT NULL,
    deleted    TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

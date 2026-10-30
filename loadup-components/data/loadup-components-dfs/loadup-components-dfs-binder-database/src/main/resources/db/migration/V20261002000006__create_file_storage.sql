CREATE TABLE IF NOT EXISTS dfs_file (
    id VARCHAR(64) PRIMARY KEY, tenant_id VARCHAR(64), filename VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL DEFAULT 0, content_type VARCHAR(255), content BYTEA NOT NULL,
    metadata_json TEXT, created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

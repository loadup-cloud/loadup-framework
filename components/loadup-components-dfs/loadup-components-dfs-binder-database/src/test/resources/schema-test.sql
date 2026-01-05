-- Test schema for DFS component with Spring Data JDBC

CREATE TABLE IF NOT EXISTS dfs_file_storage
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id       VARCHAR(64)  NOT NULL UNIQUE,
    filename      VARCHAR(255) NOT NULL,
    content       LONGBLOB     NOT NULL,
    size          BIGINT       NOT NULL,
    content_type  VARCHAR(100),
    hash          VARCHAR(64),
    biz_type      VARCHAR(50),
    biz_id        VARCHAR(64),
    public_access BOOLEAN   DEFAULT FALSE,
    upload_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_file_id ON dfs_file_storage (file_id);
CREATE INDEX idx_biz_type ON dfs_file_storage (biz_type);
CREATE INDEX idx_biz_id ON dfs_file_storage (biz_id);


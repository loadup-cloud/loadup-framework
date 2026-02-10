-- DFS Component Database Schema
-- For database storage provider

-- File storage table
CREATE TABLE dfs_file_storage
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id       VARCHAR(64)  NOT NULL UNIQUE,
    filename      VARCHAR(255) NOT NULL,
    content       LONGBLOB     NOT NULL,
    size          BIGINT       NOT NULL,
    content_type  VARCHAR(100),
    hash          VARCHAR(64),
    biz_type      VARCHAR(50),
    biz_id        VARCHAR(64),
    public_access BOOLEAN   DEFAULT FALSE,
    upload_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_file_id (file_id),
    INDEX idx_biz_type (biz_type),
    INDEX idx_biz_id (biz_id),
    INDEX idx_upload_time (upload_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='DFS文件存储表';

-- File metadata table (for all providers)
CREATE TABLE dfs_file_metadata
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id          VARCHAR(64)  NOT NULL UNIQUE,
    filename         VARCHAR(255) NOT NULL,
    size             BIGINT       NOT NULL,
    content_type     VARCHAR(100),
    provider         VARCHAR(50)  NOT NULL,
    path             VARCHAR(500),
    url              VARCHAR(1000),
    hash             VARCHAR(64),
    biz_type         VARCHAR(50),
    biz_id           VARCHAR(64),
    status           VARCHAR(20) DEFAULT 'AVAILABLE',
    public_access    BOOLEAN     DEFAULT FALSE,
    metadata         JSON,
    upload_time      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    uploader         VARCHAR(64),
    last_access_time TIMESTAMP,
    access_count     BIGINT      DEFAULT 0,
    created_at       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_file_id (file_id),
    INDEX idx_provider (provider),
    INDEX idx_biz_type (biz_type),
    INDEX idx_biz_id (biz_id),
    INDEX idx_status (status),
    INDEX idx_upload_time (upload_time),
    INDEX idx_uploader (uploader)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='DFS文件元数据表';


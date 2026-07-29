CREATE TABLE IF NOT EXISTS file_storage (
    id         VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id  VARCHAR(64),
    filename   VARCHAR(255) NOT NULL,
    file_size  BIGINT       NOT NULL DEFAULT 0,
    content_type VARCHAR(128),
    content    LONGBLOB,
    biz_type   VARCHAR(64),
    biz_id     VARCHAR(64),
    created_at DATETIME     NOT NULL,
    updated_at DATETIME     NOT NULL,
    deleted    TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

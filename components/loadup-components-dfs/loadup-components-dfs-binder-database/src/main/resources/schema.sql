-- DFS Component Database Schema
-- 规范：所有表必须包含 id(VARCHAR64)/tenant_id/created_at/updated_at/deleted 标准字段

-- File storage table
CREATE TABLE IF NOT EXISTS dfs_file_storage
(
    id            VARCHAR(64)  NOT NULL                                              COMMENT 'ID',
    tenant_id     VARCHAR(64)                                                        COMMENT '租户ID',
    file_id       VARCHAR(64)  NOT NULL                                              COMMENT '文件ID（唯一）',
    filename      VARCHAR(255) NOT NULL                                              COMMENT '文件名',
    content       LONGBLOB     NOT NULL                                              COMMENT '文件内容',
    size          BIGINT       NOT NULL                                              COMMENT '文件大小(字节)',
    content_type  VARCHAR(100)                                                       COMMENT 'Content-Type',
    hash          VARCHAR(64)                                                        COMMENT '文件哈希',
    biz_type      VARCHAR(50)                                                        COMMENT '业务类型',
    biz_id        VARCHAR(64)                                                        COMMENT '业务ID',
    public_access TINYINT               DEFAULT 0                                    COMMENT '是否公开访问',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                    COMMENT '创建时间',
    updated_at    DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP             COMMENT '更新时间',
    deleted       TINYINT      NOT NULL DEFAULT 0                                    COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_id (file_id),
    INDEX idx_tenant_id  (tenant_id),
    INDEX idx_biz_type   (biz_type),
    INDEX idx_biz_id     (biz_id),
    INDEX idx_created_at (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='DFS文件存储表';

-- File metadata table (for all providers)
CREATE TABLE IF NOT EXISTS dfs_file_metadata
(
    id               VARCHAR(64)  NOT NULL                                           COMMENT 'ID',
    tenant_id        VARCHAR(64)                                                     COMMENT '租户ID',
    file_id          VARCHAR(64)  NOT NULL                                           COMMENT '文件ID（唯一）',
    filename         VARCHAR(255) NOT NULL                                           COMMENT '文件名',
    size             BIGINT       NOT NULL                                           COMMENT '文件大小(字节)',
    content_type     VARCHAR(100)                                                    COMMENT 'Content-Type',
    provider         VARCHAR(50)  NOT NULL                                           COMMENT '存储提供商',
    path             VARCHAR(500)                                                    COMMENT '存储路径',
    url              VARCHAR(1000)                                                   COMMENT '访问URL',
    hash             VARCHAR(64)                                                     COMMENT '文件哈希',
    biz_type         VARCHAR(50)                                                     COMMENT '业务类型',
    biz_id           VARCHAR(64)                                                     COMMENT '业务ID',
    status           VARCHAR(20)           DEFAULT 'AVAILABLE'                       COMMENT '状态',
    public_access    TINYINT               DEFAULT 0                                 COMMENT '是否公开访问',
    metadata         JSON                                                            COMMENT '扩展元数据',
    uploader         VARCHAR(64)                                                     COMMENT '上传人',
    last_access_time DATETIME                                                        COMMENT '最后访问时间',
    access_count     BIGINT                DEFAULT 0                                 COMMENT '访问次数',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                 COMMENT '创建时间',
    updated_at       DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP          COMMENT '更新时间',
    deleted          TINYINT      NOT NULL DEFAULT 0                                 COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_id (file_id),
    INDEX idx_tenant_id  (tenant_id),
    INDEX idx_provider   (provider),
    INDEX idx_biz_type   (biz_type),
    INDEX idx_biz_id     (biz_id),
    INDEX idx_status     (status),
    INDEX idx_created_at (created_at),
    INDEX idx_uploader   (uploader)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='DFS文件元数据表';

CREATE TABLE IF NOT EXISTS transfer_task
(
    id              VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id       VARCHAR(64)  NOT NULL,
    owner_id        VARCHAR(64)  NOT NULL,
    kind            VARCHAR(16)  NOT NULL,
    handler_key     VARCHAR(64)  NOT NULL,
    source_file_id  VARCHAR(64),
    result_file_id  VARCHAR(64),
    status          VARCHAR(16)  NOT NULL,
    processed_count BIGINT       NOT NULL DEFAULT 0,
    total_count     BIGINT       NOT NULL DEFAULT 0,
    error_message   VARCHAR(500),
    created_at      DATETIME(6)  NOT NULL,
    started_at      DATETIME(6),
    finished_at     DATETIME(6),
    KEY idx_transfer_owner (tenant_id, owner_id, created_at),
    KEY idx_transfer_status (status, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS transfer_task_option
(
    task_id      VARCHAR(64)   NOT NULL,
    option_name  VARCHAR(64)   NOT NULL,
    option_value VARCHAR(1000) NOT NULL,
    PRIMARY KEY (task_id, option_name),
    CONSTRAINT fk_transfer_task_option_task FOREIGN KEY (task_id) REFERENCES transfer_task (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

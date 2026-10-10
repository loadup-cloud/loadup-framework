CREATE TABLE IF NOT EXISTS notification_inbox
(
    id           VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id    VARCHAR(64)  NOT NULL,
    recipient_id VARCHAR(64)  NOT NULL,
    sender_id    VARCHAR(64),
    category     VARCHAR(64)  NOT NULL,
    title        VARCHAR(200) NOT NULL,
    body         VARCHAR(4000) NOT NULL,
    action_url   VARCHAR(500),
    request_key  VARCHAR(128),
    read_at      DATETIME(6),
    archived_at  DATETIME(6),
    created_at   DATETIME(6)  NOT NULL,
    UNIQUE KEY uk_notification_delivery (tenant_id, recipient_id, request_key),
    KEY idx_notification_inbox (tenant_id, recipient_id, archived_at, created_at),
    KEY idx_notification_unread (tenant_id, recipient_id, archived_at, read_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

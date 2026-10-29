CREATE TABLE IF NOT EXISTS notification_inbox (
    id VARCHAR(64) PRIMARY KEY, tenant_id VARCHAR(64) NOT NULL, recipient_id VARCHAR(64) NOT NULL,
    sender_id VARCHAR(64), category VARCHAR(64) NOT NULL, title VARCHAR(200) NOT NULL, body VARCHAR(4000) NOT NULL,
    action_url VARCHAR(500), request_key VARCHAR(128), read_at TIMESTAMP(6), archived_at TIMESTAMP(6),
    created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6) NOT NULL, deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_notification_delivery UNIQUE (tenant_id, recipient_id, request_key)
);
CREATE INDEX idx_notification_inbox ON notification_inbox (tenant_id, recipient_id, archived_at, created_at);
CREATE INDEX idx_notification_unread ON notification_inbox (tenant_id, recipient_id, archived_at, read_at);

CREATE TABLE notification_request
(
    id VARCHAR(64) PRIMARY KEY,
    source VARCHAR(128) NOT NULL,
    request_key VARCHAR(128) NOT NULL,
    content_digest VARCHAR(64) NOT NULL,
    content_json TEXT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_notification_request UNIQUE (source, request_key)
);

CREATE TABLE notification_delivery
(
    id VARCHAR(64) PRIMARY KEY,
    notification_id VARCHAR(64) NOT NULL REFERENCES notification_request(id),
    recipient_id VARCHAR(64) NOT NULL,
    channel VARCHAR(16) NOT NULL CHECK (channel = 'IN_APP'),
    event_id VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(16) NOT NULL CHECK (status IN ('PENDING', 'DELIVERED', 'EXPIRED')),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_notification_target UNIQUE (notification_id, recipient_id, channel)
);
CREATE INDEX idx_notification_delivery_status ON notification_delivery (status, created_at);

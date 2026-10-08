CREATE TABLE loadup_outbox_event (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    event_type VARCHAR(128) NOT NULL,
    payload_version INT NOT NULL,
    business_id VARCHAR(128) NOT NULL,
    payload MEDIUMTEXT NOT NULL,
    trace_id VARCHAR(64),
    status VARCHAR(16) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    available_at DATETIME(6) NOT NULL,
    lease_until DATETIME(6),
    claim_token VARCHAR(64),
    last_error VARCHAR(256),
    KEY idx_outbox_ready (status, available_at, created_at),
    KEY idx_outbox_lease (status, lease_until),
    KEY idx_outbox_tenant (tenant_id, status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE loadup_outbox_replay (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    event_id VARCHAR(64) NOT NULL,
    operator_id VARCHAR(64) NOT NULL,
    reason VARCHAR(512) NOT NULL,
    KEY idx_outbox_replay_event (tenant_id, event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE loadup_outbox_inbox (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    consumer_id VARCHAR(128) NOT NULL,
    event_id VARCHAR(64) NOT NULL,
    UNIQUE KEY uk_outbox_inbox (tenant_id, consumer_id, event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

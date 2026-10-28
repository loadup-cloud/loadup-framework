-- PostgreSQL initial schema.

CREATE TABLE loadup_outbox_event (
    id VARCHAR(64) COLLATE "C" NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(64) COLLATE "C" NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    event_type VARCHAR(128) COLLATE "C" NOT NULL,
    payload_version INT NOT NULL,
    business_id VARCHAR(128) COLLATE "C" NOT NULL,
    payload TEXT NOT NULL,
    trace_id VARCHAR(64) COLLATE "C",
    status VARCHAR(16) COLLATE "C" NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    available_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    lease_until TIMESTAMP(6) WITHOUT TIME ZONE,
    claim_token VARCHAR(64) COLLATE "C",
    last_error VARCHAR(256) COLLATE "C",
    CHECK (deleted IN (0, 1))
);

CREATE INDEX idx_outbox_ready ON loadup_outbox_event (status, available_at, created_at);

CREATE INDEX idx_outbox_lease ON loadup_outbox_event (status, lease_until);

CREATE INDEX idx_outbox_tenant ON loadup_outbox_event (tenant_id, status, created_at);

CREATE TABLE loadup_outbox_replay (
    id VARCHAR(64) COLLATE "C" NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(64) COLLATE "C" NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    event_id VARCHAR(64) COLLATE "C" NOT NULL,
    operator_id VARCHAR(64) COLLATE "C" NOT NULL,
    reason VARCHAR(512) COLLATE "C" NOT NULL,
    CHECK (deleted IN (0, 1))
);

CREATE INDEX idx_outbox_replay_event ON loadup_outbox_replay (tenant_id, event_id);

CREATE TABLE loadup_outbox_inbox (
    id VARCHAR(64) COLLATE "C" NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(64) COLLATE "C" NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    deleted SMALLINT NOT NULL DEFAULT 0,
    consumer_id VARCHAR(128) COLLATE "C" NOT NULL,
    event_id VARCHAR(64) COLLATE "C" NOT NULL,
    CHECK (deleted IN (0, 1))
);

CREATE UNIQUE INDEX uk_outbox_inbox ON loadup_outbox_inbox (tenant_id, consumer_id, event_id);

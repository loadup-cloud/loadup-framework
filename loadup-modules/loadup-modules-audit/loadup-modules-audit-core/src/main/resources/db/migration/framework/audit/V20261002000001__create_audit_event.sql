CREATE TABLE IF NOT EXISTS audit_event
(
    id           VARCHAR(64) PRIMARY KEY,
    actor_id     VARCHAR(128),
    action       VARCHAR(100) NOT NULL,
    http_method  VARCHAR(10),
    request_path VARCHAR(512),
    outcome      VARCHAR(16),
    trace_id     VARCHAR(64),
    occurred_at  TIMESTAMP(6) NOT NULL,
    created_at   TIMESTAMP(6) NOT NULL,
    updated_at   TIMESTAMP(6) NOT NULL,
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_audit_occurred_at ON audit_event (occurred_at);
CREATE INDEX idx_audit_actor_time ON audit_event (actor_id, occurred_at);

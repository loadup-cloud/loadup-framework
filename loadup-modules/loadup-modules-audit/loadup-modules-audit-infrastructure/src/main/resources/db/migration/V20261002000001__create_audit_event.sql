CREATE TABLE IF NOT EXISTS audit_event
(
    id           VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id    VARCHAR(64),
    actor_id     VARCHAR(128),
    action       VARCHAR(100) NOT NULL,
    http_method  VARCHAR(10),
    request_path VARCHAR(512),
    outcome      VARCHAR(16),
    trace_id     VARCHAR(64),
    occurred_at  DATETIME(6) NOT NULL,
    created_at   DATETIME(6) NOT NULL,
    updated_at   DATETIME(6) NOT NULL,
    deleted      TINYINT     NOT NULL DEFAULT 0,
    KEY idx_audit_tenant_time (tenant_id, occurred_at),
    KEY idx_audit_actor_time (actor_id, occurred_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

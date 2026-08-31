-- Gotone default JDBC store schema.
-- Every table carries the five standard fields: id, tenant_id, created_at, updated_at, deleted.

CREATE TABLE IF NOT EXISTS gotone_notification_service
(
    id           VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id    VARCHAR(64),
    service_code VARCHAR(100) NOT NULL,
    service_name VARCHAR(200),
    description  VARCHAR(500),
    enabled      TINYINT      NOT NULL DEFAULT 1,
    priority     INT          NOT NULL DEFAULT 0,
    created_at   DATETIME     NOT NULL,
    updated_at   DATETIME     NOT NULL,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    UNIQUE KEY uk_service_code (service_code),
    KEY idx_service_enabled (service_code, enabled)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS gotone_service_channel
(
    id                 VARCHAR(64) NOT NULL PRIMARY KEY,
    tenant_id          VARCHAR(64),
    service_code       VARCHAR(100) NOT NULL,
    channel            VARCHAR(50)  NOT NULL,
    template_content   TEXT,
    channel_config     JSON,
    provider           VARCHAR(64),
    fallback_providers JSON,
    enabled            TINYINT      NOT NULL DEFAULT 1,
    priority           INT          NOT NULL DEFAULT 0,
    created_at         DATETIME     NOT NULL,
    updated_at         DATETIME     NOT NULL,
    deleted            TINYINT      NOT NULL DEFAULT 0,
    UNIQUE KEY uk_service_channel (service_code, channel),
    KEY idx_channel_enabled (service_code, channel, enabled)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS gotone_notification_record
(
    id            VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id     VARCHAR(64),
    service_code  VARCHAR(100) NOT NULL,
    request_id    VARCHAR(100),
    channel       VARCHAR(50)  NOT NULL,
    provider      VARCHAR(64),
    receiver      VARCHAR(255) NOT NULL,
    content       TEXT,
    status        VARCHAR(32)  NOT NULL,
    error_message TEXT,
    send_time     DATETIME,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    KEY idx_record_service (service_code, send_time),
    KEY idx_record_channel_status (channel, status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

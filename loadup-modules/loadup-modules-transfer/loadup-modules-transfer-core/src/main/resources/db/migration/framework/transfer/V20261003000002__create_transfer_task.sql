-- PostgreSQL initial schema.

CREATE TABLE transfer_task
(
    id              VARCHAR(64) COLLATE "C"        NOT NULL PRIMARY KEY,

    owner_id        VARCHAR(64) COLLATE "C"        NOT NULL,
    kind            VARCHAR(16) COLLATE "C"        NOT NULL,
    handler_key     VARCHAR(64) COLLATE "C"        NOT NULL,
    source_file_id  VARCHAR(64) COLLATE "C",
    result_file_id  VARCHAR(64) COLLATE "C",
    status          VARCHAR(16) COLLATE "C"        NOT NULL,
    processed_count BIGINT                         NOT NULL DEFAULT 0,
    total_count     BIGINT                         NOT NULL DEFAULT 0,
    error_message   VARCHAR(500) COLLATE "C",
    created_at      TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    started_at      TIMESTAMP(6) WITHOUT TIME ZONE,
    finished_at     TIMESTAMP(6) WITHOUT TIME ZONE,
    updated_at      TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    deleted         BOOLEAN                        NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_transfer_owner ON transfer_task (owner_id, created_at);

CREATE INDEX idx_transfer_status ON transfer_task (status, created_at);

CREATE TABLE transfer_task_option
(
    task_id      VARCHAR(64) COLLATE "C"        NOT NULL,
    option_name  VARCHAR(64) COLLATE "C"        NOT NULL,
    option_value VARCHAR(1000) COLLATE "C"      NOT NULL,
    id           VARCHAR(64) COLLATE "C"        NOT NULL PRIMARY KEY,

    created_at   TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at   TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    deleted      BOOLEAN                        NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX uk_transfer_option ON transfer_task_option (task_id, option_name);

-- MySQL 8 (InnoDB) initial schema; installed by the consuming application.
CREATE TABLE loadup_task (
    id VARCHAR(64) PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    task_type VARCHAR(128) NOT NULL,
    biz_id VARCHAR(128) NOT NULL,
    payload MEDIUMTEXT,
    trace_id VARCHAR(64),
    next_execute_at DATETIME(6) NOT NULL,
    status VARCHAR(16) NOT NULL CHECK (status IN ('PENDING','RUNNING','FAILED','CANCELLED')),
    attempts INT NOT NULL DEFAULT 0 CHECK (attempts >= 0),
    max_attempts INT NOT NULL CHECK (max_attempts > 0),
    lease_until DATETIME(6),
    claim_token VARCHAR(64),
    last_error VARCHAR(256)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin;
CREATE INDEX loadup_task_ready ON loadup_task(status,next_execute_at,id);
CREATE INDEX loadup_task_lease ON loadup_task(status,lease_until,id);
CREATE UNIQUE INDEX loadup_task_business ON loadup_task(task_type,biz_id);
CREATE TABLE loadup_task_operation (
    id VARCHAR(64) PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    task_id VARCHAR(64) NOT NULL,
    operator_id VARCHAR(64) NOT NULL,
    reason VARCHAR(512) NOT NULL
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin;
CREATE INDEX loadup_task_operation ON loadup_task_operation(task_id);

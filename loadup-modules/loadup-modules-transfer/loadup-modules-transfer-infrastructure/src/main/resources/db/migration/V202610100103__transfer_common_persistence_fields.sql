ALTER TABLE transfer_task ADD COLUMN updated_at DATETIME(6) NULL, ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0;
UPDATE transfer_task SET updated_at = created_at;
ALTER TABLE transfer_task MODIFY COLUMN updated_at DATETIME(6) NOT NULL;
ALTER TABLE transfer_task_option ADD COLUMN id VARCHAR(64) NULL, ADD COLUMN tenant_id VARCHAR(64) NULL,
    ADD COLUMN created_at DATETIME(6) NULL, ADD COLUMN updated_at DATETIME(6) NULL,
    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0;
UPDATE transfer_task_option o JOIN transfer_task t ON o.task_id = t.id
    SET o.id = UUID(), o.tenant_id = t.tenant_id, o.created_at = t.created_at, o.updated_at = t.updated_at;
ALTER TABLE transfer_task_option MODIFY COLUMN id VARCHAR(64) NOT NULL,
    MODIFY COLUMN created_at DATETIME(6) NOT NULL, MODIFY COLUMN updated_at DATETIME(6) NOT NULL,
    DROP PRIMARY KEY, ADD PRIMARY KEY (id), ADD UNIQUE KEY uk_transfer_option (task_id, option_name);

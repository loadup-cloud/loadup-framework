ALTER TABLE notification_inbox ADD COLUMN updated_at DATETIME(6) NULL, ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0;
UPDATE notification_inbox SET updated_at = created_at;
ALTER TABLE notification_inbox MODIFY COLUMN updated_at DATETIME(6) NOT NULL;

ALTER TABLE file_resource ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0;
ALTER TABLE file_resource_reference ADD COLUMN updated_at DATETIME(6) NULL, ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0;
UPDATE file_resource_reference SET updated_at = created_at;
ALTER TABLE file_resource_reference MODIFY COLUMN updated_at DATETIME(6) NOT NULL;

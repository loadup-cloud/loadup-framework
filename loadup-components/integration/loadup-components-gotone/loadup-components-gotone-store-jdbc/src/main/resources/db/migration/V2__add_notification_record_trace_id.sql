ALTER TABLE gotone_notification_record
    ADD COLUMN trace_id VARCHAR(64) NULL AFTER service_code;

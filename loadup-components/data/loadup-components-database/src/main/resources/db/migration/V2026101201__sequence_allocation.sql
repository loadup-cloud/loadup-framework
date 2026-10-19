CREATE TABLE loadup_sequence_allocation (
 id VARCHAR(64) NOT NULL PRIMARY KEY,
 tenant_id VARCHAR(64),
 created_at DATETIME NOT NULL,
 updated_at DATETIME NOT NULL,
 deleted TINYINT NOT NULL DEFAULT 0,
 scope_key VARCHAR(160) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 max_allocated BIGINT NOT NULL,
 max_value BIGINT NOT NULL,
 UNIQUE KEY uk_loadup_sequence_scope (scope_key),
 CONSTRAINT chk_loadup_sequence_bounds CHECK (max_allocated >= 0 AND max_allocated <= max_value AND max_value > 0),
 CONSTRAINT chk_loadup_sequence_deleted CHECK (deleted = 0)
);

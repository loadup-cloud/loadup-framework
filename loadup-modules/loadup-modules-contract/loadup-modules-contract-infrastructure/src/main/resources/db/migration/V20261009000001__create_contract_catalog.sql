CREATE TABLE contract_catalog_version (
 id VARCHAR(64) NOT NULL PRIMARY KEY, tenant_id VARCHAR(64) NOT NULL,
 kind VARCHAR(24) NOT NULL, code VARCHAR(128) NOT NULL, version INT NOT NULL,
 status VARCHAR(24) NOT NULL, row_version BIGINT NOT NULL, content LONGTEXT NOT NULL,
 updated_by VARCHAR(256) NOT NULL,
 created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL, deleted TINYINT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_contract_catalog_version (tenant_id,kind,code,version),
 KEY idx_contract_catalog_page (tenant_id,kind,status,updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
CREATE TABLE merchant_contract (
 id VARCHAR(64) NOT NULL PRIMARY KEY, tenant_id VARCHAR(64) NOT NULL,
 merchant_id VARCHAR(64) NOT NULL, scope_key VARCHAR(128) NOT NULL,
 plan_version_id VARCHAR(64) NOT NULL, status VARCHAR(24) NOT NULL, generation BIGINT NOT NULL,
 request_key VARCHAR(128) NOT NULL, request_digest VARCHAR(64) NOT NULL, created_by VARCHAR(256) NOT NULL,
 created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL, deleted TINYINT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_merchant_contract_scope (tenant_id,merchant_id,scope_key),
 UNIQUE KEY uk_merchant_contract_request (tenant_id,request_key),
 KEY idx_merchant_contract_page (tenant_id,merchant_id,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
CREATE TABLE merchant_contract_revision (
 id VARCHAR(64) NOT NULL PRIMARY KEY, tenant_id VARCHAR(64) NOT NULL,
 contract_id VARCHAR(64) NOT NULL, revision INT NOT NULL,
 snapshot LONGTEXT NOT NULL, snapshot_hash VARCHAR(128) NOT NULL,
 created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL, deleted TINYINT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_merchant_contract_revision (tenant_id,contract_id,revision)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

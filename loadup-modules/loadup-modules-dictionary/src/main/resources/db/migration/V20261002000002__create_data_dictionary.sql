CREATE TABLE IF NOT EXISTS dictionary_type
(
    id          VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id   VARCHAR(64)  NOT NULL,
    type_code   VARCHAR(64)  NOT NULL,
    type_name   VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    enabled     TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    UNIQUE KEY uk_dictionary_type_tenant_code (tenant_id, type_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS dictionary_item
(
    id          VARCHAR(64)  NOT NULL PRIMARY KEY,
    tenant_id   VARCHAR(64)  NOT NULL,
    type_id     VARCHAR(64)  NOT NULL,
    item_value  VARCHAR(128) NOT NULL,
    item_label  VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    sort_order  INT          NOT NULL DEFAULT 0,
    enabled     TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    UNIQUE KEY uk_dictionary_item_type_value (tenant_id, type_id, item_value),
    KEY idx_dictionary_item_display (tenant_id, type_id, enabled, sort_order),
    CONSTRAINT fk_dictionary_item_type FOREIGN KEY (type_id) REFERENCES dictionary_type (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

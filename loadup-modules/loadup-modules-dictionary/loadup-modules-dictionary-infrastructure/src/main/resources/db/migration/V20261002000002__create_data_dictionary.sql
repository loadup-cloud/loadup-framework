CREATE TABLE IF NOT EXISTS dictionary_type
(
    id          VARCHAR(64) PRIMARY KEY,
    tenant_id   VARCHAR(64)  NOT NULL,
    type_code   VARCHAR(64)  NOT NULL,
    type_name   VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP(6) NOT NULL,
    updated_at  TIMESTAMP(6) NOT NULL,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_dictionary_type_tenant_code UNIQUE (tenant_id, type_code)
);
CREATE TABLE IF NOT EXISTS dictionary_item
(
    id          VARCHAR(64) PRIMARY KEY,
    tenant_id   VARCHAR(64)  NOT NULL,
    type_id     VARCHAR(64)  NOT NULL,
    item_value  VARCHAR(128) NOT NULL,
    item_label  VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    sort_order  INT          NOT NULL DEFAULT 0,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP(6) NOT NULL,
    updated_at  TIMESTAMP(6) NOT NULL,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_dictionary_item_type_value UNIQUE (tenant_id, type_id, item_value)
);
CREATE INDEX idx_dictionary_item_display ON dictionary_item (tenant_id, type_id, enabled, sort_order);

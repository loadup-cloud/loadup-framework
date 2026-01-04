-- Multi-Tenant Schema
-- LoadUp Framework Database Component

-- Tenant table
CREATE TABLE IF NOT EXISTS sys_tenant
(
    id                     VARCHAR(64) PRIMARY KEY COMMENT '租户ID',
    tenant_code            VARCHAR(50)  NOT NULL UNIQUE COMMENT '租户编码',
    tenant_name            VARCHAR(100) NOT NULL COMMENT '租户名称',
    status                 TINYINT(1) DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    admin_user_id          VARCHAR(64) COMMENT '管理员用户ID',
    contact_person         VARCHAR(50) COMMENT '联系人',
    contact_phone          VARCHAR(20) COMMENT '联系电话',
    contact_email          VARCHAR(100) COMMENT '联系邮箱',
    expire_time            DATETIME COMMENT '过期时间',
    config                 JSON COMMENT '租户个性化配置(JSON格式)',
    logical_delete_enabled BOOLEAN    DEFAULT TRUE COMMENT '是否启用逻辑删除',
    remark                 VARCHAR(500) COMMENT '备注',
    created_at             DATETIME   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at             DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                BOOLEAN    DEFAULT FALSE COMMENT '逻辑删除标记',
    INDEX idx_tenant_code (tenant_code),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='租户表';

-- Sample tenant data
INSERT INTO sys_tenant (id, tenant_code, tenant_name, status, logical_delete_enabled, config)
VALUES ('default', 'default', '默认租户', 1, TRUE, '{
  "theme": "light",
  "locale": "zh_CN"
}'),
       ('tenant_a', 'tenant_a', '租户A（启用逻辑删除）', 1, TRUE, '{
         "theme": "dark",
         "locale": "en_US"
       }'),
       ('tenant_b', 'tenant_b', '租户B（物理删除）', 1, FALSE, '{
         "theme": "light",
         "locale": "zh_CN"
       }');


-- ============================================================
-- Config Module Schema
-- Module: loadup-modules-config
-- 规范：所有表必须包含 id/tenant_id/created_at/updated_at/deleted 标准字段
-- ============================================================

CREATE TABLE IF NOT EXISTS config_item (
    id             VARCHAR(64)  NOT NULL                                             COMMENT 'ID',
    tenant_id      VARCHAR(64)                                                       COMMENT '租户ID',
    config_key     VARCHAR(200) NOT NULL                                             COMMENT '配置键（全局唯一）',
    config_value   TEXT                                                              COMMENT '配置值',
    value_type     VARCHAR(20)  NOT NULL DEFAULT 'STRING'                           COMMENT '值类型: STRING/INTEGER/LONG/DOUBLE/BOOLEAN/JSON',
    category       VARCHAR(50)  NOT NULL DEFAULT 'default'                          COMMENT '分类',
    description    VARCHAR(500)                                                      COMMENT '描述',
    editable       TINYINT      NOT NULL DEFAULT 1                                   COMMENT '是否可编辑',
    encrypted      TINYINT      NOT NULL DEFAULT 0                                   COMMENT '是否加密存储',
    system_defined TINYINT      NOT NULL DEFAULT 0                                   COMMENT '是否系统内置',
    sort_order     INT          NOT NULL DEFAULT 0                                   COMMENT '排序',
    enabled        TINYINT      NOT NULL DEFAULT 1                                   COMMENT '是否启用',
    created_by     VARCHAR(64)                                                       COMMENT '创建人',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '创建时间',
    updated_by     VARCHAR(64)                                                       COMMENT '更新人',
    updated_at     DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP            COMMENT '更新时间',
    deleted        TINYINT      NOT NULL DEFAULT 0                                   COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key),
    KEY idx_tenant_id (tenant_id),
    KEY idx_category  (category),
    KEY idx_enabled   (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统参数配置表';

-- -----------------------------------------------------------

CREATE TABLE IF NOT EXISTS dict_type (
    id             VARCHAR(64)  NOT NULL                                             COMMENT 'ID',
    tenant_id      VARCHAR(64)                                                       COMMENT '租户ID',
    dict_code      VARCHAR(100) NOT NULL                                             COMMENT '字典编码（全局唯一）',
    dict_name      VARCHAR(200) NOT NULL                                             COMMENT '字典名称',
    description    VARCHAR(500)                                                      COMMENT '描述',
    system_defined TINYINT      NOT NULL DEFAULT 0                                   COMMENT '是否系统内置',
    sort_order     INT          NOT NULL DEFAULT 0                                   COMMENT '排序',
    enabled        TINYINT      NOT NULL DEFAULT 1                                   COMMENT '是否启用',
    created_by     VARCHAR(64)                                                       COMMENT '创建人',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '创建时间',
    updated_by     VARCHAR(64)                                                       COMMENT '更新人',
    updated_at     DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP            COMMENT '更新时间',
    deleted        TINYINT      NOT NULL DEFAULT 0                                   COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_code (dict_code),
    KEY idx_tenant_id (tenant_id),
    KEY idx_enabled   (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典类型表';

-- -----------------------------------------------------------

CREATE TABLE IF NOT EXISTS dict_item (
    id           VARCHAR(64)  NOT NULL                                               COMMENT 'ID',
    tenant_id    VARCHAR(64)                                                         COMMENT '租户ID',
    dict_code    VARCHAR(100) NOT NULL                                               COMMENT '字典编码',
    item_label   VARCHAR(200) NOT NULL                                               COMMENT '显示标签',
    item_value   VARCHAR(200) NOT NULL                                               COMMENT '字典值',
    parent_value VARCHAR(200)                                                        COMMENT '父级值（级联字典）',
    css_class    VARCHAR(100)                                                        COMMENT 'CSS 样式类',
    sort_order   INT          NOT NULL DEFAULT 0                                     COMMENT '排序',
    enabled      TINYINT      NOT NULL DEFAULT 1                                     COMMENT '是否启用',
    created_by   VARCHAR(64)                                                         COMMENT '创建人',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                     COMMENT '创建时间',
    updated_by   VARCHAR(64)                                                         COMMENT '更新人',
    updated_at   DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP              COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0                                     COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_code_value (dict_code, item_value),
    KEY idx_tenant_id    (tenant_id),
    KEY idx_dict_code    (dict_code),
    KEY idx_parent_value (parent_value),
    KEY idx_enabled      (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典项表';

-- -----------------------------------------------------------

CREATE TABLE IF NOT EXISTS config_history (
    id          VARCHAR(64)  NOT NULL                                                COMMENT 'ID',
    tenant_id   VARCHAR(64)                                                          COMMENT '租户ID',
    config_key  VARCHAR(200) NOT NULL                                                COMMENT '配置键',
    old_value   TEXT                                                                 COMMENT '变更前值',
    new_value   TEXT                                                                 COMMENT '变更后值',
    change_type VARCHAR(20)  NOT NULL                                                COMMENT '变更类型: CREATE/UPDATE/DELETE',
    operator    VARCHAR(64)  NOT NULL DEFAULT 'system'                               COMMENT '操作人',
    remark      VARCHAR(500)                                                         COMMENT '备注',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                      COMMENT '创建时间',
    updated_at  DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP               COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0                                      COMMENT '删除标记',
    PRIMARY KEY (id),
    KEY idx_tenant_id  (tenant_id),
    KEY idx_config_key (config_key),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置变更历史表';

-- ============================================================
-- Initial seed data
-- ============================================================
INSERT IGNORE INTO dict_type (id, dict_code, dict_name, system_defined, sort_order, enabled, created_at)
VALUES
    ('01', 'user_status', '用户状态', 1, 1, 1, NOW()),
    ('02', 'gender',      '性别',     1, 2, 1, NOW()),
    ('03', 'yes_no',      '是否',     1, 3, 1, NOW());

INSERT IGNORE INTO dict_item (id, dict_code, item_label, item_value, sort_order, enabled, created_at)
VALUES
    ('001', 'user_status', '正常', '1', 1, 1, NOW()),
    ('002', 'user_status', '停用', '0', 2, 1, NOW()),
    ('003', 'user_status', '锁定', '2', 3, 1, NOW()),
    ('004', 'gender',      '男',   '1', 1, 1, NOW()),
    ('005', 'gender',      '女',   '2', 2, 1, NOW()),
    ('006', 'gender',      '未知', '0', 3, 1, NOW()),
    ('007', 'yes_no',      '是',   '1', 1, 1, NOW()),
    ('008', 'yes_no',      '否',   '0', 2, 1, NOW());

INSERT IGNORE INTO config_item
    (id, config_key, config_value, value_type, category, description, editable, system_defined, enabled, created_at)
VALUES
    ('c01', 'system.name',                   'LoadUp Framework', 'STRING',  'system',   '系统名称',            0, 1, 1, NOW()),
    ('c02', 'upload.max-file-size',          '10485760',         'LONG',    'upload',   '文件上传大小限制(字节)', 1, 0, 1, NOW()),
    ('c03', 'security.password-expire-days', '90',               'INTEGER', 'security', '密码过期天数',          1, 0, 1, NOW());

-- MySQL Compatible Schema for Gotone Notification Component
-- Version: 2.0.0 (ServiceCode-Driven Architecture)
-- Date: 2026-02-09

-- Drop tables if exists (for clean test)
DROP TABLE IF EXISTS gotone_notification_record;
DROP TABLE IF EXISTS gotone_service_channel;
DROP TABLE IF EXISTS gotone_notification_service;

-- ============================================================================
-- 1. 通知服务配置表（主表）
-- ============================================================================
CREATE TABLE gotone_notification_service
(
    id           VARCHAR(64) PRIMARY KEY COMMENT '主键ID',

    -- 服务标识
    service_code VARCHAR(100) NOT NULL COMMENT '服务代码（唯一）',
    service_name VARCHAR(200) COMMENT '服务名称',
    description  VARCHAR(500) COMMENT '服务描述',

    -- 状态控制
    enabled      BOOLEAN               DEFAULT TRUE COMMENT '是否启用',
    priority     INT                   DEFAULT 0 COMMENT '优先级',

    -- 审计字段
    created_at   DATETIME     NOT NULL COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL COMMENT '更新时间',
    tenant_id    VARCHAR(64) COMMENT '租户ID',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标记',

    UNIQUE KEY uk_service_code (service_code),
    INDEX        idx_enabled (enabled)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT '通知服务配置表';

-- ============================================================================
-- 2. 服务渠道映射表（核心配置）
-- ============================================================================
CREATE TABLE gotone_service_channel
(
    id                 VARCHAR(64) PRIMARY KEY COMMENT '主键ID',

    -- 关联
    service_code       VARCHAR(100) NOT NULL COMMENT '服务代码',
    channel            VARCHAR(50)  NOT NULL COMMENT '渠道：EMAIL/SMS/PUSH',

    -- 模板配置
    template_code      VARCHAR(100) NOT NULL COMMENT '模板代码',
    template_content   TEXT COMMENT '模板内容（支持${var}占位符）',

    -- 渠道特定配置（JSON）
    channel_config     JSON COMMENT '渠道配置：
        EMAIL: {"subject": "订单通知", "from": "no-reply@example.com"}
        SMS: {"signName": "LoadUp", "templateId": "SMS_123456"}
        PUSH: {"title": "新订单", "sound": "default"}',

    -- 提供商配置
    provider           VARCHAR(64) COMMENT '主提供商',
    fallback_providers JSON COMMENT '降级提供商列表: ["provider2", "provider3"]',

    -- 发送策略
    send_strategy      VARCHAR(32)           DEFAULT 'ASYNC' COMMENT '发送策略：SYNC/ASYNC/SCHEDULED',
    retry_config       JSON COMMENT '重试配置: {"maxRetries": 3, "retryInterval": 60}',

    -- 状态控制
    enabled            BOOLEAN               DEFAULT TRUE COMMENT '是否启用此渠道',
    priority           INT                   DEFAULT 0 COMMENT '渠道优先级（数字越大越优先）',

    -- 审计字段
    created_at         DATETIME     NOT NULL COMMENT '创建时间',
    updated_at         DATETIME     NOT NULL COMMENT '更新时间',
    tenant_id          VARCHAR(64) COMMENT '租户ID',
    deleted            TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标记',

    UNIQUE KEY uk_service_channel (service_code, channel),
    INDEX              idx_service_code (service_code),
    INDEX              idx_enabled (enabled),
    INDEX              idx_priority (priority)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT '服务渠道映射表';

-- ============================================================================
-- 3. 通知发送记录表（单表+JSON扩展字段）
-- ============================================================================
CREATE TABLE gotone_notification_record
(
    id              VARCHAR(64) PRIMARY KEY COMMENT '主键ID',

    -- 业务标识
    service_code    VARCHAR(100) NOT NULL COMMENT '服务代码',
    trace_id        VARCHAR(64) COMMENT '追踪ID（批量发送时相同）',
    request_id      VARCHAR(100) COMMENT '请求ID（幂等性）',

    -- 渠道信息
    channel         VARCHAR(50)  NOT NULL COMMENT '渠道：EMAIL/SMS/PUSH',
    provider        VARCHAR(64) COMMENT '实际使用的提供商',
    receiver        VARCHAR(255) NOT NULL COMMENT '收件人',

    -- 内容信息
    template_code   VARCHAR(100) COMMENT '模板代码',
    content         TEXT COMMENT '实际发送内容',

    -- 渠道扩展数据（JSON）
    channel_data    JSON COMMENT '渠道特定数据：
        EMAIL: {"subject": "xxx", "cc": [], "bcc": [], "attachments": []}
        SMS: {"phoneNumber": "xxx", "signName": "xxx", "templateId": "xxx"}
        PUSH: {"title": "xxx", "badge": 1, "sound": "default", "extras": {}}',

    -- 发送状态
    status          VARCHAR(32)  NOT NULL COMMENT '状态：PENDING/SUCCESS/FAILED/RETRY',
    error_code      VARCHAR(50) COMMENT '错误码',
    error_message   TEXT COMMENT '错误信息',

    -- 重试信息
    retry_count     INT                   DEFAULT 0 COMMENT '重试次数',
    max_retries     INT                   DEFAULT 3 COMMENT '最大重试次数',
    next_retry_time DATETIME COMMENT '下次重试时间',

    -- 时间信息
    send_time       DATETIME COMMENT '发送时间',
    success_time    DATETIME COMMENT '成功时间',

    -- 审计字段
    created_at      DATETIME     NOT NULL COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL COMMENT '更新时间',
    tenant_id       VARCHAR(64) COMMENT '租户ID',
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标记',

    INDEX           idx_service_code (service_code),
    INDEX           idx_trace_id (trace_id),
    INDEX           idx_request_id (request_id),
    INDEX           idx_channel_status (channel, status),
    INDEX           idx_retry (status, next_retry_time),
    INDEX           idx_send_time (send_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT '通知发送记录表（单表+JSON扩展）';

-- ============================================================================
-- 测试数据
-- ============================================================================
#
# -- 1. 创建服务：订单创建通知
# INSERT INTO gotone_notification_service (id, service_code, service_name, description, enabled, priority, created_at,
#                                          updated_at)
# VALUES ('SERVICE_001', 'ORDER_CREATED', '订单创建通知', '用户下单后的通知服务', TRUE, 10, NOW(), NOW());
#
# -- 2. 创建服务：支付成功通知
# INSERT INTO gotone_notification_service (id, service_code, service_name, description, enabled, priority, created_at,
#                                          updated_at)
# VALUES ('SERVICE_002', 'PAYMENT_SUCCESS', '支付成功通知', '用户支付成功后的通知服务', TRUE, 9, NOW(), NOW());
#
# -- 3. 创建服务：验证码通知
# INSERT INTO gotone_notification_service (id, service_code, service_name, description, enabled, priority, created_at,
#                                          updated_at)
# VALUES ('SERVICE_003', 'VERIFICATION_CODE', '验证码通知', '用户登录/注册验证码', TRUE, 10, NOW(), NOW());
#
# -- 4. 配置 ORDER_CREATED 的 EMAIL 渠道
# INSERT INTO gotone_service_channel (id, service_code, channel, template_code, template_content,
#                                     channel_config, provider, fallback_providers, send_strategy,
#                                     retry_config, enabled, priority, created_at, updated_at)
# VALUES ('CHANNEL_001',
#         'ORDER_CREATED',
#         'EMAIL',
#         'ORDER_CREATED_EMAIL',
#         '尊敬的${userName}，您的订单${orderNo}已创建成功。订单金额：${amount}元。',
#         '{
#           "subject": "订单创建通知",
#           "from": "order@loadup.com"
#         }',
#         'smtp',
#         NULL,
#         'ASYNC',
#         '{
#           "maxRetries": 3,
#           "retryInterval": 60
#         }',
#         TRUE,
#         10,
#         NOW(),
#         NOW());
#
# -- 5. 配置 ORDER_CREATED 的 SMS 渠道
# INSERT INTO gotone_service_channel (id, service_code, channel, template_code, template_content,
#                                     channel_config, provider, fallback_providers, send_strategy,
#                                     retry_config, enabled, priority, created_at, updated_at)
# VALUES ('CHANNEL_002',
#         'ORDER_CREATED',
#         'SMS',
#         'ORDER_CREATED_SMS',
#         '您的订单${orderNo}已创建，金额${amount}元。感谢购买！',
#         '{
#           "signName": "LoadUp商城",
#           "templateId": "SMS_123456"
#         }',
#         'aliyun',
#         '[
#           "tencent",
#           "huawei"
#         ]',
#         'ASYNC',
#         '{
#           "maxRetries": 3,
#           "retryInterval": 60
#         }',
#         TRUE,
#         9,
#         NOW(),
#         NOW());
#
# -- 6. 配置 PAYMENT_SUCCESS 的 PUSH 渠道
# INSERT INTO gotone_service_channel (id, service_code, channel, template_code, template_content,
#                                     channel_config, provider, fallback_providers, send_strategy,
#                                     retry_config, enabled, priority, created_at, updated_at)
# VALUES ('CHANNEL_003',
#         'PAYMENT_SUCCESS',
#         'PUSH',
#         'PAYMENT_SUCCESS_PUSH',
#         '支付成功！订单${orderNo}，金额${amount}元。',
#         '{
#           "title": "支付成功",
#           "sound": "default"
#         }',
#         'fcm',
#         NULL,
#         'ASYNC',
#         '{
#           "maxRetries": 2,
#           "retryInterval": 30
#         }',
#         TRUE,
#         10,
#         NOW(),
#         NOW());
#
# -- 7. 配置 VERIFICATION_CODE 的 SMS 渠道
# INSERT INTO gotone_service_channel (id, service_code, channel, template_code, template_content,
#                                     channel_config, provider, fallback_providers, send_strategy,
#                                     retry_config, enabled, priority, created_at, updated_at)
# VALUES ('CHANNEL_004',
#         'VERIFICATION_CODE',
#         'SMS',
#         'VERIFICATION_CODE_SMS',
#         '您的验证码是${code}，${minutes}分钟内有效。',
#         '{
#           "signName": "LoadUp",
#           "templateId": "SMS_VERIFY"
#         }',
#         'aliyun',
#         '[
#           "yunpian",
#           "tencent"
#         ]',
#         'SYNC',
#         '{
#           "maxRetries": 1,
#           "retryInterval": 10
#         }',
#         TRUE,
#         10,
#         NOW(),
#         NOW());


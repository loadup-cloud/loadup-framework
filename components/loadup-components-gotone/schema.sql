-- MySQL Compatible Schema for Gotone Notification Component
-- 规范：所有表必须包含 id/tenant_id/created_at/updated_at/deleted 标准字段

DROP TABLE IF EXISTS gotone_notification_record;
DROP TABLE IF EXISTS gotone_service_channel;
DROP TABLE IF EXISTS gotone_notification_service;

-- ============================================================================
-- 1. 通知服务配置表
-- ============================================================================
CREATE TABLE gotone_notification_service
(
    id           VARCHAR(64)  NOT NULL                                               COMMENT 'ID',
    tenant_id    VARCHAR(64)                                                         COMMENT '租户ID',
    service_code VARCHAR(100) NOT NULL                                               COMMENT '服务代码（唯一）',
    service_name VARCHAR(200)                                                        COMMENT '服务名称',
    description  VARCHAR(500)                                                        COMMENT '服务描述',
    enabled      TINYINT               DEFAULT 1                                     COMMENT '是否启用',
    priority     INT                   DEFAULT 0                                     COMMENT '优先级',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                     COMMENT '创建时间',
    updated_at   DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP              COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0                                     COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_service_code (service_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_enabled   (enabled)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT='通知服务配置表';

-- ============================================================================
-- 2. 服务渠道映射表
-- ============================================================================
CREATE TABLE gotone_service_channel
(
    id                 VARCHAR(64)  NOT NULL                                         COMMENT 'ID',
    tenant_id          VARCHAR(64)                                                   COMMENT '租户ID',
    service_code       VARCHAR(100) NOT NULL                                         COMMENT '服务代码',
    channel            VARCHAR(50)  NOT NULL                                         COMMENT '渠道：EMAIL/SMS/PUSH/WEBHOOK',
    template_code      VARCHAR(100) NOT NULL                                         COMMENT '模板代码',
    template_content   TEXT                                                          COMMENT '模板内容（支持${var}占位符）',
    channel_config     JSON                                                          COMMENT '渠道特定配置(JSON)',
    provider           VARCHAR(64)                                                   COMMENT '主提供商',
    fallback_providers JSON                                                          COMMENT '降级提供商列表(JSON)',
    send_strategy      VARCHAR(32)           DEFAULT 'ASYNC'                         COMMENT '发送策略：SYNC/ASYNC/SCHEDULED',
    retry_config       JSON                                                          COMMENT '重试配置(JSON)',
    enabled            TINYINT               DEFAULT 1                               COMMENT '是否启用',
    priority           INT                   DEFAULT 0                               COMMENT '渠道优先级',
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP               COMMENT '创建时间',
    updated_at         DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP        COMMENT '更新时间',
    deleted            TINYINT      NOT NULL DEFAULT 0                               COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_service_channel (service_code, channel),
    INDEX idx_tenant_id   (tenant_id),
    INDEX idx_service_code(service_code),
    INDEX idx_enabled     (enabled)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT='服务渠道映射表';

-- ============================================================================
-- 3. 通知发送记录表
-- ============================================================================
CREATE TABLE gotone_notification_record
(
    id              VARCHAR(64)  NOT NULL                                            COMMENT 'ID',
    tenant_id       VARCHAR(64)                                                      COMMENT '租户ID',
    service_code    VARCHAR(100) NOT NULL                                            COMMENT '服务代码',
    trace_id        VARCHAR(64)                                                      COMMENT '追踪ID（批量发送时相同）',
    request_id      VARCHAR(100)                                                     COMMENT '请求ID（幂等性）',
    channel         VARCHAR(50)  NOT NULL                                            COMMENT '渠道：EMAIL/SMS/PUSH/WEBHOOK',
    provider        VARCHAR(64)                                                      COMMENT '实际使用的提供商',
    receiver        VARCHAR(255) NOT NULL                                            COMMENT '收件人',
    template_code   VARCHAR(100)                                                     COMMENT '模板代码',
    content         TEXT                                                             COMMENT '实际发送内容',
    channel_data    JSON                                                             COMMENT '渠道特定数据(JSON)',
    status          VARCHAR(32)  NOT NULL                                            COMMENT '状态：PENDING/SUCCESS/FAILED/RETRY',
    error_code      VARCHAR(50)                                                      COMMENT '错误码',
    error_message   TEXT                                                             COMMENT '错误信息',
    retry_count     INT                   DEFAULT 0                                  COMMENT '重试次数',
    max_retries     INT                   DEFAULT 3                                  COMMENT '最大重试次数',
    next_retry_time DATETIME                                                         COMMENT '下次重试时间',
    send_time       DATETIME                                                         COMMENT '发送时间',
    success_time    DATETIME                                                         COMMENT '成功时间',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                  COMMENT '创建时间',
    updated_at      DATETIME              NULL ON UPDATE CURRENT_TIMESTAMP           COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0                                  COMMENT '删除标记',
    PRIMARY KEY (id),
    INDEX idx_tenant_id      (tenant_id),
    INDEX idx_service_code   (service_code),
    INDEX idx_trace_id       (trace_id),
    INDEX idx_request_id     (request_id),
    INDEX idx_channel_status (channel, status),
    INDEX idx_retry          (status, next_retry_time),
    INDEX idx_send_time      (send_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT='通知发送记录表';

-- ============================================================================
-- 测试数据
-- ============================================================================
INSERT INTO gotone_notification_service (id, service_code, service_name, description, enabled, priority, created_at)
VALUES ('SERVICE_001', 'ORDER_CREATED',     '订单创建通知', '用户下单后的通知服务',   1, 10, NOW()),
       ('SERVICE_002', 'PAYMENT_SUCCESS',   '支付成功通知', '用户支付成功后的通知服务', 1, 9,  NOW()),
       ('SERVICE_003', 'VERIFICATION_CODE', '验证码通知',   '用户登录/注册验证码',     1, 10, NOW());

INSERT INTO gotone_service_channel (id, service_code, channel, template_code, template_content, channel_config, provider, send_strategy, retry_config, enabled, priority, created_at)
VALUES
('CHANNEL_001','ORDER_CREATED','EMAIL','ORDER_CREATED_EMAIL','尊敬的${userName}，您的订单${orderNo}已创建成功。','{"subject":"订单创建通知","from":"order@loadup.com"}','smtp','ASYNC','{"maxRetries":3,"retryInterval":60}',1,10,NOW()),
('CHANNEL_002','ORDER_CREATED','SMS','ORDER_CREATED_SMS','您的订单${orderNo}已创建，金额${amount}元。','{"signName":"LoadUp商城","templateId":"SMS_123456"}','aliyun','ASYNC','{"maxRetries":3,"retryInterval":60}',1,9,NOW()),
('CHANNEL_003','PAYMENT_SUCCESS','PUSH','PAYMENT_SUCCESS_PUSH','支付成功！订单${orderNo}，金额${amount}元。','{"title":"支付成功","sound":"default"}','fcm','ASYNC','{"maxRetries":2,"retryInterval":30}',1,10,NOW()),
('CHANNEL_004','VERIFICATION_CODE','SMS','VERIFICATION_CODE_SMS','您的验证码是${code}，${minutes}分钟内有效。','{"signName":"LoadUp","templateId":"SMS_VERIFY"}','aliyun','SYNC','{"maxRetries":1,"retryInterval":10}',1,10,NOW());

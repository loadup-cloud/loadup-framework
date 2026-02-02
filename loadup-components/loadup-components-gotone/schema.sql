-- MySQL Compatible Schema

-- Drop tables if exists (for clean test)
DROP TABLE IF EXISTS gotone_notification_record;
DROP TABLE IF EXISTS gotone_notification_template;
DROP TABLE IF EXISTS gotone_channel_mapping;
DROP TABLE IF EXISTS gotone_business_code;

-- 创建业务代码表
CREATE TABLE gotone_business_code
(
    id            VARCHAR(64) PRIMARY KEY,
    business_code VARCHAR(100) NOT NULL,
    business_name VARCHAR(200),
    description   VARCHAR(500),
    enabled       BOOLEAN   DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_business_code (business_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 创建渠道映射表
CREATE TABLE gotone_channel_mapping
(
    id            VARCHAR(64) PRIMARY KEY,
    business_code VARCHAR(100) NOT NULL,
    channel       VARCHAR(50)  NOT NULL,
    template_code VARCHAR(100) NOT NULL,
    provider_list TEXT,
    priority      INT       DEFAULT 0,
    enabled       BOOLEAN   DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_business_code (business_code),
    KEY idx_channel (channel)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 创建通知模板表
CREATE TABLE gotone_notification_template
(
    id             VARCHAR(64) PRIMARY KEY,
    template_code  VARCHAR(100) NOT NULL,
    template_name  VARCHAR(200),
    channel        VARCHAR(50)  NOT NULL,
    content        TEXT,
    title_template VARCHAR(500),
    template_type  VARCHAR(50),
    enabled        BOOLEAN   DEFAULT TRUE,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_template_code (template_code),
    KEY idx_channel (channel)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 创建通知记录表
CREATE TABLE gotone_notification_record
(
    id            VARCHAR(64) PRIMARY KEY,
    trace_id      VARCHAR(100),
    business_code VARCHAR(100),
    biz_id        VARCHAR(100),
    message_id    VARCHAR(100),
    channel       VARCHAR(50),
    receivers     TEXT,
    template_code VARCHAR(100),
    title         VARCHAR(500),
    content       TEXT,
    provider      VARCHAR(50),
    status        VARCHAR(50),
    retry_count   INT       DEFAULT 0,
    priority      INT       DEFAULT 0,
    error_message TEXT,
    send_time     TIMESTAMP NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_biz_id (biz_id),
    KEY idx_trace_id (trace_id),
    KEY idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 插入测试数据
INSERT INTO gotone_business_code (id, business_code, business_name, description, enabled)
VALUES ('1', 'ORDER_CONFIRM', '订单确认', '订单确认通知', TRUE);

INSERT INTO gotone_business_code (id, business_code, business_name, description, enabled)
VALUES ('2', 'PAYMENT_SUCCESS', '支付成功', '支付成功通知', TRUE);

INSERT INTO gotone_business_code (id, business_code, business_name, description, enabled)
VALUES ('3', 'VERIFICATION_CODE', '验证码', '验证码通知', TRUE);

INSERT INTO gotone_channel_mapping (id, business_code, channel, template_code, provider_list, priority, enabled)
VALUES ('1', 'ORDER_CONFIRM', 'SMS', 'ORDER_CONFIRM_SMS', '["aliyun","tencent"]', 10, TRUE);

INSERT INTO gotone_channel_mapping (id, business_code, channel, template_code, provider_list, priority, enabled)
VALUES ('2', 'ORDER_CONFIRM', 'EMAIL', 'ORDER_CONFIRM_EMAIL', '["smtp"]', 9, TRUE);

INSERT INTO gotone_channel_mapping (id, business_code, channel, template_code, provider_list, priority, enabled)
VALUES ('3', 'PAYMENT_SUCCESS', 'PUSH', 'PAYMENT_SUCCESS_PUSH', '["fcm"]', 8, TRUE);

INSERT INTO gotone_channel_mapping (id, business_code, channel, template_code, provider_list, priority, enabled)
VALUES ('4', 'VERIFICATION_CODE', 'SMS', 'VERIFICATION_CODE_SMS', '["aliyun","yunpian"]', 10, TRUE);

INSERT INTO gotone_notification_template (id, template_code, template_name, channel, content, title_template, template_type, enabled)
VALUES ('1', 'ORDER_CONFIRM_SMS', '订单确认短信', 'SMS', '您的订单${orderId}已确认，感谢您的购买！', NULL, 'SMS', TRUE);

INSERT INTO gotone_notification_template (id, template_code, template_name, channel, content, title_template, template_type, enabled)
VALUES ('2', 'ORDER_CONFIRM_EMAIL', '订单确认邮件', 'EMAIL', '尊敬的${userName}，您的订单${orderId}已确认。', '订单确认通知', 'EMAIL', TRUE);

INSERT INTO gotone_notification_template (id, template_code, template_name, channel, content, title_template, template_type, enabled)
VALUES ('3', 'PAYMENT_SUCCESS_PUSH', '支付成功推送', 'PUSH', '支付成功！订单${orderId}，金额${amount}元。', '支付成功', 'PUSH', TRUE);

INSERT INTO gotone_notification_template (id, template_code, template_name, channel, content, title_template, template_type, enabled)
VALUES ('4', 'VERIFICATION_CODE_SMS', '验证码短信', 'SMS', '您的验证码是${code}，${minutes}分钟内有效。', NULL, 'SMS', TRUE);


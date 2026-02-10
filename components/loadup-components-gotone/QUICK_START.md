# Gotone 通知组件 - 快速开始

## 📦 已实现的 Provider

| 渠道 | Provider | 说明 |
|------|----------|------|
| EMAIL | smtp | SMTP 邮件发送 |
| SMS | aliyun | 阿里云短信 |
| SMS | huawei | 华为云短信 |
| SMS | yunpian | 云片短信 |
| PUSH | fcm | Firebase Cloud Messaging |
| WEBHOOK | dingtalk | 钉钉机器人 |
| WEBHOOK | wechat | 企业微信机器人 |
| WEBHOOK | feishu | 飞书机器人 |

## 🚀 快速开始

### 1. 配置数据库

执行 SQL 创建必要的表：

```sql
-- 服务配置表
CREATE TABLE gotone_notification_service (
    id VARCHAR(64) PRIMARY KEY,
    service_code VARCHAR(100) NOT NULL UNIQUE COMMENT '服务代码',
    service_name VARCHAR(200) NOT NULL COMMENT '服务名称',
    description VARCHAR(500) COMMENT '描述',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_by VARCHAR(64)
);

-- 渠道配置表
CREATE TABLE gotone_service_channel (
    id VARCHAR(64) PRIMARY KEY,
    service_code VARCHAR(100) NOT NULL COMMENT '服务代码',
    channel VARCHAR(50) NOT NULL COMMENT '渠道：EMAIL/SMS/PUSH/WEBHOOK',
    template_code VARCHAR(100) COMMENT '模板代码',
    template_content TEXT COMMENT '模板内容',
    channel_config JSON COMMENT '渠道配置',
    provider VARCHAR(50) NOT NULL COMMENT '提供商',
    fallback_providers JSON COMMENT '降级提供商列表',
    send_strategy VARCHAR(50) DEFAULT 'SYNC' COMMENT '发送策略',
    retry_config JSON COMMENT '重试配置',
    enabled BOOLEAN DEFAULT TRUE,
    priority INT DEFAULT 0 COMMENT '优先级',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_by VARCHAR(64),
    INDEX idx_service_code (service_code)
);

-- 发送记录表
CREATE TABLE gotone_notification_record (
    id VARCHAR(64) PRIMARY KEY,
    service_code VARCHAR(100) NOT NULL,
    trace_id VARCHAR(100) COMMENT '追踪ID',
    request_id VARCHAR(100) UNIQUE COMMENT '请求ID（幂等）',
    channel VARCHAR(50) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    receiver VARCHAR(500) NOT NULL COMMENT '收件人',
    template_code VARCHAR(100),
    content TEXT COMMENT '发送内容',
    channel_data JSON COMMENT '渠道扩展数据',
    status VARCHAR(50) NOT NULL COMMENT '状态',
    error_code VARCHAR(100),
    error_message TEXT,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    next_retry_time DATETIME,
    send_time DATETIME,
    success_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_by VARCHAR(64),
    INDEX idx_trace_id (trace_id),
    INDEX idx_request_id (request_id),
    INDEX idx_status (status),
    INDEX idx_service_code (service_code)
);
```

### 2. 配置 Provider

#### Email (SMTP)

```yaml
spring:
  mail:
    host: smtp.example.com
    port: 587
    username: noreply@example.com
    password: ${MAIL_PASSWORD}
    from-name: LoadUp Notification
```

#### SMS (阿里云)

```yaml
loadup:
  gotone:
    sms:
      aliyun:
        enabled: true
        access-key-id: ${ALIYUN_ACCESS_KEY_ID}
        access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
        sign-name: 您的签名
```

#### SMS (华为云)

```yaml
loadup:
  gotone:
    sms:
      huawei:
        enabled: true
        app-key: ${HUAWEI_APP_KEY}
        app-secret: ${HUAWEI_APP_SECRET}
        sender: ${HUAWEI_SENDER}
        signature: 您的签名
```

#### PUSH (FCM)

```yaml
loadup:
  gotone:
    push:
      fcm:
        enabled: true
        server-key: ${FCM_SERVER_KEY}
        project-id: ${FCM_PROJECT_ID}
```

#### Webhook (钉钉/微信/飞书)

默认启用，无需全局配置。在渠道配置中提供 webhookUrl 即可。

### 3. 配置业务渠道

```sql
-- 创建服务
INSERT INTO gotone_notification_service (id, service_code, service_name, enabled)
VALUES ('1', 'USER_REGISTRATION', '用户注册通知', TRUE);

-- 配置邮件渠道
INSERT INTO gotone_service_channel (
    id, service_code, channel, template_code, template_content,
    channel_config, provider, enabled, priority
) VALUES (
    '1', 'USER_REGISTRATION', 'EMAIL', 'REGISTER_EMAIL',
    '<h1>欢迎 ${username}!</h1><p>您的验证码是：${code}</p>',
    '{"subject":"欢迎注册","html":"true"}',
    'smtp', TRUE, 100
);

-- 配置短信渠道（阿里云）
INSERT INTO gotone_service_channel (
    id, service_code, channel, template_code, template_content,
    channel_config, provider, enabled, priority
) VALUES (
    '2', 'USER_REGISTRATION', 'SMS', 'REGISTER_SMS',
    '【签名】您的验证码是${code}，5分钟内有效。',
    '{"templateId":"SMS_123456789"}',
    'aliyun', TRUE, 90
);

-- 配置钉钉通知
INSERT INTO gotone_service_channel (
    id, service_code, channel, template_code, template_content,
    channel_config, provider, enabled, priority
) VALUES (
    '3', 'SYSTEM_ALERT', 'WEBHOOK', 'ALERT_DINGTALK',
    '## 系统告警\n- 服务：${serviceName}\n- 错误：${errorMessage}',
    '{"webhookUrl":"https://oapi.dingtalk.com/robot/send?access_token=xxx","msgtype":"markdown","atAll":true}',
    'dingtalk', TRUE, 80
);
```

### 4. 使用示例

```java
import io.github.loadup.components.gotone.api.NotificationService;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final NotificationService notificationService;
    
    public void registerUser(String username, String email, String phone) {
        // ... 业务逻辑
        
        // 发送注册通知（自动路由到配置的所有渠道）
        NotificationRequest request = NotificationRequest.builder()
            .serviceCode("USER_REGISTRATION")
            .receivers(List.of(email, phone))
            .templateParams(Map.of(
                "username", username,
                "code", "123456"
            ))
            .build();
        
        NotificationResponse response = notificationService.send(request);
        
        log.info("通知发送结果: success={}, channels={}", 
            response.getSuccess(), response.getChannelResults().size());
    }
}
```

### 5. Webhook 使用示例

```java
// 发送钉钉告警
NotificationRequest request = NotificationRequest.builder()
    .serviceCode("SYSTEM_ALERT")
    .templateParams(Map.of(
        "serviceName", "用户服务",
        "errorMessage", "数据库连接超时"
    ))
    .build();

notificationService.send(request);
```

## 🔧 高级配置

### 降级策略

配置多个提供商，当主提供商失败时自动降级：

```sql
UPDATE gotone_service_channel 
SET fallback_providers = '["huawei", "yunpian"]'
WHERE service_code = 'USER_REGISTRATION' 
AND channel = 'SMS' 
AND provider = 'aliyun';
```

### 异步发送

```java
NotificationRequest request = NotificationRequest.builder()
    .serviceCode("USER_REGISTRATION")
    .receivers(List.of("user@example.com"))
    .async(true)  // 异步发送
    .build();
```

### 重试配置

在渠道配置中设置重试策略：

```json
{
  "maxRetries": 3,
  "retryInterval": 60
}
```

## 📖 完整文档

- [Channels README](./channels/README.md) - 各渠道 Provider 详细说明
- [Gotone README](./README.md) - 组件整体架构和设计

## 🆘 常见问题

### Q: 如何集成真实的 SDK/API？

A: 所有 Provider 都提供了 Mock 实现，在对应的 `mock*Api` 方法中有详细的集成示例代码。将 Mock 代码替换为真实的 SDK 调用即可。

### Q: 如何禁用某个渠道？

A: 在数据库中将对应渠道的 `enabled` 字段设置为 `FALSE`。

### Q: 如何添加新的 Provider？

A: 参考 [Channels README](./channels/README.md) 中的"扩展新渠道"部分。

### Q: 发送失败如何处理？

A: 
1. 查看 `gotone_notification_record` 表中的错误信息
2. 配置降级提供商实现自动切换
3. 配置重试策略实现自动重试

---

现在您可以开始使用 Gotone 通知组件了！ 🎉


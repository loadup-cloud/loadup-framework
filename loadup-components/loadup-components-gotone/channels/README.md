# Gotone Channel Providers

通知渠道提供商实现模块。

## 📦 已实现的渠道

### 📧 Email (邮件)

#### SMTP Provider

基于 Spring Boot Mail 的 SMTP 邮件发送。

**配置示例：**

```yaml
spring:
  mail:
    host: smtp.example.com
    port: 587
    username: noreply@example.com
    password: ${MAIL_PASSWORD}
    from-name: LoadUp Notification
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

**特性：**

- ✅ 支持 HTML 和纯文本邮件
- ✅ 支持 CC/BCC
- ✅ 支持批量发送和单独发送
- ✅ 邮箱地址验证和脱敏
- ✅ 详细的发送状态跟踪

**渠道配置参数（channelConfig）：**

```json
{
  "subject": "邮件主题",
  "from": "sender@example.com",
  "cc": ["cc1@example.com", "cc2@example.com"],
  "bcc": ["bcc@example.com"],
  "html": "true",
  "batch": "false"
}
```

---

### 📱 SMS (短信)

#### Aliyun SMS Provider (阿里云短信)

阿里云短信服务提供商。

**配置示例：**

```yaml
loadup:
  gotone:
    sms:
      aliyun:
        enabled: true
        access-key-id: ${ALIYUN_ACCESS_KEY_ID}
        access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
        sign-name: 您的签名
        region-id: cn-hangzhou
```

**特性：**

- ✅ 支持中国和国际手机号
- ✅ 手机号格式验证和脱敏
- ✅ 详细的发送状态跟踪
- ⚠️ Mock 实现（需集成阿里云 SDK）

**渠道配置参数（channelConfig）：**

```json
{
  "templateId": "SMS_123456789",
  "signName": "可选签名（覆盖全局配置）"
}
```

---

#### Huawei SMS Provider (华为云短信)

华为云短信服务提供商。

**配置示例：**

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
        endpoint: https://smsapi.cn-north-4.myhuaweicloud.com:443
```

**特性：**

- ✅ 支持中国和国际手机号
- ✅ 手机号格式验证和脱敏
- ✅ 详细的发送状态跟踪
- ⚠️ Mock 实现（需集成华为云 SDK）

**渠道配置参数（channelConfig）：**

```json
{
  "templateId": "xxxxx",
  "signature": "可选签名（覆盖全局配置）",
  "sender": "可选发送方（覆盖全局配置）"
}
```

---

#### Yunpian SMS Provider (云片短信)

云片短信服务提供商。

**配置示例：**

```yaml
loadup:
  gotone:
    sms:
      yunpian:
        enabled: true
        api-key: ${YUNPIAN_API_KEY}
        api-url: https://sms.yunpian.com/v2/sms/single_send.json
```

**特性：**

- ✅ 支持中国和国际手机号
- ✅ 手机号格式验证和脱敏
- ✅ 详细的发送状态跟踪
- ⚠️ Mock 实现（需集成云片 API）

---

### 🔔 PUSH (推送)

#### FCM Provider (Firebase Cloud Messaging)

Firebase Cloud Messaging 推送服务提供商。

**配置示例：**

```yaml
loadup:
  gotone:
    push:
      fcm:
        enabled: true
        server-key: ${FCM_SERVER_KEY}
        project-id: ${FCM_PROJECT_ID}
```

**特性：**

- ✅ 支持 Android/iOS 设备推送
- ✅ 设备 Token 验证和脱敏
- ✅ 详细的发送状态跟踪
- ⚠️ Mock 实现（需集成 Firebase SDK）

**渠道配置参数（channelConfig）：**

```json
{
  "title": "推送标题",
  "sound": "default",
  "badge": "1",
  "extras": {
    "customKey": "customValue"
  }
}
```

---

### 🤖 WEBHOOK (机器人通知)

#### Dingtalk Webhook Provider (钉钉机器人)

钉钉群机器人 Webhook 提供商。

**配置示例：**

```yaml
loadup:
  gotone:
    webhook:
      dingtalk:
        enabled: true
```

**渠道配置参数（channelConfig）：**

```json
{
  "webhookUrl": "https://oapi.dingtalk.com/robot/send?access_token=xxx",
  "secret": "SEC...",
  "msgtype": "text",
  "atMobiles": ["13800138000"],
  "atAll": false
}
```

**特性：**

- ✅ 支持文本、Markdown、Link、ActionCard 消息类型
- ✅ 支持 @ 指定人员或全员
- ✅ 支持加签安全验证
- ⚠️ Mock 实现（需集成真实 HTTP 调用）

**参考文档：** [钉钉自定义机器人](https://open.dingtalk.com/document/robots/custom-robot-access)

---

#### Wechat Webhook Provider (企业微信机器人)

企业微信群机器人 Webhook 提供商。

**配置示例：**

```yaml
loadup:
  gotone:
    webhook:
      wechat:
        enabled: true
```

**渠道配置参数（channelConfig）：**

```json
{
  "webhookUrl": "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx",
  "msgtype": "text",
  "mentionedList": ["@all"],
  "mentionedMobileList": ["13800138000"]
}
```

**特性：**

- ✅ 支持文本、Markdown、图片、图文消息类型
- ✅ 支持 @ 指定成员或手机号
- ⚠️ Mock 实现（需集成真实 HTTP 调用）

**参考文档：** [企业微信群机器人配置说明](https://developer.work.weixin.qq.com/document/path/91770)

---

#### Feishu Webhook Provider (飞书机器人)

飞书群机器人 Webhook 提供商。

**配置示例：**

```yaml
loadup:
  gotone:
    webhook:
      feishu:
        enabled: true
```

**渠道配置参数（channelConfig）：**

```json
{
  "webhookUrl": "https://open.feishu.cn/open-apis/bot/v2/hook/xxx",
  "secret": "xxx",
  "msgtype": "text",
  "atAll": false,
  "atUserIds": ["ou_xxx"]
}
```

**特性：**

- ✅ 支持文本、富文本、图片、交互式消息类型
- ✅ 支持 @ 指定用户或全员
- ✅ 支持签名安全验证
- ⚠️ Mock 实现（需集成真实 HTTP 调用）

**参考文档：** [飞书自定义机器人使用指南](https://open.feishu.cn/document/ukTMukTMukTM/ucTM5YjL3ETO24yNxkjN)

---

## 🔧 使用示例

### 1. 通过 ServiceCode 发送（推荐）

```java
@Autowired
private NotificationService notificationService;

// 发送通知（后台自动根据 serviceCode 路由到配置的渠道）
NotificationRequest request = NotificationRequest.builder()
    .serviceCode("USER_REGISTRATION")
    .receivers(List.of("user@example.com", "13800138000"))
    .templateParams(Map.of("username", "张三", "code", "123456"))
    .build();

NotificationResponse response = notificationService.send(request);
```

### 2. 渠道配置示例

在数据库 `gotone_service_channel` 表中配置：

```sql
-- Email 渠道
INSERT INTO gotone_service_channel (
    id, service_code, channel, template_code, template_content,
    channel_config, provider, enabled, priority
) VALUES (
    '1', 'USER_REGISTRATION', 'EMAIL', 'REGISTER_EMAIL',
    '<h1>欢迎 ${username}!</h1><p>您的验证码是：${code}</p>',
    '{"subject":"欢迎注册","html":"true"}',
    'smtp', TRUE, 100
);

-- SMS 渠道（阿里云）
INSERT INTO gotone_service_channel (
    id, service_code, channel, template_code, template_content,
    channel_config, provider, enabled, priority
) VALUES (
    '2', 'USER_REGISTRATION', 'SMS', 'REGISTER_SMS',
    '【签名】您的验证码是${code}，5分钟内有效。',
    '{"templateId":"SMS_123456789"}',
    'aliyun', TRUE, 90
);

-- Webhook 渠道（钉钉）
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

---

## 🚀 扩展新渠道

### 步骤 1: 创建 Provider 类

```java
package io.github.loadup.components.gotone.channel.xxx;

@Slf4j
public class XxxProvider implements NotificationChannelProvider {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.XXX;
    }

    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {
        // 实现发送逻辑
        return ChannelSendResponse.builder()
            .successCount(...)
            .failedCount(...)
            .receiverStatus(...)
            .receiverErrors(...)
            .build();
    }

    @Override
    public boolean isAvailable() {
        return true; // 检查配置是否可用
    }

    @Override
    public String getProviderName() {
        return "xxx";
    }
}
```

### 步骤 2: 创建 AutoConfiguration

```java
@Configuration
public class XxxChannelAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "loadup.gotone.xxx", name = "enabled")
    public NotificationChannelProvider xxxProvider() {
        return new XxxProvider();
    }
}
```

### 步骤 3: 注册到 Spring Boot

创建 `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:

```
io.github.loadup.components.gotone.channel.xxx.config.XxxChannelAutoConfiguration
```

---

## 📊 发送状态说明

### ChannelSendResponse 字段说明

| 字段               | 类型                   | 说明         |
|------------------|----------------------|------------|
| `content`        | String               | 实际发送的内容    |
| `successCount`   | Integer              | 成功发送数量     |
| `failedCount`    | Integer              | 失败发送数量     |
| `receiverStatus` | Map<String, Boolean> | 每个收件人的成功状态 |
| `receiverErrors` | Map<String, String>  | 每个收件人的错误信息 |

### 示例

```json
{
  "content": "您的验证码是123456",
  "successCount": 2,
  "failedCount": 1,
  "receiverStatus": {
    "user1@example.com": true,
    "user2@example.com": true,
    "invalid-email": false
  },
  "receiverErrors": {
    "invalid-email": "无效的邮箱地址格式"
  }
}
```

---

## 🔐 安全建议

1. **敏感信息脱敏**：Provider 实现中已对手机号、邮箱、URL、Token 进行脱敏显示
2. **配置加密**：建议使用环境变量或 Vault 管理敏感配置
3. **日志安全**：避免在日志中记录完整的手机号、邮箱、密钥、Webhook URL 等
4. **Webhook 安全**：启用加签验证（钉钉、飞书支持）

---

## 📝 Provider 汇总

| 渠道      | Provider | 状态     | Mock   |
|---------|----------|--------|--------|
| EMAIL   | smtp     | ✅ 完整实现 | ❌ 真实调用 |
| SMS     | aliyun   | ✅ 完整实现 | ✅ Mock |
| SMS     | huawei   | ✅ 完整实现 | ✅ Mock |
| SMS     | yunpian  | ✅ 完整实现 | ✅ Mock |
| PUSH    | fcm      | ✅ 完整实现 | ✅ Mock |
| WEBHOOK | dingtalk | ✅ 完整实现 | ✅ Mock |
| WEBHOOK | wechat   | ✅ 完整实现 | ✅ Mock |
| WEBHOOK | feishu   | ✅ 完整实现 | ✅ Mock |

---

## 📖 参考文档

- [阿里云短信服务](https://help.aliyun.com/product/44282.html)
- [华为云短信服务](https://support.huaweicloud.com/sms/index.html)
- [云片短信API](https://www.yunpian.com/doc)
- [Spring Boot Mail](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.email)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [钉钉自定义机器人](https://open.dingtalk.com/document/robots/custom-robot-access)
- [企业微信群机器人](https://developer.work.weixin.qq.com/document/path/91770)
- [飞书自定义机器人](https://open.feishu.cn/document/ukTMukTMukTM/ucTM5YjL3ETO24yNxkjN)


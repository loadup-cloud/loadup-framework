# Gotone Email 模块

## 概述

`loadup-components-gotone-binder-email` 是 Gotone 通知组件的邮件发送模块，提供 SMTP 协议的邮件发送实现。

## 特性

- ✅ SMTP 协议支持
- ✅ HTML 邮件
- ✅ 附件支持
- ✅ 抄送/密送
- ✅ TLS/SSL 加密
- ✅ 认证支持

## 快速开始

### 1. 添加依赖

```xml

<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-gotone-provider-email</artifactId>
    <version>${loadup.version}</version>
</dependency>
```

### 2. 配置邮件服务器

```yaml
spring:
  mail:
    host: smtp.example.com
    port: 587
    username: your-email@example.com
    password: your-password
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
      mail.smtp.starttls.required: true
      mail.smtp.connectiontimeout: 5000
      mail.smtp.timeout: 5000
      mail.smtp.writetimeout: 5000
```

### 3. 使用示例

```java

@Autowired
private GotoneNotificationService notificationService;

public void sendEmail() {
    NotificationRequest request = NotificationRequest.builder()
            .businessCode("ORDER_CONFIRM")
            .address("user@example.com")
            .params(Map.of(
                    "userName", "张三",
                    "orderId", "123456",
                    "amount", "299.00"
            ))
            .build();

    NotificationResult result = notificationService.send(request);
}
```

## 提供商实现

### SmtpEmailProvider

基于 Spring Mail 实现的 SMTP 邮件发送：

```java

@Component
@Extension(bizId = "EMAIL", useCase = "smtp", scenario = "default")
public class SmtpEmailProvider implements IEmailProvider {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public SendResult send(SendRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(request.getReceivers().toArray(new String[0]));
            helper.setSubject(request.getTitle());
            helper.setText(request.getContent(), true);  // HTML

            // 添加附件
            if (request.getAttachments() != null) {
                for (Attachment att : request.getAttachments()) {
                    helper.addAttachment(att.getName(), att.getFile());
                }
            }

            mailSender.send(message);
            return SendResult.success();

        } catch (Exception e) {
            return SendResult.failure(e.getMessage());
        }
    }
}
```

## 高级功能

### HTML 邮件模板

```sql
INSERT INTO gotone_notification_template
    (template_code, template_name, channel, content)
VALUES ('ORDER_CONFIRM_EMAIL',
        '订单确认邮件',
        'EMAIL',
        '<html>
            <body>
                <h1>订单确认</h1>
                <p>尊敬的 ${userName}：</p>
                <p>您的订单 <strong>${orderId}</strong> 已确认。</p>
                <p>订单金额：<strong>¥${amount}</strong></p>
            </body>
        </html>');
```

### 发送带附件的邮件

```java
NotificationRequest request = NotificationRequest.builder()
        .businessCode("INVOICE_EMAIL")
        .address("customer@example.com")
        .params(params)
        .attachments(List.of(
                Attachment.builder()
                        .name("invoice.pdf")
                        .file(new FileSystemResource("path/to/invoice.pdf"))
                        .build()
        ))
        .build();
```

### 抄送和密送

```java
NotificationRequest request = NotificationRequest.builder()
        .businessCode("REPORT_EMAIL")
        .address("to@example.com")
        .cc("cc1@example.com,cc2@example.com")  // 抄送
        .bcc("bcc@example.com")                  // 密送
        .params(params)
        .build();
```

## 配置说明

### 基础配置

```yaml
spring:
  mail:
    host: smtp.example.com          # SMTP 服务器地址
    port: 587                        # SMTP 端口（25/465/587）
    username: your-email@example.com # 用户名
    password: your-password          # 密码
    default-encoding: UTF-8          # 默认编码
```

### 高级配置

```yaml
spring:
  mail:
    properties:
      # 认证配置
      mail.smtp.auth: true

      # SSL/TLS 配置
      mail.smtp.ssl.enable: true                    # 启用 SSL
      mail.smtp.starttls.enable: true               # 启用 STARTTLS
      mail.smtp.starttls.required: true             # 要求 STARTTLS

      # 超时配置
      mail.smtp.connectiontimeout: 5000             # 连接超时（毫秒）
      mail.smtp.timeout: 5000                       # 读取超时
      mail.smtp.writetimeout: 5000                  # 写入超时

      # 调试配置
      mail.debug: false                              # 调试模式
```

### 不同邮件服务商配置

#### Gmail

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: app-password  # 使用应用专用密码
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

#### QQ 邮箱

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: your-qq@qq.com
    password: authorization-code  # 使用授权码
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

#### 163 邮箱

```yaml
spring:
  mail:
    host: smtp.163.com
    port: 465
    username: your-email@163.com
    password: authorization-code
    properties:
      mail.smtp.auth: true
      mail.smtp.ssl.enable: true
```

#### 阿里云邮件

```yaml
spring:
  mail:
    host: smtpdm.aliyun.com
    port: 465
    username: your-email@example.com
    password: your-password
    properties:
      mail.smtp.auth: true
      mail.smtp.ssl.enable: true
```

## 常见问题

### 1. 发送失败：Authentication failed

**原因**: 用户名或密码错误

**解决方案**:

- 检查用户名和密码是否正确
- 某些邮箱需要使用"授权码"而不是登录密码
- 确保已开启 SMTP 服务

### 2. 连接超时

**原因**: 网络问题或服务器地址错误

**解决方案**:

```yaml
spring:
  mail:
    properties:
      mail.smtp.connectiontimeout: 10000  # 增加超时时间
```

### 3. SSL/TLS 错误

**原因**: SSL/TLS 配置不正确

**解决方案**:

```yaml
spring:
  mail:
    properties:
      mail.smtp.ssl.trust: smtp.example.com  # 信任的服务器
      mail.smtp.ssl.protocols: TLSv1.2       # 指定协议版本
```

### 4. 邮件被当作垃圾邮件

**解决方案**:

- 配置 SPF 记录
- 配置 DKIM 签名
- 使用真实的发件人地址
- 避免垃圾邮件关键词

## 测试

### 单元测试

```bash
mvn test -pl loadup-components-gotone-binder-email
```

### 测试用例

```java

@Test
public void testSendSimpleEmail() {
    SendRequest request = SendRequest.builder()
            .receivers(List.of("test@example.com"))
            .title("测试邮件")
            .content("这是一封测试邮件")
            .build();

    SendResult result = emailProvider.send(request);

    assertThat(result.isSuccess()).isTrue();
}

@Test
public void testSendHtmlEmail() {
    SendRequest request = SendRequest.builder()
            .receivers(List.of("test@example.com"))
            .title("HTML 测试")
            .content("<html><body><h1>测试</h1></body></html>")
            .build();

    SendResult result = emailProvider.send(request);

    assertThat(result.isSuccess()).isTrue();
}
```

## 性能优化

### 1. 连接池配置

```yaml
spring:
  mail:
    properties:
      mail.smtp.connectionpool.size: 10  # 连接池大小
```

### 2. 异步发送

```java

@Async
public CompletableFuture<NotificationResult> sendEmailAsync(NotificationRequest request) {
    NotificationResult result = notificationService.send(request);
    return CompletableFuture.completedFuture(result);
}
```

### 3. 批量发送

```java
// 使用 BCC 批量发送（不暴露其他收件人）
NotificationRequest request = NotificationRequest.builder()
                .businessCode("NEWSLETTER")
                .address("no-reply@example.com")
                .bcc(String.join(",", emailList))  // 批量收件人
                .params(params)
                .build();
```

## 监控指标

```java
// 发送成功率
gotone.email.send.success.rate

// 发送耗时
gotone.email.send.duration

// 失败次数
gotone.email.send.failure.count
```

## 最佳实践

1. **使用专用的发件邮箱**: 不要使用个人邮箱
2. **配置 SPF/DKIM**: 提高邮件送达率
3. **控制发送频率**: 避免被封号
4. **使用邮件模板**: 统一邮件风格
5. **处理退信**: 监控退信并更新邮箱列表
6. **异步发送**: 不阻塞主流程
7. **日志记录**: 记录发送日志便于排查

## 依赖

```xml

<dependencies>
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-components-gotone-api</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
</dependencies>
```

## 相关文档

- [主文档](../README.md)
- [API 模块](../loadup-components-gotone-api/README.md)
- [扩展指南](../PROVIDER_EXTENSION_GUIDE.md)

## 许可证

GPL-3.0 License

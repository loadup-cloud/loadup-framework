# Gotone Provider 扩展指南

> **版本**: v1.0.0
> **最后更新**: 2025-12-30
> **适用场景**: 扩展自定义通知提供商

## 📚 概述

Gotone 组件基于 **LoadUp Extension** 机制，采用插件化架构设计。业务模块可以非常便捷地扩展自定义 Provider，无需修改核心代码。

### 核心优势

- ✅ **零侵入**: 不需要修改 Gotone 核心代码
- ✅ **热插拔**: Provider 自动发现和注册
- ✅ **多场景**: 支持不同业务场景路由
- ✅ **类型安全**: 基于接口编程，编译时检查
- ✅ **易测试**: 每个 Provider 独立测试

### 已有 Provider

|    渠道     |  提供商  |  useCase  |  状态   |
|-----------|-------|-----------|-------|
| **SMS**   | 阿里云短信 | `aliyun`  | ✅ 已实现 |
| **SMS**   | 腾讯云短信 | `tencent` | ✅ 已实现 |
| **SMS**   | 华为云短信 | `huawei`  | ✅ 已实现 |
| **SMS**   | 云片短信  | `yunpian` | ✅ 已实现 |
| **EMAIL** | SMTP  | `smtp`    | ✅ 已实现 |
| **PUSH**  | FCM   | `fcm`     | ✅ 已实现 |

## 🔌 扩展步骤

### 1. 选择接口类型

根据通知渠道选择对应的接口：

```java
// SMS 短信
public interface ISmsProvider extends IExtensionPoint {
    SendResult send(SendRequest request);

    String getProviderName();
}

// Email 邮件
public interface IEmailProvider extends IExtensionPoint {
    SendResult send(SendRequest request);

    String getProviderName();
}

// Push 推送
public interface IPushProvider extends IExtensionPoint {
    SendResult send(SendRequest request);

    String getProviderName();
}
```

### 2. 创建 Provider 类

**示例：云片短信 Provider**

```java
package com.mycompany.notification.provider;

import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.components.gotone.api.provider.ISmsProvider;
import com.github.loadup.components.gotone.api.provider.model.SendRequest;
import com.github.loadup.components.gotone.api.provider.model.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 云片短信提供商
 */
@Slf4j
@Component
@Extension(bizId = "SMS", useCase = "yunpian", scenario = "default")
public class YunpianSmsProvider implements ISmsProvider {

    @Value("${loadup.gotone.sms.yunpian.api-key}")
    private String apiKey;

    @Value("${loadup.gotone.sms.yunpian.sign-name}")
    private String signName;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public SendResult send(SendRequest request) {
        try {
            log.info("Sending SMS via Yunpian to: {}", request.getReceivers());

            // 1. 构建请求参数
            String content = signName + request.getContent();
            String mobile = String.join(",", request.getReceivers());

            // 2. 调用云片 API
            String apiUrl = "https://sms.yunpian.com/v2/sms/single_send.json";
            String params = String.format("apikey=%s&mobile=%s&text=%s",
                    apiKey, mobile, content);

            String response = restTemplate.postForObject(apiUrl, params, String.class);

            // 3. 解析响应
            if (response.contains("\"code\":0")) {
                log.info("Yunpian SMS sent successfully");
                return SendResult.success();
            } else {
                log.error("Yunpian SMS failed: {}", response);
                return SendResult.failure("API returned error: " + response);
            }

        } catch (Exception e) {
            log.error("Failed to send SMS via Yunpian: {}", e.getMessage(), e);
            return SendResult.failure(e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "yunpian";
    }
}
```

### 3. 配置 @Extension 注解

`@Extension` 注解参数说明：

|      参数      | 必需 |         说明         |                  示例                  |
|--------------|----|--------------------|--------------------------------------|
| **bizId**    | ✅  | 业务ID，对应渠道类型        | `"SMS"`, `"EMAIL"`, `"PUSH"`         |
| **useCase**  | ✅  | 用例，通常是提供商名称        | `"yunpian"`, `"aliyun"`, `"tencent"` |
| **scenario** | ✅  | 场景，默认用 `"default"` | `"default"`, `"vip"`, `"marketing"`  |

**匹配优先级**：

1. **精确匹配**: `bizId` + `useCase` + `scenario`
2. **降级匹配**: `bizId` + `useCase` + `"default"`
3. **默认匹配**: `bizId` + `"default"` + `"default"`

### 4. 添加配置

**application.yml**:

```yaml
loadup:
  gotone:
    sms:
      yunpian:
        enabled: true
        api-key: ${YUNPIAN_API_KEY}
        sign-name: 【您的签名】
```

**环境变量**:

```bash
export YUNPIAN_API_KEY=your-api-key
```

### 5. 数据库配置

在 `gotone_channel_mapping` 表中配置使用你的 Provider：

```sql
-- 方式1: 添加到现有映射的提供商列表
UPDATE gotone_channel_mapping
SET provider_list = '["aliyun", "yunpian", "tencent"]'
WHERE business_code = 'VERIFICATION_CODE'
  AND channel = 'SMS';

-- 方式2: 创建新的渠道映射
INSERT INTO gotone_channel_mapping
    (id, business_code, channel, template_code, provider_list, priority, enabled)
VALUES (UUID(),
        'VIP_NOTIFY',
        'SMS',
        'VIP_NOTIFY_SMS',
        '["yunpian"]',
        10,
        TRUE);
```

### 6. 测试 Provider

```java

@SpringBootTest
class YunpianSmsProviderTest {

    @Autowired
    private YunpianSmsProvider provider;

    @Test
    void testSend() {
        SendRequest request = SendRequest.builder()
                .receivers(List.of("13800138000"))
                .content("验证码：123456")
                .templateParams(Map.of("code", "123456"))
                .build();

        SendResult result = provider.send(request);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void testGetProviderName() {
        assertThat(provider.getProviderName()).isEqualTo("yunpian");
    }
}
```

## 📦 完整示例

### 示例1: 钉钉机器人 Provider

```java
package com.mycompany.notification.provider;

import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.components.gotone.api.provider.IMessageProvider;
import com.github.loadup.components.gotone.api.provider.model.SendRequest;
import com.github.loadup.components.gotone.api.provider.model.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 钉钉机器人通知提供商
 */
@Slf4j
@Component
@Extension(bizId = "DINGTALK", useCase = "robot", scenario = "default")
public class DingtalkRobotProvider implements IMessageProvider {

    @Value("${loadup.gotone.dingtalk.webhook}")
    private String webhook;

    @Value("${loadup.gotone.dingtalk.secret}")
    private String secret;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public SendResult send(SendRequest request) {
        try {
            log.info("Sending Dingtalk message");

            // 1. 构建钉钉消息格式
            Map<String, Object> message = new HashMap<>();
            message.put("msgtype", "text");
            message.put("text", Map.of("content", request.getContent()));

            // 2. 计算签名
            long timestamp = System.currentTimeMillis();
            String sign = calculateSign(timestamp);

            // 3. 构建URL
            String url = String.format("%s&timestamp=%d&sign=%s",
                    webhook, timestamp, sign);

            // 4. 发送请求
            Map<String, Object> response = restTemplate.postForObject(
                    url, message, Map.class);

            // 5. 检查响应
            if (response != null && Integer.valueOf(0).equals(response.get("errcode"))) {
                log.info("Dingtalk message sent successfully");
                return SendResult.success();
            } else {
                String errorMsg = response != null ?
                        String.valueOf(response.get("errmsg")) : "Unknown error";
                log.error("Dingtalk message failed: {}", errorMsg);
                return SendResult.failure(errorMsg);
            }

        } catch (Exception e) {
            log.error("Failed to send Dingtalk message: {}", e.getMessage(), e);
            return SendResult.failure(e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "dingtalk-robot";
    }

    /**
     * 计算钉钉签名
     */
    private String calculateSign(long timestamp) throws Exception {
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return URLEncoder.encode(Base64.getEncoder().encodeToString(signData), "UTF-8");
    }
}
```

**配置**:

```yaml
loadup:
  gotone:
    dingtalk:
      webhook: https://oapi.dingtalk.com/robot/send?access_token=xxx
      secret: SECxxx
```

**数据库配置**:

```sql
INSERT INTO gotone_business_code (id, business_code, business_name, enabled)
VALUES (UUID(), 'SYSTEM_ALERT', '系统告警', TRUE);

INSERT INTO gotone_channel_mapping
    (id, business_code, channel, template_code, provider_list, priority, enabled)
VALUES (UUID(),
        'SYSTEM_ALERT',
        'DINGTALK',
        'SYSTEM_ALERT_DINGTALK',
        '["robot"]',
        10,
        TRUE);

INSERT INTO gotone_notification_template
    (id, template_code, template_name, channel, content, enabled)
VALUES (UUID(),
        'SYSTEM_ALERT_DINGTALK',
        '系统告警钉钉通知',
        'DINGTALK',
        '【告警】${alertType}: ${message}',
        TRUE);
```

### 示例2: 企业微信 Provider

```java
package com.mycompany.notification.provider;

import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.components.gotone.api.provider.IMessageProvider;
import com.github.loadup.components.gotone.api.provider.model.SendRequest;
import com.github.loadup.components.gotone.api.provider.model.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 企业微信应用消息提供商
 */
@Slf4j
@Component
@Extension(bizId = "WECHAT", useCase = "work", scenario = "default")
public class WechatWorkProvider implements IMessageProvider {

    @Value("${loadup.gotone.wechat.corpid}")
    private String corpId;

    @Value("${loadup.gotone.wechat.secret}")
    private String secret;

    @Value("${loadup.gotone.wechat.agentid}")
    private Integer agentId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public SendResult send(SendRequest request) {
        try {
            log.info("Sending Wechat Work message");

            // 1. 获取 access_token
            String accessToken = getAccessToken();

            // 2. 构建消息
            Map<String, Object> message = new HashMap<>();
            message.put("touser", String.join("|", request.getReceivers()));
            message.put("msgtype", "text");
            message.put("agentid", agentId);
            message.put("text", Map.of("content", request.getContent()));

            // 3. 发送消息
            String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token="
                    + accessToken;
            Map<String, Object> response = restTemplate.postForObject(
                    url, message, Map.class);

            // 4. 检查响应
            if (response != null && Integer.valueOf(0).equals(response.get("errcode"))) {
                log.info("Wechat Work message sent successfully");
                return SendResult.success();
            } else {
                String errorMsg = response != null ?
                        String.valueOf(response.get("errmsg")) : "Unknown error";
                log.error("Wechat Work message failed: {}", errorMsg);
                return SendResult.failure(errorMsg);
            }

        } catch (Exception e) {
            log.error("Failed to send Wechat Work message: {}", e.getMessage(), e);
            return SendResult.failure(e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "wechat-work";
    }

    /**
     * 获取企业微信 access_token（缓存2小时）
     */
    @Cacheable(value = "wechat_access_token", key = "'work'")
    private String getAccessToken() {
        String url = String.format(
                "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s",
                corpId, secret);

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response != null && response.containsKey("access_token")) {
            return String.valueOf(response.get("access_token"));
        }

        throw new RuntimeException("Failed to get access token");
    }
}
```

**配置**:

```yaml
loadup:
  gotone:
    wechat:
      corpid: ${WECHAT_CORP_ID}
      secret: ${WECHAT_SECRET}
      agentid: 1000002
```

### 示例3: 极光推送 Provider

```java
package com.mycompany.notification.provider;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.Notification;
import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.components.gotone.api.provider.IPushProvider;
import com.github.loadup.components.gotone.api.provider.model.SendRequest;
import com.github.loadup.components.gotone.api.provider.model.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 极光推送提供商
 */
@Slf4j
@Component
@Extension(bizId = "PUSH", useCase = "jpush", scenario = "default")
public class JPushProvider implements IPushProvider {

    @Value("${loadup.gotone.push.jpush.app-key}")
    private String appKey;

    @Value("${loadup.gotone.push.jpush.master-secret}")
    private String masterSecret;

    private JPushClient jpushClient;

    @Override
    public SendResult send(SendRequest request) {
        try {
            log.info("Sending JPush notification");

            // 1. 初始化客户端（懒加载）
            if (jpushClient == null) {
                jpushClient = new JPushClient(masterSecret, appKey);
            }

            // 2. 构建推送消息
            PushPayload payload = PushPayload.newBuilder()
                    .setPlatform(Platform.all())
                    .setAudience(Audience.registrationId(request.getReceivers()))
                    .setNotification(Notification.alert(request.getContent()))
                    .build();

            // 3. 发送推送
            PushResult result = jpushClient.sendPush(payload);

            // 4. 检查结果
            if (result.isResultOK()) {
                log.info("JPush sent successfully, msgId: {}", result.msg_id);
                return SendResult.success().messageId(result.msg_id);
            } else {
                log.error("JPush failed: {}", result.error);
                return SendResult.failure(result.error.getMessage());
            }

        } catch (Exception e) {
            log.error("Failed to send JPush: {}", e.getMessage(), e);
            return SendResult.failure(e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "jpush";
    }
}
```

**Maven 依赖**:

```xml

<dependency>
    <groupId>cn.jpush.api</groupId>
    <artifactId>jpush-client</artifactId>
    <version>3.6.8</version>
</dependency>
```

**配置**:

```yaml
loadup:
  gotone:
    push:
      jpush:
        enabled: true
        app-key: ${JPUSH_APP_KEY}
        master-secret: ${JPUSH_MASTER_SECRET}
```

## 🎯 最佳实践

### 1. 配置管理

**使用 @ConfigurationProperties 管理配置**:

```java

@Data
@ConfigurationProperties(prefix = "loadup.gotone.sms.custom")
public class CustomSmsConfig {
    private String  apiKey;
    private String  apiUrl;
    private Integer timeout  = 5000;
    private Integer maxRetry = 3;
}

@Component
@Extension(bizId = "SMS", useCase = "custom", scenario = "default")
@RequiredArgsConstructor
public class CustomSmsProvider implements ISmsProvider {

    private final CustomSmsConfig config;

    @Override
    public SendResult send(SendRequest request) {
        // 使用 config.getApiKey()
        return SendResult.success();
    }

    @Override
    public String getProviderName() {
        return "custom";
    }
}
```

### 2. 异常处理

**统一异常处理，返回标准响应**:

```java

@Override
public SendResult send(SendRequest request) {
    try {
        // 发送逻辑
        String result = callApi(request);
        return SendResult.success().messageId(result);

    } catch (TimeoutException e) {
        log.error("Timeout: {}", e.getMessage());
        return SendResult.failure("Request timeout");

    } catch (HttpClientErrorException e) {
        log.error("Client error: {}", e.getMessage());
        return SendResult.failure("Invalid request: " + e.getStatusCode());

    } catch (HttpServerErrorException e) {
        log.error("Server error: {}", e.getMessage());
        return SendResult.failure("Server error: " + e.getStatusCode());

    } catch (Exception e) {
        log.error("Unknown error: {}", e.getMessage(), e);
        return SendResult.failure("Unknown error: " + e.getMessage());
    }
}
```

### 3. 日志记录

**结构化日志，便于追踪**:

```java

@Override
public SendResult send(SendRequest request) {
    String traceId = UUID.randomUUID().toString();
    MDC.put("traceId", traceId);

    try {
        log.info("Sending notification: provider={}, receivers={}",
                getProviderName(), request.getReceivers());

        SendResult result = doSend(request);

        if (result.isSuccess()) {
            log.info("Notification sent successfully: messageId={}",
                    result.getMessageId());
        } else {
            log.error("Notification failed: error={}",
                    result.getMessage());
        }

        return result;

    } finally {
        MDC.clear();
    }
}
```

### 4. 超时控制

**设置合理的超时时间**:

```java

@Component
@Extension(bizId = "SMS", useCase = "custom", scenario = "default")
public class CustomSmsProvider implements ISmsProvider {

    private final RestTemplate restTemplate;

    public CustomSmsProvider() {
        // 配置超时
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public SendResult send(SendRequest request) {
        // 使用配置了超时的 restTemplate
        return SendResult.success();
    }

    @Override
    public String getProviderName() {
        return "custom";
    }
}
```

### 5. 重试机制

**实现智能重试**:

```java

@Component
@Extension(bizId = "SMS", useCase = "custom", scenario = "default")
public class CustomSmsProvider implements ISmsProvider {

    private static final int  MAX_RETRY   = 3;
    private static final long RETRY_DELAY = 1000L;

    @Override
    public SendResult send(SendRequest request) {
        for (int i = 0; i < MAX_RETRY; i++) {
            try {
                SendResult result = doSend(request);

                if (result.isSuccess()) {
                    return result;
                }

                // 判断是否可重试
                if (!isRetryable(result)) {
                    return result;
                }

                // 等待后重试
                if (i < MAX_RETRY - 1) {
                    Thread.sleep(RETRY_DELAY * (i + 1));
                    log.info("Retrying... attempt {}/{}", i + 2, MAX_RETRY);
                }

            } catch (Exception e) {
                if (i == MAX_RETRY - 1) {
                    return SendResult.failure(e.getMessage());
                }
            }
        }

        return SendResult.failure("Max retry exceeded");
    }

    private boolean isRetryable(SendResult result) {
        // 某些错误不应该重试（如参数错误）
        String message = result.getMessage();
        return !message.contains("invalid") &&
                !message.contains("forbidden");
    }

    @Override
    public String getProviderName() {
        return "custom";
    }
}
```

### 6. 批量发送优化

**支持批量API调用**:

```java

@Override
public SendResult send(SendRequest request) {
    List<String> receivers = request.getReceivers();

    // 单个接收人，使用单发API
    if (receivers.size() == 1) {
        return sendSingle(request);
    }

    // 多个接收人，判断是否支持批量
    if (receivers.size() <= 100 && supportsBatch()) {
        return sendBatch(request);
    }

    // 超过批量限制，分批发送
    return sendInBatches(request, 100);
}

private SendResult sendInBatches(SendRequest request, int batchSize) {
    List<String> receivers = request.getReceivers();
    List<String> successIds = new ArrayList<>();
    List<String> failedIds = new ArrayList<>();

    for (int i = 0; i < receivers.size(); i += batchSize) {
        int end = Math.min(i + batchSize, receivers.size());
        List<String> batch = receivers.subList(i, end);

        SendRequest batchRequest = request.toBuilder()
                .receivers(batch)
                .build();

        SendResult result = sendBatch(batchRequest);

        if (result.isSuccess()) {
            successIds.addAll(batch);
        } else {
            failedIds.addAll(batch);
        }
    }

    if (failedIds.isEmpty()) {
        return SendResult.success();
    } else if (successIds.isEmpty()) {
        return SendResult.failure("All batches failed");
    } else {
        return SendResult.failure(
                String.format("Partial success: %d/%d",
                        successIds.size(), receivers.size()));
    }
}
```

### 7. 限流保护

**使用限流器防止过载**:

```java

@Component
@Extension(bizId = "SMS", useCase = "custom", scenario = "default")
public class CustomSmsProvider implements ISmsProvider {

    // 每秒最多10个请求
    private final RateLimiter rateLimiter = RateLimiter.create(10.0);

    @Override
    public SendResult send(SendRequest request) {
        // 尝试获取许可，最多等待1秒
        if (!rateLimiter.tryAcquire(1, TimeUnit.SECONDS)) {
            log.warn("Rate limit exceeded");
            return SendResult.failure("Rate limit exceeded");
        }

        return doSend(request);
    }

    @Override
    public String getProviderName() {
        return "custom";
    }
}
```

### 8. 参数验证

**发送前验证参数**:

```java

@Override
public SendResult send(SendRequest request) {
    // 验证接收人
    if (request.getReceivers() == null || request.getReceivers().isEmpty()) {
        return SendResult.failure("Receivers cannot be empty");
    }

    // 验证手机号格式（短信）
    for (String phone : request.getReceivers()) {
        if (!isValidPhone(phone)) {
            return SendResult.failure("Invalid phone number: " + phone);
        }
    }

    // 验证内容
    if (request.getContent() == null || request.getContent().trim().isEmpty()) {
        return SendResult.failure("Content cannot be empty");
    }

    // 验证内容长度
    if (request.getContent().length() > 500) {
        return SendResult.failure("Content too long (max 500 chars)");
    }

    return doSend(request);
}

private boolean isValidPhone(String phone) {
    return phone != null && phone.matches("^1[3-9]\\d{9}$");
}
```

### 9. 缓存令牌

**缓存 access_token 等**:

```java

@Component
@Extension(bizId = "WECHAT", useCase = "work", scenario = "default")
public class WechatWorkProvider implements IMessageProvider {

    private String        cachedToken;
    private LocalDateTime tokenExpireTime;

    @Override
    public SendResult send(SendRequest request) {
        String token = getAccessToken();
        // 使用 token 发送消息
        return SendResult.success();
    }

    private synchronized String getAccessToken() {
        // 检查缓存是否有效
        if (cachedToken != null &&
                tokenExpireTime != null &&
                LocalDateTime.now().isBefore(tokenExpireTime)) {
            return cachedToken;
        }

        // 重新获取 token
        cachedToken = fetchAccessToken();
        tokenExpireTime = LocalDateTime.now().plusHours(2);

        return cachedToken;
    }

    private String fetchAccessToken() {
        // 调用API获取token
        return "new_token";
    }

    @Override
    public String getProviderName() {
        return "wechat-work";
    }
}
```

### 10. 单元测试

**编写全面的单元测试**:

```java

@SpringBootTest
class CustomSmsProviderTest {

    @Autowired
    private CustomSmsProvider provider;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void testSendSuccess() {
        // Given
        SendRequest request = SendRequest.builder()
                .receivers(List.of("13800138000"))
                .content("Test message")
                .build();

        when(restTemplate.postForObject(any(), any(), eq(String.class)))
                .thenReturn("{\"code\":0}");

        // When
        SendResult result = provider.send(request);

        // Then
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void testSendWithInvalidPhone() {
        // Given
        SendRequest request = SendRequest.builder()
                .receivers(List.of("invalid"))
                .content("Test")
                .build();

        // When
        SendResult result = provider.send(request);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Invalid phone");
    }

    @Test
    void testSendWithEmptyContent() {
        // Given
        SendRequest request = SendRequest.builder()
                .receivers(List.of("13800138000"))
                .content("")
                .build();

        // When
        SendResult result = provider.send(request);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("empty");
    }

    @Test
    void testGetProviderName() {
        assertThat(provider.getProviderName()).isEqualTo("custom");
    }
}
```

## 🔗 参考资源

- [Gotone 主文档](README.md) - 组件概述和快速开始
- [架构设计](ARCHITECTURE.md) - 详细的架构说明
- [API 模块文档](loadup-components-gotone-api/README.md) - 核心接口说明
- [配置指南](CONFIGURATION.md) - 完整的配置说明
- [测试文档](loadup-components-gotone-test/README.md) - 测试最佳实践

## ❓ 常见问题

### Q1: Provider 没有被扫描到？

**症状**: 调用时提示找不到 Provider

**排查步骤**:

1. 检查类上是否有 `@Component` 注解
2. 检查类上是否有 `@Extension` 注解
3. 检查类是否在 Spring 扫描路径下
4. 检查包名是否正确

**解决方案**:

```java
// ✅ 正确
@Component  // 必须有
@Extension(bizId = "SMS", useCase = "custom", scenario = "default")
public class CustomSmsProvider implements ISmsProvider {
    // ...
}

// ❌ 错误 - 缺少 @Component
@Extension(bizId = "SMS", useCase = "custom", scenario = "default")
public class CustomSmsProvider implements ISmsProvider {
    // ...
}
```

### Q2: 如何调试 Provider 匹配过程？

**启用 DEBUG 日志**:

```yaml
logging:
  level:
    com.github.loadup.components.extension: DEBUG
    com.github.loadup.components.gotone: DEBUG
```

**查看日志输出**:

```
DEBUG Extension matched: bizId=SMS, useCase=aliyun, scenario=default
DEBUG Provider found: AliyunSmsProvider
```

### Q3: 如何实现 Provider 降级？

**配置多个提供商**:

```sql
UPDATE gotone_channel_mapping
SET provider_list = '["primary", "backup1", "backup2"]'
WHERE business_code = 'MY_BUSINESS';
```

**执行顺序**:

1. 优先使用 `primary`
2. 失败则使用 `backup1`
3. 再失败则使用 `backup2`

### Q4: 如何测试 Provider？

**单元测试示例**:

```java

@SpringBootTest
class CustomSmsProviderTest {

    @Autowired
    private CustomSmsProvider provider;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void testSend() {
        // Mock 外部API调用
        when(restTemplate.postForObject(any(), any(), eq(String.class)))
                .thenReturn("{\"code\":0}");

        SendRequest request = SendRequest.builder()
                .receivers(List.of("13800138000"))
                .content("Test")
                .build();

        SendResult result = provider.send(request);

        assertThat(result.isSuccess()).isTrue();
    }
}
```

### Q5: Provider 配置项如何管理？

**使用 @ConfigurationProperties**:

```java

@Data
@Component
@ConfigurationProperties(prefix = "loadup.gotone.sms.custom")
public class CustomSmsConfig {
    private String  apiKey;
    private String  apiSecret;
    private String  signName;
    private Integer timeout = 5000;
}
```

**在 application.yml 中配置**:

```yaml
loadup:
  gotone:
    sms:
      custom:
        api-key: ${CUSTOM_API_KEY}
        api-secret: ${CUSTOM_API_SECRET}
        sign-name: 【公司名】
        timeout: 10000
```

### Q6: 如何处理 API 限流？

**实现限流保护**:

```java

@Component
@Extension(bizId = "SMS", useCase = "custom", scenario = "default")
public class CustomSmsProvider implements ISmsProvider {

    private final RateLimiter rateLimiter = RateLimiter.create(10.0); // 每秒10个

    @Override
    public SendResult send(SendRequest request) {
        if (!rateLimiter.tryAcquire(1, TimeUnit.SECONDS)) {
            return SendResult.failure("Rate limit exceeded");
        }

        return doSend(request);
    }

    @Override
    public String getProviderName() {
        return "custom";
    }
}
```

### Q7: 如何实现异步发送？

**使用 @Async 注解**:

```java

@Service
public class NotificationAsyncService {

    @Autowired
    private GotoneNotificationService notificationService;

    @Async("notificationExecutor")
    public CompletableFuture<NotificationResult> sendAsync(NotificationRequest request) {
        NotificationResult result = notificationService.send(request);
        return CompletableFuture.completedFuture(result);
    }
}
```

**配置线程池**:

```yaml
loadup:
  gotone:
    executor:
      core-pool-size: 10
      max-pool-size: 50
      queue-capacity: 1000
```

### Q8: 如何添加自定义渠道？

**步骤**:

1. 定义新的渠道接口
2. 实现 Provider
3. 配置渠道映射

**示例**:

```java
// 1. 定义接口
public interface IVoiceProvider extends IExtensionPoint {
    SendResult send(SendRequest request);

    String getProviderName();
}

// 2. 实现 Provider
@Component
@Extension(bizId = "VOICE", useCase = "aliyun", scenario = "default")
public class AliyunVoiceProvider implements IVoiceProvider {
    @Override
    public SendResult send(SendRequest request) {
        // 语音通知实现
        return SendResult.success();
    }

    @Override
    public String getProviderName() {
        return "aliyun-voice";
    }
}

// 3. 数据库配置
INSERT INTO

gotone_channel_mapping
        (id, business_code, channel, template_code, provider_list, priority, enabled)

VALUES(UUID(), 'URGENT_ALERT','VOICE','URGENT_ALERT_VOICE',
        '["aliyun"]',10,TRUE);
```

### Q9: Provider 抛出异常怎么办？

**Provider 应该捕获所有异常并返回 SendResult**:

```java

@Override
public SendResult send(SendRequest request) {
    try {
        // 发送逻辑
        return SendResult.success();
    } catch (Exception e) {
        log.error("Send failed: {}", e.getMessage(), e);
        // 返回失败结果，不要抛出异常
        return SendResult.failure(e.getMessage());
    }
}
```

### Q10: 如何监控 Provider 性能？

**添加监控指标**:

```java

@Component
@Extension(bizId = "SMS", useCase = "custom", scenario = "default")
public class CustomSmsProvider implements ISmsProvider {

    @Autowired
    private MeterRegistry meterRegistry;

    @Override
    public SendResult send(SendRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            SendResult result = doSend(request);

            // 记录成功/失败
            meterRegistry.counter("gotone.sms.send",
                    "provider", "custom",
                    "status", result.isSuccess() ? "success" : "failure"
            ).increment();

            return result;

        } finally {
            sample.stop(Timer.builder("gotone.sms.duration")
                    .tag("provider", "custom")
                    .register(meterRegistry));
        }
    }

    @Override
    public String getProviderName() {
        return "custom";
    }
}
```

## 📞 获取帮助

### 文档资源

- [GitHub Issues](https://github.com/loadup-cloud/loadup-framework/issues) - 报告问题
- [主文档](README.md) - 组件概述
- [配置指南](CONFIGURATION.md) - 配置说明

### 联系方式

- Email: support@loadup-cloud.com
- 企业技术支持: 提供付费技术支持服务

---

**版本**: v1.0.0
**最后更新**: 2025-12-30
**维护团队**: LoadUp Cloud Team

🎉 **扩展 Provider 就是这么简单！开始创建你的第一个自定义 Provider 吧！**

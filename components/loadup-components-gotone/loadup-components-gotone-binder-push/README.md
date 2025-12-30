# Gotone Push 模块

## 概述

`loadup-components-gotone-binder-push` 是 Gotone 通知组件的推送通知模块，支持移动端消息推送。

## 支持的提供商

- ✅ **Firebase Cloud Messaging (FCM)** - Google 推送服务
- 🔄 **极光推送 (JPush)** - 计划支持
- 🔄 **个推 (GeTui)** - 计划支持
- 🔄 **小米推送** - 计划支持

## 快速开始

### 1. 添加依赖

```xml

<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-gotone-binder-push</artifactId>
    <version>${loadup.version}</version>
</dependency>
```

### 2. 配置 FCM

```yaml
loadup:
  gotone:
    push:
      fcm:
        server-key: ${FCM_SERVER_KEY}
        sender-id: ${FCM_SENDER_ID}  # 可选
        api-url: https://fcm.googleapis.com/fcm/send  # 可选
        timeout: 5000  # 超时时间（毫秒）
```

### 3. 使用示例

```java

@Autowired
private GotoneNotificationService notificationService;

public void sendPush() {
    NotificationRequest request = NotificationRequest.builder()
            .businessCode("ORDER_SHIPPED")
            .address("device_token_here")  // 设备 Token
            .params(Map.of(
                    "orderId", "123456",
                    "trackingNumber", "SF1234567890"
            ))
            .build();

    NotificationResult result = notificationService.send(request);
}
```

## Firebase Cloud Messaging (FCM)

### 配置步骤

#### 1. 创建 Firebase 项目

1. 访问 [Firebase Console](https://console.firebase.google.com/)
2. 创建新项目或选择现有项目
3. 添加应用（Android/iOS/Web）

#### 2. 获取 Server Key

1. 进入项目设置
2. 选择"Cloud Messaging"标签
3. 复制"服务器密钥"（Server Key）

#### 3. 配置应用

```yaml
loadup:
  gotone:
    push:
      fcm:
        server-key: AAAAxxxxxxx:xxxxxxxxxxxxxxxxxxx
        # 可选配置
        validate-token: true  # 验证设备 Token
        batch-size: 100       # 批量发送大小
```

### 发送类型

#### 1. 通知消息（Notification Message）

显示在系统通知栏：

```java
NotificationRequest request = NotificationRequest.builder()
        .businessCode("PROMOTION_PUSH")
        .address(deviceToken)
        .params(Map.of(
                "title", "限时优惠",
                "body", "全场5折，快来抢购！",
                "icon", "ic_notification",
                "color", "#FF0000"
        ))
        .build();
```

#### 2. 数据消息（Data Message）

不显示通知，由应用处理：

```java
NotificationRequest request = NotificationRequest.builder()
        .businessCode("SILENT_UPDATE")
        .address(deviceToken)
        .params(Map.of(
                "type", "update",
                "version", "2.0.0",
                "silent", "true"
        ))
        .build();
```

#### 3. 混合消息

同时包含通知和数据：

```java
NotificationRequest request = NotificationRequest.builder()
        .businessCode("ORDER_STATUS_PUSH")
        .address(deviceToken)
        .params(Map.of(
                "title", "订单状态更新",
                "body", "您的订单已发货",
                "orderId", "123456",
                "status", "shipped"
        ))
        .build();
```

### 高级功能

#### 1. 主题订阅

发送给订阅特定主题的所有设备：

```java
NotificationRequest request = NotificationRequest.builder()
        .businessCode("TOPIC_MESSAGE")
        .address("/topics/news")  // 主题名称
        .params(params)
        .build();
```

#### 2. 条件消息

根据条件发送：

```java
// 发送给订阅 A 或 B 的用户
.address("'TopicA' in topics || 'TopicB' in topics")

// 发送给同时订阅 A 和 B 的用户
.

address("'TopicA' in topics && 'TopicB' in topics")
```

#### 3. 批量发送

```java
List<String> tokens = Arrays.asList("token1", "token2", "token3");
NotificationRequest request = NotificationRequest.builder()
        .businessCode("BATCH_PUSH")
        .address(String.join(",", tokens))  // 多个 token 用逗号分隔
        .params(params)
        .build();
```

#### 4. 优先级设置

```java
NotificationRequest request = NotificationRequest.builder()
        .businessCode("URGENT_PUSH")
        .address(deviceToken)
        .priority(10)  // 高优先级
        .params(params)
        .build();
```

## 消息模板

### 通知模板

```sql
INSERT INTO gotone_notification_template
    (template_code, template_name, channel, content, title_template)
VALUES ('ORDER_SHIPPED_PUSH',
        '订单发货推送',
        'PUSH',
        '您的订单已发货，物流单号：${trackingNumber}',
        '订单${orderId}已发货');
```

### 使用模板

```java
NotificationRequest request = NotificationRequest.builder()
        .businessCode("ORDER_SHIPPED")
        .address(deviceToken)
        .params(Map.of(
                "orderId", "123456",
                "trackingNumber", "SF1234567890"
        ))
        .build();

// 系统会自动渲染模板：
// 标题：订单123456已发货
// 内容：您的订单已发货，物流单号：SF1234567890
```

## 常见问题

### 1. 推送未送达

**可能原因**:

- 设备 Token 无效或过期
- 应用未在前台且未配置后台推送
- 用户关闭了通知权限
- 网络问题

**解决方案**:

```java
// 1. 验证 Token
boolean isValid = fcmProvider.validateToken(deviceToken);

// 2. 检查发送结果
if(!result.

isSuccess()){
        log.

error("Push failed: {}",result.getMessage());
        // 处理失败情况（如更新 Token 状态）
        }
```

### 2. Token 过期处理

```java

@Service
public class DeviceTokenService {

    public void handleInvalidToken(String token) {
        // 1. 标记 Token 为无效
        deviceTokenRepository.markAsInvalid(token);

        // 2. 通知客户端更新 Token
        // ...
    }
}
```

### 3. iOS 推送证书问题

FCM 需要正确配置 APNs 证书：

1. 在 Firebase Console 上传 APNs 证书
2. 确保证书未过期
3. 验证 Bundle ID 匹配

## 测试

### 单元测试

```bash
mvn test -pl loadup-components-gotone-binder-push
```

### 测试用例

```java

@Test
public void testSendNotification() {
    SendRequest request = SendRequest.builder()
            .receivers(List.of(testToken))
            .title("Test Push")
            .content("This is a test")
            .build();

    SendResult result = pushProvider.send(request);

    assertThat(result.isSuccess()).isTrue();
}

@Test
public void testSendToTopic() {
    SendRequest request = SendRequest.builder()
            .receivers(List.of("/topics/test"))
            .title("Topic Test")
            .content("Test message")
            .build();

    SendResult result = pushProvider.send(request);

    assertThat(result.isSuccess()).isTrue();
}
```

## 性能优化

### 1. 批量发送

```java
// FCM 支持一次发送给最多 1000 个设备
List<String> tokens = getDeviceTokens();
int batchSize = 1000;

for(
int i = 0; i <tokens.

size();

i +=batchSize){
List<String> batch = tokens.subList(i, Math.min(i + batchSize, tokens.size()));

sendBatch(batch);
}
```

### 2. 异步发送

```java

@Async
public CompletableFuture<NotificationResult> sendPushAsync(NotificationRequest request) {
    return CompletableFuture.completedFuture(notificationService.send(request));
}
```

### 3. Token 缓存

```java

@Cacheable(value = "deviceTokens", key = "#userId")
public String getDeviceToken(String userId) {
    return deviceTokenRepository.findByUserId(userId);
}
```

## 监控指标

```java
// 发送成功率
gotone.push.send.success.rate

// Token 有效率
gotone.push.token.valid.rate

// 各平台使用情况
gotone.push.platform.

usage {platform = "android"}
gotone.push.platform.

usage {platform = "ios"}

// 发送延迟
gotone.push.send.latency
```

## 最佳实践

1. **Token 管理**:
    - 定期更新 Token
    - 处理过期 Token
    - 多设备场景处理

2. **消息分类**:
    - 重要消息（订单、支付）
    - 营销消息（促销、活动）
    - 系统消息（更新、公告）

3. **推送时机**:
    - 避开用户休息时间
    - 考虑时区差异
    - 合理控制频率

4. **用户体验**:
    - 支持用户订阅/取消订阅
    - 提供推送设置选项
    - 避免骚扰用户

5. **安全性**:
    - 保护 Server Key
    - 验证 Token 来源
    - 加密敏感数据

## 平台对比

| 特性     | FCM   | JPush | GeTui | 小米推送  |
|--------|-------|-------|-------|-------|
| 国际化    | ✅ 优秀  | ⚠️ 一般 | ❌ 弱   | ❌ 弱   |
| 国内到达率  | ⚠️ 一般 | ✅ 优秀  | ✅ 优秀  | ✅ 优秀  |
| 价格     | 免费    | 付费    | 免费+付费 | 免费    |
| 文档     | ✅ 详细  | ✅ 详细  | ⚠️ 一般 | ⚠️ 一般 |
| SDK 质量 | ✅ 优秀  | ✅ 优秀  | ⚠️ 一般 | ⚠️ 一般 |

## 依赖

```xml

<dependencies>
    <dependency>
        <groupId>com.github.loadup.components</groupId>
        <artifactId>loadup-components-gotone-api</artifactId>
    </dependency>

    <!-- Firebase Admin SDK -->
    <dependency>
        <groupId>com.google.firebase</groupId>
        <artifactId>firebase-admin</artifactId>
    </dependency>

    <!-- HTTP Client -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

## 相关文档

- [主文档](../README.md)
- [API 模块](../loadup-components-gotone-api/README.md)
- [FCM 官方文档](https://firebase.google.com/docs/cloud-messaging)

## 许可证

GPL-3.0 License


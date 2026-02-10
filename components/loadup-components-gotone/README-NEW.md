# Gotone 通知组件 - ServiceCode 驱动架构

## 概述

Gotone 是一个基于 **ServiceCode 驱动的智能路由** 通知组件，支持多渠道（EMAIL/SMS/PUSH）通知发送。

### 核心特性

- ✅ **ServiceCode 驱动** - 业务代码与渠道解耦，配置化管理
- ✅ **智能路由** - 自动识别收件人类型，智能分发到对应渠道
- ✅ **多渠道支持** - 一次调用，多渠道并发发送
- ✅ **动态配置** - 数据库配置，支持动态启用/禁用渠道
- ✅ **幂等性保证** - 基于 requestId 防止重复发送
- ✅ **失败重试** - 自动重试机制
- ✅ **单表+JSON** - 简化数据层，3张表替代原来的7张表

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>io.github.loadup</groupId>
    <artifactId>loadup-components-gotone-starter</artifactId>
    <version>${loadup.version}</version>
</dependency>
```

### 2. 配置数据库

执行 `schema.sql` 创建表结构：

```sql
-- 3张核心表
- gotone_notification_service      -- 服务配置
- gotone_service_channel            -- 渠道映射
- gotone_notification_record        -- 发送记录（单表+JSON）
```

### 3. 配置服务和渠道

```sql
-- 创建服务
INSERT INTO gotone_notification_service (id, service_code, service_name, enabled)
VALUES ('1', 'ORDER_CREATED', '订单创建通知', TRUE);

-- 配置 EMAIL 渠道
INSERT INTO gotone_service_channel (
    id, service_code, channel, template_code, template_content,
    channel_config, provider, enabled, priority
) VALUES (
    '1001', 
    'ORDER_CREATED', 
    'EMAIL', 
    'ORDER_CREATED_EMAIL',
    '尊敬的${userName}，您的订单${orderNo}已创建成功。',
    '{"subject": "订单创建通知", "from": "order@example.com"}',
    'smtp',
    TRUE,
    10
);

-- 配置 SMS 渠道
INSERT INTO gotone_service_channel (
    id, service_code, channel, template_code, template_content,
    channel_config, provider, enabled, priority
) VALUES (
    '1002', 
    'ORDER_CREATED', 
    'SMS', 
    'ORDER_CREATED_SMS',
    '您的订单${orderNo}已创建，感谢购买！',
    '{"signName": "LoadUp商城", "templateId": "SMS_123456"}',
    'aliyun',
    TRUE,
    9
);
```

### 4. 业务代码调用

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final NotificationService notificationService;
    
    public void createOrder(Order order) {
        // ... 创建订单逻辑 ...
        
        // ✅ 发送通知（自动根据配置发送 EMAIL + SMS）
        notificationService.send(NotificationRequest.builder()
            .serviceCode("ORDER_CREATED")  // 只需指定业务代码
            .receivers(List.of(
                order.getUserEmail(),      // 自动识别为 EMAIL
                order.getUserPhone()       // 自动识别为 SMS
            ))
            .templateParams(Map.of(
                "userName", order.getUserName(),
                "orderNo", order.getOrderNo()
            ))
            .requestId("ORDER_" + order.getId())  // 幂等性
            .build());
    }
}
```

## 核心概念

### ServiceCode（服务代码）

业务标识，例如：
- `ORDER_CREATED` - 订单创建
- `PAYMENT_SUCCESS` - 支付成功
- `VERIFICATION_CODE` - 验证码

### Channel（渠道）

支持的通知渠道：
- `EMAIL` - 邮件
- `SMS` - 短信
- `PUSH` - 推送

### 智能路由

系统自动根据收件人格式识别渠道：
- 邮箱格式（`xxx@xxx.com`）→ EMAIL
- 手机号格式（`1xxxxxxxxxx`）→ SMS
- 其他 → PUSH

## 高级特性

### 1. 动态启用/禁用渠道

```sql
-- 禁用 EMAIL 渠道（不需要修改代码）
UPDATE gotone_service_channel 
SET enabled = FALSE 
WHERE service_code = 'ORDER_CREATED' AND channel = 'EMAIL';
```

### 2. 指定渠道发送

```java
// 只发送 EMAIL
notificationService.send(NotificationRequest.builder()
    .serviceCode("ORDER_CREATED")
    .receivers(receivers)
    .templateParams(params)
    .channels(List.of("EMAIL"))  // 指定渠道
    .build());
```

### 3. 异步发送

```java
// 异步发送（不阻塞）
notificationService.sendAsync(request);
```

### 4. 幂等性保证

```java
notificationService.send(NotificationRequest.builder()
    .serviceCode("ORDER_CREATED")
    .receivers(receivers)
    .templateParams(params)
    .requestId("ORDER_12345")  // 同一个 requestId 只发送一次
    .build());
```

## 架构对比

| 维度 | 旧架构 | 新架构（ServiceCode驱动） | 改进 |
|------|--------|-------------------------|------|
| **数据表** | 7张表 | 3张表 | -57% |
| **DO类** | 7个 | 3个 | -57% |
| **Repository** | 7个 | 3个 | -57% |
| **查询链路** | 3次查询 | 1次查询 | -67% |
| **写入次数** | 2次/条 | 1次/条 | -50% |
| **扩展性** | 需改表改代码 | 只需配置 | ✓ |
| **业务解耦** | ✗ | ✓ | ✓ |

## 数据库表结构

### 1. gotone_notification_service

服务配置表：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(64) | 主键 |
| service_code | VARCHAR(100) | 服务代码（唯一） |
| service_name | VARCHAR(200) | 服务名称 |
| enabled | BOOLEAN | 是否启用 |

### 2. gotone_service_channel

渠道映射表（核心）：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(64) | 主键 |
| service_code | VARCHAR(100) | 服务代码 |
| channel | VARCHAR(50) | 渠道：EMAIL/SMS/PUSH |
| template_content | TEXT | 模板内容 |
| channel_config | JSON | 渠道配置 |
| provider | VARCHAR(64) | 提供商 |
| fallback_providers | JSON | 降级提供商列表 |
| enabled | BOOLEAN | 是否启用 |
| priority | INT | 优先级 |

### 3. gotone_notification_record

发送记录表（单表+JSON）：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(64) | 主键 |
| service_code | VARCHAR(100) | 服务代码 |
| trace_id | VARCHAR(64) | 追踪ID |
| request_id | VARCHAR(100) | 请求ID（幂等性） |
| channel | VARCHAR(50) | 渠道 |
| provider | VARCHAR(64) | 提供商 |
| receiver | VARCHAR(255) | 收件人 |
| content | TEXT | 发送内容 |
| **channel_data** | **JSON** | **渠道扩展数据** |
| status | VARCHAR(32) | 状态 |
| retry_count | INT | 重试次数 |

## 扩展开发

### 实现自定义渠道提供商

```java
@Component
public class CustomSmsProvider implements NotificationChannelProvider {
    
    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }
    
    @Override
    public String getProviderName() {
        return "custom";
    }
    
    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {
        // 实现发送逻辑
        return ChannelSendResponse.builder()
            .content(request.getContent())
            .successCount(request.getReceivers().size())
            .failedCount(0)
            .build();
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
}
```

## 常见问题

### 1. 如何添加新的业务通知？

只需在数据库中配置：
1. 在 `gotone_notification_service` 添加服务记录
2. 在 `gotone_service_channel` 配置渠道和模板
3. 业务代码调用 `notificationService.send()`

### 2. 如何临时禁用某个渠道？

```sql
UPDATE gotone_service_channel 
SET enabled = FALSE 
WHERE service_code = 'xxx' AND channel = 'xxx';
```

### 3. 如何查看发送记录？

```sql
SELECT * FROM gotone_notification_record 
WHERE service_code = 'ORDER_CREATED' 
AND trace_id = 'xxx';
```

## License

GPL-3.0 License


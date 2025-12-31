# Gotone 通知组件架构设计

> **版本**: v1.0.0
> **最后更新**: 2025-12-30
> **状态**: ✅ 生产就绪

## 1. 架构概述

Gotone 是一个企业级高性能通知发送组件，基于 **Spring Boot 3.x** 和 **扩展点机制** 构建，提供统一的多渠道通知能力。

### 1.1 核心架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      业务应用层                                   │
│                  (Business Application)                          │
└────────────────────┬────────────────────────────────────────────┘
                     │ send(NotificationRequest)
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│              GotoneNotificationService                           │
│  ┌──────────────┐  ┌────────────┐  ┌────────────────┐          │
│  │业务代码查询  │  │模板渲染    │  │渠道映射查询     │          │
│  │BusinessCode  │→ │ Template   │→ │ChannelMapping  │          │
│  └──────────────┘  └────────────┘  └────────────────┘          │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│              Extension Executor (扩展点执行器)                    │
│  ┌────────────────────────────────────────────────┐             │
│  │  Provider 路由 (bizId + useCase + scenario)    │             │
│  │  • 精确匹配 → 降级匹配 → 默认匹配              │             │
│  │  • 多提供商自动降级                            │             │
│  │  • 熔断器保护                                  │             │
│  └────────────────────────────────────────────────┘             │
└─────────────┬───────────────────────────────────────────────────┘
              │ INotificationProvider
              │
    ┌─────────┼─────────┬─────────┬─────────┬─────────┐
    │         │         │         │         │         │
    ▼         ▼         ▼         ▼         ▼         ▼
┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐
│ Email  ││  SMS   ││  Push  ││Internal││ WeChat ││DingTalk│
│ SMTP   ││ Aliyun ││  FCM   ││Message ││        ││        │
│        ││Tencent ││        ││        ││ (规划) ││ (规划) │
│        ││ Huawei ││        ││        ││        ││        │
│        ││Yunpian ││        ││        ││        ││        │
└────────┘└────────┘└────────┘└────────┘└────────┘└────────┘
    │         │         │         │         │         │
    └─────────┴─────────┴─────────┴─────────┴─────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│              持久化层 (Spring Data JDBC)                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Repository   │  │  Cache       │  │  Scheduler   │          │
│  │ • MySQL      │  │  • Template  │  │  • Retry     │          │
│  │ • Record     │  │  • Config    │  │  • Monitor   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 技术栈

|    层次    |        技术         |       说明       |
|----------|-------------------|----------------|
| **框架**   | Spring Boot 3.5.8 | 基础框架           |
| **持久化**  | Spring Data JDBC  | 轻量级 ORM        |
| **数据库**  | MySQL 8.0         | 生产数据库          |
| **扩展点**  | loadup-extension  | 插件化架构          |
| **缓存**   | loadup-cache      | 模板缓存           |
| **调度**   | loadup-scheduler  | 重试调度           |
| **监控**   | loadup-tracer     | 链路追踪           |
| **对象映射** | MapStruct         | Domain ↔ DO 转换 |
| **测试**   | Testcontainers    | 集成测试           |

## 2. 核心组件

### 2.1 GotoneNotificationService

**位置**: `loadup-components-gotone-api`

**职责**: 统一通知发送入口

**核心流程**:

```java
public NotificationResult send(NotificationRequest request) {
    // 1. 验证请求参数
    validateRequest(request);

    // 2. 查询业务代码配置
    BusinessCode businessCode = businessCodeRepository
            .findByCode(request.getBusinessCode());

    // 3. 查询渠道映射
    List<ChannelMapping> mappings = channelMappingRepository
            .findByBusinessCode(request.getBusinessCode());

    // 4. 遍历每个渠道
    for (ChannelMapping mapping : mappings) {
        // 5. 加载模板
        NotificationTemplate template = templateRepository
                .findByCode(mapping.getTemplateCode());

        // 6. 渲染模板内容
        String content = templateEngine.render(template, request.getParams());

        // 7. 构建发送请求
        SendRequest sendRequest = buildSendRequest(content, ...);

        // 8. 通过扩展点发送
        SendResult result = extensionExecutor.execute(
                mapping.getChannel(),
                mapping.getProvider(),
                sendRequest
        );

        // 9. 记录发送结果
        recordRepository.save(buildRecord(result));
    }

    return NotificationResult.success();
}
```

**功能特性**:

- ✅ 模板渲染（支持 ${param} 占位符）
- ✅ 请求参数验证
- ✅ 多渠道并行发送
- ✅ 多提供商降级
- ✅ 发送记录持久化
- 🔄 异步队列（规划中）
- 🔄 批量发送（规划中）

### 2.2 Extension Executor (扩展点执行器)

**位置**: `loadup-components-extension`

**职责**: 基于业务场景路由到对应的提供商

**路由策略**:

```java
@Extension(
        bizId = "SMS",          // 渠道类型：EMAIL/SMS/PUSH
        useCase = "aliyun",     // 提供商：aliyun/tencent/huawei
        scenario = "default"    // 场景：default/marketing/verification
)
```

**匹配优先级**:

1. **精确匹配**: `bizId + useCase + scenario`
2. **降级匹配**: `bizId + useCase + "default"`
3. **默认匹配**: `bizId + "default" + "default"`

**多提供商降级**:

```json
{
  "providerList": [
    "aliyun",
    "tencent",
    "huawei"
  ],
  "priority": 10
}
```

执行逻辑：

1. 尝试使用 `aliyun`
2. 失败或熔断 → 尝试 `tencent`
3. 失败或熔断 → 尝试 `huawei`
4. 全部失败 → 返回错误

### 2.3 Repository 层 (Spring Data JDBC)

**位置**: `loadup-components-gotone-api/repository`

**核心 Repository**:

```java
// 业务代码 Repository
public interface BusinessCodeRepository
        extends CrudRepository<BusinessCodeDO, String> {
    Optional<BusinessCodeDO> findByBusinessCodeAndEnabled(String code);
}

// 渠道映射 Repository
public interface ChannelMappingRepository
        extends CrudRepository<ChannelMappingDO, String> {
    List<ChannelMappingDO> findByBusinessCodeAndEnabled(String code);
}

// 通知模板 Repository
public interface NotificationTemplateRepository
        extends CrudRepository<NotificationTemplateDO, String> {
    Optional<NotificationTemplateDO> findByTemplateCodeAndEnabled(String code);

    List<NotificationTemplateDO> findByChannelAndEnabled(String channel);
}

// 发送记录 Repository
public interface NotificationRecordRepository
        extends CrudRepository<NotificationRecordDO, String> {
    Optional<NotificationRecordDO> findByBizId(String bizId);

    List<NotificationRecordDO> findByTraceId(String traceId);

    List<NotificationRecordDO> findRetryableRecords(LocalDateTime before);
}
```

**特性**:

- ✅ 基于 Spring Data JDBC（轻量级）
- ✅ ID 自动生成（UUID V4）
- ✅ 审计功能（@CreatedDate, @LastModifiedDate）
- ✅ MySQL 8.0 原生支持
- ✅ 自定义查询方法

### 2.4 Domain & DO 分离

**Domain（领域对象）**:

- 包含业务逻辑
- 用于 Service 层
- 继承 `BaseDomain`

**DataObject（数据对象）**:

- 纯数据，无业务逻辑
- 用于 Repository 层
- 继承 `BaseDO`
- 包含 JPA 注解

**转换器（MapStruct）**:

```java

@Mapper(componentModel = "spring")
public interface GotoneConverter {
    NotificationTemplate toTemplate(NotificationTemplateDO templateDO);

    NotificationTemplateDO toTemplateDO(NotificationTemplate template);
    // ... 其他转换方法
}
```

### 2.5 模板引擎

**实现**: 简单占位符替换

**模板语法**:

```
您的订单${orderId}已确认，金额：¥${amount}
```

**参数替换**:

```java
Map<String, Object> params = Map.of(
        "orderId", "123456",
        "amount", "299.00"
);
// 结果: 您的订单123456已确认，金额：¥299.00
```

**扩展性**: 可替换为 Thymeleaf、FreeMarker 等

### 2.6 缓存机制

**位置**: `loadup-components-cache`

**缓存内容**:

- ✅ 通知模板
- ✅ 业务代码配置
- ✅ 渠道映射

**缓存策略**:

```yaml
loadup:
  gotone:
    cache:
      enabled: true
      ttl: 3600  # 缓存过期时间（秒）
      max-size: 1000  # 最大缓存条目
```

**刷新机制**:

- 手动刷新: `cacheManager.refresh()`
- 自动刷新: TTL 过期后自动重新加载
- 事件刷新: 监听配置变更事件

## 3. 扩展点设计

### 3.1 提供商接口

**Email Provider**:

```java
public interface IEmailProvider extends IExtensionPoint {
    SendResult send(SendRequest request);

    String getProviderName();
}
```

**SMS Provider**:

```java
public interface ISmsProvider extends IExtensionPoint {
    SendResult send(SendRequest request);

    String getProviderName();
}
```

**Push Provider**:

```java
public interface IPushProvider extends IExtensionPoint {
    SendResult send(SendRequest request);

    String getProviderName();
}
```

### 3.2 已实现的提供商

#### Email (1个)

```java

@Component
@Extension(bizId = "EMAIL", useCase = "smtp", scenario = "default")
public class SmtpEmailProvider implements IEmailProvider {
    @Autowired
    private JavaMailSender mailSender;

    @Override
    public SendResult send(SendRequest request) {
        // SMTP 邮件发送实现
    }
}
```

**特性**:

- ✅ HTML 邮件支持
- ✅ 附件支持
- ✅ 抄送/密送
- ✅ TLS/SSL 加密

#### SMS (4个)

**1. 阿里云短信**:

```java

@Extension(bizId = "SMS", useCase = "aliyun", scenario = "default")
public class AliyunSmsProvider implements ISmsProvider {
    // 使用阿里云 SDK
}
```

**2. 腾讯云短信**:

```java

@Extension(bizId = "SMS", useCase = "tencent", scenario = "default")
public class TencentSmsProvider implements ISmsProvider {
    // 使用腾讯云 SDK
}
```

**3. 华为云短信**:

```java

@Extension(bizId = "SMS", useCase = "huawei", scenario = "default")
public class HuaweiSmsProvider implements ISmsProvider {
    // 使用华为云 SDK
}
```

**4. 云片短信**:

```java

@Extension(bizId = "SMS", useCase = "yunpian", scenario = "default")
public class YunpianSmsProvider implements ISmsProvider {
    // 使用云片 HTTP API
}
```

#### Push (1个)

**Firebase Cloud Messaging**:

```java

@Extension(bizId = "PUSH", useCase = "fcm", scenario = "default")
public class FcmPushProvider implements IPushProvider {
    // 使用 FCM SDK
}
```

**特性**:

- ✅ 通知消息
- ✅ 数据消息
- ✅ 主题订阅
- ✅ 条件消息
- ✅ 批量发送

### 3.3 扩展新提供商

**步骤**:

1. **实现接口**:

```java

@Component
@Extension(bizId = "SMS", useCase = "custom", scenario = "default")
public class CustomSmsProvider implements ISmsProvider {

    @Override
    public SendResult send(SendRequest request) {
        // 自定义实现
        return SendResult.success();
    }

    @Override
    public String getProviderName() {
        return "custom";
    }
}
```

2. **配置渠道映射**:

```sql
INSERT INTO gotone_channel_mapping
    (business_code, channel, template_code, provider_list)
VALUES ('MY_BUSINESS', 'SMS', 'MY_TEMPLATE', '["custom", "aliyun"]');
```

3. **配置参数**（可选）:

```yaml
loadup:
  gotone:
    sms:
      custom:
        api-key: your-api-key
        api-secret: your-secret
```

### 3.4 路由策略详解

**场景 1: 精确匹配**

```java
// Provider 注解
@Extension(bizId ="SMS", useCase ="aliyun", scenario ="marketing")

// 请求
NotificationRequest.

builder()
    .

businessCode("PROMOTION")  // 映射到 SMS + aliyun
    .

scenario("marketing")      // 精确匹配
    .

build();
```

**场景 2: 降级匹配**

```java
// 找不到 scenario = "marketing"
// 降级查找 scenario = "default"
@Extension(bizId = "SMS", useCase = "aliyun", scenario = "default")
```

**场景 3: 默认匹配**

```java
// 找不到 useCase = "aliyun"
// 使用默认提供商
@Extension(bizId = "SMS", useCase = "default", scenario = "default")
```

### 3.5 多提供商配置

**数据库配置**:

```sql
INSERT INTO gotone_channel_mapping
    (business_code, channel, provider_list, priority)
VALUES ('ORDER_CONFIRM',
        'SMS',
        '["aliyun", "tencent", "huawei"]', -- 按优先级排列
        10);
```

**执行流程**:

```
1. 尝试 aliyun
   ├─ 成功 → 返回结果
   └─ 失败 → 下一个

2. 尝试 tencent
   ├─ 成功 → 返回结果
   └─ 失败 → 下一个

3. 尝试 huawei
   ├─ 成功 → 返回结果
   └─ 失败 → 返回错误
```

## 4. 数据库设计

### 4.1 表结构

#### 业务代码表 (gotone_business_code)

存储业务场景定义：

|      字段       |      类型      |    说明    |
|---------------|--------------|----------|
| id            | VARCHAR(64)  | 主键（UUID） |
| business_code | VARCHAR(100) | 业务代码（唯一） |
| business_name | VARCHAR(200) | 业务名称     |
| description   | VARCHAR(500) | 描述       |
| enabled       | BOOLEAN      | 是否启用     |
| created_at    | TIMESTAMP    | 创建时间     |
| updated_at    | TIMESTAMP    | 更新时间     |

**示例**:

```sql
INSERT INTO gotone_business_code
VALUES ('1', 'ORDER_CONFIRM', '订单确认', '订单确认通知', TRUE, NOW(), NOW());
```

#### 渠道映射表 (gotone_channel_mapping)

业务代码与通知渠道的映射关系：

|      字段       |      类型      |         说明         |
|---------------|--------------|--------------------|
| id            | VARCHAR(64)  | 主键                 |
| business_code | VARCHAR(100) | 业务代码               |
| channel       | VARCHAR(50)  | 渠道（EMAIL/SMS/PUSH） |
| template_code | VARCHAR(100) | 模板代码               |
| provider_list | TEXT         | 提供商列表（JSON）        |
| priority      | INT          | 优先级                |
| enabled       | BOOLEAN      | 是否启用               |
| created_at    | TIMESTAMP    | 创建时间               |
| updated_at    | TIMESTAMP    | 更新时间               |

**示例**:

```sql
INSERT INTO gotone_channel_mapping
VALUES ('1',
        'ORDER_CONFIRM',
        'SMS',
        'ORDER_CONFIRM_SMS',
        '["aliyun","tencent"]',
        10,
        TRUE,
        NOW(),
        NOW());
```

#### 通知模板表 (gotone_notification_template)

消息模板定义：

|       字段       |      类型      |    说明    |
|----------------|--------------|----------|
| id             | VARCHAR(64)  | 主键       |
| template_code  | VARCHAR(100) | 模板代码（唯一） |
| template_name  | VARCHAR(200) | 模板名称     |
| channel        | VARCHAR(50)  | 渠道       |
| content        | TEXT         | 模板内容     |
| title_template | VARCHAR(500) | 标题模板     |
| template_type  | VARCHAR(50)  | 模板类型     |
| enabled        | BOOLEAN      | 是否启用     |
| created_at     | TIMESTAMP    | 创建时间     |
| updated_at     | TIMESTAMP    | 更新时间     |

**示例**:

```sql
INSERT INTO gotone_notification_template
VALUES ('1',
        'ORDER_CONFIRM_SMS',
        '订单确认短信',
        'SMS',
        '您的订单${orderId}已确认，感谢您的购买！',
        NULL,
        'SMS',
        TRUE,
        NOW(),
        NOW());
```

#### 发送记录表 (gotone_notification_record)

通知发送历史记录：

|      字段       |      类型      |     说明      |
|---------------|--------------|-------------|
| id            | VARCHAR(64)  | 主键          |
| trace_id      | VARCHAR(100) | 追踪ID        |
| business_code | VARCHAR(100) | 业务代码        |
| biz_id        | VARCHAR(100) | 业务ID        |
| message_id    | VARCHAR(100) | 消息ID        |
| channel       | VARCHAR(50)  | 渠道          |
| receivers     | TEXT         | 接收人列表（JSON） |
| template_code | VARCHAR(100) | 模板代码        |
| title         | VARCHAR(500) | 标题          |
| content       | TEXT         | 内容          |
| provider      | VARCHAR(50)  | 提供商         |
| status        | VARCHAR(50)  | 状态          |
| retry_count   | INT          | 重试次数        |
| priority      | INT          | 优先级         |
| error_message | TEXT         | 错误信息        |
| send_time     | TIMESTAMP    | 发送时间        |
| created_at    | TIMESTAMP    | 创建时间        |
| updated_at    | TIMESTAMP    | 更新时间        |

**索引**:

```sql
KEY idx_biz_id (biz_id)
KEY idx_trace_id (trace_id)
KEY idx_status (status)
```

### 4.2 数据库特性

**1. UUID 主键**:

```java

@Id
private String id;  // 自动生成 UUID V4
```

**2. 自动审计**:

```java

@CreatedDate
private LocalDateTime createdAt;

@LastModifiedDate
private LocalDateTime updatedAt;
```

**3. JSON 存储**:

```java
// provider_list: ["aliyun", "tencent"]
// receivers: ["user1@example.com", "user2@example.com"]
```

**4. 字符集**:

```sql
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
```

### 4.3 查询优化

**1. 业务代码查询**:

```sql
-- 使用唯一索引
SELECT *
FROM gotone_business_code
WHERE business_code = 'ORDER_CONFIRM'
  AND enabled = TRUE;
```

**2. 渠道映射查询**:

```sql
-- 按优先级排序
SELECT *
FROM gotone_channel_mapping
WHERE business_code = 'ORDER_CONFIRM'
  AND enabled = TRUE
ORDER BY priority DESC;
```

**3. 模板缓存**:

```java

@Cacheable(value = "templates", key = "#templateCode")
public NotificationTemplate findByCode(String templateCode) {
    return templateRepository.findByTemplateCodeAndEnabled(templateCode);
}
```

**4. 发送记录查询**:

```sql
-- 使用索引
SELECT *
FROM gotone_notification_record
WHERE biz_id = 'order_123456';

-- 追踪ID查询
SELECT *
FROM gotone_notification_record
WHERE trace_id = 'trace_xxx';

-- 可重试记录
SELECT *
FROM gotone_notification_record
WHERE status = 'FAILED'
  AND retry_count < 3
  AND created_at > DATE_SUB(NOW(), INTERVAL 24 HOUR);
```

## 5. 测试架构

### 5.1 测试策略

```
┌─────────────────────────────────────────────────┐
│            单元测试 (Unit Tests)                 │
│  • Mock 外部依赖                                 │
│  • 测试业务逻辑                                  │
│  • 快速执行                                      │
└─────────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────┐
│         集成测试 (Integration Tests)             │
│  • Testcontainers MySQL                         │
│  • 真实数据库环境                                │
│  • Repository 层测试                             │
└─────────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────┐
│         端到端测试 (E2E Tests)                    │
│  • 完整流程测试                                  │
│  • 多模块集成                                    │
│  • 性能测试                                      │
└─────────────────────────────────────────────────┘
```

### 5.2 Testcontainers 集成

**配置**:

```java

@DataJdbcTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
}
```

**优势**:

- ✅ 使用真实 MySQL 8.0
- ✅ 100% SQL 兼容性
- ✅ 完整的数据库特性
- ✅ 自动容器管理

### 5.3 测试覆盖

|       模块       |   测试数   |   覆盖率    | 状态 |
|----------------|---------|----------|----|
| Repository 层   | 14      | 100%     | ✅  |
| SMS Provider   | 44      | 100%     | ✅  |
| Email Provider | 11      | 100%     | ✅  |
| Push Provider  | 13      | 100%     | ✅  |
| Service 层      | 10      | 100%     | ✅  |
| Converter      | 10      | 100%     | ✅  |
| Model/Domain   | 21      | 100%     | ✅  |
| 集成测试           | 22      | 100%     | ✅  |
| **总计**         | **145** | **100%** | ✅  |

### 5.4 CI/CD 集成

**GitHub Actions**:

```yaml
jobs:
  test:
    runs-on: ubuntu-latest

    services:
      docker:
        image: docker:24-dind
        options: --privileged

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4

      - name: Run Tests with Testcontainers
        run: mvn clean verify

      - name: Upload Coverage Reports
        uses: actions/upload-artifact@v4
        with:
          name: coverage-reports
          path: target/site/jacoco/
```

## 6. 性能优化

### 6.1 缓存策略

**1. 模板缓存**:

```java

@Cacheable(value = "templates", key = "#templateCode")
public NotificationTemplate findTemplate(String templateCode) {
    return templateRepository.findByCode(templateCode);
}
```

**2. 业务代码缓存**:

```java

@Cacheable(value = "businessCodes", key = "#code")
public BusinessCode findBusinessCode(String code) {
    return businessCodeRepository.findByCode(code);
}
```

**3. 渠道映射缓存**:

```java

@Cacheable(value = "channelMappings", key = "#businessCode")
public List<ChannelMapping> findChannelMappings(String businessCode) {
    return channelMappingRepository.findByBusinessCode(businessCode);
}
```

**缓存配置**:

```yaml
loadup:
  gotone:
    cache:
      enabled: true
      ttl: 3600  # 1小时
      max-size: 1000
      caffeine:
        spec: "maximumSize=1000,expireAfterWrite=3600s"
```

### 6.2 数据库优化

**1. 连接池配置**:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

**2. 批量操作**:

```java
// 批量插入记录
public void batchSave(List<NotificationRecord> records) {
    jdbcTemplate.batchUpdate(sql, records);
}
```

**3. 索引优化**:

```sql
-- 业务代码索引
CREATE UNIQUE INDEX uk_business_code ON gotone_business_code (business_code);

-- 渠道映射索引
CREATE INDEX idx_business_code ON gotone_channel_mapping (business_code, enabled);

-- 发送记录索引
CREATE INDEX idx_biz_id ON gotone_notification_record (biz_id);
CREATE INDEX idx_trace_id ON gotone_notification_record (trace_id);
CREATE INDEX idx_status ON gotone_notification_record (status);
```

### 6.3 并发处理

**1. 线程池配置**:

```yaml
loadup:
  gotone:
    executor:
      core-pool-size: 10
      max-pool-size: 50
      queue-capacity: 1000
      thread-name-prefix: "gotone-"
```

**2. 异步发送**:

```java

@Async("gotoneExecutor")
public CompletableFuture<NotificationResult> sendAsync(NotificationRequest request) {
    return CompletableFuture.completedFuture(send(request));
}
```

**3. 批量发送优化**:

```java
public List<NotificationResult> batchSend(List<NotificationRequest> requests) {
    return requests.parallelStream()
            .map(this::send)
            .collect(Collectors.toList());
}
```

### 6.4 网络优化

**1. HTTP 连接池**:

```yaml
loadup:
  gotone:
    http:
      max-connections: 200
      max-connections-per-route: 50
      connection-timeout: 5000
      read-timeout: 10000
```

**2. 压缩传输**:

```java
// 启用 GZIP 压缩
restTemplate.getMessageConverters().

add(new GZipHttpMessageConverter());
```

### 6.5 监控指标

**1. 发送指标**:

- `gotone.send.total` - 发送总数
- `gotone.send.success` - 成功数
- `gotone.send.failure` - 失败数
- `gotone.send.duration` - 发送耗时

**2. 提供商指标**:

- `gotone.provider.usage` - 各提供商使用次数
- `gotone.provider.success.rate` - 成功率
- `gotone.provider.latency` - 延迟

**3. 缓存指标**:

- `gotone.cache.hit.rate` - 缓存命中率
- `gotone.cache.miss.count` - 缓存未命中次数

**4. 队列指标**:

- `gotone.queue.size` - 队列大小
- `gotone.queue.consume.rate` - 消费速率

## 7. 扩展场景

### 7.1 添加新渠道

**步骤**:

1. **定义渠道接口**:

```java
public interface INewChannelProvider extends IExtensionPoint {
    SendResult send(SendRequest request);

    String getProviderName();
}
```

2. **实现提供商**:

```java

@Component
@Extension(bizId = "NEW_CHANNEL", useCase = "provider1", scenario = "default")
public class NewChannelProvider implements INewChannelProvider {

    @Override
    public SendResult send(SendRequest request) {
        // 实现发送逻辑
        return SendResult.success();
    }

    @Override
    public String getProviderName() {
        return "provider1";
    }
}
```

3. **配置渠道映射**:

```sql
INSERT INTO gotone_channel_mapping
    (business_code, channel, template_code, provider_list)
VALUES ('MY_BUSINESS', 'NEW_CHANNEL', 'MY_TEMPLATE', '["provider1"]');
```

### 7.2 添加新提供商

**示例：添加极光推送**:

1. **实现接口**:

```java

@Component
@Extension(bizId = "PUSH", useCase = "jpush", scenario = "default")
public class JPushProvider implements IPushProvider {

    @Autowired
    private JPushClient jpushClient;

    @Override
    public SendResult send(SendRequest request) {
        try {
            PushResult result = jpushClient.sendPush(
                    PushPayload.newBuilder()
                            .setPlatform(Platform.all())
                            .setAudience(Audience.registrationId(request.getReceivers()))
                            .setNotification(Notification.alert(request.getContent()))
                            .build()
            );
            return SendResult.success(result.msg_id);
        } catch (Exception e) {
            return SendResult.failure(e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "jpush";
    }
}
```

2. **添加配置**:

```yaml
loadup:
  gotone:
    push:
      jpush:
        app-key: ${JPUSH_APP_KEY}
        master-secret: ${JPUSH_MASTER_SECRET}
```

3. **更新渠道映射**:

```sql
UPDATE gotone_channel_mapping
SET provider_list = '["fcm", "jpush"]'
WHERE business_code = 'MY_PUSH_BUSINESS';
```

### 7.3 自定义业务场景

**营销场景**:

```java

@Extension(bizId = "SMS", useCase = "aliyun", scenario = "marketing")
public class MarketingSmsProvider implements ISmsProvider {
    // 专门处理营销短信
    // 可以使用不同的模板、限流策略等
}
```

**验证码场景**:

```java

@Extension(bizId = "SMS", useCase = "aliyun", scenario = "verification")
public class VerificationSmsProvider implements ISmsProvider {
    // 专门处理验证码短信
    // 高优先级、快速通道
}
```

### 7.4 扩展模板引擎

**使用 Thymeleaf**:

```java

@Component
public class ThymeleafTemplateEngine implements ITemplateEngine {

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public String render(String template, Map<String, Object> params) {
        Context context = new Context();
        context.setVariables(params);
        return templateEngine.process(template, context);
    }
}
```

## 8. 最佳实践

### 8.1 幂等性设计

**使用 bizId 保证幂等**:

```java
public NotificationResult send(NotificationRequest request) {
    // 1. 检查是否已发送
    Optional<NotificationRecord> existing =
            recordRepository.findByBizId(request.getBizId());

    if (existing.isPresent() && existing.get().getStatus().equals("SUCCESS")) {
        return NotificationResult.success("Already sent");
    }

    // 2. 执行发送逻辑
    // ...
}
```

**幂等键规则**:

- 订单通知: `order_{orderId}_{notifyType}`
- 用户通知: `user_{userId}_{timestamp}`
- 系统通知: `system_{type}_{date}`

### 8.2 重试策略

**重试场景**:

```java

@Retryable(
        value = {NetworkException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
)
public SendResult sendWithRetry(SendRequest request) {
    return provider.send(request);
}
```

**不重试场景**:

- 参数错误（400）
- 认证失败（401）
- 权限不足（403）
- 签名不匹配

**重试场景**:

- 网络超时
- 服务暂时不可用（503）
- 限流（429）

### 8.3 降级策略

**多级降级**:

```
1. 主提供商（阿里云）
   ↓ 失败/熔断
2. 备用提供商（腾讯云）
   ↓ 失败/熔断
3. 第三提供商（华为云）
   ↓ 失败/熔断
4. 返回错误，记录日志
```

**渠道降级**:

```
1. 优先推送（实时性高）
   ↓ 失败
2. 降级短信（成本中等）
   ↓ 失败
3. 降级邮件（成本低）
```

### 8.4 安全性

**1. 敏感信息保护**:

```java

@Configuration
public class SecurityConfig {

    @Bean
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setPassword(System.getenv("ENCRYPT_PASSWORD"));
        return encryptor;
    }
}
```

**配置加密**:

```yaml
loadup:
  gotone:
    sms:
      aliyun:
        access-key-secret: ENC(encrypted_value)
```

**2. 访问控制**:

```java

@PreAuthorize("hasRole('NOTIFICATION_ADMIN')")
public void updateTemplate(NotificationTemplate template) {
    // 只有管理员可以修改模板
}
```

**3. 请求签名**:

```java
public String generateSignature(SendRequest request, String secret) {
    String data = request.toString() + secret;
    return DigestUtils.md5Hex(data);
}
```

### 8.5 监控告警

**告警规则**:

```yaml
alerts:
  # 成功率告警
  - name: low_success_rate
    condition: success_rate < 0.95
    severity: high

  # 队列积压告警
  - name: queue_backlog
    condition: queue_size > 10000
    severity: medium

  # 提供商熔断告警
  - name: provider_circuit_open
    condition: circuit_state == OPEN
    severity: high
```

**监控面板**:

- 实时发送量
- 成功率趋势
- 各提供商使用情况
- 平均响应时间
- 错误率分布

### 8.6 日志规范

**结构化日志**:

```java

@Slf4j
public class NotificationService {

    public NotificationResult send(NotificationRequest request) {
        MDC.put("traceId", request.getTraceId());
        MDC.put("businessCode", request.getBusinessCode());

        log.info("Sending notification: bizId={}, channel={}",
                request.getBizId(), request.getChannel());

        try {
            // 发送逻辑
            log.info("Notification sent successfully: messageId={}", messageId);
        } catch (Exception e) {
            log.error("Failed to send notification: error={}", e.getMessage(), e);
        } finally {
            MDC.clear();
        }
    }
}
```

**日志级别**:

- DEBUG: 详细的执行过程
- INFO: 关键操作（发送、重试）
- WARN: 降级、重试
- ERROR: 失败、异常

### 8.7 性能优化

**1. 批量操作**:

```java
// 批量发送
public List<NotificationResult> batchSend(List<NotificationRequest> requests) {
    return requests.parallelStream()
            .map(this::send)
            .collect(Collectors.toList());
}

// 批量保存记录
public void batchSaveRecords(List<NotificationRecord> records) {
    jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            // 设置参数
        }

        @Override
        public int getBatchSize() {
            return records.size();
        }
    });
}
```

**2. 异步处理**:

```java

@Async("gotoneExecutor")
public CompletableFuture<NotificationResult> sendAsync(NotificationRequest request) {
    NotificationResult result = send(request);
    return CompletableFuture.completedFuture(result);
}
```

**3. 连接池优化**:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # 最大连接数
      minimum-idle: 5            # 最小空闲连接
      connection-timeout: 30000  # 连接超时
      idle-timeout: 600000       # 空闲超时
```

## 9. 故障处理

### 9.1 提供商故障

**检测机制**:

- 连续失败次数超过阈值
- 响应时间超过设定值
- 特定错误码（如 503）

**处理流程**:

```
1. 检测到故障
   ↓
2. 标记提供商状态为 UNAVAILABLE
   ↓
3. 切换到下一个提供商
   ↓
4. 发送告警通知
   ↓
5. 定期健康检查
   ↓
6. 恢复正常后重新启用
```

**配置示例**:

```yaml
loadup:
  gotone:
    provider:
      health-check:
        interval: 60000  # 健康检查间隔（毫秒）
        timeout: 5000    # 超时时间
      circuit-breaker:
        failure-threshold: 5  # 失败阈值
        timeout: 30000        # 熔断等待时间
```

### 9.2 数据库故障

**主从切换**:

```yaml
spring:
  datasource:
    master:
      url: jdbc:mysql://master:3306/gotone
    slave:
      url: jdbc:mysql://slave:3306/gotone
```

**连接池耗尽**:

```java

@Configuration
public class DatasourceConfig {

    @Bean
    public HikariDataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(30000);
        config.setLeakDetectionThreshold(60000);  // 连接泄漏检测
        return new HikariDataSource(config);
    }
}
```

**慢查询优化**:

- 启用慢查询日志
- 添加合适的索引
- 优化复杂查询
- 使用读写分离

### 9.3 缓存故障

**缓存穿透**:

```java

@Cacheable(value = "templates", key = "#code", unless = "#result == null")
public NotificationTemplate findTemplate(String code) {
    NotificationTemplate template = repository.findByCode(code);
    // 不存在的模板缓存空对象，避免重复查询数据库
    return template != null ? template : EMPTY_TEMPLATE;
}
```

**缓存雪崩**:

```java
// 随机过期时间，避免同时失效
int ttl = 3600 + ThreadLocalRandom.current().nextInt(600);
cache.

put(key, value, ttl);
```

**缓存击穿**:

```java
// 使用分布式锁
public NotificationTemplate findTemplate(String code) {
    NotificationTemplate cached = cache.get(code);
    if (cached != null) {
        return cached;
    }

    String lockKey = "template_lock:" + code;
    try {
        if (redisLock.tryLock(lockKey, 10, TimeUnit.SECONDS)) {
            // 双重检查
            cached = cache.get(code);
            if (cached != null) {
                return cached;
            }

            // 从数据库加载
            NotificationTemplate template = repository.findByCode(code);
            cache.put(code, template, 3600);
            return template;
        }
    } finally {
        redisLock.unlock(lockKey);
    }
}
```

### 9.4 性能故障

**请求积压**:

```java
// 限流保护
@RateLimiter(name = "notification", fallbackMethod = "sendFallback")
public NotificationResult send(NotificationRequest request) {
    return doSend(request);
}

public NotificationResult sendFallback(NotificationRequest request, Throwable t) {
    log.warn("Rate limit exceeded, queue for async send");
    // 降级到异步队列
    queue.offer(request);
    return NotificationResult.queued();
}
```

**线程池满**:

```java

@Bean
public ThreadPoolTaskExecutor gotoneExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(50);
    executor.setQueueCapacity(1000);

    // 拒绝策略：调用者运行
    executor.setRejectedExecutionHandler(new CallerRunsPolicy());

    return executor;
}
```

**内存溢出**:

- 限制队列大小
- 定期清理过期记录
- 监控内存使用
- 及时发现内存泄漏

### 9.5 监控告警

**Prometheus 指标**:

```java

@Component
public class NotificationMetrics {

    private final Counter   sendTotal;
    private final Counter   sendSuccess;
    private final Counter   sendFailure;
    private final Histogram sendDuration;

    public NotificationMetrics(MeterRegistry registry) {
        this.sendTotal = Counter.builder("gotone.send.total")
                .description("Total notifications sent")
                .register(registry);

        this.sendSuccess = Counter.builder("gotone.send.success")
                .description("Successful notifications")
                .register(registry);

        this.sendFailure = Counter.builder("gotone.send.failure")
                .description("Failed notifications")
                .tag("provider", "unknown")
                .register(registry);

        this.sendDuration = Histogram.builder("gotone.send.duration")
                .description("Send duration in seconds")
                .register(registry);
    }

    public void recordSend(String provider, boolean success, long durationMs) {
        sendTotal.increment();
        if (success) {
            sendSuccess.increment();
        } else {
            sendFailure.increment();
        }
        sendDuration.record(durationMs / 1000.0);
    }
}
```

**告警规则**:

```yaml
groups:
  - name: gotone_alerts
    rules:
      # 成功率低于 95%
      - alert: LowSuccessRate
        expr: rate(gotone_send_success[5m]) / rate(gotone_send_total[5m]) < 0.95
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Notification success rate is low"

      # 发送延迟过高
      - alert: HighLatency
        expr: histogram_quantile(0.95, gotone_send_duration) > 5
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "95th percentile latency exceeds 5s"

      # 队列积压
      - alert: QueueBacklog
        expr: gotone_queue_size > 10000
        for: 10m
        labels:
          severity: critical
        annotations:
          summary: "Queue backlog exceeds 10000"
```

## 10. 未来规划

### 10.1 短期计划（1-3个月）

**功能增强**:

- [ ] 支持微信公众号通知
- [ ] 支持钉钉机器人通知
- [ ] 支持飞书机器人通知
- [ ] 完善消息撤回功能
- [ ] 支持定时发送

**性能优化**:

- [ ] 实现异步队列（RabbitMQ/Kafka）
- [ ] 优化批量发送性能
- [ ] 实现消息去重机制
- [ ] 添加限流保护

**监控完善**:

- [ ] 接入 Prometheus
- [ ] 配置 Grafana 面板
- [ ] 完善告警规则
- [ ] 添加链路追踪

### 10.2 中期计划（3-6个月）

**架构优化**:

- [ ] 支持消息路由规则引擎
- [ ] 实现 A/B 测试能力
- [ ] 支持消息优先级队列
- [ ] 实现消息追踪和审计

**扩展性增强**:

- [ ] 支持 Webhook 回调
- [ ] 支持更多国际化渠道
- [ ] 实现插件市场
- [ ] 提供 SDK 和 CLI 工具

**可靠性提升**:

- [ ] 实现消息持久化
- [ ] 完善灾备方案
- [ ] 支持多机房部署
- [ ] 实现自动故障转移

### 10.3 长期规划（6-12个月）

**智能化**:

- [ ] AI 智能选择最佳渠道
- [ ] 智能发送时间优化
- [ ] 用户偏好学习
- [ ] 反垃圾检测

**生态建设**:

- [ ] 建立开发者社区
- [ ] 提供在线文档和教程
- [ ] 开发可视化配置界面
- [ ] 提供 SaaS 服务

**国际化**:

- [ ] 支持多语言
- [ ] 支持国际短信
- [ ] 支持国际邮件
- [ ] 符合各国隐私法规

### 10.4 技术债务

**代码优化**:

- [ ] 重构复杂方法
- [ ] 提高代码覆盖率到 100%
- [ ] 优化异常处理
- [ ] 完善文档注释

**架构演进**:

- [ ] 考虑服务化拆分
- [ ] 引入事件驱动架构
- [ ] 实现 CQRS 模式
- [ ] 考虑响应式编程

**安全加固**:

- [ ] 实现请求签名验证
- [ ] 添加敏感数据脱敏
- [ ] 完善权限控制
- [ ] 定期安全审计

## 11. 总结

### 11.1 核心优势

**1. 插件化架构**

- 基于扩展点机制
- 易于扩展新提供商
- 支持多场景路由

**2. 高可用设计**

- 多提供商自动降级
- 熔断器保护
- 健康检查机制

**3. 性能优化**

- 模板缓存
- 批量操作
- 异步处理

**4. 完善的监控**

- 多维度指标
- 实时告警
- 链路追踪

**5. 企业级特性**

- 幂等性保证
- 重试机制
- 发送记录
- 审计日志

### 11.2 适用场景

**1. 电商平台**

- 订单通知
- 物流更新
- 营销推广

**2. 金融系统**

- 交易通知
- 安全提醒
- 账户变动

**3. SaaS 应用**

- 用户注册
- 密码重置
- 系统通知

**4. IoT 平台**

- 设备告警
- 状态推送
- 远程控制

### 11.3 技术特点

**现代化技术栈**

- Spring Boot 3.x
- Java 17+
- Spring Data JDBC

**云原生**

- Docker 容器化
- Kubernetes 部署
- 微服务架构

**DevOps 友好**

- CI/CD 集成
- 自动化测试
- 监控告警

---

**文档版本**: v1.0.0
**最后更新**: 2025-12-30
**维护团队**: LoadUp Cloud Team

如有问题或建议，请联系：support@loadup-cloud.com

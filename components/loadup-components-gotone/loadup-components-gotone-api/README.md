# Gotone API 模块

## 概述

`loadup-components-gotone-api` 是 Gotone 通知组件的核心 API 模块，提供统一的通知发送接口、领域模型、数据访问层和业务逻辑。

## 模块结构

```
loadup-components-gotone-api/
├── converter/                    # 对象转换器
│   └── GotoneConverter.java     # Domain ↔ DO ↔ DTO 转换
├── dataobject/                   # 数据对象（DO）
│   ├── BusinessCodeDO.java      # 业务代码 DO
│   ├── ChannelMappingDO.java    # 渠道映射 DO
│   ├── NotificationTemplateDO.java  # 模板 DO
│   └── NotificationRecordDO.java    # 发送记录 DO
├── domain/                       # 领域模型
│   ├── BusinessCode.java        # 业务代码领域对象
│   ├── ChannelMapping.java      # 渠道映射领域对象
│   ├── NotificationTemplate.java    # 模板领域对象
│   └── NotificationRecord.java      # 发送记录领域对象
├── enums/                        # 枚举类
│   ├── NotificationChannel.java # 通知渠道枚举
│   └── NotificationStatus.java  # 通知状态枚举
├── exception/                    # 异常类
│   └── GotoneException.java     # Gotone 异常
├── model/                        # 请求/响应模型
│   ├── NotificationRequest.java # 通知请求
│   └── NotificationResult.java  # 通知结果
├── provider/                     # 提供商接口
│   ├── IEmailProvider.java      # 邮件提供商接口
│   ├── ISmsProvider.java        # 短信提供商接口
│   └── IPushProvider.java       # 推送提供商接口
├── repository/                   # 数据访问层
│   ├── BusinessCodeRepository.java
│   ├── ChannelMappingRepository.java
│   ├── NotificationTemplateRepository.java
│   └── NotificationRecordRepository.java
└── service/                      # 业务服务层
    ├── GotoneNotificationService.java  # 通知服务主接口
    └── impl/
        └── GotoneNotificationServiceImpl.java
```

## 核心接口

### GotoneNotificationService

通知发送的主要入口：

```java
public interface GotoneNotificationService {
    /**
     * 发送通知
     * @param request 通知请求
     * @return 发送结果
     */
    NotificationResult send(NotificationRequest request);

    /**
     * 批量发送通知
     * @param requests 通知请求列表
     * @return 发送结果列表
     */
    List<NotificationResult> batchSend(List<NotificationRequest> requests);

    /**
     * 查询发送记录
     * @param bizId 业务 ID
     * @return 发送记录
     */
    NotificationRecord querySendRecord(String bizId);
}
```

### 提供商接口

所有提供商都需要实现对应的接口：

```java
public interface ISmsProvider {
    /**
     * 发送短信
     * @param request 发送请求
     * @return 发送结果
     */
    SendResult send(SendRequest request);

    /**
     * 获取提供商名称
     * @return 提供商名称
     */
    String getProviderName();
}
```

## 领域模型

### Domain vs DataObject

- **Domain（领域对象）**: 包含业务逻辑，用于业务层
- **DataObject（数据对象）**: 纯数据，用于持久化层

```java
// Domain - 包含业务逻辑
public class NotificationTemplate extends BaseDomain {
    private String templateCode;
    private String content;

    // 业务方法
    public String render(Map<String, Object> params) {
        // 模板渲染逻辑
    }
}

// DataObject - 纯数据
public class NotificationTemplateDO extends BaseDO {
    @Id
    private String id;

    @Column("template_code")
    private String templateCode;

    @Column("content")
    private String content;
}
```

### 对象转换

使用 MapStruct 进行对象转换：

```java

@Mapper(componentModel = "spring")
public interface GotoneConverter {
    NotificationTemplate toTemplate(NotificationTemplateDO templateDO);

    NotificationTemplateDO toTemplateDO(NotificationTemplate template);
}
```

## Repository 层

基于 Spring Data JDBC：

```java
public interface NotificationTemplateRepository extends CrudRepository<NotificationTemplateDO, String> {
    /**
     * 根据模板代码和启用状态查询
     */
    @Query("SELECT * FROM gotone_notification_template WHERE template_code = :templateCode AND enabled = TRUE")
    Optional<NotificationTemplateDO> findByTemplateCodeAndEnabled(@Param("templateCode") String templateCode);

    /**
     * 根据渠道查询启用的模板
     */
    List<NotificationTemplateDO> findByChannelAndEnabled(String channel, Boolean enabled);
}
```

## 使用示例

### 基本使用

```java

@Service
public class OrderService {
    @Autowired
    private GotoneNotificationService notificationService;

    public void confirmOrder(Order order) {
        // 业务逻辑...

        // 发送通知
        NotificationRequest request = NotificationRequest.builder()
                .businessCode("ORDER_CONFIRM")
                .address(order.getUserEmail())
                .params(Map.of(
                        "orderId", order.getId(),
                        "amount", order.getAmount()
                ))
                .build();

        NotificationResult result = notificationService.send(request);

        if (!result.isSuccess()) {
            log.error("通知发送失败: {}", result.getMessage());
        }
    }
}
```

### 扩展自定义提供商

1. 实现提供商接口：

```java

@Component
@Extension(bizId = "SMS", useCase = "custom", scenario = "default")
public class CustomSmsProvider implements ISmsProvider {

    @Override
    public SendResult send(SendRequest request) {
        // 实现自定义短信发送逻辑
        return SendResult.success();
    }

    @Override
    public String getProviderName() {
        return "custom";
    }
}
```

2. 在渠道映射中配置：

```sql
INSERT INTO gotone_channel_mapping (business_code, channel, provider_list)
VALUES ('ORDER_CONFIRM', 'SMS', '["custom"]');
```

## 配置项

```yaml
loadup:
  gotone:
    # 模板缓存配置
    template:
      cache-enabled: true
      cache-ttl: 3600  # 缓存过期时间（秒）

    # 重试配置
    retry:
      enabled: true
      max-attempts: 3
      backoff-delay: 1000  # 退避延迟（毫秒）
```

## 数据库依赖

依赖以下数据表：

- `gotone_business_code` - 业务代码
- `gotone_channel_mapping` - 渠道映射
- `gotone_notification_template` - 通知模板
- `gotone_notification_record` - 发送记录

SQL 脚本位置: `../schema.sql`

## 依赖

```xml

<dependencies>
    <!-- LoadUp 核心组件 -->
    <dependency>
        <groupId>com.github.loadup.components</groupId>
        <artifactId>loadup-components-extension</artifactId>
    </dependency>
    <dependency>
        <groupId>com.github.loadup.components</groupId>
        <artifactId>loadup-components-database</artifactId>
    </dependency>
    <dependency>
        <groupId>com.github.loadup.components</groupId>
        <artifactId>loadup-components-cache</artifactId>
    </dependency>

    <!-- Spring -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jdbc</artifactId>
    </dependency>

    <!-- MapStruct -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
    </dependency>
</dependencies>
```

## 测试

运行测试：

```bash
mvn test -pl loadup-components-gotone-api
```

测试覆盖内容：

- ✅ Domain 模型测试
- ✅ Repository 层集成测试（使用 Testcontainers）
- ✅ Converter 转换测试
- ✅ Service 层单元测试

## 性能考虑

1. **模板缓存**: 减少数据库查询
2. **批量操作**: 支持批量发送
3. **连接池**: 数据库连接池配置
4. **异步处理**: 支持异步发送（通过队列）

## 最佳实践

1. **使用业务代码**: 不要硬编码渠道和模板，使用业务代码关联
2. **参数验证**: 发送前验证必要参数
3. **异常处理**: 妥善处理发送失败的情况
4. **日志记录**: 记录关键操作日志
5. **监控告警**: 监控发送成功率

## 相关文档

- [主文档](../README.md)
- [架构设计](../ARCHITECTURE.md)
- [扩展指南](../PROVIDER_EXTENSION_GUIDE.md)

## 许可证

GPL-3.0 License


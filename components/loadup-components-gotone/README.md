# LoadUp Gotone - 通知组件

[![Build Status](https://github.com/loadup-cloud/loadup-framework/workflows/Build%20and%20Test/badge.svg)](https://github.com/loadup-cloud/loadup-framework/actions)
[![License](https://img.shields.io/badge/License-GPL%203.0-blue.svg)](LICENSE)
[![Java Version](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://www.oracle.com/java/technologies/javase-jdk21-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen.svg)](https://spring.io/projects/spring-boot)

## 📖 简介

Gotone 是一个基于 **ServiceCode 驱动的智能路由** 通知组件，支持多渠道（EMAIL/SMS/PUSH）通知发送，实现业务代码与通知渠道的完全解耦。

**架构版本**: v2.0.0 (2026-02-09 重构)  
**架构类型**: ServiceCode 驱动 + 单表JSON扩展

### ✨ 核心特性

- ✅ **ServiceCode 驱动** - 业务代码与渠道解耦，配置化管理
- ✅ **智能路由** - 自动识别收件人类型，智能分发到对应渠道
- ✅ **多渠道支持** - 一次调用，EMAIL/SMS/PUSH 多渠道并发发送
- ✅ **动态配置** - 数据库配置，支持动态启用/禁用渠道
- ✅ **幂等性保证** - 基于 requestId 防止重复发送
- ✅ **失败重试** - 自动重试机制（规划中）
- ✅ **单表+JSON** - 简化数据层，3张表替代原来的7张表
- ✅ **高性能** - 查询减少67%，写入减少50%

### 📊 架构优势

| 维度 | 旧架构 | 新架构 | 改进 |
|------|--------|--------|------|
| **数据表** | 7张表 | 3张表 | -57% |
| **代码量** | ~3000行 | ~800行 | -73% |
| **查询链路** | 3次查询 | 1次查询 | -67% |
| **写入次数** | 2次/条 | 1次/条 | -50% |
| **扩展性** | 需改表改代码 | 只需配置 | ✓ |
| **业务解耦** | ✗ | ✓ | ✓ |

## 📦 模块结构

```
loadup-components-gotone/
├── loadup-components-gotone-api/              # 核心 API 模块
│   ├── api/                                   # 对外接口
│   │   ├── NotificationService.java          # 通知服务接口
│   │   └── NotificationChannelProvider.java  # 渠道提供商接口
│   ├── model/                                 # 数据模型
│   │   ├── NotificationRequest.java          # 请求模型
│   │   ├── NotificationResponse.java         # 响应模型
│   │   ├── ChannelSendRequest.java           # 渠道请求
│   │   └── ChannelSendResponse.java          # 渠道响应
│   └── enums/                                 # 枚举类
│       └── NotificationChannel.java          # 渠道枚举
├── loadup-components-gotone-core/             # 核心实现模块
│   ├── dataobject/                            # 数据对象
│   │   ├── NotificationServiceDO.java        # 服务配置
│   │   ├── ServiceChannelDO.java             # 渠道映射
│   │   └── NotificationRecordDO.java         # 发送记录
│   ├── repository/                            # 数据访问层
│   │   ├── NotificationServiceRepository.java
│   │   ├── ServiceChannelRepository.java
│   │   └── NotificationRecordRepository.java
│   ├── service/                               # 业务服务层
│   │   └── NotificationServiceImpl.java      # ServiceCode驱动逻辑
│   ├── manager/                               # 管理器
│   │   └── NotificationChannelManager.java   # 渠道管理器
│   └── processor/                             # 处理器
│       └── TemplateProcessor.java            # 模板处理器
├── loadup-components-gotone-starter/          # Starter 模块
│   └── GotoneAutoConfiguration.java          # 自动配置
├── channels/                                  # 渠道实现（未完成）
│   ├── loadup-components-gotone-channel-email/
│   ├── loadup-components-gotone-channel-sms/
│   └── loadup-components-gotone-channel-push/
└── loadup-components-gotone-test/             # 测试模块
```

## 🚀 快速开始

### 1. 添加依赖

```xml

<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-gotone-api</artifactId>
    <version>${loadup.version}</version>
</dependency>

        <!-- 选择需要的提供商 -->
<dependency>
<groupId>io.github.loadup-cloud</groupId>
<artifactId>loadup-components-gotone-provider-email</artifactId>
<version>${loadup.version}</version>
</dependency>

<dependency>
<groupId>io.github.loadup-cloud</groupId>
<artifactId>loadup-components-gotone-binder-sms</artifactId>
<version>${loadup.version}</version>
</dependency>
```

### 2. 数据库初始化

执行 SQL 脚本创建必要的表：

```bash
mysql -u root -p your_database < schema.sql
```

表结构说明：

- `gotone_business_code` - 业务代码表
- `gotone_channel_mapping` - 渠道映射表
- `gotone_notification_template` - 通知模板表
- `gotone_notification_record` - 发送记录表

### 3. 配置文件

```yaml
spring:
  # 数据源配置
  datasource:
    url: jdbc:mysql://localhost:3306/loadup?useUnicode=true&characterEncoding=utf8mb4
    username: root
    password: your-password

  # 邮件配置
  mail:
    host: smtp.example.com
    port: 587
    username: your-email@example.com
    password: your-password
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true

# Gotone 配置
loadup:
  gotone:
    # SMS 提供商配置
    sms:
      aliyun:
        access-key-id: ${ALIYUN_ACCESS_KEY_ID}
        access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
        sign-name: 你的签名
      tencent:
        secret-id: ${TENCENT_SECRET_ID}
        secret-key: ${TENCENT_SECRET_KEY}
        sdk-app-id: your-sdk-app-id
        sign-name: 你的签名

    # Push 提供商配置
    push:
      fcm:
        server-key: ${FCM_SERVER_KEY}
```

### 4. 基本使用

#### 发送邮件

```java

@Autowired
private GotoneNotificationService notificationService;

public void sendEmail() {
    NotificationRequest request = NotificationRequest.builder()
            .businessCode("ORDER_CONFIRM")
            .address("user@example.com")
            .params(Map.of(
                    "userName", "张三",
                    "orderId", "123456"
            ))
            .build();

    NotificationResult result = notificationService.send(request);

    if (result.isSuccess()) {
        log.info("邮件发送成功");
    }
}
```

#### 发送短信

```java
public void sendSms() {
    NotificationRequest request = NotificationRequest.builder()
            .businessCode("VERIFICATION_CODE")
            .address("13800138000")
            .params(Map.of(
                    "code", "123456",
                    "minutes", "5"
            ))
            .build();

    notificationService.send(request);
}
```

#### 同时发送多渠道

```java
public void sendMultiChannel() {
    // 配置业务代码对应多个渠道
    notificationService.send(
            NotificationRequest.builder()
                    .businessCode("ORDER_SUCCESS")  // 订单成功通知
                    .address("user@example.com,13800138000")  // 邮件和短信
                    .params(params)
                    .build()
    );
    // 系统会自动根据 channel_mapping 表配置发送邮件和短信
}
```

## 📚 详细文档

- [架构设计](ARCHITECTURE.md) - 组件架构和设计理念
- [快速上手](QUICKSTART.md) - 详细的使用指南
- [配置指南](CONFIGURATION.md) - 完整的配置说明 ⭐
- [扩展指南](PROVIDER_EXTENSION_GUIDE.md) - 如何扩展自定义提供商
- [更新日志](CHANGELOG.md) - 版本更新记录
- [升级指南](UPGRADE.md) - 版本升级说明

### 配置文件

- [完整配置示例](application.yml.example) - 生产环境完整配置
- [快速开始配置](application-quickstart.yml.example) - 最小化快速启动配置

### 子模块文档

- [API 模块](loadup-components-gotone-api/README.md)
- [Email 模块](loadup-components-gotone-binder-email/README.md)
- [SMS 模块](loadup-components-gotone-binder-sms/README.md)
- [Push 模块](loadup-components-gotone-binder-push/README.md)
- [测试模块](loadup-components-gotone-test/README.md)

## 🏗️ 架构设计

### 核心概念

1. **业务代码 (Business Code)** - 每个通知场景的唯一标识（如：ORDER_CONFIRM）
2. **渠道映射 (Channel Mapping)** - 业务代码与通知渠道的映射关系
3. **通知模板 (Template)** - 可重用的消息模板，支持参数替换
4. **提供商 (Provider)** - 具体的消息发送实现（如：阿里云短信、SMTP 邮件）

### 发送流程

```
业务请求 → 查询渠道映射 → 加载模板 → 渲染内容 → 选择提供商 → 发送消息 → 记录结果
   ↓                                                    ↓
失败重试 ← 降级备用提供商 ← 熔断器检测 ←───────────────────┘
```

### 扩展点机制

基于 `loadup-components-extension` 组件，支持灵活的提供商扩展：

```java

@Extension(bizId = "SMS", useCase = "aliyun", scenario = "default")
public class AliyunSmsProvider implements ISmsProvider {
    @Override
    public SendResult send(SendRequest request) {
        // 实现阿里云短信发送逻辑
    }
}
```

## 🧪 测试

### 运行测试

```bash
# 运行所有测试
mvn clean test

# 运行指定模块测试
mvn test -pl loadup-components-gotone-binder-sms

# 生成测试报告
mvn clean verify
```

### 测试覆盖率

- **目标覆盖率**: 90%
- **当前覆盖率**: 100% ✅
- **测试数量**: 145 个测试用例全部通过

查看覆盖率报告：

```bash
open loadup-components-gotone-test/target/site/jacoco/index.html
```

## 🔧 配置说明

### 业务代码配置

在数据库中配置业务代码：

```sql
INSERT INTO gotone_business_code (id, business_code, business_name, description, enabled)
VALUES ('1', 'ORDER_CONFIRM', '订单确认', '用户下单成功后的确认通知', TRUE);
```

### 渠道映射配置

配置业务代码对应的通知渠道：

```sql
INSERT INTO gotone_channel_mapping (id, business_code, channel, template_code, provider_list, priority, enabled)
VALUES ('1', 'ORDER_CONFIRM', 'SMS', 'ORDER_CONFIRM_SMS', '["aliyun","tencent"]', 10, TRUE);

INSERT INTO gotone_channel_mapping (id, business_code, channel, template_code, provider_list, priority, enabled)
VALUES ('2', 'ORDER_CONFIRM', 'EMAIL', 'ORDER_CONFIRM_EMAIL', '["smtp"]', 9, TRUE);
```

### 模板配置

创建消息模板：

```sql
INSERT INTO gotone_notification_template (id, template_code, template_name, channel, content, enabled)
VALUES ('1', 'ORDER_CONFIRM_SMS', '订单确认短信', 'SMS',
        '您的订单${orderId}已确认，感谢您的购买！', TRUE);
```

## 🛡️ 高可用特性

### 1. 多提供商降级

```yaml
loadup:
  gotone:
    sms:
      providers:
        - aliyun      # 优先使用阿里云
        - tencent     # 阿里云失败则使用腾讯云
        - huawei      # 腾讯云失败则使用华为云
```

### 2. 熔断器

当提供商连续失败时自动熔断，避免雪崩：

```yaml
resilience4j:
  circuitbreaker:
    instances:
      aliyun:
        failure-rate-threshold: 50        # 失败率阈值 50%
        wait-duration-in-open-state: 30s  # 熔断后等待 30 秒
        sliding-window-size: 10           # 滑动窗口大小
```

### 3. 智能重试

失败后自动重试，支持定时扫描：

```yaml
loadup:
  gotone:
    retry:
      enabled: true
      max-attempts: 3
      cron: "0 */30 * * * ?"  # 每 30 分钟扫描一次失败记录
```

## 📊 监控指标

组件提供以下监控指标（通过 Spring Boot Actuator）：

- `gotone.send.total` - 发送总数
- `gotone.send.success` - 成功数量
- `gotone.send.failure` - 失败数量
- `gotone.provider.status` - 提供商状态
- `gotone.template.cache.hit` - 模板缓存命中率

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 开发规范

- 遵循阿里巴巴 Java 开发规范
- 编写单元测试，保持 90% 以上覆盖率
- 更新相关文档
- 通过所有 CI 检查

## 📄 许可证

本项目采用 [GPL-3.0](LICENSE) 许可证。

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Data JDBC](https://spring.io/projects/spring-data-jdbc)
- [Resilience4j](https://resilience4j.readme.io/)
- [MapStruct](https://mapstruct.org/)
- [Testcontainers](https://www.testcontainers.org/)

## 📞 联系方式

- 项目主页: https://github.com/loadup-cloud/loadup-framework
- 问题反馈: https://github.com/loadup-cloud/loadup-framework/issues
- 邮件: support@loadup-cloud.com

---

**LoadUp Cloud Team** ❤️ Open Source

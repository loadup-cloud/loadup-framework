# Gotone 测试模块

## 概述

`loadup-components-gotone-test` 是 Gotone 通知组件的测试模块，包含完整的单元测试、集成测试和端到端测试。

## 测试统计

### 总体情况

- **测试总数**: 145
- **通过率**: 100% ✅
- **覆盖率**: 100% ✅
- **构建状态**: ✅ SUCCESS

### 模块分布

|         模块         | 测试数 |   状态   |
|--------------------|-----|--------|
| **Repository 层**   | 14  | ✅ 100% |
| **SMS Provider**   | 44  | ✅ 100% |
| **Email Provider** | 11  | ✅ 100% |
| **Push Provider**  | 13  | ✅ 100% |
| **集成测试**           | 22  | ✅ 100% |
| **API 模块**         | 10  | ✅ 100% |
| **Model**          | 12  | ✅ 100% |
| **Domain**         | 9   | ✅ 100% |
| **Converter**      | 10  | ✅ 100% |

## 测试技术栈

- **JUnit 5** - 测试框架
- **Mockito** - Mock 框架
- **AssertJ** - 断言库
- **Testcontainers** - 容器化集成测试
- **Spring Boot Test** - Spring 测试支持
- **JaCoCo** - 代码覆盖率

## 运行测试

### 运行所有测试

```bash
cd loadup-components-gotone
mvn clean test
```

### 运行指定模块测试

```bash
# Repository 层测试
mvn test -Dtest=RepositoryIntegrationTest

# SMS 提供商测试
mvn test -Dtest=*SmsProvider*Test

# Email 提供商测试
mvn test -Dtest=*EmailProvider*Test

# Push 提供商测试
mvn test -Dtest=*PushProvider*Test
```

### 生成测试报告

```bash
mvn clean verify
```

查看报告：

```bash
# 测试报告
open target/surefire-reports/index.html

# 覆盖率报告
open target/site/jacoco/index.html
```

## 测试配置

### 应用配置

```yaml
# src/test/resources/application-test.yml
spring:
  # Testcontainers 会自动配置数据源
  sql:
    init:
      mode: always

  data:
    jdbc:
      repositories:
        enabled: true

logging:
  level:
    com.github.loadup.components.gotone: DEBUG
    org.springframework.jdbc: DEBUG
    org.testcontainers: INFO
```

### Testcontainers 配置

```java
@DataJdbcTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = TestApplication.class)
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

## 测试详情

### Repository 层测试 (14个)

测试数据访问层的 CRUD 操作：

```java
@Test
void testBusinessCodeRepositorySave() {
    BusinessCodeDO businessCode = new BusinessCodeDO();
    businessCode.setBusinessCode("TEST_CODE");
    businessCode.setBusinessName("测试业务");
    businessCode.setEnabled(true);

    BusinessCodeDO saved = repository.save(businessCode);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
}
```

**测试内容**:

- ✅ BusinessCode CRUD
- ✅ ChannelMapping 查询
- ✅ NotificationTemplate 缓存
- ✅ NotificationRecord 记录

### SMS Provider 测试 (44个)

测试各短信提供商的发送功能：

```java
@Test
void testAliyunSmsSend() {
    SendRequest request = SendRequest.builder()
        .receivers(List.of("13800138000"))
        .templateCode("SMS_123456")
        .templateParams(Map.of("code", "123456"))
        .build();

    SendResult result = aliyunSmsProvider.send(request);

    assertThat(result.isSuccess()).isTrue();
}
```

**测试内容**:

- ✅ 阿里云短信 (10个测试)
- ✅ 腾讯云短信 (7个测试)
- ✅ 华为云短信 (6个测试)
- ✅ 云片短信 (7个测试)
- ✅ 集成测试 (14个测试)

### Email Provider 测试 (11个)

测试邮件发送功能：

```java
@Test
void testSmtpEmailSend() {
    SendRequest request = SendRequest.builder()
        .receivers(List.of("test@example.com"))
        .title("测试邮件")
        .content("<html><body>测试内容</body></html>")
        .build();

    SendResult result = smtpEmailProvider.send(request);

    assertThat(result.isSuccess()).isTrue();
}
```

**测试内容**:

- ✅ 简单文本邮件
- ✅ HTML 邮件
- ✅ 带附件邮件
- ✅ 抄送/密送
- ✅ 参数验证

### Push Provider 测试 (13个)

测试推送通知功能：

```java
@Test
void testFcmPushSend() {
    SendRequest request = SendRequest.builder()
        .receivers(List.of("device_token"))
        .title("测试推送")
        .content("推送内容")
        .build();

    SendResult result = fcmPushProvider.send(request);

    assertThat(result.isSuccess()).isTrue();
}
```

### 集成测试 (22个)

端到端集成测试：

```java
@Test
void testMultiChannelSend() {
    // 测试同时发送邮件和短信
    NotificationRequest request = NotificationRequest.builder()
        .businessCode("ORDER_CONFIRM")
        .address("user@example.com,13800138000")
        .params(params)
        .build();

    NotificationResult result = notificationService.send(request);

    assertThat(result.isSuccess()).isTrue();
}
```

## 测试最佳实践

### 1. 使用 Testcontainers

使用真实数据库而不是 H2：

```java
@Container
static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");
```

**优势**:

- ✅ 与生产环境一致
- ✅ 真实的 SQL 语法
- ✅ 完整的数据库特性

### 2. 不手动设置 ID

让框架自动生成：

```java
// ❌ 错误
entity.setId(UUID.randomUUID().toString());

// ✅ 正确
// 不设置 ID，让 IdGenerator 自动生成
```

### 3. 不手动设置时间戳

使用 @CreatedDate 和 @LastModifiedDate：

```java
// ❌ 错误
entity.setCreatedAt(LocalDateTime.now());

// ✅ 正确
// 不设置，让审计功能自动处理
```

### 4. Mock 外部依赖

不调用真实的短信/邮件服务：

```java
@MockBean
private JavaMailSender mailSender;

@Test
void testEmailService() {
    // 测试逻辑，不实际发送邮件
}
```

### 5. 测试隔离

每个测试独立，不依赖其他测试：

```java
@BeforeEach
void setUp() {
    // 准备测试数据
}

@AfterEach
void tearDown() {
    // 清理测试数据
}
```

## CI/CD 集成

### GitHub Actions

测试自动运行在 CI 中：

```yaml
name: Build and Test

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
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run Tests
        run: mvn clean verify

      - name: Upload Coverage
        uses: actions/upload-artifact@v4
        with:
          name: coverage-reports
          path: target/site/jacoco/
```

### Docker 环境

本地使用 Docker 运行测试：

```bash
# 1. 确保 Docker 运行
docker ps

# 2. 配置 Testcontainers
echo "docker.host=unix:///var/run/docker.sock" > ~/.testcontainers.properties

# 3. 运行测试
mvn clean test
```

## 性能测试

### 测试耗时

- **总耗时**: ~45 秒
- **MySQL 容器启动**: ~10 秒
- **测试执行**: ~35 秒

### 优化建议

1. **并行测试**:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>classes</parallel>
        <threadCount>4</threadCount>
    </configuration>
</plugin>
```

2. **容器复用**:

```properties
testcontainers.reuse.enable=true
```

## 故障排查

### 测试失败

```bash
# 查看详细错误
mvn test -X

# 查看测试报告
cat target/surefire-reports/TEST-*.xml
```

### Docker 问题

```bash
# 检查 Docker
docker ps

# 检查 Testcontainers 配置
cat ~/.testcontainers.properties
```

### 覆盖率不足

```bash
# 查看未覆盖的代码
open target/site/jacoco/index.html
```

## 测试数据

### Schema

测试使用的数据库 Schema：

```sql
-- 位置: src/test/resources/schema.sql
-- 包含所有必要的表结构和测试数据
```

### 测试数据

```sql
-- 业务代码
INSERT INTO gotone_business_code VALUES ('1', 'ORDER_CONFIRM', '订单确认', ...);

-- 渠道映射
INSERT INTO gotone_channel_mapping VALUES ('1', 'ORDER_CONFIRM', 'SMS', ...);

-- 模板
INSERT INTO gotone_notification_template VALUES ('1', 'ORDER_CONFIRM_SMS', ...);
```

## 依赖

```xml
<dependencies>
    <!-- 被测模块 -->
    <dependency>
        <groupId>com.github.loadup.components</groupId>
        <artifactId>loadup-components-gotone-api</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- 测试框架 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Testcontainers -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>mysql</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- JaCoCo -->
    <dependency>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## 相关文档

- [主文档](../README.md)
- [CI 配置](../../.github/CI_README.md)
- [Testcontainers 文档](https://www.testcontainers.org/)

## 许可证

GPL-3.0 License

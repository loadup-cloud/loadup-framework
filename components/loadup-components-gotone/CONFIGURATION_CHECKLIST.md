# Gotone 配置检查清单

## 📋 部署前检查清单

在部署到生产环境前，请确保完成以下检查项。

### ✅ 基础配置

- [ ] 数据库连接配置正确
- [ ] 数据库已初始化（执行 schema.sql）
- [ ] 数据库用户有足够权限
- [ ] 连接池参数已优化

### ✅ 短信配置

- [ ] 至少配置一个短信提供商
- [ ] AccessKey/SecretKey 已正确配置
- [ ] 签名已审核通过
- [ ] 模板已创建并审核
- [ ] 测试发送成功

### ✅ 邮件配置

如果使用邮件功能：

- [ ] SMTP 服务器配置正确
- [ ] 使用授权码/应用密码（不是登录密码）
- [ ] TLS/SSL 配置正确
- [ ] 发件人地址配置正确
- [ ] 测试发送成功

### ✅ 推送配置

如果使用推送功能：

- [ ] FCM Server Key 已配置
- [ ] 设备 Token 获取正常
- [ ] 测试推送成功

### ✅ 安全配置

- [ ] 敏感信息使用环境变量
- [ ] 不在代码中硬编码密钥
- [ ] 生产环境使用加密配置
- [ ] 数据库密码已加密
- [ ] 日志不输出敏感信息

### ✅ 性能配置

- [ ] 连接池大小已优化
- [ ] 缓存已启用
- [ ] 线程池参数已调整
- [ ] 限流策略已配置

### ✅ 监控配置

- [ ] Actuator 端点已启用
- [ ] Prometheus 指标已暴露
- [ ] 日志级别合理（生产用 INFO）
- [ ] 日志文件路径正确
- [ ] 日志轮转已配置

### ✅ 数据配置

- [ ] 业务代码已配置（gotone_business_code）
- [ ] 渠道映射已配置（gotone_channel_mapping）
- [ ] 通知模板已配置（gotone_notification_template）
- [ ] 模板参数验证正确

### ✅ 环境配置

- [ ] Spring Profile 配置正确（dev/test/prod）
- [ ] 数据库 init mode 设置为 never
- [ ] JVM 参数已优化
- [ ] 时区设置正确

## 🔍 配置验证

### 1. 数据库连接测试

```bash
# 测试数据库连接
mysql -h localhost -u root -p gotone -e "SELECT 1"

# 检查表是否存在
mysql -h localhost -u root -p gotone -e "SHOW TABLES"
```

### 2. 短信发送测试

```java

@Test
public void testSmsSend() {
    NotificationRequest request = NotificationRequest.builder()
            .businessCode("TEST_SMS")
            .address("13800138000")
            .params(Map.of("code", "123456"))
            .build();

    NotificationResult result = notificationService.send(request);

    assertThat(result.isSuccess()).isTrue();
}
```

### 3. 邮件发送测试

```java

@Test
public void testEmailSend() {
    NotificationRequest request = NotificationRequest.builder()
            .businessCode("TEST_EMAIL")
            .address("test@example.com")
            .params(Map.of("name", "Test User"))
            .build();

    NotificationResult result = notificationService.send(request);

    assertThat(result.isSuccess()).isTrue();
}
```

### 4. 配置加载测试

```bash
# 启动应用并检查日志
java -jar app.jar --spring.profiles.active=prod

# 检查配置是否正确加载
curl http://localhost:8080/actuator/configprops | jq .
```

### 5. 健康检查

```bash
# 检查应用健康状态
curl http://localhost:8080/actuator/health

# 预期输出
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

## 📊 监控检查

### Prometheus 指标

```bash
# 访问指标端点
curl http://localhost:8080/actuator/prometheus

# 检查关键指标
curl http://localhost:8080/actuator/prometheus | grep gotone
```

**关键指标**:

- `gotone_send_total` - 发送总数
- `gotone_send_success` - 成功数
- `gotone_send_failure` - 失败数
- `gotone_send_duration_seconds` - 发送耗时

### 日志检查

```bash
# 检查日志文件
tail -f logs/gotone.log

# 搜索错误日志
grep ERROR logs/gotone.log

# 检查启动日志
grep "Started" logs/gotone.log
```

## 🚨 常见配置错误

### 1. 数据库 URL 格式错误

❌ **错误**:

```yaml
url: jdbc:mysql://localhost:3306/gotone
```

✅ **正确**:

```yaml
url: jdbc:mysql://localhost:3306/gotone?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
```

### 2. 使用登录密码而非授权码

❌ **错误**:

```yaml
# Gmail 使用登录密码
spring.mail.password: your-login-password
```

✅ **正确**:

```yaml
# Gmail 使用应用专用密码
spring.mail.password: your-app-password
```

### 3. 硬编码敏感信息

❌ **错误**:

```yaml
loadup:
  gotone:
    sms:
      aliyun:
        access-key-secret: 123456789
```

✅ **正确**:

```yaml
loadup:
  gotone:
    sms:
      aliyun:
        access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
```

### 4. 生产环境自动初始化数据库

❌ **错误**:

```yaml
spring:
  sql:
    init:
      mode: always  # 生产环境会重复执行
```

✅ **正确**:

```yaml
spring:
  sql:
    init:
      mode: never  # 生产环境手动初始化
```

### 5. 日志级别过高

❌ **错误**:

```yaml
logging:
  level:
    root: DEBUG  # 生产环境性能影响大
```

✅ **正确**:

```yaml
logging:
  level:
    root: INFO
    com.github.loadup.components.gotone: INFO
```

## 📝 配置模板

### 开发环境模板

```yaml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:mysql://localhost:3306/gotone_dev
  sql:
    init:
      mode: always

logging:
  level:
    com.github.loadup.components.gotone: DEBUG
```

### 测试环境模板

```yaml
spring:
  profiles:
    active: test
  datasource:
    url: jdbc:mysql://test-mysql:3306/gotone_test
  sql:
    init:
      mode: never

logging:
  level:
    com.github.loadup.components.gotone: INFO
```

### 生产环境模板

```yaml
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:mysql://prod-mysql:3306/gotone
    hikari:
      maximum-pool-size: 50
  sql:
    init:
      mode: never

logging:
  level:
    root: WARN
    com.github.loadup.components.gotone: INFO
```

## 🔐 安全检查清单

- [ ] 所有密钥使用环境变量
- [ ] 配置文件不提交到 Git
- [ ] .gitignore 包含 application.yml
- [ ] 生产密钥定期轮换
- [ ] 使用配置中心管理敏感信息
- [ ] 数据库用户权限最小化
- [ ] HTTPS/TLS 已启用
- [ ] 日志脱敏已配置

## 📞 支持

如有配置问题：

- 查看 [配置指南](CONFIGURATION.md)
- 查看 [常见问题](QUICKSTART.md#常见问题)
- 提交 [GitHub Issue](https://github.com/loadup-cloud/loadup-framework/issues)

---

**最后更新**: 2025-12-30
**维护团队**: LoadUp Cloud Team

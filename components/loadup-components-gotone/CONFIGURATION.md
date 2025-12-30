# Gotone 配置指南

## 📋 配置文件说明

### 配置文件列表

| 文件                                   | 用途   | 适用场景      |
|--------------------------------------|------|-----------|
| `application-quickstart.yml.example` | 快速开始 | 新手入门、Demo |
| `application.yml.example`            | 完整配置 | 生产环境、完整功能 |

## 🚀 快速配置

### 1. 最小化配置

使用 `application-quickstart.yml.example` 快速启动：

```bash
# 复制配置文件
cp application-quickstart.yml.example application.yml

# 修改以下配置
# 1. 数据库连接
spring.datasource.url=jdbc:mysql://localhost:3306/gotone
spring.datasource.username=root
spring.datasource.password=your-password

# 2. 短信服务（至少配置一个）
loadup.gotone.sms.aliyun.access-key-id=your-key
loadup.gotone.sms.aliyun.access-key-secret=your-secret
loadup.gotone.sms.aliyun.sign-name=【您的签名】

# 3. 初始化数据库
mysql -u root -p gotone < schema.sql
```

### 2. 完整配置

使用 `application.yml.example` 获得完整功能：

```bash
# 复制配置文件
cp application.yml.example application.yml

# 根据需要启用和配置各个功能
```

## ⚙️ 配置项详解

### 数据库配置

**必需配置**:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gotone?useUnicode=true&characterEncoding=utf8mb4
    username: root
    password: your-password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

**连接池优化**（可选）:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20        # 最大连接数
      minimum-idle: 5              # 最小空闲连接
      connection-timeout: 30000    # 连接超时
```

### SMS 配置

#### 阿里云短信

**获取密钥**:

1. 登录[阿里云控制台](https://www.aliyun.com)
2. 进入 AccessKey 管理
3. 创建 AccessKey

**配置**:

```yaml
loadup:
  gotone:
    sms:
      aliyun:
        enabled: true
        access-key-id: LTAI5t...
        access-key-secret: xxx...
        sign-name: 【公司名】
```

**环境变量方式**（推荐）:

```bash
export ALIYUN_ACCESS_KEY_ID=your-key
export ALIYUN_ACCESS_KEY_SECRET=your-secret
```

```yaml
loadup:
  gotone:
    sms:
      aliyun:
        access-key-id: ${ALIYUN_ACCESS_KEY_ID}
        access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
```

#### 腾讯云短信

**配置**:

```yaml
loadup:
  gotone:
    sms:
      tencent:
        enabled: true
        secret-id: ${TENCENT_SECRET_ID}
        secret-key: ${TENCENT_SECRET_KEY}
        sdk-app-id: 1400123456
        sign-name: 公司名
```

#### 华为云短信

**配置**:

```yaml
loadup:
  gotone:
    sms:
      huawei:
        enabled: true
        app-key: ${HUAWEI_APP_KEY}
        app-secret: ${HUAWEI_APP_SECRET}
        sender: 106XXXXXXXX
        signature: 【公司名】
```

#### 云片短信

**配置**:

```yaml
loadup:
  gotone:
    sms:
      yunpian:
        enabled: true
        api-key: ${YUNPIAN_API_KEY}
        sign-name: 【公司名】
```

### Email 配置

#### Gmail

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password  # 使用应用专用密码
    properties:
      mail.smtp:
        auth: true
        starttls.enable: true
```

**获取应用密码**:

1. Google 账户 > 安全性
2. 两步验证 > 应用密码
3. 生成应用密码

#### QQ 邮箱

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: your-qq@qq.com
    password: authorization-code  # 使用授权码
    properties:
      mail.smtp:
        auth: true
        starttls.enable: true
```

**获取授权码**:

1. QQ 邮箱 > 设置 > 账户
2. POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务
3. 开启服务并获取授权码

#### 163 邮箱

```yaml
spring:
  mail:
    host: smtp.163.com
    port: 465
    username: your-email@163.com
    password: authorization-code
    properties:
      mail.smtp:
        auth: true
        ssl.enable: true
```

### Push 配置

#### Firebase Cloud Messaging

**配置**:

```yaml
loadup:
  gotone:
    push:
      fcm:
        enabled: true
        server-key: ${FCM_SERVER_KEY}
```

**获取 Server Key**:

1. [Firebase Console](https://console.firebase.google.com/)
2. 项目设置 > Cloud Messaging
3. 复制服务器密钥

### 缓存配置

**Caffeine（推荐）**:

```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=3600s
```

**Redis**:

```yaml
spring:
  cache:
    type: redis
  redis:
    host: localhost
    port: 6379
```

### 重试配置

```yaml
loadup:
  gotone:
    retry:
      enabled: true
      max-attempts: 3               # 最大重试次数
      backoff-delay: 1000           # 退避延迟（毫秒）
      scan-cron: "0 */30 * * * ?"   # 定时扫描（每30分钟）
```

## 🔐 安全配置

### 1. 使用环境变量

**不要在配置文件中硬编码敏感信息**:

```yaml
# ❌ 错误
loadup:
  gotone:
    sms:
      aliyun:
        access-key-secret: 123456789

# ✅ 正确
loadup:
  gotone:
    sms:
      aliyun:
        access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
```

**设置环境变量**:

```bash
# Linux/macOS
export ALIYUN_ACCESS_KEY_SECRET=your-secret

# Windows
set ALIYUN_ACCESS_KEY_SECRET=your-secret
```

### 2. 使用配置加密

**Jasypt 加密**:

```yaml
# 加密后的配置
loadup:
  gotone:
    sms:
      aliyun:
        access-key-secret: ENC(encrypted_value)
```

```bash
# 启动时提供解密密钥
java -jar app.jar --jasypt.encryptor.password=your-master-password
```

### 3. 使用配置中心

**Spring Cloud Config**:

```yaml
spring:
  cloud:
    config:
      uri: http://config-server:8888
      name: gotone
      profile: prod
```

**Nacos**:

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        namespace: gotone
```

## 🌍 环境配置

### 开发环境

```yaml
spring:
  profiles:
    active: dev

---
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:mysql://localhost:3306/gotone_dev
  sql:
    init:
      mode: always  # 自动初始化

logging:
  level:
    com.github.loadup.components.gotone: DEBUG
```

### 生产环境

```yaml
---
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:mysql://prod-mysql:3306/gotone
    hikari:
      maximum-pool-size: 50
  sql:
    init:
      mode: never  # 不自动初始化

logging:
  level:
    com.github.loadup.components.gotone: INFO
    root: WARN
```

**启动命令**:

```bash
java -jar app.jar --spring.profiles.active=prod
```

## 📊 监控配置

### Prometheus

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

**访问指标**:

```
http://localhost:8080/actuator/prometheus
```

### 日志配置

```yaml
logging:
  level:
    com.github.loadup.components.gotone: DEBUG
    org.springframework.jdbc: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/gotone.log
    max-size: 100MB
    max-history: 30
```

## ⚡ 性能优化配置

### 线程池

```yaml
loadup:
  gotone:
    executor:
      core-pool-size: 10
      max-pool-size: 50
      queue-capacity: 1000
      thread-name-prefix: "gotone-"
```

### 限流

```yaml
loadup:
  gotone:
    rate-limit:
      enabled: true
      per-phone: 5      # 每个号码每分钟最多 5 条
      per-ip: 100       # 每个 IP 每分钟最多 100 条
```

## 🔍 常见问题

### 1. 数据库连接失败

**问题**: `Communications link failure`

**解决方案**:

- 检查数据库是否启动
- 检查 URL、用户名、密码是否正确
- 检查防火墙设置

### 2. 短信发送失败

**问题**: `InvalidAccessKeyId`

**解决方案**:

- 检查 AccessKey 是否正确
- 检查 AccessKey 是否有权限
- 检查签名是否审核通过

### 3. 邮件发送失败

**问题**: `Authentication failed`

**解决方案**:

- Gmail: 使用应用专用密码
- QQ/163: 使用授权码，不是登录密码
- 检查 SMTP 服务是否开启

### 4. 模板找不到

**问题**: `Template not found`

**解决方案**:

- 检查数据库中是否有模板记录
- 检查模板代码是否正确
- 检查模板是否启用（enabled = true）

## 📚 参考文档

- [主文档](README.md)
- [快速开始](QUICKSTART.md)
- [架构设计](ARCHITECTURE.md)

---

**最后更新**: 2025-12-30  
**维护团队**: LoadUp Cloud Team


# UPMS 配置检查清单

在部署 LoadUp UPMS 之前，请确保完成以下配置检查：

## ✅ 基础环境

- [ ] JDK 17+ 已安装
- [ ] Maven 3.8+ 已安装
- [ ] PostgreSQL 14+ 已安装并运行
- [ ] Redis 6.0+ 已安装并运行（如使用Redis缓存）

## ✅ 数据库配置

- [ ] 创建数据库：`CREATE DATABASE loadup_upms;`
- [ ] 执行初始化脚本：`psql -U postgres -d loadup_upms -f schema.sql`
- [ ] 验证表创建成功：应有12张表
- [ ] 验证初始数据：默认管理员账号 `admin/admin123`

## ✅ 应用配置文件

复制 `application.yml.example` 为 `application.yml` 并修改以下配置：

### 1. 数据源配置
```yaml
spring:
  datasource:
    url: jdbc:postgresql://YOUR_DB_HOST:5432/loadup_upms
    username: YOUR_DB_USERNAME
    password: YOUR_DB_PASSWORD
```

- [ ] 数据库连接地址正确
- [ ] 用户名密码正确
- [ ] 连接池参数根据实际调整

### 2. Redis配置
```yaml
spring:
  redis:
    host: YOUR_REDIS_HOST
    port: 6379
    password: YOUR_REDIS_PASSWORD  # 如无密码可留空
```

- [ ] Redis连接信息正确
- [ ] 网络可达性测试通过

### 3. JWT密钥配置 ⚠️ 重要
```yaml
upms:
  security:
    jwt:
      secret: YOUR_CUSTOM_SECRET_KEY_AT_LEAST_32_CHARACTERS_LONG
```

- [ ] **生产环境必须修改默认密钥！**
- [ ] 密钥长度至少32个字符
- [ ] 密钥包含大小写字母、数字、特殊字符
- [ ] 密钥已妥善保管（不要提交到版本控制）

### 4. 安全策略配置
```yaml
upms:
  security:
    login:
      max-fail-attempts: 5      # 最大登录失败次数
      lock-duration: 30         # 锁定时长（分钟）
    captcha:
      enabled: true             # 是否启用验证码
```

- [ ] 登录失败策略符合安全要求
- [ ] 验证码开关设置正确

### 5. 白名单配置
```yaml
upms:
  security:
    whitelist:
      - /api/v1/auth/**
      - /actuator/health
      # 添加你的自定义路径
```

- [ ] 检查是否有需要排除的路径
- [ ] 确保敏感接口不在白名单中

## ✅ 组件集成配置

### DFS 文件存储（用于头像上传）
```yaml
loadup:
  dfs:
    type: local  # 或 s3/database
    local:
      base-path: /var/loadup/uploads
      url-prefix: http://localhost:8080/files
```

- [ ] 存储路径已创建并有写权限
- [ ] URL前缀与实际访问地址一致

### Gotone 消息通知（用于短信/邮件）
```yaml
loadup:
  gotone:
    sms:
      provider: aliyun  # 根据实际配置
    email:
      provider: smtp
      from: noreply@yourdomain.com
```

- [ ] 短信服务商配置正确
- [ ] 邮件SMTP配置正确
- [ ] 测试发送功能正常

## ✅ 日志配置

```yaml
logging:
  level:
    com.github.loadup.modules.upms: DEBUG  # 生产环境改为INFO
  file:
    name: logs/upms.log
```

- [ ] 日志级别适合当前环境
- [ ] 日志路径有写权限
- [ ] 配置了日志轮转策略

## ✅ 监控配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

- [ ] 健康检查端点可访问
- [ ] Prometheus metrics已启用（如需要）

## ✅ 编译与部署

### 编译检查
```bash
mvn clean package -DskipTests
```

- [ ] 编译成功，无错误
- [ ] 生成的JAR包大小正常

### 运行检查
```bash
java -jar loadup-modules-upms-starter.jar
```

- [ ] 应用启动成功
- [ ] 无ERROR级别日志
- [ ] 端口监听正常（默认8080）

## ✅ 功能测试

### 1. 健康检查
```bash
curl http://localhost:8080/actuator/health
```
- [ ] 返回 `{"status":"UP"}`

### 2. API文档访问
```bash
http://localhost:8080/swagger-ui.html
```
- [ ] Swagger页面可正常访问
- [ ] 所有接口列表显示正常

### 3. 登录测试
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```
- [ ] 返回access_token和refresh_token
- [ ] 用户信息正确

### 4. Token验证测试
```bash
curl -X GET http://localhost:8080/api/v1/user/profile \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```
- [ ] 返回用户详情
- [ ] 权限列表正确

## ✅ 安全检查（生产环境必做）

- [ ] 修改默认管理员密码
- [ ] 关闭或保护Swagger端点
- [ ] 配置HTTPS
- [ ] 启用CORS白名单
- [ ] 配置防火墙规则
- [ ] 数据库只允许应用服务器访问
- [ ] Redis设置密码认证
- [ ] 日志脱敏敏感信息

## ✅ 性能优化检查

- [ ] 数据库连接池参数已优化
- [ ] Redis缓存已启用
- [ ] 索引已正确创建
- [ ] JVM参数已调优（生产环境）
```bash
java -Xms2g -Xmx2g -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar loadup-modules-upms-starter.jar
```

## ✅ 备份策略

- [ ] 数据库定期备份计划已制定
- [ ] 配置文件已备份
- [ ] JWT密钥已安全存储
- [ ] 灾难恢复流程已测试

## 📋 常见问题

### Q1: 启动时报 "Failed to configure a DataSource"
**解决**: 检查数据库配置是否正确，数据库是否可访问

### Q2: JWT验证失败
**解决**: 检查JWT密钥配置，确保所有节点使用相同密钥

### Q3: 权限始终返回403
**解决**: 检查用户角色权限配置，查看操作日志确认权限验证逻辑

### Q4: 操作日志未记录
**解决**: 检查是否启用了@EnableAsync，数据库写权限是否正常

### Q5: 文件上传失败
**解决**: 检查DFS配置，确认存储路径权限和空间

## 📞 获取帮助

遇到问题？

1. 查看日志：`tail -f logs/upms.log`
2. 提交Issue：https://github.com/loadup-cloud/loadup-framework/issues
3. 查看文档：[README.md](./README.md) | [ARCHITECTURE.md](./ARCHITECTURE.md)

---

**配置完成后，请保存此清单作为运维文档的一部分**

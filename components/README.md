# LoadUp Components

## 概述

LoadUp Components 提供了一系列可复用的组件，用于简化企业级应用开发。

## 组件列表

| 组件 | 说明 | 文档 |
|------|------|------|
| **loadup-components-authorization** | 轻量级授权框架（基于 AOP） | [README](loadup-components-authorization/README.md) |
| **loadup-components-cache** | 缓存抽象层（支持 Redis、Caffeine） | [README](loadup-components-cache/README.md) |
| **loadup-components-captcha** | 验证码组件 | - |
| **loadup-components-database** | 数据库增强（MyBatis-Flex） | - |
| **loadup-components-dfs** | 分布式文件存储 | - |
| **loadup-components-extension** | 扩展点框架 | - |
| **loadup-components-gotone** | 通知服务（邮件/短信/推送） | [README](loadup-components-gotone/README.md) |
| **loadup-components-liquibase** | 数据库迁移 | [README](loadup-components-liquibase/README.md) |
| **loadup-components-scheduler** | 定时任务（支持 XXL-Job） | - |
| **loadup-components-testcontainers** | 测试容器支持 | [README](loadup-components-testcontainers/README.md) |
| **loadup-components-tracer** | 链路追踪 | - |
| **loadup-components-web** | Web 增强 | - |

## 核心组件详解

### loadup-components-authorization

**特性**:
- ✅ 轻量级（~50KB，无 Spring Security 依赖）
- ✅ 基于 AOP 的方法级权限控制
- ✅ 支持角色和权限两种模式
- ✅ ThreadLocal 用户上下文

**快速开始**:
```java
@Service
public class UserService {
    
    @RequireRole("ADMIN")
    public void deleteUser(String userId) {
        // 只有 ADMIN 可以删除
    }
    
    @RequirePermission("user:delete")
    public void delete(String userId) {
        // 检查权限
    }
}
```

### loadup-components-cache

**特性**:
- ✅ 统一缓存抽象层
- ✅ 支持多种缓存实现（Redis、Caffeine、内存）
- ✅ 注解驱动
- ✅ 自动刷新和过期

**快速开始**:
```java
@Service
public class UserService {
    
    @Cacheable(key = "'user:' + #userId", ttl = 3600)
    public UserDTO getUser(String userId) {
        return userRepository.findById(userId);
    }
    
    @CacheEvict(key = "'user:' + #userId")
    public void updateUser(String userId, UserDTO dto) {
        userRepository.update(userId, dto);
    }
}
```

### loadup-components-gotone

**特性**:
- ✅ 统一通知服务抽象
- ✅ 支持邮件、短信、推送
- ✅ 模板引擎集成
- ✅ 异步发送

**快速开始**:
```java
@Service
public class NotificationService {
    
    @Autowired
    private GotOneTemplate gotOneTemplate;
    
    public void sendEmail(String to, String subject, String content) {
        EmailMessage message = EmailMessage.builder()
            .to(to)
            .subject(subject)
            .content(content)
            .build();
        gotOneTemplate.send(message);
    }
}
```

## 使用指南

### 添加依赖

在项目 `pom.xml` 中添加所需组件：

```xml
<dependencies>
    <!-- 授权组件 -->
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-components-authorization</artifactId>
    </dependency>
    
    <!-- 缓存组件 -->
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-components-cache</artifactId>
    </dependency>
    
    <!-- 选择缓存实现 -->
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-components-cache-binder-redis</artifactId>
    </dependency>
</dependencies>
```

### 配置

在 `application.yml` 中配置组件：

```yaml
loadup:
  cache:
    type: redis
    redis:
      host: localhost
      port: 6379
  
  gotone:
    email:
      enabled: true
      host: smtp.example.com
      port: 465
```

## 架构原则

1. **单一职责**: 每个组件专注于一个领域
2. **可选依赖**: 组件之间低耦合，按需引入
3. **配置化**: 通过配置文件控制组件行为
4. **可扩展**: 提供 SPI 接口支持自定义实现

## 相关文档

- [组件概览](../docs/components.md)
- [缓存组件](../docs/components/cache.md)
- [验证码组件](../docs/components/captcha.md)
- [数据库组件](../docs/components/database.md)
- [更多组件文档...](../docs/components/)

## 贡献

欢迎提交 Issue 和 Pull Request！

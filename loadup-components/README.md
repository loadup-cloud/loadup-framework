# LoadUp Components

## 概述

LoadUp Components 提供了一系列可复用的组件，用于简化企业级应用开发。

## 组件列表

| 组件                                   | 说明                       | 文档                                                   |
|--------------------------------------|--------------------------|------------------------------------------------------|
| **loadup-components-authorization**  | 方法级授权（Spring Security 标准 API） | [README](loadup-components-authorization/README.md)  |
| **loadup-components-cache**          | 缓存抽象层（支持 Redis、Caffeine） | [README](loadup-components-cache/README.md)          |
| **loadup-components-captcha**        | 验证码组件（tianai 行为 / nanocaptcha 图像，binder 可插拔） | -                          |
| **loadup-components-database**       | 数据库增强（MyBatis-Flex）      | -                                                    |
| **loadup-components-dfs**            | 分布式文件存储                  | -                                                    |
| **loadup-components-extension**      | 扩展点框架                    | -                                                    |
| **loadup-components-gotone**         | 统一通知（ServiceCode 路由，邮件/webhook/短信/推送） | [README](loadup-components-gotone/README.md)         |
| **loadup-components-resilience4j**   | 容错（熔断/重试/限流/舱壁/超时，Resilience4j 底座） | [README](loadup-components-resilience4j/README.md) |
| **loadup-components-signature**      | 数字签名（JCA 薄封装：RSA/DSA/ECDSA + 摘要/HMAC） | [README](loadup-components-signature/README.md)    |
| **loadup-components-globalunique**   | 全局幂等控制（数据库唯一键，事务内幂等）            | [README](loadup-components-globalunique/README.md) |
| **loadup-components-liquibase**      | 数据库迁移                    | [README](loadup-components-liquibase/README.md)      |
| **loadup-components-scheduler**      | 定时任务（支持 XXL-Job）         | -                                                    |
| **loadup-components-testcontainers** | 测试容器支持                   | [README](loadup-components-testcontainers/README.md) |
| **loadup-components-web**            | Web 增强                   | -                                                    |

## 核心组件详解

### loadup-components-authorization

**特性**:

- ✅ 基于 Spring Security 标准 API 的方法级权限控制
- ✅ 支持角色和权限两种模式
- ✅ `SecurityContextHolder` 薄适配（`UserContext`）

**快速开始**:

```java
@Service
public class UserService {
    
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        // 只有 ADMIN 可以删除
    }
    
    @PreAuthorize("hasAuthority('user:delete')")
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

- ✅ ServiceCode 驱动的 `NotificationService` 门面（业务零渠道 SDK 感知）
- ✅ email（Spring Mail）/ webhook（真实 HTTP）渠道 binder；sms / push 为桩
- ✅ 渠道内 provider 降级链 + resilience4j 熔断/重试
- ✅ 可选 `store-jdbc` 存储（MyBatis-Flex + Flyway），引擎零存储零 DB
- ✅ 与 retrytask 集成：永久失败自动走 gotone 告警

**快速开始**:

```java
notificationService.send(NotificationRequest.builder()
        .serviceCode("ORDER_CONFIRM")
        .receivers(List.of("ops@example.com"))
        .templateParams(Map.of("orderId", "123"))
        .build());
```

引入 `-api` + `-engine` + 需要的 `-binder-*`（+ 可选 `-store-jdbc`）即可，详见
[gotone README](loadup-components-gotone/README.md)。

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
    resilience:
      enabled: true
    binder:
      email:
        smtp:
          enabled: true
spring:
  mail:
    host: smtp.example.com   # email 渠道复用标准 spring.mail.*
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

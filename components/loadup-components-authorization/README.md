# LoadUp Components Authorization

## 概述

**轻量级方法级授权框架**，基于 AOP 实现，完全替代 Spring Security，提供简洁高效的权限控制。

## 特性

- ✅ **轻量级**: 仅依赖 Spring AOP 和 AspectJ，jar 包 ~50KB
- ✅ **简单易用**: 注解驱动，API 简洁
- ✅ **高性能**: 最小 AOP 开销，ThreadLocal 存储
- ✅ **灵活**: 支持角色和权限两种模式
- ✅ **无侵入**: 与 Spring Boot 无缝集成

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-authorization</artifactId>
</dependency>
```

### 2. 使用注解

```java
@Service
public class UserService {
    
    // 角色检查：只有 ADMIN 可以执行
    @RequireRole("ADMIN")
    public void deleteUser(String userId) {
        // 业务逻辑
    }
    
    // 多角色（任一即可）
    @RequireRole(value = {"ADMIN", "USER"}, logical = Logical.OR)
    public UserDTO getUser(String userId) {
        return userRepository.findById(userId);
    }
    
    // 多角色（必须全部具备）
    @RequireRole(value = {"ADMIN", "AUDITOR"}, logical = Logical.AND)
    public void auditAction() {
        // 审计逻辑
    }
    
    // 权限检查
    @RequirePermission("user:delete")
    public void deleteByPermission(String userId) {
        // 业务逻辑
    }
    
    // 获取当前用户
    public void someMethod() {
        String userId = UserContext.getUserId();
        String username = UserContext.getUsername();
        LoadUpUser user = UserContext.get();
    }
}
```

### 3. Gateway 集成

在 Gateway 的 Proxy 插件中设置用户上下文：

```java
public class MyProxyProcessor implements ProxyProcessor {
    
    @Override
    public Response proxy(Request request, RouteConfig route) {
        try {
            // 从 request 获取用户信息（由 SecurityAction 填充）
            String userId = (String) request.getAttributes().get("userId");
            String username = (String) request.getAttributes().get("username");
            List<String> roles = (List<String>) request.getAttributes().get("roles");
            
            // 设置用户上下文
            LoadUpUser user = LoadUpUser.builder()
                .userId(userId)
                .username(username)
                .roles(roles)
                .build();
            UserContext.set(user);
            
            // 执行业务逻辑
            return invokeTargetBean(request);
            
        } finally {
            // 必须清理，防止内存泄漏
            UserContext.clear();
        }
    }
}
```

## 核心 API

### 注解

#### @RequireRole

角色检查注解，可用于类或方法。

```java
@RequireRole("ADMIN")                                    // 单角色
@RequireRole({"ADMIN", "USER"})                         // 多角色 OR（默认）
@RequireRole(value = {"ADMIN", "USER"}, logical = OR)   // 明确指定 OR
@RequireRole(value = {"ADMIN", "AUDITOR"}, logical = AND) // 多角色 AND
```

#### @RequirePermission

权限检查注解，可用于类或方法。

```java
@RequirePermission("user:delete")                       // 单权限
@RequirePermission({"user:read", "user:write"})        // 多权限 OR
@RequirePermission(value = {"admin:read", "admin:write"}, logical = AND) // 多权限 AND
```

### UserContext

静态工具类，用于访问当前用户信息。

```java
// 设置用户（通常由 Gateway 完成）
LoadUpUser user = LoadUpUser.builder()
    .userId("123")
    .username("admin")
    .roles(Arrays.asList("ADMIN", "USER"))
    .build();
UserContext.set(user);

// 获取用户
LoadUpUser currentUser = UserContext.get();
String userId = UserContext.getUserId();
String username = UserContext.getUsername();

// 检查用户是否存在
boolean hasUser = UserContext.isPresent();

// 清理（必须在 finally 块中）
UserContext.clear();
```

### LoadUpUser

用户模型，包含 userId、username、roles、permissions 等信息。

```java
LoadUpUser user = LoadUpUser.builder()
    .userId("123")
    .username("admin")
    .roles(Arrays.asList("ADMIN", "USER"))
    .permissions(Arrays.asList("user:read", "user:write"))
    .attributes(Map.of("orgId", "org-001"))
    .build();

// 检查角色
boolean isAdmin = user.hasRole("ADMIN");
boolean hasAnyRole = user.hasAnyRole("ADMIN", "USER");
boolean hasAllRoles = user.hasAllRoles("ADMIN", "AUDITOR");

// 检查权限
boolean canDelete = user.hasPermission("user:delete");
```

## 异常处理

### UnauthorizedException (401)

用户未认证时抛出。

```java
throw new UnauthorizedException("User not authenticated");
```

### ForbiddenException (403)

用户已认证但权限不足时抛出。

```java
throw new ForbiddenException("Insufficient permissions");
```

### 全局异常处理

建议在全局异常处理器中捕获：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(401)
            .body(Result.error("401", e.getMessage()));
    }
    
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Result> handleForbidden(ForbiddenException e) {
        return ResponseEntity.status(403)
            .body(Result.error("403", e.getMessage()));
    }
}
```

## 工作原理

### 架构流程

```
1. Gateway 认证
   ├─ 验证 JWT/签名等
   ├─ 提取用户信息
   └─ 存储到 request.attributes

2. Gateway 转发
   ├─ Proxy 设置 UserContext
   └─ 调用业务 Bean

3. AOP 拦截
   ├─ AuthorizationAspect 拦截 @RequireRole/@RequirePermission
   ├─ 从 UserContext 获取当前用户
   ├─ 检查角色/权限
   └─ 通过/拒绝

4. 业务执行
   └─ 执行实际业务逻辑

5. 清理
   └─ Proxy finally 清理 UserContext
```

### ThreadLocal 管理

```java
// 设置（请求开始）
UserContext.set(user);  // ThreadLocal.set()

// 获取（业务处理）
LoadUpUser user = UserContext.get();  // ThreadLocal.get()

// 清理（请求结束）
UserContext.clear();  // ThreadLocal.remove()
```

**⚠️ 重要**: 必须在 `finally` 块中调用 `UserContext.clear()`，否则在线程池环境下会导致内存泄漏。

## 与 Spring Security 对比

| 维度 | Spring Security | LoadUp Authorization |
|------|----------------|---------------------|
| **依赖大小** | ~2MB | ~50KB |
| **学习曲线** | 陡峭 | 平缓 |
| **配置复杂度** | 高 | 低 |
| **性能** | 中等 | 优秀 |
| **功能丰富度** | 丰富 | 精简 |
| **适用场景** | 复杂权限系统 | 简单权限控制 |

## 注意事项

### 1. 异步调用

异步调用时 ThreadLocal 不会自动传递，需要手动处理：

```java
LoadUpUser user = UserContext.get();

CompletableFuture.supplyAsync(() -> {
    UserContext.set(user);  // 手动设置
    try {
        return doSomething();
    } finally {
        UserContext.clear();  // 手动清理
    }
});
```

### 2. @Async 方法

对于 Spring 的 `@Async` 方法，建议使用自定义 TaskDecorator：

```java
@Configuration
public class AsyncConfig {
    
    @Bean
    public TaskDecorator userContextTaskDecorator() {
        return runnable -> {
            LoadUpUser user = UserContext.get();
            return () -> {
                try {
                    UserContext.set(user);
                    runnable.run();
                } finally {
                    UserContext.clear();
                }
            };
        };
    }
}
```

### 3. 角色前缀

框架自动支持带或不带 `ROLE_` 前缀的角色：

```java
@RequireRole("ADMIN")  // 会匹配 "ADMIN" 或 "ROLE_ADMIN"
```

如果数据库存储的是 `ADMIN`，框架会自动适配；如果存储的是 `ROLE_ADMIN`，也能正确识别。

## 迁移指南

### 从 Spring Security 迁移

#### 1. 替换注解

| Spring Security | LoadUp Authorization |
|----------------|---------------------|
| `@PreAuthorize("hasRole('ADMIN')")` | `@RequireRole("ADMIN")` |
| `@PreAuthorize("hasAnyRole('ADMIN','USER')")` | `@RequireRole({"ADMIN","USER"})` |
| `@PreAuthorize("hasAuthority('user:delete')")` | `@RequirePermission("user:delete")` |

#### 2. 替换上下文访问

| Spring Security | LoadUp Authorization |
|----------------|---------------------|
| `SecurityContextHolder.getContext()` | `UserContext.get()` |
| `Authentication.getPrincipal()` | `UserContext.get()` |
| `SecurityContextHolder.getContext().getAuthentication().getName()` | `UserContext.getUsername()` |

#### 3. 替换依赖

```xml
<!-- 移除 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- 添加 -->
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-authorization</artifactId>
</dependency>
```

## 示例项目

完整示例请参考：
- [Gateway 集成示例](../../loadup-gateway/plugins/proxy-springbean-plugin/)
- [UPMS 模块使用示例](../../loadup-modules/loadup-modules-upms/)

## 常见问题

**Q: 为什么不用 Spring Security？**

A: 对于简单的角色权限控制，Spring Security 过于复杂和重量级。本框架提供了足够的功能，同时保持轻量和易用。

**Q: 支持复杂的权限表达式吗？**

A: 目前只支持简单的角色和权限检查。如需复杂表达式，可以在业务代码中手动检查 `UserContext.get()`。

**Q: 如何实现行级权限控制？**

A: 在业务代码中检查：

```java
public void updateUser(String userId, UserDTO dto) {
    String currentUserId = UserContext.getUserId();
    if (!currentUserId.equals(userId) && !hasRole("ADMIN")) {
        throw new ForbiddenException("Can only update own profile");
    }
    // 更新逻辑
}
```

**Q: 性能如何？**

A: AOP 开销极小（~微秒级），ThreadLocal 读写也非常快。相比 Spring Security，性能提升约 10%。

## 许可证

本项目采用 GPL-3.0 许可证。

## 贡献

欢迎提交 Issue 和 Pull Request！

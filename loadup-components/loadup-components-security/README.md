# LoadUp Components Security

## 组件定位

**授权组件** - 提供基于角色的方法级权限控制

> **注意**: 本组件**不负责认证**，认证由 Gateway 组件完成。

## 核心功能

### 1. 方法级权限注解支持

使用 Spring Security 的 `@PreAuthorize` / `@PostAuthorize` 注解：

```java
@Service
public class UserService {
    
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        // 只有 ADMIN 角色可以删除用户
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public UserDTO getProfile(String userId) {
        // ADMIN 或 USER 角色都可以查看
    }
    
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    public void updateUser(String userId, UserDTO dto) {
        // ADMIN 可以修改任何用户，普通用户只能修改自己
    }
}
```

### 2. 用户信息模型

```java
LoadUpUser user = SecurityHelper.getCurUser();
String userId = user.getUserId();
String username = user.getUsername();
List<String> roles = user.getRoles();
```

## 工作流程

```
请求流程：
1. Gateway 进行认证（JWT/签名/等）
2. Gateway 将用户信息填充到 SecurityContext
3. 业务方法执行前，Spring Security 检查 @PreAuthorize
4. 通过后执行业务逻辑
```

## 依赖说明

- `spring-security-core`: 提供 UserDetails、GrantedAuthority 等核心接口
- `spring-security-config`: 提供 `@EnableMethodSecurity` 注解支持

**不包含**：
- ❌ Spring Security Filter Chain
- ❌ 认证逻辑
- ❌ Servlet Filter
- ❌ JWT 解析

## 使用示例

### 在业务模块中引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-security</artifactId>
</dependency>
```

### 获取当前用户

```java
import io.github.loadup.components.security.util.SecurityHelper;

public class MyService {
    
    public void doSomething() {
        String currentUserId = SecurityHelper.getCurUserId();
        String currentUserName = SecurityHelper.getCurUserName();
        
        // 业务逻辑
    }
}
```

### 权限表达式

| 表达式 | 说明 |
|--------|------|
| `hasRole('ADMIN')` | 用户必须有 ADMIN 角色 |
| `hasAnyRole('ADMIN', 'USER')` | 用户有任意一个角色即可 |
| `hasAuthority('user:delete')` | 用户必须有特定权限 |
| `principal.userId == #userId` | 当前用户ID 与参数匹配 |
| `isAuthenticated()` | 用户已认证 |

## 架构约定

- **Gateway**: 负责认证（Authentication）
- **Security 组件**: 负责授权（Authorization）
- **业务模块**: 专注业务逻辑，使用注解声明权限

## 注意事项

1. **SecurityContext 必须在业务方法前填充**
   - Gateway 需要在转发到业务 Bean 前设置好用户信息
   
2. **角色命名规范**
   - Spring Security 默认会在角色名前加 `ROLE_` 前缀
   - 如果数据库存储的是 `ADMIN`，使用 `hasRole('ADMIN')`
   - 如果存储的是 `ROLE_ADMIN`，使用 `hasAuthority('ROLE_ADMIN')`

3. **线程安全**
   - `SecurityContextHolder` 默认使用 `ThreadLocal`
   - JDK 21 虚拟线程环境下也是安全的
   - 异步操作需要手动传递 Context（使用 `DelegatingSecurityContextExecutor`）

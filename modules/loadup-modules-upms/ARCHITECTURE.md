# LoadUp UPMS 架构设计文档

## 1. 架构概述

LoadUp UPMS（User Permission Management System）采用 **COLA 4.0** 应用架构，实现了严格的分层设计和依赖倒置原则。

### 1.1 分层架构

```
┌─────────────────────────────────────────────────────┐
│                  Adapter Layer                       │
│  (REST API, DTO, Request/Response Mapping)          │
└────────────────────┬────────────────────────────────┘
                     │ depends on
┌────────────────────▼────────────────────────────────┐
│                  Application Layer                   │
│  (Command/Query Handler, Business Orchestration)    │
└────────────────────┬────────────────────────────────┘
                     │ depends on
┌────────────────────▼────────────────────────────────┐
│                   Domain Layer                       │
│  (Entity, Value Object, Domain Service, Repository) │
└────────────────────▲────────────────────────────────┘
                     │ implemented by
┌────────────────────┴────────────────────────────────┐
│               Infrastructure Layer                   │
│  (Repository Impl, Security, Cache, External API)   │
└─────────────────────────────────────────────────────┘
```

### 1.2 核心设计原则

1. **单一职责原则 (SRP)**: 每层只负责特定职责
2. **依赖倒置原则 (DIP)**: 高层不依赖低层，都依赖抽象
3. **接口隔离原则 (ISP)**: 细粒度的Repository接口设计
4. **开闭原则 (OCP)**: 通过策略模式支持扩展
5. **里氏替换原则 (LSP)**: Repository实现可替换

## 2. 各层详细设计

### 2.1 Domain Layer (领域层)

**职责**：定义核心业务模型和规则

#### 2.1.1 实体设计

```java
// 聚合根示例
@Table("upms_user")
public class User {
    @Id
    private Long id;
    private String username;
    // ... other fields
    
    // 领域行为
    public void lockAccount() { ... }
    public void unlockAccount() { ... }
    public boolean isActive() { ... }
}
```

**实体特点**：
- 充血模型：包含业务逻辑方法
- 不依赖任何框架（除Spring Data JDBC注解）
- 严格的封装性

#### 2.1.2 值对象

```java
public class DataScope {
    public static final short ALL = 1;
    public static final short DEPT = 3;
    // ...
    
    public boolean isAll() { ... }
    public boolean isDept() { ... }
}
```

**设计要点**：
- 不可变对象
- 自解释的业务语义
- 类型安全

#### 2.1.3 Repository 接口

```java
public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    // ...
}
```

**接口设计**：
- 面向领域概念，而非数据库操作
- 返回领域对象，而非DTO
- 方法命名符合业务语言

#### 2.1.4 Domain Service

```java
@RequiredArgsConstructor
public class UserPermissionService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    public List<Permission> getUserPermissions(Long userId) {
        // 实现 RBAC3 角色继承逻辑
    }
}
```

**适用场景**：
- 跨聚合的业务逻辑
- 复杂的计算逻辑
- 无状态服务

### 2.2 Application Layer (应用层)

**职责**：业务流程编排和事务管理

#### 2.2.1 Command/Query 设计

```java
// Command - 修改操作
@Data
public class UserLoginCommand {
    private String username;
    private String password;
    private String captchaKey;
    private String captchaCode;
}

// Query - 查询操作（预留）
@Data
public class UserQuery {
    private String keyword;
    private Long deptId;
    private Pageable pageable;
}
```

**CQRS 原则**：
- Command：改变系统状态
- Query：只读查询
- 明确的意图分离

#### 2.2.2 Application Service

```java
@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    
    public LoginResultDTO login(UserLoginCommand command) {
        // 1. 验证用户
        // 2. 检查账号状态
        // 3. 生成令牌
        // 4. 记录日志
        // 5. 返回结果
    }
}
```

**设计要点**：
- 编排Domain层逻辑
- 管理事务边界
- 转换为DTO返回
- 不包含复杂业务逻辑

### 2.3 Infrastructure Layer (基础设施层)

**职责**：技术实现和外部系统集成

#### 2.3.1 Repository 实现

```java
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final JdbcUserRepository jdbcRepository;
    
    @Override
    public User save(User user) {
        return jdbcRepository.save(user);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return jdbcRepository.findByUsername(username);
    }
}
```

**技术选型**：
- Spring Data JDBC（轻量级ORM）
- 自定义@Query查询
- 软删除支持

#### 2.3.2 Spring Security 集成

```
┌──────────────┐
│   Request    │
└──────┬───────┘
       │
       ▼
┌──────────────────────┐
│ JwtAuthenticationFilter │ ← 解析JWT
└──────┬───────────────┘
       │
       ▼
┌──────────────────────┐
│ CustomUserDetailsService │ ← 加载用户权限
└──────┬───────────────┘
       │
       ▼
┌──────────────────────┐
│ SecurityUser         │ ← 包含权限信息
└──────────────────────┘
```

**安全流程**：
1. JWT过滤器提取token
2. 验证token有效性
3. 加载用户详情和权限
4. 设置SecurityContext
5. 后续请求可通过@PreAuthorize验证

#### 2.3.3 AOP 日志记录

```java
@Aspect
@Component
public class OperationLogAspect {
    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, 
                         OperationLog operationLog) {
        // 1. 记录请求信息
        // 2. 执行目标方法
        // 3. 记录响应信息
        // 4. 异步保存日志
    }
}
```

**优势**：
- 无侵入性
- 异步处理，不影响性能
- 统一的日志格式

### 2.4 Adapter Layer (适配层)

**职责**：接收外部请求，转换数据格式

#### 2.4.1 Controller 设计

```java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    
    @PostMapping("/login")
    public LoginResultDTO login(@Valid @RequestBody LoginRequest request) {
        // 1. 验证请求参数
        // 2. 转换为Command
        // 3. 调用Application层
        // 4. 返回DTO
    }
}
```

**设计规范**：
- RESTful API风格
- Bean Validation参数校验
- Swagger文档注解
- 统一异常处理

#### 2.4.2 DTO 设计

```java
// Request DTO
@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
}

// Response DTO
@Data
public class LoginResultDTO {
    private String accessToken;
    private String refreshToken;
    private UserInfoDTO userInfo;
}
```

**职责边界**：
- Request: 接收前端数据，校验格式
- Response: 向前端返回数据
- 与Domain实体完全解耦

## 3. RBAC3 权限模型实现

### 3.1 角色继承机制

```
┌─────────────────┐
│  ROLE_SUPER_ADMIN  │ (拥有所有权限)
└────────┬────────┘
         │ inherits
         ▼
┌─────────────────┐
│   ROLE_ADMIN    │ (继承超管权限)
└────────┬────────┘
         │ inherits
         ▼
┌─────────────────┐
│ ROLE_DEPT_MANAGER │ (继承管理员权限)
└─────────────────┘
```

**实现方式**：
```java
public List<Permission> getAllInheritedPermissions(Role role) {
    List<Permission> permissions = new ArrayList<>();
    
    // 添加当前角色权限
    permissions.addAll(getDirectPermissions(role));
    
    // 递归添加父角色权限
    if (role.getParentRoleId() != null) {
        Role parentRole = roleRepository.findById(role.getParentRoleId());
        permissions.addAll(getAllInheritedPermissions(parentRole));
    }
    
    return permissions;
}
```

### 3.2 数据权限控制

```java
public enum DataScope {
    ALL(1, "全部数据权限"),
    CUSTOM(2, "自定义数据权限"),
    DEPT(3, "本部门数据权限"),
    DEPT_AND_CHILDREN(4, "本部门及子部门数据权限"),
    SELF_ONLY(5, "仅本人数据权限");
}
```

**SQL过滤示例**：
```sql
-- 根据用户角色的 data_scope 动态添加 WHERE 条件
SELECT * FROM upms_user u
WHERE 
    CASE 
        WHEN :dataScope = 1 THEN 1=1  -- 全部数据
        WHEN :dataScope = 3 THEN u.dept_id = :userDeptId  -- 本部门
        WHEN :dataScope = 4 THEN u.dept_id IN (
            SELECT id FROM upms_department 
            WHERE parent_id = :userDeptId OR id = :userDeptId
        )  -- 本部门及子部门
        WHEN :dataScope = 5 THEN u.id = :userId  -- 仅本人
    END
```

## 4. 安全架构

### 4.1 认证流程

```
┌─────────┐     ┌──────────┐     ┌─────────────┐     ┌──────────┐
│  Client │────▶│ Controller│────▶│   AuthService│────▶│ Database │
└─────────┘     └──────────┘     └─────────────┘     └──────────┘
     │                                     │
     │ 1. POST /login                     │ 3. Verify credentials
     │ {username, password}               │
     │                                     │
     │◀────────────────────────────────────┘
     │ 4. Return JWT tokens
     │ {accessToken, refreshToken}
```

### 4.2 授权流程

```
┌─────────┐     ┌──────────────┐     ┌────────────────┐
│  Client │────▶│ JWT Filter   │────▶│ Security       │
└─────────┘     └──────────────┘     │ Context        │
     │               │                └────────────────┘
     │ Bearer token  │ 2. Validate JWT        │
     │               │                        │
     │               ▼                        │
     │          ┌──────────────┐              │
     │          │ Load User    │◀─────────────┘
     │          │ Permissions  │ 3. Check @PreAuthorize
     │          └──────────────┘
     │               │
     │◀──────────────┘
     │ 4. Access granted/denied
```

### 4.3 Token 管理

**双Token机制**：
- **Access Token**: 短期有效（24小时），用于API访问
- **Refresh Token**: 长期有效（7天），用于刷新access token

**优势**：
- 安全性高：即使access token泄露，影响范围有限
- 用户体验好：无需频繁登录
- 可撤销：refresh token存储在数据库，支持强制下线

## 5. 性能优化

### 5.1 缓存策略

```java
@Cacheable(value = "user:permissions", key = "#userId")
public Set<String> getUserPermissionCodes(Long userId) {
    // 权限查询结果缓存
}

@CacheEvict(value = "user:permissions", key = "#userId")
public void refreshUserPermissions(Long userId) {
    // 权限变更时清除缓存
}
```

### 5.2 数据库优化

**索引设计**：
```sql
-- 用户名唯一索引
CREATE UNIQUE INDEX idx_user_username ON upms_user(username);

-- 角色-权限关联复合索引
CREATE INDEX idx_rp_role_perm ON upms_role_permission(role_id, permission_id);

-- 操作日志时间索引（用于归档清理）
CREATE INDEX idx_log_created_time ON upms_operation_log(created_time);
```

### 5.3 异步处理

- **操作日志**: @Async异步保存，不阻塞主流程
- **邮件/短信**: 异步发送通知
- **统计任务**: 定时任务处理

## 6. 扩展性设计

### 6.1 多租户支持（预留）

```java
@Data
public class User {
    private Long tenantId;  // 租户ID
    // ...
}

// 在Repository查询时自动添加租户过滤
WHERE tenant_id = :currentTenantId
```

### 6.2 插件化权限验证

```java
public interface PermissionEvaluator {
    boolean hasPermission(Long userId, String resource, String action);
}

// 可替换不同的权限验证策略
@Component
public class RbacPermissionEvaluator implements PermissionEvaluator {
    // RBAC实现
}

@Component
public class AbacPermissionEvaluator implements PermissionEvaluator {
    // ABAC实现
}
```

## 7. 测试策略

### 7.1 单元测试

- Domain Service: Mock Repository
- Application Service: Mock Domain层和Infrastructure层

### 7.2 集成测试

- 使用Testcontainers启动真实数据库
- 完整的端到端测试
- 覆盖关键业务流程

## 8. 部署架构

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Nginx      │────▶│   UPMS       │────▶│  PostgreSQL  │
│  (Gateway)   │     │  Application │     │   (Primary)  │
└──────────────┘     └──────────────┘     └──────────────┘
                            │                      │
                            │                      │ replication
                            ▼                      ▼
                     ┌──────────────┐     ┌──────────────┐
                     │    Redis     │     │  PostgreSQL  │
                     │   (Cache)    │     │  (Replica)   │
                     └──────────────┘     └──────────────┘
```

## 9. 未来演进方向

1. **微服务拆分**: 将UPMS独立为认证中心
2. **GraphQL支持**: 提供更灵活的API查询
3. **事件驱动**: 引入Domain Event解耦
4. **多数据源**: 支持读写分离
5. **分布式会话**: 支持集群部署

---

**文档版本**: 1.0.0  
**最后更新**: 2025-12-31  
**维护者**: LoadUp Framework Team

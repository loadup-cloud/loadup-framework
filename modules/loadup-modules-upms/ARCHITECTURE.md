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
│   Nginx      │────▶│   UPMS       │────▶│    MySQL     │
│  (Gateway)   │     │  Application │     │   (Primary)  │
└──────────────┘     └──────────────┘     └──────────────┘
                            │                      │
                            │                      │ replication
                            ▼                      ▼
                     ┌──────────────┐     ┌──────────────┐
                     │    Redis     │     │    MySQL     │
                     │   (Cache)    │     │  (Replica)   │
                     └──────────────┘     └──────────────┘
```

## 9. 实现状态

### 9.1 已完成功能 ✅

#### Domain层

- ✅ 所有核心实体 (User, Role, Permission, Department, LoginLog, OperationLog)
- ✅ Repository接口定义
- ✅ 领域服务 (UserPermissionService)
- ✅ 值对象和枚举

#### Infrastructure层

- ✅ Spring Data JDBC Repository实现
- ✅ Spring Security配置
- ✅ JWT认证过滤器
- ✅ 数据权限AOP (@DataScope注解)
- ✅ 软删除支持

#### Application层

- ✅ 认证服务 (AuthenticationService)
- ✅ 用户服务 (UserService - 8个方法)
- ✅ 角色服务 (RoleService - 10个方法)
- ✅ 权限服务 (PermissionService - 8个方法)
- ✅ 部门服务 (DepartmentService - 8个方法)
- ✅ 密码重置服务 (PasswordResetService)
- ✅ 验证码服务 (VerificationCodeService)
- ✅ Command/Query对象 (12个)
- ✅ DTO对象 (9个)

#### Adapter层

- ✅ REST API Controllers (5个)
- ✅ 统一响应格式 (Response/SingleResponse/MultiResponse/PageResponse)
- ✅ 统一POST请求方法
- ✅ Springdoc OpenAPI集成
- ✅ Request对象 (10个)

### 9.2 API端点清单 (38个)

#### 认证管理 (6个)

- POST `/api/v1/auth/login` - 用户登录
- POST `/api/v1/auth/register` - 用户注册
- POST `/api/v1/auth/refresh-token` - 刷新令牌
- POST `/api/v1/auth/send-email-code` - 发送邮箱验证码
- POST `/api/v1/auth/send-sms-code` - 发送短信验证码
- POST `/api/v1/auth/reset-password` - 重置密码

#### 用户管理 (8个)

- POST `/api/v1/users/create` - 创建用户
- POST `/api/v1/users/update` - 更新用户
- POST `/api/v1/users/delete` - 删除用户
- POST `/api/v1/users/get` - 获取用户详情
- POST `/api/v1/users/query` - 查询用户列表
- POST `/api/v1/users/change-password` - 修改密码
- POST `/api/v1/users/lock` - 锁定用户
- POST `/api/v1/users/unlock` - 解锁用户

#### 角色管理 (9个)

- POST `/api/v1/roles/create` - 创建角色
- POST `/api/v1/roles/update` - 更新角色
- POST `/api/v1/roles/delete` - 删除角色
- POST `/api/v1/roles/get` - 获取角色详情
- POST `/api/v1/roles/query` - 查询角色列表
- POST `/api/v1/roles/tree` - 获取角色树
- POST `/api/v1/roles/assign-user` - 分配角色给用户
- POST `/api/v1/roles/remove-user` - 从用户移除角色
- POST `/api/v1/roles/assign-permissions` - 分配权限给角色

#### 权限管理 (8个)

- POST `/api/v1/permissions/create` - 创建权限
- POST `/api/v1/permissions/update` - 更新权限
- POST `/api/v1/permissions/delete` - 删除权限
- POST `/api/v1/permissions/get` - 获取权限详情
- POST `/api/v1/permissions/tree` - 获取权限树
- POST `/api/v1/permissions/by-type` - 按类型获取权限
- POST `/api/v1/permissions/user-permissions` - 获取用户权限
- POST `/api/v1/permissions/user-menu-tree` - 获取用户菜单树

#### 部门管理 (7个)

- POST `/api/v1/departments/create` - 创建部门
- POST `/api/v1/departments/update` - 更新部门
- POST `/api/v1/departments/delete` - 删除部门
- POST `/api/v1/departments/get` - 获取部门详情
- POST `/api/v1/departments/tree` - 获取部门树
- POST `/api/v1/departments/sub-tree` - 获取部门子树
- POST `/api/v1/departments/move` - 移动部门

### 9.3 技术栈

- **框架**: Spring Boot 3.1.2
- **语言**: Java 17
- **ORM**: Spring Data JDBC
- **安全**: Spring Security + JWT
- **文档**: Springdoc OpenAPI 2.2.0
- **数据库**: MySQL 8.0+
- **缓存**: Redis (可选)
- **构建**: Maven
- **代码规范**: Google Java Format + Spotless

### 9.4 代码统计

- **总文件数**: ~120个Java文件
- **代码行数**: ~8,000行
- **Controllers**: 5个
- **Services**: 7个
- **Repositories**: 6个接口 + 实现
- **Entities**: 6个
- **DTOs**: 9个
- **Commands**: 10个
- **Queries**: 2个
- **Request Objects**: 10个

### 9.5 最近优化 (2026-01-04)

1. **统一API规范**
   - 所有接口统一使用POST方法
   - 移除所有路径变量 (@PathVariable)
   - 统一响应格式 (Response系列)

2. **文档升级**
   - 替换为Springdoc OpenAPI
   - 完整的API文档注解
   - Swagger UI支持

3. **代码质量**
   - 修复所有编译错误
   - 应用Google Java Format
   - 完整的参数验证

## 9. MyBatis-Flex 集成

### 9.1 概述

本模块已全面迁移到 **MyBatis-Flex 1.11.5**，提供类型安全的数据库访问。

**核心优势：**

- ✅ 编译时类型检查，避免字段名拼写错误
- ✅ IDE 自动完成，提高开发效率
- ✅ 重构友好，字段重命名自动更新查询
- ✅ 零字符串拼接，代码更清晰
- ✅ 高性能分页，自动优化查询

### 9.2 TableDef 类型安全查询

#### Tables 类

所有表定义集中在 `Tables` 类中：

```java
package io.github.loadup.modules.upms.infrastructure.dataobject;

public class Tables {
   public static final UserTableDef         USER          = new UserTableDef();
   public static final RoleTableDef         ROLE          = new RoleTableDef();
   public static final DepartmentTableDef   DEPARTMENT    = new DepartmentTableDef();
   public static final PermissionTableDef   PERMISSION    = new PermissionTableDef();
   public static final LoginLogTableDef     LOGIN_LOG     = new LoginLogTableDef();
   public static final OperationLogTableDef OPERATION_LOG = new OperationLogTableDef();

   public static class UserTableDef extends TableDef {
      public final QueryColumn ID          = new QueryColumn(this, "id");
      public final QueryColumn USERNAME    = new QueryColumn(this, "username");
      public final QueryColumn PASSWORD    = new QueryColumn(this, "password");
      public final QueryColumn EMAIL       = new QueryColumn(this, "email");
      public final QueryColumn STATUS      = new QueryColumn(this, "status");
      public final QueryColumn DEPT_ID     = new QueryColumn(this, "dept_id");
      public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
      // ... 其他字段
   }
}
```

#### 使用方式

```java


// 单条件查询
QueryWrapper query = QueryWrapper.create()
        .where(USER.USERNAME.eq("admin"));

        // 多条件查询
        QueryWrapper query = QueryWrapper.create()
                .where(USER.STATUS.eq((short) 1))
                .and(USER.DEPT_ID.in(deptIds))
                .orderBy(USER.CREATE_TIME.desc());

        // 复杂查询
        QueryWrapper query = QueryWrapper.create()
                .where(USER.USERNAME.like(keyword))
                .and(USER.STATUS.eq((short) 1))
                .and(USER.CREATE_TIME.between(startTime, endTime))
                .orderBy(USER.CREATE_TIME.desc())
                .limit(10);
```

### 9.3 Repository 实现模式

#### 标准模式

```java

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

   private final UserMapper    userMapper;
   private final UserConverter userConverter;

   @Override
   public Optional<User> findByUsername(String username) {
      QueryWrapper query = QueryWrapper.create()
              .where(USER.USERNAME.eq(username));

      UserDO userDO = userMapper.selectOneByQuery(query);
      return Optional.ofNullable(userDO)
              .map(userConverter::toEntity);
   }

   @Override
   public List<User> findByDeptId(String deptId) {
      QueryWrapper query = QueryWrapper.create()
              .where(USER.DEPT_ID.eq(deptId))
              .and(USER.STATUS.eq((short) 1));

      List<UserDO> userDOs = userMapper.selectListByQuery(query);
      return userDOs.stream()
              .map(userConverter::toEntity)
              .collect(Collectors.toList());
   }
}
```

#### 分页查询模式

```java

@Override
public org.springframework.data.domain.Page<User> findAll(Pageable pageable) {
   QueryWrapper query = QueryWrapper.create()
           .where(USER.STATUS.eq((short) 1))
           .orderBy(USER.CREATE_TIME.desc());

   // MyBatis-Flex 分页（自动计算总数）
   Page<UserDO> page = userMapper.paginate(
           Page.of(pageable.getPageNumber() + 1, pageable.getPageSize()),
           query
   );

   // 转换为领域对象
   List<User> users = page.getRecords().stream()
           .map(userConverter::toEntity)
           .collect(Collectors.toList());

   // 返回 Spring Data Page
   return new PageImpl<>(users, pageable, page.getTotalRow());
}
```

#### 动态条件查询

```java

@Override
public List<User> search(UserQuery queryDto) {
   QueryWrapper query = QueryWrapper.create()
           .where(USER.STATUS.eq((short) 1));

   // 动态添加条件
   if (StringUtils.hasText(queryDto.getUsername())) {
      query.and(USER.USERNAME.like(queryDto.getUsername()));
   }

   if (queryDto.getDeptId() != null) {
      query.and(USER.DEPT_ID.eq(queryDto.getDeptId()));
   }

   if (queryDto.getStartTime() != null && queryDto.getEndTime() != null) {
      query.and(USER.CREATE_TIME.between(
              queryDto.getStartTime(),
              queryDto.getEndTime()
      ));
   }

   List<UserDO> userDOs = userMapper.selectListByQuery(query);
   return userDOs.stream()
           .map(userConverter::toEntity)
           .collect(Collectors.toList());
}
```

### 9.4 常用查询方法对照表

| 方法          | SQL                 | MyBatis-Flex 用法                        |
|-------------|---------------------|----------------------------------------|
| 等于          | `= value`           | `USER.STATUS.eq(1)`                    |
| 不等于         | `!= value`          | `USER.STATUS.ne(0)`                    |
| 大于          | `> value`           | `USER.CREATE_TIME.gt(date)`            |
| 大于等于        | `>= value`          | `USER.CREATE_TIME.ge(date)`            |
| 小于          | `< value`           | `USER.CREATE_TIME.lt(date)`            |
| 小于等于        | `<= value`          | `USER.CREATE_TIME.le(date)`            |
| 模糊查询        | `LIKE '%value%'`    | `USER.USERNAME.like("admin")`          |
| 左模糊         | `LIKE '%value'`     | `USER.USERNAME.likeLeft("admin")`      |
| 右模糊         | `LIKE 'value%'`     | `USER.USERNAME.likeRight("admin")`     |
| IN          | `IN (...)`          | `USER.STATUS.in(1, 2, 3)`              |
| NOT IN      | `NOT IN (...)`      | `USER.STATUS.notIn(0, -1)`             |
| BETWEEN     | `BETWEEN v1 AND v2` | `USER.CREATE_TIME.between(start, end)` |
| IS NULL     | `IS NULL`           | `USER.DELETED.isNull()`                |
| IS NOT NULL | `IS NOT NULL`       | `USER.DELETED.isNotNull()`             |

### 9.5 Mapper 接口

所有 Mapper 继承 `BaseMapper<DO>`：

```java

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
   // 自动获得 CRUD 方法：
   // - insert(entity)
   // - update(entity)
   // - deleteById(id)
   // - selectOneById(id)
   // - selectListByQuery(query)
   // - selectOneByQuery(query)
   // - selectCountByQuery(query)
   // - paginate(page, query)
}
```

### 9.6 配置

#### pom.xml

```xml

<dependency>
   <groupId>com.mybatis-flex</groupId>
   <artifactId>mybatis-flex-spring-boot-starter</artifactId>
   <version>1.11.5</version>
</dependency>

        <!-- APT 处理器（编译时生成 TableDef） -->
<dependency>
<groupId>com.mybatis-flex</groupId>
<artifactId>mybatis-flex-processor</artifactId>
<version>1.11.5</version>
<scope>provided</scope>
</dependency>
```

#### application.yml

```yaml
mybatis-flex:
   configuration:
      # SQL 日志（开发环境）
      log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
      map-underscore-to-camel-case: true
      cache-enabled: true

   global-config:
      # 打印 SQL（开发环境）
      print-sql: true

# 日志配置
logging:
   level:
      com.mybatisflex: DEBUG
      io.github.loadup.modules.upms.infrastructure: DEBUG
```

### 9.7 最佳实践

#### ✅ 推荐

```java
// 使用静态导入

// 类型安全查询
QueryWrapper query = QueryWrapper.create()
        .where(USER.USERNAME.eq(username));

        // 链式调用清晰
        QueryWrapper query = QueryWrapper.create()
                .where(USER.STATUS.eq((short) 1))
                .and(USER.DEPT_ID.in(deptIds))
                .orderBy(USER.CREATE_TIME.desc());
```

#### ❌ 避免

```java
// 不要使用字符串
query.eq("username",username);  // 编译时无法检查

// 不要手动拼接 SQL
query.

where("username = ?",username);  // 失去类型安全
```

### 9.8 性能优化

#### 批量操作

```java
// 批量插入
List<UserDO> users = ...;
        userMapper.

insertBatch(users);

// 批量更新
userMapper.

updateBatch(users);
```

#### 只查询需要的字段

```java
QueryWrapper query = QueryWrapper.create()
        .select(USER.ID, USER.USERNAME, USER.EMAIL)
        .where(USER.STATUS.eq((short) 1));
```

#### 索引优化

确保查询条件字段有索引：

- `username` - UNIQUE 索引
- `email` - UNIQUE 索引
- `dept_id` - 普通索引
- `status` - 普通索引

### 9.9 测试

#### 单元测试

```java

@SpringBootTest
@Transactional
class UserRepositoryTest {

   @Autowired
   private UserRepository userRepository;

   @Test
   void testFindByUsername() {
      Optional<User> user = userRepository.findByUsername("admin");
      assertThat(user).isPresent();
      assertThat(user.get().getUsername()).isEqualTo("admin");
   }
}
```

#### H2 测试配置

```yaml
# src/test/resources/application-test.yml
spring:
   datasource:
      driver-class-name: org.h2.Driver
      url: jdbc:h2:mem:testdb;MODE=MySQL
      username: sa
      password:

   sql:
      init:
         mode: always
         schema-locations: classpath:schema.sql
         data-locations: classpath:test-data.sql
```

## 10. 未来演进方向

### 短期计划 (1-3个月)

- [ ] 集成loadup-components-gotone (邮件/短信)
- [ ] 集成Redis缓存
- [ ] 添加单元测试和集成测试
- [ ] 操作日志AOP实现
- [ ] 在线用户管理

### 中期计划 (3-6个月)

- [ ] 多因素认证 (MFA)
- [ ] LDAP/AD集成
- [ ] 细粒度字段级权限
- [ ] 权限缓存预热机制
- [ ] 性能优化和压测

### 长期计划 (6-12个月)

- [ ] 微服务拆分：将UPMS独立为认证中心
- [ ] GraphQL支持：提供更灵活的API查询
- [ ] 事件驱动：引入Domain Event解耦
- [ ] 多数据源：支持读写分离
- [ ] 分布式会话：支持集群部署
- [ ] OAuth 2.0授权服务器

## 11. 开发指南

### 11.1 代码规范

执行代码格式化：

```bash
mvn spotless:apply
```

### 11.2 编译构建

```bash
# 编译
mvn clean compile

# 打包
mvn clean package

# 跳过测试
mvn clean install -DskipTests
```

### 11.3 添加新功能

1. **添加实体**: 在`domain`层创建Entity
2. **定义Repository**: 在`domain`层定义接口
3. **实现Repository**: 在`infrastructure`层实现
4. **创建Service**: 在`app`层创建服务
5. **添加Controller**: 在`adapter`层暴露API
6. **编写测试**: 在`test`层添加测试用例

### 11.4 调试技巧

- 查看SQL日志: `logging.level.org.springframework.jdbc=DEBUG`
- 查看Security日志: `logging.level.org.springframework.security=DEBUG`
- 使用Swagger UI测试接口: `http://localhost:8080/swagger-ui.html`

---

**文档版本**: 2.0.0  
**最后更新**: 2026-01-04  
**维护者**: LoadUp Framework Team

# MyBatis-Flex 多租户与逻辑删除集成指南

## 概述

本组件集成了 MyBatis-Flex 的多租户（Multi-Tenant）和逻辑删除（Logical Delete）功能，提供了开箱即用的配置和实现。

## 功能特性

### 1. 多租户支持 (Multi-Tenant)

- **自动租户ID填充**: 在插入和更新时自动填充 `tenant_id` 字段
- **自动租户隔离**: 查询时自动添加 `tenant_id` 过滤条件
- **灵活的租户识别**: 支持从 HTTP Header、Query Parameter、Subdomain 提取租户ID
- **可配置的忽略表**: 支持配置不需要租户隔离的表（如系统表）

### 2. 逻辑删除支持 (Logical Delete)

- **软删除**: DELETE 操作自动转换为 UPDATE 设置删除标记
- **自动过滤**: 查询时自动过滤已删除的记录
- **可配置字段**: 支持自定义删除标记字段名和值

### 3. 实体监听器 (Entity Listener)

- **自动时间戳**: 自动填充 `created_at` 和 `updated_at` 字段
- **自动租户ID**: 根据租户上下文自动填充 `tenant_id`
- **自动删除标记**: 插入时自动初始化 `deleted` 字段为 false

## 配置说明

### application.yml 配置

```yaml
loadup:
  database:
    # 多租户配置
    multi-tenant:
      # 是否启用多租户（默认：false）
      enabled: true
      # 租户ID字段名（默认：tenant_id）
      column-name: tenant_id
      # 忽略租户过滤的表（逗号分隔）
      ignore-tables: sys_tenant,sys_user,sys_role,sys_permission,sys_config
      # 默认租户ID（当未设置租户上下文时使用）
      default-tenant-id: default

    # 逻辑删除配置
    logical-delete:
      # 是否启用逻辑删除（默认：false）
      enabled: true
      # 删除标记字段名（默认：deleted）
      column-name: deleted
      # 已删除记录的值（默认：true）
      deleted-value: "true"
      # 正常记录的值（默认：false）
      normal-value: "false"

    # ID生成器配置
    id-generator:
      enabled: true
      strategy: uuid-v7  # random, uuid-v4, uuid-v7, snowflake
      length: 20
```

## 实体类定义

### 继承 BaseDO

所有实体类应继承 `BaseDO` 基类以获得自动功能：

```java
import com.github.loadup.commons.dataobject.BaseDO;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("sys_user")
public class UserDO extends BaseDO {

    @Id
    private String id;

    @Column("username")
    private String username;

    @Column("email")
    private String email;

    @Column("status")
    private Integer status;

    // 以下字段由 BaseDO 提供，会被自动填充：
    // - tenantId: 租户ID（多租户启用时）
    // - deleted: 逻辑删除标记（逻辑删除启用时）
    // - createdAt: 创建时间
    // - updatedAt: 更新时间
}
```

### BaseDO 提供的字段

```java
public abstract class BaseDO implements Serializable {
    private LocalDateTime createdAt;    // 创建时间（插入时自动填充）
    private LocalDateTime updatedAt;    // 更新时间（插入/更新时自动填充）
    private String        tenantId;            // 租户ID（多租户启用时自动填充）
    private Boolean       deleted;            // 逻辑删除标记（逻辑删除启用时自动初始化为false）
}
```

## 数据库表设计

### 创建表时添加相应字段

```sql
CREATE TABLE sys_user
(
    id         VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    username   VARCHAR(50) NOT NULL COMMENT '用户名',
    email      VARCHAR(100) COMMENT '邮箱',
    status     INT      DEFAULT 1 COMMENT '状态',

    -- 多租户字段（启用多租户时需要）
    tenant_id  VARCHAR(32) NOT NULL COMMENT '租户ID',

    -- 逻辑删除字段（启用逻辑删除时需要）
    deleted    BOOLEAN  DEFAULT FALSE COMMENT '是否删除',

    -- 时间戳字段
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_tenant_id (tenant_id),
    INDEX idx_deleted (deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';
```

### 租户表设计（可选）

如果需要租户配置管理：

```sql
CREATE TABLE sys_tenant
(
    id                     VARCHAR(32) PRIMARY KEY COMMENT '租户ID',
    name                   VARCHAR(100) NOT NULL COMMENT '租户名称',
    code                   VARCHAR(50)  NOT NULL UNIQUE COMMENT '租户编码',
    status                 INT      DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    logical_delete_enabled BOOLEAN  DEFAULT FALSE COMMENT '是否启用逻辑删除',
    created_at             DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at             DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='租户表';
```

## 使用示例

### 1. 设置租户上下文

#### 方式一：通过 TenantFilter（Web 应用自动）

Web 应用中，租户ID会自动从请求中提取（优先级：Header > Query Parameter > Subdomain）：

```bash
# Header方式
curl -H "X-Tenant-Id: tenant_001" http://localhost:8080/api/users

# Query Parameter方式
curl http://localhost:8080/api/users?tenantId=tenant_001

# Subdomain方式
curl http://tenant_001.example.com/api/users
```

#### 方式二：手动设置（非 Web 环境）

```java
import com.github.loadup.components.database.tenant.TenantContextHolder;

// 设置租户上下文
TenantContextHolder.setTenantId("tenant_001");

try{
// 执行业务逻辑
List<UserDO> users = userMapper.selectAll();
// SQL: SELECT * FROM sys_user WHERE tenant_id = 'tenant_001' AND deleted = false
}finally{
        // 清理租户上下文
        TenantContextHolder.

clear();
}
```

#### 方式三：使用工具方法

```java
// 在指定租户上下文中执行代码
TenantContextHolder.runWithTenant("tenant_001",() ->{
List<UserDO> users = userMapper.selectAll();
// 自动添加租户过滤
});
// 执行完毕后自动恢复之前的租户上下文
```

### 2. CRUD 操作示例

#### 插入数据

```java

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public void createUser(UserDO user) {
        // tenantId、deleted、createdAt、updatedAt 会自动填充
        userMapper.insert(user);

        // 生成的SQL（假设当前租户ID为 tenant_001）：
        // INSERT INTO sys_user (id, username, email, tenant_id, deleted, created_at, updated_at)
        // VALUES (?, ?, ?, 'tenant_001', false, NOW(), NOW())
    }
}
```

#### 查询数据

```java
public List<UserDO> listUsers() {
    // 自动添加租户和逻辑删除过滤
    List<UserDO> users = userMapper.selectAll();

    // 生成的SQL：
    // SELECT * FROM sys_user 
    // WHERE tenant_id = 'tenant_001' AND deleted = false

    return users;
}
```

#### 更新数据

```java
public void updateUser(UserDO user) {
    // updatedAt 会自动更新
    userMapper.updateById(user);

    // 生成的SQL：
    // UPDATE sys_user SET username = ?, email = ?, updated_at = NOW()
    // WHERE id = ? AND tenant_id = 'tenant_001' AND deleted = false
}
```

#### 逻辑删除

```java
public void deleteUser(String userId) {
    // 实际上执行的是 UPDATE 操作，设置 deleted = true
    userMapper.deleteById(userId);

    // 生成的SQL：
    // UPDATE sys_user SET deleted = true, updated_at = NOW()
    // WHERE id = ? AND tenant_id = 'tenant_001'
}
```

### 3. 忽略租户过滤（特殊场景）

某些场景下需要查询所有租户的数据（如系统管理员）：

```java
import com.mybatisflex.core.tenant.TenantManager;

public List<UserDO> listAllTenantsUsers() {
    // 临时忽略租户过滤
    return TenantManager.withoutTenantCondition(() -> {
        return userMapper.selectAll();
        // 生成的SQL：SELECT * FROM sys_user WHERE deleted = false
        // 注意：仍然会应用逻辑删除过滤
    });
}
```

### 4. 忽略逻辑删除过滤（查询已删除数据）

```java
import com.mybatisflex.core.FlexGlobalConfig;

public List<UserDO> listAllUsersIncludingDeleted() {
    // 临时禁用逻辑删除过滤
    FlexGlobalConfig globalConfig = FlexGlobalConfig.getDefaultConfig();
    boolean originalLogicDeleteEnabled = globalConfig.isLogicDelete();

    try {
        globalConfig.setLogicDelete(false);
        return userMapper.selectAll();
        // 生成的SQL：SELECT * FROM sys_user WHERE tenant_id = 'tenant_001'
    } finally {
        globalConfig.setLogicDelete(originalLogicDeleteEnabled);
    }
}
```

## 配置忽略表

某些系统表不需要租户隔离，可以通过配置忽略：

```yaml
loadup:
  database:
    multi-tenant:
      ignore-tables: sys_tenant,sys_user,sys_role,sys_permission,sys_config,sys_dict
```

被忽略的表在查询时不会自动添加 `tenant_id` 过滤条件。

## 注意事项

### 1. 字段命名

- 确保数据库表中的字段名与配置中的字段名一致
- 默认字段名：`tenant_id`、`deleted`、`created_at`、`updated_at`

### 2. 索引优化

- 建议在 `tenant_id` 和 `deleted` 字段上创建索引
- 对于多租户应用，建议创建复合索引 `(tenant_id, deleted, ...)`

### 3. 性能考虑

- 租户过滤在 SQL 层面实现，性能影响较小
- 逻辑删除会增加表数据量，建议定期归档已删除数据

### 4. 数据迁移

如果是现有项目迁移，需要：

1. 添加相应字段到数据库表
2. 更新实体类继承 BaseDO
3. 初始化现有数据的 `tenant_id` 和 `deleted` 字段

```sql
-- 为现有数据设置默认租户ID
UPDATE sys_user
SET tenant_id = 'default'
WHERE tenant_id IS NULL;

-- 为现有数据初始化删除标记
UPDATE sys_user
SET deleted = FALSE
WHERE deleted IS NULL;
```

### 5. 事务处理

租户上下文存储在 ThreadLocal 中，在事务中会自动传递。但在异步操作中需要手动传递：

```java

@Async
public void asyncOperation() {
    String tenantId = TenantContextHolder.getTenantId();

    CompletableFuture.runAsync(() -> {
        TenantContextHolder.setTenantId(tenantId);
        try {
            // 执行异步业务逻辑
        } finally {
            TenantContextHolder.clear();
        }
    });
}
```

## 故障排查

### 问题1：租户过滤不生效

**症状**：查询返回了其他租户的数据

**解决方法**：

1. 检查 `loadup.database.multi-tenant.enabled` 是否为 `true`
2. 检查表名是否在 `ignore-tables` 列表中
3. 确认租户上下文已正确设置：`TenantContextHolder.getTenantId()`

### 问题2：逻辑删除不生效

**症状**：DELETE 操作真实删除了数据

**解决方法**：

1. 检查 `loadup.database.logical-delete.enabled` 是否为 `true`
2. 确认数据库表有 `deleted` 字段
3. 确认实体类继承了 `BaseDO`

### 问题3：字段未自动填充

**症状**：`created_at`、`updated_at` 等字段为空

**解决方法**：

1. 确认实体类继承了 `BaseDO`
2. 检查 `BaseEntityListener` 是否已注册（查看启动日志）
3. 确认使用的是 MyBatis-Flex 的 Mapper 方法

## 扩展开发

### 自定义租户识别策略

可以通过实现自定义 Filter 来扩展租户识别策略：

```java

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomTenantFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // 自定义租户识别逻辑，例如从JWT token中提取
        String token = httpRequest.getHeader("Authorization");
        String tenantId = extractTenantIdFromToken(token);

        TenantContextHolder.setTenantId(tenantId);

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }

    private String extractTenantIdFromToken(String token) {
        // 实现token解析逻辑
        return "tenant_001";
    }
}
```

## 相关链接

- [MyBatis-Flex 官方文档](https://mybatis-flex.com/)
- [MyBatis-Flex 多租户文档](https://mybatis-flex.com/zh/base/multi-tenancy.html)
- [MyBatis-Flex 逻辑删除文档](https://mybatis-flex.com/zh/base/logic-delete.html)


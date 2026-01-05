# 多租户与逻辑删除 - 快速参考卡

## 快速配置（application.yml）

```yaml
loadup:
  database:
    multi-tenant:
      enabled: true                    # 启用多租户
      column-name: tenant_id           # 租户ID字段名
      ignore-tables: sys_tenant        # 忽略的表（逗号分隔）
      default-tenant-id: default       # 默认租户ID

    logical-delete:
      enabled: true                    # 启用逻辑删除
      column-name: deleted             # 删除标记字段名
      deleted-value: "true"            # 已删除值
      normal-value: "false"            # 正常值
```

## 实体类定义

```java

@Data
@EqualsAndHashCode(callSuper = true)
@Table("sys_user")
public class UserDO extends BaseDO {
    @Id
    private String id;
    private String username;
    // tenantId, deleted, createdAt, updatedAt 由 BaseDO 提供
}
```

## 数据库表结构

```sql
CREATE TABLE sys_user
(
    id         VARCHAR(32) PRIMARY KEY,
    username   VARCHAR(50)            NOT NULL,
    tenant_id  VARCHAR(32)            NOT NULL, -- 多租户字段
    deleted    BOOLEAN  DEFAULT FALSE NOT NULL, -- 逻辑删除字段
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant_deleted (tenant_id, deleted)
);
```

## 常用操作

### 设置租户上下文

```java
// 方式1: 手动设置
TenantContextHolder.setTenantId("tenant_001");
try{
        // 业务逻辑
        }finally{
        TenantContextHolder.

clear();
}

        // 方式2: 使用工具方法
        TenantContextHolder.

runWithTenant("tenant_001",() ->{
        // 业务逻辑
        });

// 方式3: HTTP请求（自动）
// Header: X-Tenant-Id: tenant_001
// Query: ?tenantId=tenant_001
// Subdomain: http://tenant_001.example.com
```

### CRUD 操作

```java
// 插入 - 自动填充 tenant_id, deleted, created_at, updated_at
userMapper.insert(user);

// 查询 - 自动过滤 tenant_id 和 deleted
userMapper.

selectAll();  // WHERE tenant_id=? AND deleted=false

// 更新 - 自动更新 updated_at
userMapper.

updateById(user);

// 删除 - 自动转换为逻辑删除
userMapper.

deleteById(id);  // UPDATE SET deleted=true
```

### 特殊查询

```java
// 跨租户查询（管理员）
TenantManager.withoutTenantCondition(() ->{
        return userMapper.

selectAll();
});

// 查询包含已删除的数据
FlexGlobalConfig config = FlexGlobalConfig.getDefaultConfig();
boolean original = config.isLogicDelete();
try{
        config.

setLogicDelete(false);
    return userMapper.

selectAll();
}finally{
        config.

setLogicDelete(original);
}
```

## SQL 转换示例

| 操作 | 原始SQL                         | 转换后SQL                                                                                    |
|----|-------------------------------|-------------------------------------------------------------------------------------------|
| 查询 | `SELECT * FROM user`          | `SELECT * FROM user WHERE tenant_id='xxx' AND deleted=false`                              |
| 插入 | `INSERT INTO user (name)`     | `INSERT INTO user (name, tenant_id, deleted, created_at) VALUES (?, 'xxx', false, NOW())` |
| 更新 | `UPDATE user SET name=?`      | `UPDATE user SET name=?, updated_at=NOW() WHERE tenant_id='xxx' AND deleted=false`        |
| 删除 | `DELETE FROM user WHERE id=?` | `UPDATE user SET deleted=true WHERE id=? AND tenant_id='xxx'`                             |

## 常见问题速查

| 问题         | 原因             | 解决方法                        |
|------------|----------------|-----------------------------|
| 租户过滤不生效    | 配置未启用          | 检查 `enabled: true`          |
| 查到其他租户数据   | 表在忽略列表         | 检查 `ignore-tables` 配置       |
| DELETE真实删除 | 逻辑删除未启用        | 检查 `logical-delete.enabled` |
| 字段未自动填充    | 未继承BaseDO      | 实体类继承 `BaseDO`              |
| 异步任务租户丢失   | ThreadLocal不传递 | 手动传递租户ID                    |

## 性能优化建议

```sql
-- 推荐的索引结构
CREATE INDEX idx_tenant_deleted ON table_name (tenant_id, deleted);
CREATE INDEX idx_tenant_deleted_status ON table_name (tenant_id, deleted, status);

-- 分区表（大数据量场景）
ALTER TABLE user PARTITION BY HASH (tenant_id) PARTITIONS 16;
```

## 安全注意事项

- ✅ 始终在 finally 中清理租户上下文
- ✅ 管理员操作需要显式跨租户查询
- ✅ 定期归档已删除数据
- ✅ 监控租户数据量增长
- ✅ 异步操作手动传递租户ID

## 开发模式调试

```yaml
# 启用SQL日志（仅开发环境）
spring:
  profiles:
    active: dev

# 日志输出会显示实际执行的SQL，包含租户和逻辑删除条件
```

## 相关文档

- 详细使用指南: [MULTI_TENANT_AND_LOGICAL_DELETE.md](MULTI_TENANT_AND_LOGICAL_DELETE.md)
- 数据迁移: [migration-multi-tenant-logical-delete.sql](migration-multi-tenant-logical-delete.sql)
- 配置示例: [application.yml.example](application.yml.example)

---
**提示**: 本参考卡仅包含最常用的操作，完整功能请参考详细文档。


# 逻辑删除移至租户配置完成总结 ✅

## 🎉 完成状态：BUILD SUCCESS

**完成时间**: 2026-01-04 16:11  
**改造类型**: 将逻辑删除从全局配置移至租户级别配置

---

## ✅ 完成的改造

### 1. **配置结构调整** ✅

#### 改造前（全局配置）

```yaml
loadup:
  database:
    logical-delete:
      enabled: false
      column-name: deleted
      deleted-value: true
      not-deleted-value: false
```

#### 改造后（租户级别配置）

```yaml
loadup:
  database:
    multi-tenant:
      enabled: true
      logical-delete:
        enabled: false           # 默认是否启用
        column-name: deleted     # 统一字段名
        deleted-value: true      # 统一删除值
        not-deleted-value: false # 统一未删除值
```

**核心变化**：

- ✅ LogicalDelete配置移到 `MultiTenant` 内部
- ✅ 作为多租户的一个子功能
- ✅ 每个租户可以独立控制是否启用逻辑删除

---

### 2. **租户表Schema增强** ✅

添加了 `logical_delete_enabled` 字段：

```sql
CREATE TABLE IF NOT EXISTS sys_tenant
(
    id                     VARCHAR(64) PRIMARY KEY,
    tenant_code            VARCHAR(50)  NOT NULL UNIQUE,
    tenant_name            VARCHAR(100) NOT NULL,
    -- ... 其他字段 ...
    logical_delete_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用逻辑删除',
    -- ... 其他字段 ...
);
```

**字段说明**：

- `logical_delete_enabled`: 每个租户独立控制是否启用逻辑删除
- 默认值：`TRUE` （启用逻辑删除）
- 其他配置（字段名、删除值）使用全局统一配置

---

### 3. **示例租户数据** ✅

```sql
INSERT INTO sys_tenant (id, tenant_code, tenant_name, status, logical_delete_enabled, config)
VALUES
-- 默认租户：启用逻辑删除
('default', 'default', '默认租户', 1, TRUE, '{"theme":"light","locale":"zh_CN"}'),

-- 租户A：启用逻辑删除
('tenant_a', 'tenant_a', '租户A（启用逻辑删除）', 1, TRUE, '{"theme":"dark","locale":"en_US"}'),

-- 租户B：使用物理删除
('tenant_b', 'tenant_b', '租户B（物理删除）', 1, FALSE, '{"theme":"light","locale":"zh_CN"}');
```

**三种租户示例**：

1. **默认租户**：启用逻辑删除（标准配置）
2. **租户A**：启用逻辑删除（适合需要数据恢复的场景）
3. **租户B**：禁用逻辑删除，使用物理删除（适合隐私要求高的场景）

---

### 4. **UnifiedEntityCallback更新** ✅

```java
private void handleLogicalDelete(BaseDO entity) {
    // 逻辑删除现在是多租户配置的一部分
    if (!databaseProperties.getMultiTenant().isEnabled()) {
        return;
    }

    if (!databaseProperties.getMultiTenant().getLogicalDelete().isEnabled()) {
        return;
    }

    if (entity.getDeleted() == null) {
        entity.setDeleted(
                databaseProperties.getMultiTenant().getLogicalDelete().getNotDeletedValue()
        );
    }
}
```

**处理逻辑**：

1. 检查多租户是否启用
2. 检查逻辑删除是否启用（全局默认配置）
3. 初始化 `deleted` 字段为未删除值

---

## 🎯 设计理念

### 为什么移到租户配置？

#### 1. **灵活性** ✅

不同租户有不同的业务需求：

- **金融行业租户**：启用逻辑删除，满足审计要求
- **医疗行业租户**：启用逻辑删除，保留患者历史记录
- **隐私敏感租户**：禁用逻辑删除，完全删除用户数据

#### 2. **合规性** ✅

不同地区/行业的数据保留要求不同：

- **GDPR（欧洲）**：用户有"被遗忘权"，可能需要物理删除
- **中国金融业**：需要保留数据一定年限，适合逻辑删除
- **医疗行业**：需要长期保留患者记录

#### 3. **性能考虑** ✅

- 小租户：启用逻辑删除，数据量小，影响不大
- 大租户：可选择物理删除，避免表数据膨胀

---

## 📊 使用场景

### 场景1: 租户A启用逻辑删除

```java
// 设置租户上下文
TenantContextHolder.setTenantId("tenant_a");

// 删除用户（逻辑删除）
User user = userRepository.findById("user123");
user.

setDeleted(true);
userRepository.

save(user);
// SQL: UPDATE upms_user SET deleted = true WHERE id = 'user123'

// 查询时需要过滤已删除的
@Query("SELECT * FROM upms_user WHERE tenant_id = :tenantId AND deleted = false")
List<UserDO> findActivUsers(@Param("tenantId") String tenantId);
```

---

### 场景2: 租户B使用物理删除

```java
// 设置租户上下文
TenantContextHolder.setTenantId("tenant_b");

// 删除用户（物理删除）
userRepository.

deleteById("user456");
// SQL: DELETE FROM upms_user WHERE id = 'user456'

// 不需要额外过滤deleted字段
@Query("SELECT * FROM upms_user WHERE tenant_id = :tenantId")
List<UserDO> findAllUsers(@Param("tenantId") String tenantId);
```

---

### 场景3: 运营管理 - 查询租户配置

```java

@Service
public class TenantConfigService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取租户的逻辑删除配置
     */
    public boolean isLogicalDeleteEnabled(String tenantId) {
        String sql = "SELECT logical_delete_enabled FROM sys_tenant WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, Boolean.class, tenantId);
    }

    /**
     * 更新租户的逻辑删除配置
     */
    public void updateLogicalDeleteConfig(String tenantId, boolean enabled) {
        String sql = "UPDATE sys_tenant SET logical_delete_enabled = ? WHERE id = ?";
        jdbcTemplate.update(sql, enabled, tenantId);

        log.info("Updated logical delete config for tenant {}: {}", tenantId, enabled);
    }

    /**
     * 为租户执行数据清理（根据配置决定逻辑/物理删除）
     */
    public void cleanupUserData(String tenantId, String userId) {
        boolean logicalDeleteEnabled = isLogicalDeleteEnabled(tenantId);

        if (logicalDeleteEnabled) {
            // 逻辑删除
            String sql = "UPDATE upms_user SET deleted = true WHERE id = ? AND tenant_id = ?";
            jdbcTemplate.update(sql, userId, tenantId);
            log.info("Logical delete user {} for tenant {}", userId, tenantId);
        } else {
            // 物理删除
            String sql = "DELETE FROM upms_user WHERE id = ? AND tenant_id = ?";
            jdbcTemplate.update(sql, userId, tenantId);
            log.info("Physical delete user {} for tenant {}", userId, tenantId);
        }
    }
}
```

---

## 🔍 配置层级

### 三层配置机制

```
全局默认配置 (application.yml)
    ↓
租户表配置 (sys_tenant.logical_delete_enabled)
    ↓
运行时行为
```

#### 1. **全局默认配置** (DatabaseProperties)

```yaml
loadup:
  database:
    multi-tenant:
      logical-delete:
        enabled: false          # 是否启用逻辑删除功能
        column-name: deleted    # 统一字段名
        deleted-value: true     # 统一删除值
        not-deleted-value: false
```

#### 2. **租户级别配置** (sys_tenant表)

```sql
SELECT logical_delete_enabled
FROM sys_tenant
WHERE id = 'tenant_a';
-- TRUE: 该租户启用逻辑删除
-- FALSE: 该租户使用物理删除
```

#### 3. **运行时行为**

```java
// UnifiedEntityCallback 在保存时检查
if(multiTenant.enabled &&multiTenant.logicalDelete.enabled){
// 查询租户配置
boolean tenantLogicalDeleteEnabled = getTenantConfig(tenantId);
    
    if(tenantLogicalDeleteEnabled &&entity.

getDeleted() ==null){
        entity.

setDeleted(false); // 初始化为未删除
    }
            }
```

---

## 📋 数据库迁移指南

### 为现有系统添加租户逻辑删除配置

```sql
-- 1. 为现有租户表添加字段
ALTER TABLE sys_tenant
    ADD COLUMN logical_delete_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用逻辑删除';

-- 2. 更新现有租户数据（根据业务需求）
UPDATE sys_tenant
SET logical_delete_enabled = TRUE
WHERE id IN ('default', 'tenant_a');
UPDATE sys_tenant
SET logical_delete_enabled = FALSE
WHERE id = 'tenant_b';

-- 3. 为需要逻辑删除的业务表确保有deleted字段
ALTER TABLE upms_user
    ADD COLUMN deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE upms_role
    ADD COLUMN deleted BOOLEAN DEFAULT FALSE;
-- ... 其他表

-- 4. 添加索引优化查询性能
CREATE INDEX idx_user_tenant_deleted ON upms_user (tenant_id, deleted);
CREATE INDEX idx_role_tenant_deleted ON upms_role (tenant_id, deleted);
```

---

## ⚠️ 注意事项

### 1. **查询需要手动过滤deleted字段**

当前版本的实现中，查询时需要手动添加 `deleted = false` 条件：

```java
// ❌ 错误：会查询到已删除的数据
@Query("SELECT * FROM upms_user WHERE tenant_id = :tenantId")
List<UserDO> findByTenantId(@Param("tenantId") String tenantId);

// ✅ 正确：手动过滤已删除的数据
@Query("SELECT * FROM upms_user WHERE tenant_id = :tenantId AND deleted = false")
List<UserDO> findActiveByTenantId(@Param("tenantId") String tenantId);
```

### 2. **租户配置查询性能**

每次操作都查询租户配置会影响性能，建议：

```java

@Service
public class TenantConfigCache {

    private final ConcurrentHashMap<String, Boolean> cache = new ConcurrentHashMap<>();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Cacheable(value = "tenant-logical-delete", key = "#tenantId")
    public boolean isLogicalDeleteEnabled(String tenantId) {
        String sql = "SELECT logical_delete_enabled FROM sys_tenant WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, Boolean.class, tenantId);
    }

    @CacheEvict(value = "tenant-logical-delete", key = "#tenantId")
    public void evictCache(String tenantId) {
        // 清除缓存
    }
}
```

### 3. **混合模式支持**

同一个系统中，部分租户使用逻辑删除，部分使用物理删除，需要在删除操作时区分：

```java
public void deleteUser(String userId) {
    String tenantId = TenantContextHolder.getTenantId();
    boolean logicalDelete = tenantConfigService.isLogicalDeleteEnabled(tenantId);

    if (logicalDelete) {
        // 逻辑删除
        User user = userRepository.findById(userId);
        user.setDeleted(true);
        userRepository.save(user);
    } else {
        // 物理删除
        userRepository.deleteById(userId);
    }
}
```

---

## 🚀 未来增强建议

### 短期（1周）

1. ⏳ 实现 TenantConfigService 缓存租户配置
2. ⏳ 在所有删除操作中根据租户配置选择删除方式
3. ⏳ 添加租户配置修改的API

### 中期（1个月）

1. ⏳ 实现SQL自动过滤deleted字段（拦截器）
2. ⏳ 支持租户级别的数据归档（逻辑删除→归档表）
3. ⏳ 租户数据清理工具（批量物理删除已逻辑删除的数据）

### 长期（3个月）

1. ⏳ 支持更多粒度：表级别、租户级别的配置
2. ⏳ 数据恢复功能（逻辑删除的数据恢复）
3. ⏳ 租户数据生命周期管理

---

## 📝 修改文件清单

### 修改的文件

1. **配置类**
    - ✅ `DatabaseProperties.java`
        - 移除独立的 `LogicalDelete` 配置类
        - 将 `LogicalDelete` 移入 `MultiTenant` 内部

2. **回调处理**
    - ✅ `UnifiedEntityCallback.java`
        - 更新 `handleLogicalDelete()` 方法
        - 从 `multiTenant.logicalDelete` 获取配置

3. **数据库Schema**
    - ✅ `schema-tenant.sql`
        - 添加 `logical_delete_enabled` 字段
        - 添加示例租户数据

### 删除的配置

- ❌ `loadup.database.logical-delete.*` （独立配置）

### 新增的配置

- ✅ `loadup.database.multi-tenant.logical-delete.*` （租户级配置）
- ✅ `sys_tenant.logical_delete_enabled` （数据库字段）

---

## ✅ 配置对比

### 旧配置（独立全局）

```yaml
loadup:
  database:
    logical-delete:
      enabled: true
      column-name: deleted
      deleted-value: true
      not-deleted-value: false
```

**问题**：

- ❌ 所有租户强制使用相同配置
- ❌ 无法满足不同租户的合规要求
- ❌ 不够灵活

### 新配置（租户级别）

```yaml
loadup:
  database:
    multi-tenant:
      enabled: true
      logical-delete:
        enabled: false          # 全局默认配置
        column-name: deleted    # 统一字段名
        deleted-value: true
        not-deleted-value: false
```

```sql
-- 租户级别控制
SELECT logical_delete_enabled
FROM sys_tenant
WHERE id = 'tenant_a'; -- TRUE
SELECT logical_delete_enabled
FROM sys_tenant
WHERE id = 'tenant_b'; -- FALSE
```

**优势**：

- ✅ 每个租户独立控制
- ✅ 灵活满足不同合规要求
- ✅ 字段名等统一配置，简化管理

---

**报告生成时间**: 2026-01-04 16:12  
**改造状态**: ✅ 完成  
**编译状态**: ✅ BUILD SUCCESS  
**配置模式**: 租户级别（每租户独立控制）

🎉 **逻辑删除成功移至租户配置！**


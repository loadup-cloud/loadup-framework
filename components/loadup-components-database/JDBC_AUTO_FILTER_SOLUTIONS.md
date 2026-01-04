# JDBC Repository 自动过滤方案对比 - 仅探讨

## 问题定义

**核心问题**：Spring Data JDBC 和 MyBatis 混合使用时，如何让 **Spring Data JDBC** 部分也能自动添加 `tenant_id` 和 `deleted` 过滤条件？

---

## 当前状况分析

### 现状

```java
// Spring Data JDBC - 方法名查询
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
    // Spring Data 生成：SELECT * FROM upms_user WHERE username = ?
    // ❌ 没有 tenant_id 和 deleted 过滤
}

// MyBatis - @Select 查询
@Mapper
public interface UserMapper {
    @Select("SELECT * FROM upms_user WHERE username = #{username}")
    Optional<User> findByUsername(String username);
    // MyBatis 拦截器修改：
    // SELECT * FROM upms_user WHERE username = ? 
    // AND tenant_id = 'xxx' AND deleted = false
    // ✅ 有过滤
}
```

### 问题

如果采用混合使用策略：

- ✅ MyBatis 查询：有拦截器，自动添加过滤
- ❌ Spring Data JDBC 查询：**无拦截器，需要手动过滤**
- ⚠️ 这会导致数据泄露风险

---

## 方案对比

我提供 **5 个方案**，从简单到复杂排序：

---

## 方案1：手动添加过滤（不推荐 ⭐）

### 描述

在每个 Spring Data JDBC 查询方法中手动添加 `tenantId` 和 `deleted` 参数。

### 实现

```java
public interface UserRepository extends CrudRepository<User, Long> {

    // 手动添加参数
    @Query("""
            SELECT * FROM upms_user 
            WHERE username = :username 
            AND tenant_id = :tenantId 
            AND deleted = false
            """)
    Optional<User> findByUsername(
            @Param("username") String username,
            @Param("tenantId") String tenantId
    );
}

// 使用时
String tenantId = TenantContextHolder.getTenantId();
userRepository.

findByUsername("admin",tenantId);
```

### 优劣分析

| 维度   | 评价        | 说明            |
|------|-----------|---------------|
| 实现难度 | ⭐⭐⭐⭐⭐ 极简单 | 无需任何基础设施      |
| 代码量  | ⭐ 最多      | 每个方法都要写过滤条件   |
| 容错性  | ⭐ 最差      | 容易遗漏，导致数据泄露   |
| 维护性  | ⭐ 最差      | 修改过滤逻辑需要改所有方法 |
| 性能   | ⭐⭐⭐⭐⭐ 最优  | 零开销           |

### 结论

❌ **不推荐**：代码冗余，易出错，不可维护

---

## 方案2：使用 @Query + 占位符宏（实验性 ⭐⭐）

### 描述

定义宏占位符，在编译时或运行时替换为过滤条件。

### 实现（概念）

```java
// 定义宏
public class QueryMacro {
    public static final String TENANT_FILTER =
            "AND tenant_id = :#{T(TenantContextHolder).getTenantId()} AND deleted = false";
}

// 使用
public interface UserRepository extends CrudRepository<User, Long> {

    @Query("SELECT * FROM upms_user WHERE username = :username " + TENANT_FILTER)
    Optional<User> findByUsername(@Param("username") String username);
}
```

### 优劣分析

| 维度   | 评价      | 说明          |
|------|---------|-------------|
| 实现难度 | ⭐⭐⭐ 中等  | 需要 SpEL 支持  |
| 代码量  | ⭐⭐⭐ 中等  | 每个方法添加宏     |
| 容错性  | ⭐⭐ 较差   | 仍可能忘记添加宏    |
| 维护性  | ⭐⭐⭐ 中等  | 宏统一管理       |
| 性能   | ⭐⭐⭐⭐ 良好 | SpEL 解析有小开销 |

### 问题

- ⚠️ Spring Data JDBC 对 SpEL 支持有限
- ⚠️ 仍需要手动添加宏

### 结论

⚠️ **实验性方案**：可行但不完美

---

## 方案3：DataSource Proxy 拦截（推荐 ⭐⭐⭐⭐）

### 描述

使用 `datasource-proxy` 库在 JDBC 层面拦截所有 SQL，适用于 Spring Data JDBC。

### 架构

```
Spring Data JDBC 生成 SQL
    ↓
JdbcTemplate 执行
    ↓
DataSource Proxy 拦截 ← 在这里修改 SQL
    ↓
添加：AND tenant_id = ? AND deleted = false
    ↓
真实数据库执行
```

### 依赖

```xml

<dependency>
    <groupId>net.ttddyy</groupId>
    <artifactId>datasource-proxy</artifactId>
    <version>1.9</version>
</dependency>
```

### 实现（简化版）

```java

@Configuration
public class DataSourceProxyConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSource actualDataSource) {
        return ProxyDataSourceBuilder
                .create(actualDataSource)
                .listener(new TenantSqlListener())
                .build();
    }
}

class TenantSqlListener implements QueryExecutionListener {

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        for (QueryInfo query : queryInfoList) {
            String sql = query.getQuery();
            String modified = addTenantFilter(sql);
            // 注意：beforeQuery 无法直接修改 SQL
            // 需要使用 Connection wrapper
        }
    }
}
```

### 优劣分析

| 维度   | 评价       | 说明                                            |
|------|----------|-----------------------------------------------|
| 实现难度 | ⭐⭐⭐ 中等   | 需要理解 JDBC 层                                   |
| 代码量  | ⭐⭐⭐⭐ 少   | 一次实现，全局生效                                     |
| 容错性  | ⭐⭐⭐⭐⭐ 最优 | 自动拦截，无遗漏                                      |
| 维护性  | ⭐⭐⭐⭐ 良好  | 集中管理                                          |
| 性能   | ⭐⭐⭐⭐ 良好  | 字符串处理开销小                                      |
| 覆盖范围 | ⭐⭐⭐⭐⭐ 全部 | Spring Data JDBC + JdbcTemplate + MyBatis 全支持 |

### 适用场景

✅ **同时拦截 Spring Data JDBC 和 MyBatis**  
✅ **统一的 SQL 过滤机制**  
✅ **对开发者透明**

### 注意事项

⚠️ **技术难点**：

1. `datasource-proxy` 在 `beforeQuery` 中**无法修改 SQL**
2. 需要在 `Connection` 级别包装 `PreparedStatement`
3. 实现较复杂

### 结论

✅ **推荐**：一次实现，全局生效，覆盖所有场景

---

## 方案4：自定义 Spring Data JDBC Dialect（复杂 ⭐⭐⭐）

### 描述

扩展 Spring Data JDBC 的 SQL 生成器，在生成 SQL 时就添加过滤条件。

### 实现（概念）

```java

@Configuration
public class TenantJdbcConfiguration extends AbstractJdbcConfiguration {

    @Bean
    @Override
    public JdbcConverter jdbcConverter(
            JdbcMappingContext mappingContext,
            NamedParameterJdbcOperations operations,
            RelationResolver relationResolver,
            JdbcCustomConversions conversions) {

        return new TenantAwareJdbcConverter(
                mappingContext, relationResolver, conversions);
    }
}

class TenantAwareJdbcConverter extends MappingJdbcConverter {

    @Override
    public <T> T read(Class<T> type, ResultSet rs) {
        // 在查询时自动添加过滤
        // 修改 SQL 生成逻辑
    }
}
```

### 优劣分析

| 维度   | 评价       | 说明                         |
|------|----------|----------------------------|
| 实现难度 | ⭐ 极复杂    | 需要深入理解 Spring Data JDBC 内部 |
| 代码量  | ⭐⭐ 较多    | 需要实现多个接口                   |
| 容错性  | ⭐⭐⭐⭐⭐ 最优 | 生成时添加，无遗漏                  |
| 维护性  | ⭐⭐ 较差    | Spring 升级可能破坏              |
| 性能   | ⭐⭐⭐⭐⭐ 最优 | 生成时处理，运行时无开销               |

### 问题

- ❌ 实现极其复杂
- ❌ 与 Spring Data JDBC 内部实现强耦合
- ❌ 版本升级风险高

### 结论

❌ **不推荐**：实现成本过高，维护困难

---

## 方案5：完全迁移到 MyBatis（激进 ⭐⭐⭐⭐⭐）

### 描述

放弃 Spring Data JDBC 方法名查询，全部使用 MyBatis。

### 实现

```java
// 替换前
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
}

// 替换后
@Mapper
public interface UserMapper {
    @Select("SELECT * FROM upms_user WHERE username = #{username}")
    Optional<User> findByUsername(String username);
    // 拦截器自动添加过滤
}
```

### 优劣分析

| 维度   | 评价       | 说明                |
|------|----------|-------------------|
| 实现难度 | ⭐⭐⭐⭐ 简单  | 只需要迁移代码           |
| 代码量  | ⭐⭐⭐ 中等   | 需要写 SQL，但简洁       |
| 容错性  | ⭐⭐⭐⭐⭐ 最优 | 拦截器全局生效           |
| 维护性  | ⭐⭐⭐⭐⭐ 最优 | 统一技术栈             |
| 性能   | ⭐⭐⭐⭐ 良好  | 拦截器开销小            |
| 迁移成本 | ⭐⭐ 较高    | 需要改写所有 Repository |

### 优势

✅ **统一技术栈**：只用 MyBatis，简化维护  
✅ **功能强大**：动态 SQL、复杂查询  
✅ **自动过滤**：拦截器全局生效

### 劣势

❌ **迁移成本**：需要重写所有简单查询  
❌ **学习曲线**：团队需要熟悉 MyBatis

### 结论

✅ **长期最优方案**：统一技术栈，彻底解决问题

---

## 方案对比总结表

| 方案                  | 实现难度  | 代码量  | 容错性   | 维护性   | 性能    | 迁移成本  | 推荐度  |
|---------------------|-------|------|-------|-------|-------|-------|------|
| 1. 手动过滤             | ⭐⭐⭐⭐⭐ | ⭐    | ⭐     | ⭐     | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ❌    |
| 2. 宏占位符             | ⭐⭐⭐   | ⭐⭐⭐  | ⭐⭐    | ⭐⭐⭐   | ⭐⭐⭐⭐  | ⭐⭐⭐⭐  | ⚠️   |
| 3. DataSource Proxy | ⭐⭐⭐   | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐  | ⭐⭐⭐⭐  | ⭐⭐⭐⭐  | ✅ 推荐 |
| 4. 自定义 Dialect      | ⭐     | ⭐⭐   | ⭐⭐⭐⭐⭐ | ⭐⭐    | ⭐⭐⭐⭐⭐ | ⭐⭐    | ❌    |
| 5. 完全 MyBatis       | ⭐⭐⭐⭐  | ⭐⭐⭐  | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐  | ⭐⭐    | ✅ 长期 |

---

## 推荐策略

### 短期方案（1-2周实施）：DataSource Proxy ⭐⭐⭐⭐

```
优势：
✅ 同时支持 Spring Data JDBC 和 MyBatis
✅ 一次实现，全局生效
✅ 对开发者透明
✅ 不需要修改现有代码

劣势：
⚠️ 实现有一定技术难度
⚠️ 需要处理 Connection wrapper
```

**适合**：

- 已有大量 Spring Data JDBC 代码
- 需要快速实现自动过滤
- 追求技术统一性

---

### 长期方案（1-2月实施）：完全 MyBatis ⭐⭐⭐⭐⭐

```
优势：
✅ 统一技术栈
✅ 功能强大（动态SQL、复杂查询）
✅ 自动过滤完美支持
✅ 社区成熟，文档丰富

劣势：
⚠️ 需要迁移现有代码
⚠️ 简单查询也要写 SQL
```

**适合**：

- 新项目或重构项目
- 追求长期技术一致性
- 有复杂查询需求

---

### 混合方案（推荐 ⭐⭐⭐⭐⭐）

**阶段1（立即）：DataSource Proxy**

- 实现拦截器，覆盖现有代码
- 保持 Spring Data JDBC 不动

**阶段2（渐进）：新功能用 MyBatis**

- 新功能优先使用 MyBatis
- 简单 CRUD 保持 Spring Data JDBC

**阶段3（长期）：逐步迁移复杂查询**

- 遇到复杂查询时迁移到 MyBatis
- 最终形成 80% MyBatis + 20% JDBC 的格局

---

## 技术细节：DataSource Proxy 实现难点

### 难点1：无法在 beforeQuery 中修改 SQL

```java
// ❌ 这样不行
@Override
public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
    query.setQuery(modifiedSql); // 没有这个方法
}
```

**解决方案**：

- 方案A：包装 `Connection`，在 `prepareStatement` 时修改
- 方案B：使用字节码增强（CGLIB/ByteBuddy）

### 难点2：参数绑定

```java
// 原始 SQL: SELECT * FROM user WHERE name = ?
// 修改后: SELECT * FROM user WHERE name = ? AND tenant_id = ? AND deleted = ?

// 问题：参数索引变了！
// 原来：? (index 1) = "admin"
// 现在：? (index 1) = "admin", ? (index 2) = "tenant_a", ? (index 3) = false
```

**解决方案**：

- 使用字符串替换而非参数化：`AND tenant_id = 'tenant_a'`
- 或者重写 `PreparedStatement` 的参数绑定逻辑

### 难点3：性能优化

```java
// 每次查询都要解析 SQL
String tableName = extractTableName(sql);  // 正则匹配，耗时

// 优化：缓存表名提取结果
ConcurrentHashMap<String, String> sqlTableCache = new ConcurrentHashMap<>();
```

---

## 决策建议

### 如果你的项目...

#### 场景1：大量简单CRUD，少量复杂查询

**推荐**：**DataSource Proxy（短期）+ 保持现状**

- Spring Data JDBC 方法名仍然好用
- Proxy 拦截器保障安全性

#### 场景2：大量复杂查询、JOIN、动态SQL

**推荐**：**完全迁移到 MyBatis**

- MyBatis 更适合复杂场景
- 统一技术栈，降低维护成本

#### 场景3：新项目

**推荐**：**直接使用 MyBatis**

- 从一开始就统一
- 避免后期迁移成本

#### 场景4：技术栈极简化

**推荐**：**DataSource Proxy**

- 不引入新技术（MyBatis）
- 用现有的 JDBC 拦截机制

---

## 实施建议

### 第1周：调研和 POC

```
任务：
- [ ] 搭建 DataSource Proxy POC
- [ ] 测试 SQL 修改能力
- [ ] 性能基准测试
- [ ] 评估技术难度
```

### 第2周：选择方案

```
决策点：
1. DataSource Proxy 技术可行？
   - Yes → 实施 Proxy 方案
   - No → 考虑完全 MyBatis

2. 团队是否熟悉 MyBatis？
   - Yes → 优先 MyBatis
   - No → DataSource Proxy

3. 是否有大量复杂查询？
   - Yes → MyBatis
   - No → 保持 JDBC + Proxy
```

### 第3-4周：实施

根据选择的方案实施。

---

## 风险分析

### DataSource Proxy 风险

| 风险       | 概率 | 影响 | 缓解          |
|----------|----|----|-------------|
| SQL 解析错误 | 中  | 高  | 完善测试用例      |
| 参数绑定问题   | 中  | 高  | 使用字符串替换     |
| 性能下降     | 低  | 中  | 缓存+优化       |
| 升级兼容性    | 低  | 中  | 隔离 Proxy 逻辑 |

### MyBatis 迁移风险

| 风险     | 概率 | 影响 | 缓解    |
|--------|----|----|-------|
| 迁移工作量大 | 高  | 中  | 渐进式迁移 |
| 学习成本   | 中  | 低  | 培训+文档 |
| 新旧混用混乱 | 中  | 中  | 明确规范  |

---

## 最终建议

### 我的推荐（综合考虑）

**采用混合策略** ⭐⭐⭐⭐⭐

```
阶段划分：
├─ 第1个月：实施 DataSource Proxy
│  └─ 保障现有代码安全
│
├─ 第2-3个月：新功能使用 MyBatis
│  └─ 渐进式引入
│
└─ 长期：形成 MyBatis 为主、JDBC 为辅的格局
   ├─ 80% MyBatis（复杂查询）
   └─ 20% Spring Data JDBC（超简单的方法名查询）
```

### 理由

1. ✅ **短期有保障**：Proxy 立即生效
2. ✅ **长期更优雅**：MyBatis 技术栈更统一
3. ✅ **风险可控**：渐进式，不激进
4. ✅ **投入合理**：分阶段投入，ROI 高

---

## 结论

**问题**：Spring Data JDBC 能否自动补充租户 ID 和逻辑删除？

**答案**：

- ❌ 原生不支持
- ✅ 通过 DataSource Proxy 可以实现
- ✅ 迁移到 MyBatis 是长期最优解

**推荐路径**：

```
立即 → DataSource Proxy（保障安全）
短期 → 新功能用 MyBatis
长期 → 统一到 MyBatis
```

---

**文档生成时间**：2026-01-04 17:00  
**状态**：方案探讨完成，未实施  
**下一步**：根据团队决策选择方案

📋 **这是一个纯探讨文档，不涉及代码实施**


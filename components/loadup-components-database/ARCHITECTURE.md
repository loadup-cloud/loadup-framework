# LoadUp Database Component - Architecture

## 1. Overview

LoadUp Database Component 是基于 **MyBatis-Flex** 的企业级数据库访问组件，提供：

- **类型安全查询** - 编译时检查的 QueryWrapper
- **自动审计** - ID生成、时间戳、操作人自动管理
- **高性能序列号** - 批量预分配的分布式序列号服务
- **多种ID策略** - Random、UUID v4/v7、Snowflake
- **多租户支持** - 自动租户隔离和数据过滤 ⭐
- **逻辑删除** - 软删除支持，数据安全可恢复 ⭐

## 2. Architecture

### 2.1 Core Components

```
┌─────────────────────────────────────────────────────────┐
│            Application Layer                            │
│  (Service, Repository using MyBatis-Flex Mapper)       │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│         LoadUp Database Component                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │ MyBatis-Flex │  │   Audit      │  │  Sequence    │ │
│  │  QueryWrapper│  │  Callback    │  │   Service    │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
│  ┌──────────────┐  ┌──────────────┐                   │
│  │ ID Generator │  │    BaseDO    │                   │
│  └──────────────┘  └──────────────┘                   │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Database (MySQL/PostgreSQL)                │
└─────────────────────────────────────────────────────────┘
```

### 2.2 Component Details

#### MyBatis-Flex Integration

- **BaseMapper<T>**: 提供基础CRUD方法
- **QueryWrapper**: 类型安全的查询构建器
- **TableDef**: 编译时生成的表定义类
- **Page<T>**: 高性能分页支持

#### Audit System

- **BeforeSaveCallback**: 保存前自动处理
- **IdGeneratorCallback**: ID自动生成
- **TimestampCallback**: 时间戳自动填充
- **OperatorCallback**: 操作人自动记录

#### Sequence Service

- **Database-backed**: 基于数据库的序列号
- **Batch Allocation**: 批量预分配（默认1000个）
- **Thread-safe**: 线程安全的内存分配
- **High Performance**: ~100,000 TPS

## 3. MyBatis-Flex Integration

### 3.1 Entity Definition

```java

@Table("t_user")
public class User extends BaseDO {
    @Id(keyType = KeyType.None)  // ID由审计功能生成
    private String id;

    private String  username;
    private String  email;
    private Integer status;

    // 继承自BaseDO:
    // - tenantId (多租户)
    // - deleted (逻辑删除)
    // - createdBy
    // - createdTime
    // - updatedBy
    // - updatedTime
}
```

### 3.2 Mapper Interface

```java

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 自动获得方法：
    // - insert(entity)
    // - insertBatch(entities)
    // - update(entity)
    // - updateBatch(entities)
    // - deleteById(id)
    // - deleteByQuery(query)
    // - selectOneById(id)
    // - selectOneByQuery(query)
    // - selectListByQuery(query)
    // - selectCountByQuery(query)
    // - paginate(page, query)
}
```

### 3.3 Type-Safe Queries

#### Static Import

```java
import static com.github.loadup.tables.Tables.USER;
import static com.github.loadup.tables.Tables.ROLE;
```

#### Basic Queries

```java
// Simple condition
QueryWrapper query = QueryWrapper.create()
                .where(USER.USERNAME.eq("admin"));

// Multiple conditions
QueryWrapper query = QueryWrapper.create()
        .where(USER.STATUS.eq(1))
        .and(USER.EMAIL.like("%@example.com"))
        .orderBy(USER.CREATE_TIME.desc());

// Complex conditions
QueryWrapper query = QueryWrapper.create()
        .where(USER.STATUS.in(1, 2))
        .and(
                USER.USERNAME.like("admin")
                        .or(USER.EMAIL.like("admin"))
        )
        .and(USER.CREATE_TIME.between(startTime, endTime));
```

#### Pagination

```java
// Auto total count calculation
Page<User> page = userMapper.paginate(
                Page.of(pageNum, pageSize),
                query
        );

long total = page.getTotalRow();
List<User> records = page.getRecords();
```

#### Dynamic Conditions

```java
QueryWrapper query = QueryWrapper.create();

if(StringUtils.

hasText(username)){
        query.

where(USER.USERNAME.like(username));
        }

        if(status !=null){
        query.

and(USER.STATUS.eq(status));
        }

        if(startTime !=null&&endTime !=null){
        query.

and(USER.CREATE_TIME.between(startTime, endTime));
        }
```

### 3.4 Query Methods Reference

| Method             | SQL                 | Example                            |
|--------------------|---------------------|------------------------------------|
| `eq(value)`        | `= value`           | `USER.STATUS.eq(1)`                |
| `ne(value)`        | `!= value`          | `USER.STATUS.ne(0)`                |
| `gt(value)`        | `> value`           | `USER.AGE.gt(18)`                  |
| `ge(value)`        | `>= value`          | `USER.AGE.ge(18)`                  |
| `lt(value)`        | `< value`           | `USER.AGE.lt(60)`                  |
| `le(value)`        | `<= value`          | `USER.AGE.le(60)`                  |
| `like(value)`      | `LIKE '%value%'`    | `USER.USERNAME.like("admin")`      |
| `likeLeft(value)`  | `LIKE '%value'`     | `USER.USERNAME.likeLeft("admin")`  |
| `likeRight(value)` | `LIKE 'value%'`     | `USER.USERNAME.likeRight("admin")` |
| `in(values)`       | `IN (...)`          | `USER.STATUS.in(1, 2, 3)`          |
| `notIn(values)`    | `NOT IN (...)`      | `USER.STATUS.notIn(0, -1)`         |
| `between(v1, v2)`  | `BETWEEN v1 AND v2` | `USER.AGE.between(18, 60)`         |
| `isNull()`         | `IS NULL`           | `USER.DELETED_TIME.isNull()`       |
| `isNotNull()`      | `IS NOT NULL`       | `USER.EMAIL.isNotNull()`           |

## 4. Audit System

### 4.1 ID Generation

#### Strategies

1. **Random String** (Default)
    - Length: Configurable (default 20)
    - Performance: ~1,000,000 TPS
    - Use case: General purpose

2. **UUID v4**
    - Format: 32 chars (without hyphens)
    - Performance: ~500,000 TPS
    - Use case: Standard UUID requirement

3. **UUID v7** ⭐ Recommended
    - Format: 32 chars with timestamp prefix
    - Performance: ~500,000 TPS
    - Use case: Need time-ordered IDs
    - Benefit: Better for B-tree indexes

4. **Snowflake** ⭐ Recommended
    - Format: 19-digit number
    - Performance: ~1,000,000 TPS
    - Use case: Distributed systems
    - Benefit: Numeric, sortable, globally unique

#### Configuration

```yaml
loadup:
  database:
    id-generator:
      strategy: uuid-v7  # random | uuid-v4 | uuid-v7 | snowflake
      length: 20         # For random strategy
      uuid-with-hyphens: false
      snowflake-worker-id: 0      # 0-31
      snowflake-datacenter-id: 0  # 0-31
```

### 4.2 Timestamp Management

Automatically filled fields:

- `createdTime`: Set on insert
- `updatedTime`: Set on insert and update

### 4.3 Operator Tracking

Automatically filled fields:

- `createdBy`: User ID on insert
- `updatedBy`: User ID on update

Implementation uses `SecurityContextHolder` to get current user.

### 4.4 Multi-Tenancy

Optional `tenantId` field for multi-tenant applications:

```java
public class BaseDO {
    private String tenantId;  // Automatically filled from context
    // ...
}
```

### 4.5 Logical Delete

Optional `deleted` field for soft delete:

```java
public class BaseDO {
    private Boolean deleted;  // false by default
    // ...
}
```

## 5. Sequence Service

### 5.1 Design

```
┌─────────────┐
│ Application │
└──────┬──────┘
       │ getNextSequence("order_no")
       ▼
┌─────────────────────┐
│ SequenceService     │
│ (Memory Cache)      │
│ [1000...1999]       │ ← Current range in memory
└──────┬──────────────┘
       │ Range exhausted, fetch next
       ▼
┌─────────────────────┐
│ Database            │
│ UPDATE sys_sequence │
│ SET value = 2000    │
│ WHERE name = ...    │
└─────────────────────┘
```

### 5.2 Performance

- **Memory Allocation**: O(1) - Uses AtomicLong
- **Database Access**: Once per 1000 (configurable) IDs
- **Throughput**: ~100,000 TPS
- **Concurrency**: Thread-safe with ReentrantLock

### 5.3 Usage Patterns

#### Order Number

```java
public String generateOrderNumber() {
    Long seq = sequenceService.getNextSequence("order_no");
    String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    return String.format("ORD%s%06d", date, seq % 1000000);
}
```

#### User Number

```java
public String generateUserNumber() {
    Long seq = sequenceService.getNextSequence("user_no");
    return String.format("U%08d", seq);
}
```

#### Invoice Number

```java
public String generateInvoiceNumber() {
    Long seq = sequenceService.getNextSequence("invoice_no");
    String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    return String.format("INV%s%08d", yearMonth, seq % 100000000);
}
```

### 5.4 Configuration

```yaml
loadup:
  database:
    sequence:
      step: 1000              # Allocation batch size
      min-value: 0            # Minimum value
      max-value: 9223372036854775807  # Maximum value (Long.MAX_VALUE)
```

### 5.5 Database Schema

```sql
CREATE TABLE sys_sequence
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL UNIQUE,
    value      BIGINT       NOT NULL DEFAULT 0,
    min_value  BIGINT       NOT NULL DEFAULT 0,
    max_value  BIGINT       NOT NULL DEFAULT 9223372036854775807,
    step       BIGINT       NOT NULL DEFAULT 1000,
    created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_sequence_name ON sys_sequence (name);
```

## 6. Configuration

### 6.1 Complete Configuration

```yaml
# MyBatis-Flex
mybatis-flex:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
    cache-enabled: true

  global-config:
    print-sql: true  # Development only

# LoadUp Database
loadup:
  database:
    # ID Generator
    id-generator:
      enabled: true
      strategy: uuid-v7
      length: 20
      uuid-with-hyphens: false
      snowflake-worker-id: ${WORKER_ID:0}
      snowflake-datacenter-id: ${DATACENTER_ID:0}

    # Sequence
    sequence:
      step: 1000
      min-value: 0
      max-value: 9223372036854775807

# DataSource
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: user
    password: pass
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      minimum-idle: 10
      maximum-pool-size: 50
      connection-timeout: 30000

# Logging
logging:
  level:
    com.mybatisflex: DEBUG
    io.github.loadup.components.database: DEBUG
```

## 7. Best Practices

### 7.1 Entity Design

```java
// ✅ Good: Extend BaseDO
@Table("t_user")
public class User extends BaseDO {
    @Id(keyType = KeyType.None)
    private String id;
    // ...
}

// ❌ Bad: Manual timestamp management
@Table("t_user")
public class User {
    private String        id;
    private LocalDateTime createdTime;  // Manual
    private LocalDateTime updatedTime;  // Manual
}
```

### 7.2 Query Construction

```java
// ✅ Good: Type-safe

import static com.github.loadup.tables.Tables.USER;

QueryWrapper query = QueryWrapper.create()
        .where(USER.USERNAME.eq("admin"));

// ❌ Bad: String-based
QueryWrapper query = QueryWrapper.create()
        .eq("username", "admin");  // No compile-time check
```

### 7.3 ID Strategy Selection

| Scenario            | Recommended Strategy |
|---------------------|----------------------|
| Small application   | Random or UUID v4    |
| Need time-ordering  | **UUID v7** ⭐        |
| Distributed system  | **Snowflake** ⭐      |
| Need numeric IDs    | **Snowflake** ⭐      |
| Need standard UUIDs | UUID v4 or v7        |

### 7.4 Sequence Naming

```java
// ✅ Good: Descriptive names
sequenceService.getNextSequence("order_no");
sequenceService.

getNextSequence("user_id");
sequenceService.

getNextSequence("invoice_no");

// ❌ Bad: Generic names
sequenceService.

getNextSequence("seq1");
sequenceService.

getNextSequence("id");
```

### 7.5 Performance Tuning

1. **Index properly**: Create indexes on query columns
2. **Use ordered IDs**: UUID v7 or Snowflake for better insert performance
3. **Adjust sequence step**: Based on throughput requirements
4. **Connection pooling**: Configure HikariCP properly

## 8. Testing

### 8.1 Unit Test

```java

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testInsertWithAudit() {
        User user = new User();
        user.setUsername("test");

        userMapper.insert(user);

        // Verify auto-filled fields
        assertThat(user.getId()).isNotNull();
        assertThat(user.getCreatedTime()).isNotNull();
        assertThat(user.getCreatedBy()).isNotNull();
    }
}
```

### 8.2 H2 Test Configuration

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
      schema-locations: classpath:schema-h2.sql
```

## 9. Migration Guide

### 9.1 From Spring Data JDBC

```java
// Before: Spring Data JDBC
public interface UserRepository extends CrudRepository<User, String> {
    List<User> findByUsername(String username);
}

// After: MyBatis-Flex
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // Custom query
    default List<User> findByUsername(String username) {
        return selectListByQuery(
                QueryWrapper.create().where(USER.USERNAME.eq(username))
        );
    }
}
```

### 9.2 From MyBatis

```java
// Before: MyBatis with XML
@Select("SELECT * FROM t_user WHERE username = #{username}")
User findByUsername(String username);

// After: MyBatis-Flex
default User findByUsername(String username) {
    return selectOneByQuery(
            QueryWrapper.create().where(USER.USERNAME.eq(username))
    );
}
```

## 10. Troubleshooting

### 10.1 Common Issues

**Q: TableDef classes not found?**
A: Ensure `mybatis-flex-processor` is in dependencies and rebuild project.

**Q: ID not generated?**
A: Check `@Id(keyType = KeyType.None)` is set and entity extends `BaseDO`.

**Q: Sequence duplicates?**
A: Ensure `sys_sequence` table has UNIQUE constraint on `name` column.

**Q: Poor insert performance?**
A: Use ordered IDs (UUID v7 or Snowflake) and check database indexes.

### 10.2 Debug Logging

```yaml
logging:
  level:
    com.mybatisflex: TRACE
    io.github.loadup.components.database: TRACE
    org.springframework.jdbc: DEBUG
```

## 11. Performance Benchmarks

### 11.1 ID Generation

| Strategy  | Throughput     | Latency (avg) |
|-----------|----------------|---------------|
| Random    | ~1,000,000 TPS | < 1μs         |
| UUID v4   | ~500,000 TPS   | < 2μs         |
| UUID v7   | ~500,000 TPS   | < 2μs         |
| Snowflake | ~1,000,000 TPS | < 1μs         |

### 11.2 Sequence Service

| Operation         | Throughput   | Latency                |
|-------------------|--------------|------------------------|
| Memory allocation | ~100,000 TPS | < 10μs                 |
| Database fetch    | ~1,000 TPS   | < 10ms                 |
| Batch efficiency  | 1000:1       | 1 DB call per 1000 IDs |

### 11.3 Query Performance

| Query Type    | Performance Note             |
|---------------|------------------------------|
| Simple select | O(1) with index              |
| Like query    | Full scan if no index        |
| IN query      | O(n) but optimized by MySQL  |
| Pagination    | O(1) with LIMIT optimization |

## 12. Version History

- **1.0.0** - Initial release with Spring Data JDBC
- **1.5.0** - Added sequence service
- **2.0.0** - Migrated to MyBatis-Flex
- **2.1.0** - Added UUID v7 and improved audit
- **2.2.0** - Added multi-tenant and logical delete support ⭐

---

**Last Updated:** 2026-01-05  
**Maintainer:** LoadUp Framework Team


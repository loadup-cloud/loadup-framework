# LoadUp Components Database

Spring Boot 3 + MyBatis-Flex 集成组件，提供自动审计、ID 生成和高性能序列号服务。

## 核心功能

- ✅ **MyBatis-Flex** - 类型安全的数据库访问
- ✅ **自动审计** - 自动管理 ID、创建时间、修改时间
- ✅ **多种 ID 策略** - Random, UUID v4, UUID v7⭐, Snowflake⭐
- ✅ **高性能序列号** - 批量预分配，~100,000 TPS
- ✅ **Spring Boot 3** - 原生支持自动配置

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-database</artifactId>
</dependency>
```

### 2. 创建实体

```java
@Table("t_user")
public class User extends BaseDO {
    @Id(keyType = KeyType.None)  // ID 由审计功能自动生成
    private String id;
    private String username;
    // createdBy, createdTime, updatedBy, updatedTime 自动管理
}
```

### 3. 创建 Mapper

```java

@Mapper
public interface UserMapper extends BaseMapper<User> {
   // 自动获得 CRUD 方法
   // insert, update, delete, selectById, selectList, etc.
}
```

### 4. 类型安全查询

```java
import static com.github.loadup.tables.Tables.USER;

QueryWrapper query = QueryWrapper.create()
    .where(USER.USERNAME.eq("admin"))
    .and(USER.STATUS.eq(1));
    
List<User> users = userMapper.selectListByQuery(query);
```

// 使用序列号
@Autowired
private SequenceService sequenceService;

Long seq = sequenceService.getNextSequence("order_no");
```

## ID 生成策略

支持 4 种内置策略：

### Random String（默认）

```yaml
loadup:
  database:
    id-generator:
      strategy: random
      length: 20
```

**性能**: ~1,000,000 TPS
**适用**: 通用场景

### UUID v4

```yaml
loadup:
  database:
    id-generator:
      strategy: uuid-v4
      uuid-with-hyphens: false  # 是否保留连字符
```

**性能**: ~500,000 TPS
**适用**: 标准化需求

### UUID v7 ⭐ 推荐

```yaml
loadup:
  database:
    id-generator:
      strategy: uuid-v7
      uuid-with-hyphens: false
```

**优势**:

- ✅ 时间有序，插入性能好
- ✅ B-tree 索引友好
- ✅ 减少页面分裂

**性能**: ~500,000 TPS
**适用**: 需要时间排序的场景

### Snowflake ⭐ 推荐

```yaml
loadup:
  database:
    id-generator:
      strategy: snowflake
      snowflake-worker-id: ${WORKER_ID:0}      # 0-31
      snowflake-datacenter-id: ${DATACENTER_ID:0}  # 0-31
```

**优势**:

- ✅ 分布式唯一
- ✅ 趋势递增
- ✅ 数字型，19 位

**性能**: ~1,000,000 TPS
**适用**: 分布式系统

### 策略对比

|       策略        | 有序 |  性能   |  长度   |   推荐场景   |
|-----------------|----|-------|-------|----------|
| Random          | ❌  | ⭐⭐⭐⭐⭐ | 可配置   | 通用       |
| UUID v4         | ❌  | ⭐⭐⭐⭐  | 32/36 | 标准化      |
| **UUID v7** ⭐   | ✅  | ⭐⭐⭐⭐  | 32/36 | **需要排序** |
| **Snowflake** ⭐ | ✅  | ⭐⭐⭐⭐⭐ | 19    | **分布式**  |

## MyBatis-Flex 集成

### 依赖配置

```xml
<dependency>
    <groupId>com.mybatis-flex</groupId>
    <artifactId>mybatis-flex-spring-boot-starter</artifactId>
    <version>1.11.5</version>
</dependency>
```

### 基础用法

#### 1. 定义实体

```java
@Table("t_user")
public class User {
    @Id(keyType = KeyType.None)
    private String id;
    private String username;
    private String email;
    private Integer status;
    // ... getters and setters
}
```

#### 2. 创建 Mapper

```java

@Mapper
public interface UserMapper extends BaseMapper<User> {
   // 无需实现，自动获得 CRUD 方法
}
```

#### 3. 类型安全查询

```java
import static com.github.loadup.tables.Tables.USER;

// 基础查询
QueryWrapper query = QueryWrapper.create()
    .where(USER.USERNAME.eq("admin"));

// 复杂查询
QueryWrapper query = QueryWrapper.create()
    .where(USER.STATUS.eq(1))
    .and(USER.USERNAME.like("admin"))
    .orderBy(USER.CREATE_TIME.desc());

// 分页查询
Page<User> page = userMapper.paginate(
    Page.of(pageNum, pageSize), 
    query
);
```

### 常用方法

| Mapper 方法                   | 说明     |
|-----------------------------|--------|
| `insert(entity)`            | 插入记录   |
| `update(entity)`            | 更新记录   |
| `deleteById(id)`            | 根据ID删除 |
| `selectOneById(id)`         | 根据ID查询 |
| `selectListByQuery(query)`  | 查询列表   |
| `selectCountByQuery(query)` | 查询总数   |
| `paginate(page, query)`     | 分页查询   |

### 配置

```yaml
mybatis-flex:
  configuration:
    # 驼峰命名转换
    map-underscore-to-camel-case: true
    # SQL日志（开发环境）
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  
  global-config:
    # 打印SQL（开发环境）
    print-sql: true

logging:
  level:
    com.mybatisflex: DEBUG
```

### 优势

- ✅ **类型安全**：编译时检查字段名
- ✅ **性能优化**：自动分页、批量操作
- ✅ **开发效率**：减少90%的SQL代码
- ✅ **易于维护**：重构友好，IDE支持

## 自定义 ID 生成器

### 方式 1: 实现接口

```java
public class CustomIdGenerator implements IdGenerator {
    @Override
    public String generate() {
        return "CUSTOM-" + System.currentTimeMillis();
    }
}
```

### 方式 2: 注册 Bean

```java
@Configuration
public class Config {
    @Bean
    public IdGenerator customIdGenerator() {
        return () -> "PREFIX-" + UUID.randomUUID();
    }
}
```

## 序列号服务

### 1. 创建序列表

```sql
CREATE TABLE sys_sequence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    value BIGINT NOT NULL DEFAULT 0,
    min_value BIGINT NOT NULL DEFAULT 0,
    max_value BIGINT NOT NULL DEFAULT 9223372036854775807,
    step BIGINT NOT NULL DEFAULT 1000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_sequence_name ON sys_sequence(name);
```

### 2. 使用序列号

```java
@Service
public class OrderService {
    @Autowired
    private SequenceService sequenceService;

    public String generateOrderNo() {
        Long seq = sequenceService.getNextSequence("order_no");
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return String.format("ORD%s%06d", date, seq % 1000000);
        // 例如: ORD20251226000001
    }
}
```

### 3. 工作原理

- **批量预分配**: 每次从数据库获取一个范围（默认 1000 个）
- **内存分配**: 使用 AtomicLong 在内存中分配，性能极高
- **自动续期**: 范围用完自动获取下一个范围
- **线程安全**: 使用 ReentrantLock 保证并发安全

**性能**: 单次数据库访问支持 1000 次（默认）序列号生成

## 配置说明

### 完整配置

```yaml
loadup:
  database:
    # ID 生成器
    id-generator:
      enabled: true                    # 是否启用
      strategy: uuid-v7                # random | uuid-v4 | uuid-v7 | snowflake
      length: 20                       # random 策略的长度
      uuid-with-hyphens: false         # UUID 是否保留连字符
      snowflake-worker-id: 0           # Snowflake 机器 ID (0-31)
      snowflake-datacenter-id: 0       # Snowflake 数据中心 ID (0-31)

    # 序列号
    sequence:
      step: 1000                       # 预分配步长
      min-value: 0                     # 最小值
      max-value: 9223372036854775807   # 最大值

# Spring Data JDBC
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: user
    password: pass
    driver-class-name: com.mysql.cj.jdbc.Driver

# 日志（可选）
logging:
  level:
    com.github.loadup.components.database: DEBUG
```

### 禁用自动 ID 生成

```yaml
loadup:
  database:
    id-generator:
      enabled: false
```

## 使用示例

### 订单号生成

```java
@Service
public class OrderNumberGenerator {
    @Autowired
    private SequenceService sequenceService;

    public String generateOrderNumber() {
        Long seq = sequenceService.getNextSequence("order_no");
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("ORD%s%06d", date, seq % 1000000);
    }
}
```

### 用户编号生成

```java
@Service
public class UserNumberGenerator {
    @Autowired
    private SequenceService sequenceService;

    public String generateUserNumber() {
        Long seq = sequenceService.getNextSequence("user_no");
        return String.format("U%08d", seq);
    }
}
```

### 分布式 ID

```java
@Service
public class DistributedIdGenerator {
    @Autowired
    private SequenceService sequenceService;

    @Value("${spring.application.name:default}")
    private String appName;

    public String generateId(String businessType) {
        Long seq = sequenceService.getNextSequence(businessType);
        long timestamp = System.currentTimeMillis();
        return String.format("%s-%s-%d-%d", appName, businessType, seq, timestamp);
    }
}
```

## 性能调优

### 1. 调整序列号步长

根据业务量调整：

```yaml
loadup:
  database:
    sequence:
      step: 100      # 低并发 (< 100 TPS)
      # step: 1000   # 中并发 (100-1000 TPS)
      # step: 10000  # 高并发 (> 1000 TPS)
```

### 2. 使用有序 ID

UUID v7 和 Snowflake 对数据库索引更友好：

```sql
-- ✅ 有序 ID：顺序插入，性能好
INSERT INTO t_order VALUES ('018d...', ...);
INSERT INTO t_order VALUES ('018d...', ...);

-- ❌ 随机 ID：随机插入，可能触发页面分裂
INSERT INTO t_order VALUES ('x7k9...', ...);
INSERT INTO t_order VALUES ('a2b3...', ...);
```

### 3. 连接池配置

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 10
      maximum-pool-size: 50
      connection-timeout: 30000
```

## 最佳实践

1. **实体设计**: 所有需要审计的实体继承 `BaseDO`
2. **序列号命名**: 使用有意义的名称，如 `order_no`、`user_id`
3. **ID 策略选择**:
   - 小型应用 → Random 或 UUID v4
   - 需要排序 → **UUID v7** ⭐
   - 分布式系统 → **Snowflake** ⭐
4. **索引优化**: 在 `sys_sequence.name` 上创建索引
5. **监控**: 监控序列号使用情况，使用率 > 90% 时告警

## 常见问题

### Q: 如何切换 ID 生成策略？

修改配置即可，但注意不同策略生成的 ID 格式不同，建议在项目初期确定。

```yaml
loadup:
  database:
    id-generator:
      strategy: uuid-v7  # 切换到 UUID v7
```

### Q: Snowflake 的 workerId 如何分配？

在分布式环境中，每个实例需要不同的 workerId：

```yaml
# 实例 1
loadup.database.id-generator.snowflake-worker-id=1

# 实例 2
loadup.database.id-generator.snowflake-worker-id=2
```

或使用环境变量：

```yaml
loadup:
  database:
    id-generator:
      snowflake-worker-id: ${WORKER_ID:0}
```

### Q: 序列号会重复吗？

不会。每次从数据库获取范围时都会更新数据库，保证分布式环境下的唯一性。应用重启可能会丢失内存中未使用的序列号，但不影响唯一性。

### Q: 如何监控序列号使用情况？

```java
@Service
public class SequenceMonitor {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void checkUsage() {
        List<Map<String, Object>> sequences = jdbcTemplate.queryForList(
            "SELECT name, value, max_value FROM sys_sequence"
        );

        sequences.forEach(seq -> {
            Long value = (Long) seq.get("value");
            Long maxValue = (Long) seq.get("max_value");
            double usage = (double) value / maxValue * 100;

            if (usage > 90) {
                log.warn("Sequence '{}' usage: {:.2f}%", seq.get("name"), usage);
            }
        });
    }
}
```

## 技术栈

- Spring Boot 3.1.2+
- Spring Data JDBC
- Java 17+
- MySQL 5.7+ / PostgreSQL 12+ / Oracle 19c+

## 版本要求

- ✅ Spring Boot 3.x
- ✅ Java 17+
- ❌ Spring Boot 2.x（不支持）

## License

GNU General Public License v3.0 (GPL-3.0)

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

See [LICENSE](../../LICENSE) for details.

---

**快速链接**:

- [数据库脚本](schema.sql)
- [配置示例](application.yml.example)

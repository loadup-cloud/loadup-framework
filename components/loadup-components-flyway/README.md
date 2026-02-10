# LoadUp Components Flyway

Flyway 数据库迁移组件，提供简单、可靠的数据库版本控制。

## 特性

- ✅ 基于 SQL 的迁移脚本
- ✅ 自动版本管理
- ✅ 支持 baseline 和回滚
- ✅ Spring Boot 自动配置
- ✅ 支持占位符替换
- ✅ 支持多环境配置

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-flyway</artifactId>
</dependency>
```

### 2. 配置

**application.yml:**

```yaml
loadup:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true
    clean-disabled: true
```

### 3. 创建迁移脚本

在 `src/main/resources/db/migration` 目录下创建 SQL 文件：

**V1__Create_user_table.sql:**

```sql
CREATE TABLE IF NOT EXISTS user (
    id VARCHAR(32) PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    email VARCHAR(255) COMMENT '邮箱',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE INDEX idx_user_email ON user(email);
```

**V2__Add_user_status.sql:**

```sql
ALTER TABLE user ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态';
CREATE INDEX idx_user_status ON user(status);
```

### 4. 运行应用

应用启动时会自动执行迁移：

```
>>> [FLYWAY] Starting migration...
>>> [FLYWAY] Migration completed. 2 migrations applied
```

## 命名规范

### 版本化迁移（Versioned Migrations）

格式：`V<VERSION>__<DESCRIPTION>.sql`

示例：
- `V1__Initial_schema.sql`
- `V2__Add_user_table.sql`
- `V2.1__Add_email_column.sql`
- `V3__Create_indexes.sql`

### 可重复迁移（Repeatable Migrations）

格式：`R__<DESCRIPTION>.sql`

示例：
- `R__Create_views.sql`
- `R__Update_procedures.sql`

这些脚本在每次 checksum 变化时都会执行。

## 配置详解

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `enabled` | 是否启用 Flyway | `true` |
| `locations` | 迁移脚本位置 | `classpath:db/migration` |
| `baseline-on-migrate` | 首次迁移时自动 baseline | `true` |
| `baseline-version` | Baseline 版本号 | `0` |
| `validate-on-migrate` | 迁移前验证 | `true` |
| `clean-disabled` | 禁用 clean 命令 | `true` (生产必须) |
| `encoding` | SQL 文件编码 | `UTF-8` |
| `migrate-at-start` | 启动时自动迁移 | `true` |

## 高级特性

### 占位符

**配置：**

```yaml
loadup:
  flyway:
    placeholders:
      tableName: user
      schemaName: loadup
```

**SQL：**

```sql
CREATE TABLE ${tableName} (
    id VARCHAR(32) PRIMARY KEY
);

USE ${schemaName};
```

### 多环境配置

**application-dev.yml:**

```yaml
loadup:
  flyway:
    locations: classpath:db/migration,classpath:db/dev
```

**application-prod.yml:**

```yaml
loadup:
  flyway:
    locations: classpath:db/migration
    clean-disabled: true
    validate-on-migrate: true
```

### 初始化 SQL

```yaml
loadup:
  flyway:
    init-sqls:
      - "SET NAMES utf8mb4"
      - "SET SESSION sql_mode='STRICT_TRANS_TABLES'"
```

## Maven 命令

```bash
# 查看迁移状态
mvn flyway:info

# 执行迁移
mvn flyway:migrate

# 验证迁移
mvn flyway:validate

# 清理数据库（谨慎使用）
mvn flyway:clean

# 修复 checksum
mvn flyway:repair
```

## 最佳实践

### 1. 版本号规范

```
V1.0.0__Initial_schema.sql
V1.0.1__Add_user_table.sql
V1.1.0__Add_order_module.sql
V2.0.0__Major_refactor.sql
```

### 2. 描述性命名

❌ 不好：
```
V1__change.sql
V2__update.sql
```

✅ 好：
```
V1__Create_user_and_role_tables.sql
V2__Add_email_verification_columns.sql
```

### 3. 幂等性

始终使用 `IF EXISTS` 或 `IF NOT EXISTS`：

```sql
CREATE TABLE IF NOT EXISTS user (...);
ALTER TABLE user ADD COLUMN IF NOT EXISTS email VARCHAR(255);
DROP TABLE IF EXISTS temp_table;
```

### 4. 回滚脚本

虽然 Flyway 社区版不支持自动回滚，但应该准备回滚脚本：

```
migrations/
├── V1__Create_user_table.sql
├── U1__Drop_user_table.sql  # 回滚脚本（手动执行）
├── V2__Add_email_column.sql
└── U2__Drop_email_column.sql
```

### 5. 事务控制

默认情况下，每个迁移脚本在一个事务中执行。

禁用事务（某些 DDL 不支持事务）：

```sql
-- @Flyway:executeInTransaction=false
CREATE INDEX idx_user_email ON user(email);
```

### 6. 数据迁移

```sql
-- V3__Migrate_user_data.sql
-- 数据迁移示例
UPDATE user SET status = 'ACTIVE' WHERE status IS NULL;
```

## 常见问题

### Q1: 如何修复 checksum 不匹配？

```bash
mvn flyway:repair
```

或删除 `flyway_schema_history` 表中的记录（谨慎）。

### Q2: 如何跳过某个版本？

不推荐跳过，但可以手动插入记录到 `flyway_schema_history` 表。

### Q3: 生产环境如何使用？

```yaml
loadup:
  flyway:
    enabled: true
    clean-disabled: true  # 禁用清理
    validate-on-migrate: true  # 验证
    out-of-order: false  # 不允许乱序
```

### Q4: 如何处理多个数据库？

```
db/migration/
├── mysql/
│   └── V1__Create_tables.sql
└── postgresql/
    └── V1__Create_tables.sql
```

```yaml
loadup:
  flyway:
    locations: classpath:db/migration/mysql
```

## 迁移指南

### 从 Liquibase 迁移

Flyway 的优势：
- ✅ 更简单（直接写 SQL）
- ✅ 更快（无需解析 XML/YAML）
- ✅ 更易维护（清晰的文件结构）

迁移步骤：
1. 将 Liquibase changelog 转换为 Flyway SQL 脚本
2. 更新配置为 Flyway
3. 运行 `flyway:baseline` 建立基线
4. 后续使用 Flyway 进行迁移

## 参考资源

- [Flyway 官方文档](https://flywaydb.org/documentation/)
- [Spring Boot Flyway](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)
- [Flyway 最佳实践](https://flywaydb.org/documentation/tutorials/best-practices)

## License

Apache License 2.0

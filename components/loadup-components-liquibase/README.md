# Loadup Components Liquibase

这是一个用于 Spring Boot 3 的 Liquibase 集成组件，提供了开箱即用的数据库迁移能力。

## 功能特性

- ✅ Spring Boot 3 自动配置支持
- ✅ 灵活的配置选项
- ✅ 支持多种数据库类型（MySQL, PostgreSQL, Oracle, SQL Server 等）
- ✅ 支持多种 changelog 格式（YAML, XML, JSON, SQL）
- ✅ 完整的日志记录
- ✅ 条件化启用/禁用

## 快速开始

### 1. 添加依赖

在你的项目 `pom.xml` 中添加：

```xml

<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-liquibase</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

        <!-- 数据库驱动，根据你使用的数据库选择 -->
<dependency>
<groupId>com.mysql</groupId>
<artifactId>mysql-connector-j</artifactId>
</dependency>
```

### 2. 配置数据源

在 `application.yml` 中配置数据源：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

loadup:
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml
```

### 3. 创建 Changelog 文件

在 `src/main/resources/db/changelog/` 目录下创建 `db.changelog-master.yaml`：

```yaml
databaseChangeLog:
  - include:
      file: db/changelog/changes/v1.0.0-initial-schema.yaml
```

创建具体的变更文件 `db/changelog/changes/v1.0.0-initial-schema.yaml`：

```yaml
databaseChangeLog:
  - changeSet:
      id: 1
      author: your-name
      changes:
        - createTable:
            tableName: user
            columns:
              - column:
                  name: id
                  type: varchar(32)
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: username
                  type: varchar(100)
                  constraints:
                    nullable: false
              - column:
                  name: email
                  type: varchar(255)
              - column:
                  name: created_at
                  type: timestamp
                  defaultValueComputed: CURRENT_TIMESTAMP
```

### 4. 运行应用

启动 Spring Boot 应用，Liquibase 将自动执行数据库迁移。

## 配置说明

### 完整配置选项

```yaml
loadup:
  liquibase:
    # 是否启用 Liquibase（默认：true）
    enabled: true

    # Changelog 文件路径（默认：classpath:db/changelog/db.changelog-master.yaml）
    change-log: classpath:db/changelog/db.changelog-master.yaml

    # 上下文环境（用于区分不同环境的变更集）
    contexts: development,test,production

    # 标签过滤（用于选择性执行变更集）
    labels: feature-1,feature-2

    # 默认数据库 Schema
    default-schema: public

    # Liquibase 对象使用的 Schema
    liquibase-schema: public

    # Liquibase 对象使用的表空间（Oracle）
    liquibase-tablespace: liquibase_tablespace

    # 变更历史记录表名（默认：DATABASECHANGELOG）
    database-change-log-table: DATABASECHANGELOG

    # 变更锁定表名（默认：DATABASECHANGELOGLOCK）
    database-change-log-lock-table: DATABASECHANGELOGLOCK

    # 是否在迁移前删除所有数据库对象（危险！仅开发环境使用）
    drop-first: false

    # 是否清除所有 checksum
    clear-checksums: false

    # 是否在迁移时验证 checksum
    validate-on-migrate: true

    # 迁移后应用的标签
    tag: v1.0.0

    # 测试回滚标签
    test-rollback-on-update: v1.0.0
```

### 配置优先级

1. `loadup.liquibase.*` - 本组件的配置
2. Spring Boot 默认的 `spring.liquibase.*` - 被本组件覆盖

## 使用场景

### 场景 1：多环境配置

使用 `contexts` 区分不同环境的变更：

```yaml
# application-dev.yml
loadup:
  liquibase:
    contexts: development

# application-prod.yml
loadup:
  liquibase:
    contexts: production
```

在 changelog 中指定上下文：

```yaml
databaseChangeLog:
  - changeSet:
      id: 1
      author: dev-team
      context: development
      changes:
        - insert:
            tableName: user
            columns:
              - column: { name: username, value: "test-user" }
```

### 场景 2：禁用 Liquibase

在某些环境下（如测试环境）禁用 Liquibase：

```yaml
loadup:
  liquibase:
    enabled: false
```

### 场景 3：使用标签管理版本

```yaml
loadup:
  liquibase:
    tag: v1.0.0
```

```yaml
databaseChangeLog:
  - changeSet:
      id: 2
      author: dev-team
      labels: feature-user-management
      changes:
        - addColumn:
            tableName: user
            columns:
              - column: { name: status, type: varchar(20) }
```

## 支持的数据库

- MySQL / MariaDB
- PostgreSQL
- Oracle
- SQL Server
- H2
- Derby
- HSQLDB
- SQLite
- 其他 Liquibase 支持的数据库

## 高级功能

### 自定义 SpringLiquibase Bean

如果需要更高级的自定义，可以禁用自动配置并自己创建 Bean：

```java

@Configuration
public class CustomLiquibaseConfig {

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        // 自定义配置
        return liquibase;
    }
}
```

配置文件中禁用自动配置：

```yaml
loadup:
  liquibase:
    enabled: false
```

## 故障排查

### 1. Liquibase 未执行

检查：

- `loadup.liquibase.enabled` 是否为 `true`
- DataSource 是否正确配置
- Changelog 文件路径是否正确

### 2. 找不到 Changelog 文件

确保文件路径正确，使用 `classpath:` 前缀访问资源文件。

### 3. Checksum 验证失败

如果修改了已执行的 changeset，需要：

```yaml
loadup:
  liquibase:
    clear-checksums: true
```

或使用 Maven 插件清除：

```bash
mvn liquibase:clearCheckSums
```

## Maven 插件使用

本组件已包含 Liquibase Maven 插件配置，可以使用以下命令：

```bash
# 查看状态
mvn liquibase:status

# 更新数据库
mvn liquibase:update

# 回滚
mvn liquibase:rollback -Dliquibase.rollbackCount=1

# 生成文档
mvn liquibase:dbDoc

# 清除 checksum
mvn liquibase:clearCheckSums
```

需要在项目根目录创建 `liquibase.properties` 文件：

```properties
driver=com.mysql.cj.jdbc.Driver
url=jdbc:mysql://localhost:3306/mydb
username=root
password=password
changeLogFile=src/main/resources/db/changelog/db.changelog-master.yaml
```

## 参考资料

- [Liquibase 官方文档](https://docs.liquibase.com/)
- [Spring Boot Liquibase 集成](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.liquibase)
- [Changelog 格式参考](https://docs.liquibase.com/concepts/changelogs/home.html)

## License

本组件采用 GNU General Public License v3.0 (GPL-3.0) 授权。

详见 [LICENSE](../../LICENSE) 文件。


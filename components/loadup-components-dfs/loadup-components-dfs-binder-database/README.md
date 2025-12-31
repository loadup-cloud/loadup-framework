# LoadUp DFS Binder - Database

数据库存储Provider实现，使用Spring Data JPA。

## 特性

- ✅ 关系型数据库存储
- ✅ BLOB数据支持
- ✅ 事务一致性
- ✅ 完整CRUD操作
- ✅ 支持MySQL、PostgreSQL、H2等

## 依赖

```xml
<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-dfs-binder-database</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 数据库表

```sql
CREATE TABLE dfs_file_storage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id VARCHAR(64) NOT NULL UNIQUE,
    filename VARCHAR(255) NOT NULL,
    content BLOB NOT NULL,
    size BIGINT NOT NULL,
    content_type VARCHAR(100),
    hash VARCHAR(64),
    biz_type VARCHAR(50),
    biz_id VARCHAR(64),
    public_access BOOLEAN DEFAULT FALSE,
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 配置

```yaml
loadup:
  dfs:
    default-provider: database
    providers:
      database:
        enabled: true

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dfs
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update
```

## 使用

```java
// 自动使用Database Provider（如果配置为default）
FileMetadata metadata = dfsService.upload(request);

// 或显式指定
FileMetadata metadata = dfsService.upload(request, "database");
```

## 适用场景

- 需要事务一致性
- 中小文件存储
- 分布式应用
- 已有数据库的系统

## 注意事项

- 大文件（>10MB）建议使用Local或S3 Provider
- 注意数据库BLOB存储限制
- 建议定期清理过期文件

详见: [主项目文档](../README.md)

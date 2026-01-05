# DFS 配置简化指南

## 📋 概述

DFS 组件配置已经全面简化，提供更友好的配置方式和 IDE 自动提示支持。

## ✨ 主要改进

### 1. 移除冗余配置

- ❌ 移除 `default-provider` - 直接使用 `provider`
- ❌ 移除 `enabled` 开关 - 配置了即启用
- ❌ 移除 `providers` 嵌套结构 - 直接使用 `local`/`database`/`s3`

### 2. 枚举类型支持

- ✅ `provider` 使用枚举类型 `DfsProviderType`
- ✅ IDE 自动提示可选值：`local`, `database`, `s3`
- ✅ 类型安全，编译时检查

### 3. 配置复用

- ✅ S3 自动从 AWS 环境变量获取凭证
- ✅ Database 自动使用 Spring DataSource
- ✅ 支持多种配置来源

---

## 📝 配置示例

### 本地文件系统存储

```yaml
loadup:
  dfs:
    provider: local  # 使用枚举值，IDE 自动提示
    max-file-size: 104857600  # 100MB
    local:
      base-path: /data/dfs-storage  # 可选，默认：${user.home}/dfs-storage
```

**特点：**

- 简单直接，无需额外配置
- 适合开发环境和小规模应用

---

### 数据库存储

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: password

loadup:
  dfs:
    provider: database  # 使用枚举值
    max-file-size: 52428800  # 50MB
    database:
      table-prefix: dfs_  # 可选，默认：dfs_
```

**特点：**

- 自动复用 Spring DataSource
- 无需单独配置数据库连接
- 支持事务和 ACID

---

### S3 对象存储

#### 方式 1：直接配置凭证

```yaml
loadup:
  dfs:
    provider: s3  # 使用枚举值
    max-file-size: 1073741824  # 1GB
    s3:
      bucket: my-bucket
      access-key: AKIAIOSFODNN7EXAMPLE
      secret-key: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
      region: us-east-1
      endpoint: https://s3.amazonaws.com  # 可选
```

#### 方式 2：使用环境变量（推荐）

```yaml
loadup:
  dfs:
    provider: s3
    s3:
      bucket: my-bucket
      region: us-east-1
```

**环境变量：**

```bash
export AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
export AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
export AWS_REGION=us-east-1
```

#### 方式 3：兼容 MinIO

```yaml
loadup:
  dfs:
    provider: s3
    s3:
      bucket: test-bucket
      access-key: minioadmin
      secret-key: minioadmin
      region: us-east-1
      endpoint: http://localhost:9000  # MinIO 端点
```

**凭证获取优先级：**

1. `loadup.dfs.s3.access-key` 和 `secret-key`
2. `AWS_ACCESS_KEY_ID` 和 `AWS_SECRET_ACCESS_KEY` 环境变量
3. Spring Cloud AWS 配置（如果存在）

---

## 🔧 配置属性详解

### 通用配置

| 属性                      | 类型                | 默认值         | 说明                         |
|-------------------------|-------------------|-------------|----------------------------|
| `provider`              | `DfsProviderType` | `local`     | 存储类型（枚举：local/database/s3） |
| `max-file-size`         | `Long`            | `104857600` | 最大文件大小（字节）                 |
| `allowed-content-types` | `String[]`        | -           | 允许的 MIME 类型                |

### Local 配置

| 属性                | 类型       | 默认值                        | 说明     |
|-------------------|----------|----------------------------|--------|
| `local.base-path` | `String` | `${user.home}/dfs-storage` | 文件存储路径 |

### Database 配置

| 属性                      | 类型       | 默认值    | 说明     |
|-------------------------|----------|--------|--------|
| `database.table-prefix` | `String` | `dfs_` | 数据库表前缀 |

### S3 配置

| 属性              | 类型       | 默认值         | 说明               |
|-----------------|----------|-------------|------------------|
| `s3.bucket`     | `String` | -           | **必填**，S3 存储桶名称  |
| `s3.access-key` | `String` | -           | 访问密钥（可选，从环境变量获取） |
| `s3.secret-key` | `String` | -           | 秘密密钥（可选，从环境变量获取） |
| `s3.region`     | `String` | `us-east-1` | AWS 区域           |
| `s3.endpoint`   | `String` | -           | 自定义端点（MinIO/OSS） |

---

## 🎯 IDE 自动提示

### IntelliJ IDEA / VS Code

配置 `provider` 时，IDE 会自动提示可选值：

```yaml
loadup:
  dfs:
    provider: |  # 光标在此处时，按 Ctrl+Space
              # IDE 自动提示：
              # - local     (本地文件系统存储)
              # - database  (数据库存储)
              # - s3        (S3 对象存储)
```

**原理：**

- 使用 `DfsProviderType` 枚举类型
- Spring Boot Configuration Processor 自动生成元数据
- IDE 读取 `spring-configuration-metadata.json` 提供提示

---

## 🔄 迁移指南

### 旧配置（已废弃）

```yaml
loadup:
  dfs:
    default-provider: s3  # ❌ 废弃
    providers:            # ❌ 废弃
      s3:
        enabled: true     # ❌ 废弃
        bucket: my-bucket
        accessKey: xxx
        secretKey: xxx
```

### 新配置（推荐）

```yaml
loadup:
  dfs:
    provider: s3  # ✅ 直接指定，枚举类型
    s3:           # ✅ 扁平结构，配置即启用
      bucket: my-bucket
      access-key: xxx  # ✅ 支持短横线和驼峰
      secret-key: xxx
```

**迁移步骤：**

1. 将 `default-provider` 改为 `provider`
2. 移除 `providers` 层级
3. 移除 `enabled` 配置
4. 验证配置正确性

---

## 💡 最佳实践

### 1. 开发环境

```yaml
loadup:
  dfs:
    provider: local
    local:
      base-path: ./dev-storage
```

**优点：**

- 快速启动
- 无需外部依赖
- 方便调试

### 2. 测试环境

```yaml
loadup:
  dfs:
    provider: s3
    s3:
      bucket: test-bucket
      endpoint: http://localhost:4566  # LocalStack
```

**优点：**

- 使用 TestContainers + LocalStack
- 模拟真实 S3 环境
- 可靠的集成测试

### 3. 生产环境

```yaml
loadup:
  dfs:
    provider: s3
    s3:
      bucket: ${DFS_S3_BUCKET}
      region: ${AWS_REGION}
      # access-key 和 secret-key 从环境变量获取
```

**环境变量：**

```bash
export DFS_S3_BUCKET=prod-bucket
export AWS_REGION=us-east-1
export AWS_ACCESS_KEY_ID=xxx
export AWS_SECRET_ACCESS_KEY=xxx
```

**优点：**

- 凭证不暴露在配置文件
- 符合 12-Factor App 原则
- 易于 CI/CD 集成

---

## 🛠️ 故障排查

### 问题 1：IDE 没有自动提示

**原因：** Configuration Processor 未生成元数据

**解决方案：**

```bash
mvn clean compile
```

### 问题 2：S3 凭证未找到

**错误信息：**

```
S3 access credentials not configured
```

**解决方案：**

1. 检查配置文件中的 `access-key` 和 `secret-key`
2. 检查环境变量 `AWS_ACCESS_KEY_ID` 和 `AWS_SECRET_ACCESS_KEY`
3. 查看日志确认凭证获取优先级

### 问题 3：Provider 未生效

**原因：** 条件注解不匹配

**检查：**

```bash
# 查看 Spring Boot 自动配置报告
java -jar app.jar --debug
```

---

## 📚 相关文档

- [DFS 组件概述](./README.md)
- [S3 Metadata 实现](./loadup-components-dfs-binder-s3/METADATA_IMPLEMENTATION.md)
- [配置属性参考](./loadup-components-dfs-api/src/main/java/com/github/loadup/components/dfs/config/DfsProperties.java)
- [枚举类型定义](./loadup-components-dfs-api/src/main/java/com/github/loadup/components/dfs/config/DfsProviderType.java)

---

**更新日期：** 2026-01-06  
**版本：** 2.0.0  
**状态：** ✅ 配置简化完成


# LoadUp DFS Component - Architecture Design

## 1. 架构概述

LoadUp DFS (Distributed File Storage) 是一个基于扩展点机制的分布式文件存储组件，提供统一的文件存储抽象层，支持多种存储后端的无缝切换和扩展。

### 1.1 设计目标

- **统一接口**: 提供一致的API，屏蔽底层存储差异
- **可扩展性**: 基于Extension机制，支持自定义存储提供者
- **高性能**: 支持流式处理，优化大文件操作
- **生产就绪**: 完善的错误处理、监控和安全机制
- **Spring Boot集成**: 原生支持Spring Boot 3自动配置

### 1.2 核心价值

1. **降低耦合**: 应用代码与存储实现解耦
2. **提高灵活性**: 运行时动态切换存储提供者
3. **简化开发**: 统一API减少学习成本
4. **便于迁移**: 轻松实现存储方案迁移

## 2. 架构设计

### 2.1 分层架构

```
┌─────────────────────────────────────────────────────────────┐
│                     Application Layer                        │
│                   (Business Services)                        │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    DFS Service Layer                         │
│                      (DfsService)                            │
│  - 文件上传/下载/删除                                          │
│  - 提供者选择和路由                                            │
│  - 统一异常处理                                               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  Extension Framework                         │
│                  (ExtensionExecutor)                         │
│  - 扩展点发现和注册                                            │
│  - 基于BizScenario的路由                                      │
│  - 扩展点生命周期管理                                          │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌───────────────┬───────────────┬───────────────┬─────────────┐
│  Local Binder │ Database      │  S3 Binder    │  Custom     │
│               │  Binder       │               │  Binders    │
│  - 本地文件系统 │  - JDBC/JPA   │  - AWS SDK    │  - 扩展实现  │
│  - NFS/CIFS   │  - BLOB存储   │  - MinIO      │             │
│               │               │  - OSS/COS    │             │
└───────────────┴───────────────┴───────────────┴─────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  Physical Storage Layer                      │
│    (File System / Database / Object Storage)                │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 核心组件

#### 2.2.1 DFS Service (服务层)

**职责:**

- 提供统一的文件操作API
- 管理提供者选择和路由
- 实现通用业务逻辑
- 处理跨提供者的操作

**关键方法:**

```java
public class DfsService {
    FileMetadata upload(FileUploadRequest request);

    FileDownloadResponse download(String fileId);

    boolean delete(String fileId);

    boolean exists(String fileId);

    FileMetadata getMetadata(String fileId);

    String generatePresignedUrl(String fileId, long expirationSeconds);
}
```

#### 2.2.2 IDfsProvider (扩展点接口)

**职责:**

- 定义存储提供者标准接口
- 作为Extension扩展点
- 支持可选功能扩展

**设计特点:**

- 继承 `IExtensionPoint` 标记接口
- 使用 `@Extension` 注解标识实现
- 提供默认方法支持可选功能

#### 2.2.3 Storage Providers (存储提供者)

**Local Provider:**

- 适用场景: 开发环境、单机部署
- 存储结构: `{basePath}/{bizType}/{yyyy}/{MM}/{dd}/{fileId}`
- 特点: 简单快速、无外部依赖

**Database Provider:**

- 适用场景: 小文件、集中管理
- 存储方式: BLOB字段存储文件内容
- 特点: ACID事务、数据备份一致性

**S3 Provider:**

- 适用场景: 生产环境、大规模分布式
- 支持平台: AWS S3、MinIO、阿里云OSS、腾讯云COS
- 特点: 高可用、可扩展、支持CDN

### 2.3 数据模型

#### 2.3.1 核心模型

```java
// 文件元数据
FileMetadata {
    String fileId;          // 文件唯一标识
    String filename;        // 原始文件名
    Long size;             // 文件大小（字节）
    String contentType;    // MIME类型
    String provider;       // 存储提供者
    String path;           // 存储路径
    String url;            // 访问URL
    String hash;           // 文件哈希值（MD5）
    String bizType;        // 业务类型
    String bizId;          // 业务ID
    FileStatus status;     // 文件状态
    Map<String, String> metadata;  // 扩展元数据
    LocalDateTime uploadTime;      // 上传时间
    String uploader;               // 上传者
}

// 上传请求
FileUploadRequest {
    String filename;
    InputStream inputStream;
    Long size;
    String contentType;
    String bizType;
    String bizId;
    Boolean publicAccess;
    Map<String, String> metadata;
}

// 下载响应
FileDownloadResponse {
    FileMetadata metadata;
    InputStream inputStream;
    Long contentLength;
}
```

#### 2.3.2 数据库表设计

```sql
-- 文件元数据表（所有提供者共用）
CREATE TABLE dfs_file_metadata
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id          VARCHAR(64)  NOT NULL UNIQUE,
    filename         VARCHAR(255) NOT NULL,
    size             BIGINT       NOT NULL,
    content_type     VARCHAR(100),
    provider         VARCHAR(50)  NOT NULL,
    path             VARCHAR(500),
    url              VARCHAR(1000),
    hash             VARCHAR(64),
    biz_type         VARCHAR(50),
    biz_id           VARCHAR(64),
    status           VARCHAR(20),
    public_access    BOOLEAN,
    metadata         JSON,
    upload_time      TIMESTAMP,
    uploader         VARCHAR(64),
    last_access_time TIMESTAMP,
    access_count     BIGINT,
    INDEX idx_file_id (file_id),
    INDEX idx_provider (provider),
    INDEX idx_biz_type_id (biz_type, biz_id)
);

-- 文件存储表（Database Provider专用）
CREATE TABLE dfs_file_storage
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id      VARCHAR(64)  NOT NULL UNIQUE,
    filename     VARCHAR(255) NOT NULL,
    content      LONGBLOB     NOT NULL,
    size         BIGINT       NOT NULL,
    content_type VARCHAR(100),
    hash         VARCHAR(64),
    biz_type     VARCHAR(50),
    biz_id       VARCHAR(64)
);
```

### 2.4 扩展点机制

#### 2.4.1 Extension注册

```java

@Component
@Extension(bizCode = "DFS", useCase = "local")
public class LocalDfsProvider implements IDfsProvider {
    // Implementation
}
```

**BizScenario构成:**

- `bizCode`: "DFS" (固定)
- `useCase`: 提供者名称 (local/database/s3/custom)
- `scenario`: "default" (默认场景)

#### 2.4.2 Extension调用

```java
BizScenario scenario = BizScenario.valueOf("DFS", providerName, "default");
return extensionExecutor.

execute(
        IDfsProvider .class,
        scenario,
        extension ->extension.

upload(request)
);
```

### 2.5 配置管理

#### 2.5.1 配置结构

```yaml
loadup:
  dfs:
    default-provider: local          # 默认提供者
    max-file-size: 104857600         # 最大文件大小
    allowed-content-types: [ ]        # 允许的内容类型
    providers:
      local:
        enabled: true
        base-path: /var/dfs/files
      database:
        enabled: false
      s3:
        enabled: false
        endpoint: https://s3.amazonaws.com
        access-key: xxx
        secret-key: xxx
        bucket: my-bucket
        region: us-east-1
```

#### 2.5.2 配置加载

```java

@ConfigurationProperties(prefix = "loadup.dfs")
public class DfsProperties {
    private String                      defaultProvider;
    private Long                        maxFileSize;
    private String[]                    allowedContentTypes;
    private Map<String, ProviderConfig> providers;
}
```

## 3. 关键设计决策

### 3.1 为什么选择扩展点模式？

**优势:**

1. **开闭原则**: 对扩展开放，对修改关闭
2. **插件化**: 新增存储提供者无需修改核心代码
3. **解耦合**: 服务层与实现层完全解耦
4. **可测试**: 易于Mock和单元测试

**实现:**

- 基于LoadUp Extension框架
- Spring组件扫描自动发现
- 运行时动态路由

### 3.2 为什么分离API和Binder？

**模块职责清晰:**

- **API**: 定义契约、核心逻辑、配置管理
- **Binder**: 具体实现、外部依赖隔离

**按需引入依赖:**

```xml
<!-- 只使用Local Provider，无需引入S3 SDK -->
<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-dfs-binder-local</artifactId>
</dependency>
```

### 3.3 文件ID生成策略

**当前实现:**

- UUID v4 (128位随机数)
- 全局唯一性保证
- 无序性防止猜测

**未来考虑:**

- Snowflake算法 (有序ID)
- 自定义前缀 (业务标识)
- 短链接支持

### 3.4 元数据持久化策略

**当前状态:**

- 提供者内部管理元数据
- 支持从存储路径反查

**未来规划:**

- 统一元数据表
- 支持全文检索
- 版本历史追踪

## 4. 安全设计

### 4.1 访问控制

**文件级权限:**

```java
FileMetadata {
    Boolean publicAccess;      // 公开访问标志
    String uploader;           // 上传者
    // 未来扩展: ACL列表
}
```

**URL签名:**

```java
// S3 Provider支持预签名URL
String url = dfsService.generatePresignedUrl(fileId, 3600);
```

### 4.2 数据安全

**传输安全:**

- HTTPS加密传输
- 支持客户端加密

**存储安全:**

- 文件内容MD5校验
- 支持服务端加密 (SSE)

**访问审计:**

```sql
-- 访问日志表
CREATE TABLE dfs_access_log
(
    id          BIGINT PRIMARY KEY,
    file_id     VARCHAR(64),
    operation   VARCHAR(20),
    user_id     VARCHAR(64),
    ip_address  VARCHAR(50),
    access_time TIMESTAMP
);
```

### 4.3 防御措施

**文件类型验证:**

```java
// 基于Magic Number检测真实类型
// 防止文件伪装攻击
```

**文件大小限制:**

```yaml
loadup:
  dfs:
    max-file-size: 104857600  # 100MB
```

**病毒扫描集成:**

```java
// 未来集成ClamAV等扫描工具
```

## 5. 性能优化

### 5.1 上传优化

**分片上传:**

```java
// 大文件分片上传支持
// 断点续传能力
MultipartUploadRequest {
    String uploadId;
    int partNumber;
    InputStream partData;
}
```

**并发上传:**

```java
// 支持多文件并发上传
CompletableFuture<FileMetadata> uploadAsync(FileUploadRequest request);
```

### 5.2 下载优化

**Range请求:**

```java
// 支持HTTP Range请求
FileDownloadResponse download(String fileId, long start, long end);
```

**CDN集成:**

```yaml
loadup:
  dfs:
    providers:
      s3:
        cdn-domain: cdn.example.com
```

### 5.3 缓存策略

**元数据缓存:**

```java

@Cacheable(value = "dfs-metadata", key = "#fileId")
public FileMetadata getMetadata(String fileId) {
    // ...
}
```

**内容缓存:**

- 小文件本地缓存
- 热点文件分布式缓存

### 5.4 连接池优化

**S3客户端:**

```java
S3Client.builder()
    .

httpClientBuilder(ApacheHttpClient.builder()
        .

maxConnections(100)
        .

connectionTimeout(Duration.ofSeconds(10))
        )
```

**数据库连接池:**

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

## 6. 监控与运维

### 6.1 健康检查

```java

@Component
public class DfsHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // 检查各提供者可用性
        // 检查存储空间
        // 检查网络连通性
    }
}
```

### 6.2 指标收集

**关键指标:**

- 上传/下载速率
- 请求成功率
- 平均响应时间
- 存储使用量
- 错误率统计

**Prometheus集成:**

```java

@Counted("dfs.upload.count")
@Timed("dfs.upload.time")
public FileMetadata upload(FileUploadRequest request) {
    // ...
}
```

### 6.3 日志规范

**日志级别:**

- DEBUG: 详细调试信息
- INFO: 关键操作记录
- WARN: 异常情况警告
- ERROR: 错误信息

**日志格式:**

```
[DFS] [UPLOAD] [LOCAL] fileId=xxx, filename=xxx, size=xxx, duration=xxx
```

## 7. 未来规划

### 7.1 短期目标 (1-3个月)

#### Phase 1: 核心功能完善

- [ ] **元数据统一管理**
  - 实现独立的元数据存储服务
  - 支持元数据数据库持久化
  - 提供元数据查询和检索API
- [ ] **文件验证增强**
  - 基于Magic Number的文件类型检测
  - 文件大小和类型白名单验证
  - 恶意文件检测集成
- [ ] **测试覆盖**
  - 单元测试 (覆盖率 > 80%)
  - 集成测试 (各Provider)
  - 性能测试基准

#### Phase 2: 功能增强

- [ ] **分片上传支持**
  - 大文件分片上传API
  - 断点续传能力
  - 分片合并机制
- [ ] **图片处理**
  - 缩略图自动生成
  - 图片格式转换
  - 图片裁剪和水印
- [ ] **文件预览**
  - 图片在线预览
  - PDF文档预览
  - Office文档预览集成

### 7.2 中期目标 (3-6个月)

#### Phase 3: 高级特性

- [ ] **文件版本管理**
  - 版本历史记录
  - 版本回滚功能
  - 版本比对
- [ ] **文件加密**
  - 客户端加密
  - 服务端加密 (SSE-C/SSE-KMS)
  - 加密密钥管理
- [ ] **访问控制**
  - 基于角色的访问控制 (RBAC)
  - 文件级ACL
  - 临时访问令牌

#### Phase 4: 性能优化

- [ ] **缓存系统**
  - 元数据缓存 (Redis)
  - 小文件内容缓存
  - 缓存预热和淘汰策略
- [ ] **CDN集成**
  - 自动CDN分发
  - 边缘节点缓存
  - 智能DNS解析
- [ ] **并发优化**
  - 异步上传/下载
  - 批量操作API
  - 流式处理优化

### 7.3 长期目标 (6-12个月)

#### Phase 5: 企业级功能

- [ ] **多租户支持**
  - 租户隔离
  - 配额管理
  - 计费统计
- [ ] **文件生命周期管理**
  - 自动归档策略
  - 冷热数据分层
  - 自动清理过期文件
- [ ] **数据合规**
  - 数据驻留控制
  - 审计日志
  - 合规性报告

#### Phase 6: 智能化

- [ ] **智能识别**
  - 图片内容识别 (OCR)
  - 视频智能标签
  - 文档智能分类
- [ ] **搜索增强**
  - 全文检索
  - 图像相似度搜索
  - 语义搜索
- [ ] **自动化运维**
  - 智能容量规划
  - 自动故障转移
  - 性能自动调优

### 7.4 扩展方向

#### 新增Provider支持

- [ ] **FTP/SFTP Provider**
  - 传统FTP服务器集成
  - SFTP安全传输
- [ ] **WebDAV Provider**
  - WebDAV协议支持
  - 网盘集成
- [ ] **HDFS Provider**
  - Hadoop分布式文件系统
  - 大数据场景支持
- [ ] **IPFS Provider**
  - 去中心化存储
  - 区块链集成

#### 功能模块扩展

- [ ] **媒体处理模块**
  - 视频转码
  - 音频处理
  - 流媒体支持
- [ ] **文档转换模块**
  - Office转PDF
  - PDF处理
  - 格式转换
- [ ] **压缩归档模块**
  - 在线压缩
  - 批量打包下载
  - 增量备份

### 7.5 技术债务

#### 当前限制

1. **元数据持久化**: 目前各Provider独立管理，需统一
2. **事务支持**: 跨Provider操作无事务保证
3. **容错机制**: 缺少自动重试和降级策略
4. **监控告警**: 缺少完整的监控体系

#### 改进计划

- 引入分布式事务 (Saga模式)
- 实现熔断降级 (Resilience4j)
- 集成监控系统 (Prometheus + Grafana)
- 完善告警规则

## 8. 技术选型

### 8.1 当前技术栈

|   组件   |       技术        |   版本    |   用途    |
|--------|-----------------|---------|---------|
| 框架     | Spring Boot     | 3.1.2   | 核心框架    |
| 语言     | Java            | 17      | 开发语言    |
| S3 SDK | AWS SDK         | 2.20.26 | S3访问    |
| MinIO  | MinIO Client    | 8.5.7   | MinIO访问 |
| IO工具   | Commons IO      | 2.15.1  | 文件操作    |
| 类型检测   | Apache Tika     | 2.9.1   | MIME检测  |
| ORM    | Spring Data JPA | 3.1.2   | 数据访问    |

### 8.2 未来技术引入

|      技术       |  用途  | 优先级 |
|---------------|------|-----|
| Redis         | 缓存   | 高   |
| Elasticsearch | 全文检索 | 中   |
| FFmpeg        | 视频处理 | 中   |
| ImageMagick   | 图片处理 | 中   |
| Resilience4j  | 熔断降级 | 高   |
| Prometheus    | 监控   | 高   |
| ClamAV        | 病毒扫描 | 低   |

## 9. 最佳实践

### 9.1 开发规范

**命名规范:**

- Provider类: `{Name}DfsProvider`
- 配置类: `{Name}Properties`
- 服务类: `{Name}Service`

**代码规范:**

- 遵循阿里巴巴Java规范
- 使用Spotless格式化
- 必要的注释和文档

**测试规范:**

- 每个Provider需要单元测试
- 关键逻辑需要集成测试
- 性能测试基准

### 9.2 运维建议

**配置管理:**

- 敏感信息使用环境变量
- 不同环境独立配置
- 配置版本控制

**容量规划:**

- 监控存储使用率
- 定期清理过期文件
- 预留20%冗余空间

**备份策略:**

- 重要文件定期备份
- 跨区域冗余
- 备份数据定期验证

**故障处理:**

- 提供者降级策略
- 自动切换备用提供者
- 完善的错误日志

### 9.3 安全建议

**访问控制:**

- 最小权限原则
- 定期审计权限
- 敏感操作二次验证

**数据保护:**

- 传输加密 (TLS 1.2+)
- 存储加密 (AES-256)
- 密钥定期轮换

**合规性:**

- 遵守GDPR等法规
- 数据保留策略
- 用户数据删除机制

## 10. 总结

LoadUp DFS组件提供了一个灵活、可扩展、生产就绪的分布式文件存储解决方案。通过扩展点机制和分层架构设计，实现了存储后端的无缝切换和功能扩展。

### 核心优势

- ✅ 统一的API接口
- ✅ 灵活的提供者机制
- ✅ 完善的Spring Boot集成
- ✅ 清晰的架构设计
- ✅ 详细的文档支持

### 持续改进

组件将持续迭代，逐步实现规划中的功能，成为企业级的文件存储解决方案。我们欢迎社区贡献和反馈，共同完善这个项目。

---

**文档版本**: 1.0.0
**最后更新**: 2025-12-31
**维护者**: LoadUp Framework Team

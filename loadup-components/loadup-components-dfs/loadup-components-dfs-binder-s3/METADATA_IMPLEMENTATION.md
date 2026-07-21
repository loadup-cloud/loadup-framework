# S3 DFS Provider - Metadata 实现方案

## 📋 概述

本文档说明了 S3 DFS Provider 的 Metadata 存储和管理实现方案。我们采用了**混合方案**，结合了两种存储机制的优点。

## 🎯 设计目标

1. ✅ **快速查询** - 通过 fileId 快速定位文件，无需遍历 bucket
2. ✅ **完整元数据** - 保存所有文件元数据信息
3. ✅ **数据一致性** - 分布式环境下数据保持一致
4. ✅ **容错能力** - 元数据损坏时可以从备用源恢复
5. ✅ **性能优化** - 减少不必要的 S3 请求

## 🏗️ 架构设计

### 混合存储方案

我们采用了**双重存储机制**：

```
┌─────────────────────────────────────────────────────────────┐
│                    S3 Bucket                                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 文件对象 (File Object)                                  │
│     ├── 文件内容 (Binary Content)                           │
│     └── S3 Object Metadata (基本元数据)                     │
│         ├── file-id: "uuid-xxx"                             │
│         ├── filename: "test.txt"                            │
│         ├── hash: "md5-hash"                                │
│         ├── status: "AVAILABLE"                             │
│         ├── biz-type: "test"                                │
│         └── custom-*: 自定义元数据                          │
│                                                             │
│  2. .meta 文件 (Metadata Index)                             │
│     ├── .meta/{fileId}.json                                 │
│     └── 完整的 JSON 元数据                                  │
│         {                                                   │
│           "fileId": "uuid-xxx",                             │
│           "filename": "test.txt",                           │
│           "path": "2026/01/06/uuid-xxx.txt",                │
│           "size": 1024,                                     │
│           "contentType": "text/plain",                      │
│           "hash": "md5-hash",                               │
│           "metadata": { ... },                              │
│           ...                                               │
│         }                                                   │
└─────────────────────────────────────────────────────────────┘
```

### 方案对比

| 特性   | S3 Object Metadata | .meta 文件      | 混合方案 ✨   |
|------|--------------------|---------------|----------|
| 查询速度 | 快（需要objectKey）     | 快（直接通过fileId） | **最快**   |
| 容量限制 | 2KB                | 无限制           | **无限制**  |
| 原子性  | 与对象一起              | 独立操作          | **双重保证** |
| 复杂查询 | 不支持                | 支持            | **支持**   |
| 容错能力 | 好                  | 好             | **最好**   |
| 维护成本 | 低                  | 中             | **中**    |

## 💡 实现细节

### 1. 上传流程 (Upload)

```java
FileMetadata upload(FileUploadRequest request) {
    // Step 1: 生成 fileId 和 objectKey
    String fileId = UUID.randomUUID().toString();
    String objectKey = buildObjectKey(bizType, fileId, filename);

    // Step 2: 准备 S3 Object Metadata
    Map<String, String> s3Metadata = new HashMap<>();
    s3Metadata.put("file-id", fileId);
    s3Metadata.put("filename", filename);
    s3Metadata.put("hash", hash);
    // ... 其他基本元数据

    // Step 3: 上传文件到 S3（带 Object Metadata）
    s3Client.putObject(request, content);

    // Step 4: 保存完整元数据到 .meta 文件
    saveMetadataFile(fileId, metadata);

    return metadata;
}
```

**优势：**

- ✅ 文件和基本元数据原子性保存
- ✅ 完整元数据持久化到 .meta 文件
- ✅ 双重存储保证数据安全

### 2. 查询流程 (GetMetadata)

```java
FileMetadata getMetadata(String fileId) {
    // 策略1: 优先从 .meta 文件读取（O(1) 复杂度）
    try {
        String metaKey = ".meta/" + fileId + ".json";
        InputStream stream = s3Client.getObject(metaKey);
        return objectMapper.readValue(stream, FileMetadata.class);
    } catch (NoSuchKeyException e) {
        // 策略2: .meta 文件不存在，从 S3 Object Metadata 读取（容错）
        String objectKey = findObjectKeyByFileId(fileId);
        HeadObjectResponse response = s3Client.headObject(objectKey);

        // 从 S3 Object Metadata 重建元数据
        FileMetadata metadata = buildFromS3Metadata(response);

        // 重建 .meta 文件
        saveMetadataFile(fileId, metadata);

        return metadata;
    }
}
```

**优势：**

- ✅ 快速查询（直接通过 fileId 定位）
- ✅ 容错能力（.meta 丢失时自动恢复）
- ✅ 自动修复（重建丢失的 .meta 文件）

### 3. 删除流程 (Delete)

```java
boolean delete(String fileId) {
    FileMetadata metadata = getMetadata(fileId);

    // Step 1: 删除文件对象
    s3Client.deleteObject(metadata.getPath());

    // Step 2: 删除 .meta 文件
    s3Client.deleteObject(".meta/" + fileId + ".json");

    return true;
}
```

**优势：**

- ✅ 完全清理，不留痕迹
- ✅ 防止元数据泄漏

### 4. 复制流程 (Copy)

```java
FileMetadata copy(String sourceFileId, String targetPath) {
    FileMetadata sourceMetadata = getMetadata(sourceFileId);
    String newFileId = UUID.randomUUID().toString();

    // Step 1: 复制文件（同时更新 S3 Object Metadata）
    Map<String, String> newS3Metadata = buildS3Metadata(newFileId, ...);
    s3Client.copyObject(
            sourcePath,
            targetPath,
            newS3Metadata,
            MetadataDirective.REPLACE  // 使用新元数据
    );

    // Step 2: 保存新的 .meta 文件
    saveMetadataFile(newFileId, newMetadata);

    return newMetadata;
}
```

## 📊 元数据字段映射

### S3 Object Metadata 字段（2KB 限制内）

| 字段名             | 类型       | 说明         |
|-----------------|----------|------------|
| `file-id`       | String   | 文件唯一标识     |
| `filename`      | String   | 原始文件名      |
| `hash`          | String   | MD5 哈希值    |
| `status`        | Enum     | 文件状态       |
| `biz-type`      | String   | 业务类型       |
| `biz-id`        | String   | 业务ID       |
| `public-access` | Boolean  | 是否公开访问     |
| `upload-time`   | DateTime | 上传时间       |
| `access-count`  | Long     | 访问次数       |
| `custom-*`      | String   | 自定义元数据（前缀） |

### .meta 文件字段（完整）

```json
{
  "fileId": "uuid-xxx",
  "filename": "test.txt",
  "size": 1024,
  "contentType": "text/plain",
  "provider": "s3",
  "path": "2026/01/06/uuid-xxx.txt",
  "url": null,
  "hash": "md5-hash",
  "bizType": "test",
  "bizId": "test-123",
  "status": "AVAILABLE",
  "publicAccess": true,
  "metadata": {
    "author": "test-author",
    "version": "1.0",
    "tags": "test,service,dfs"
  },
  "uploadTime": "2026-01-06T00:00:00",
  "uploader": null,
  "lastAccessTime": null,
  "accessCount": 0
}
```

## 🧪 测试覆盖

### 测试用例列表

| 测试序号 | 测试名称                                | 测试内容               |
|------|-------------------------------------|--------------------|
| 1    | `testUploadWithDefaultProvider`     | 基本上传功能             |
| 2    | `testDownload`                      | 下载并验证元数据           |
| 3    | `testDelete`                        | 删除文件和元数据           |
| 4    | `testExists`                        | 文件存在性检查            |
| 5    | `testGetMetadata`                   | **完整元数据查询验证** ✨    |
| 6    | `testMultipleUploads`               | 多文件上传              |
| 7    | `testSameContentDifferentNames`     | 相同内容不同名称           |
| 8    | `testUploadWithCustomMetadata`      | **自定义元数据上传和查询** ✨  |
| 9    | `testDifferentContentTypes`         | 不同内容类型             |
| 10   | `testMetadataPersistence`           | **元数据持久化验证** ✨     |
| 11   | `testMetadataWithSpecialCharacters` | **特殊字符和Unicode** ✨ |
| 12   | `testMetadataFieldUpdates`          | **元数据字段更新** ✨      |
| 13   | `testEmptyAndNullMetadata`          | **空和null元数据处理** ✨  |
| 14   | `testMetadataAfterDownload`         | **下载后元数据验证** ✨     |

### 关键测试场景

#### ✅ 测试5: 完整元数据查询

```java

@Test
public void testGetMetadata() {
    // 上传带完整元数据的文件
    FileUploadRequest request = FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .contentType("text/plain")
            .bizType("metadata-test")
            .bizId("test-biz-123")
            .publicAccess(true)
            .build();

    FileMetadata uploaded = dfsService.upload(request);
    FileMetadata retrieved = dfsService.getMetadata(uploaded.getFileId());

    // 验证所有字段
    assertEquals(uploaded.getFileId(), retrieved.getFileId());
    assertEquals(TEST_FILENAME, retrieved.getFilename());
    assertEquals("text/plain", retrieved.getContentType());
    assertEquals("metadata-test", retrieved.getBizType());
    assertEquals("test-biz-123", retrieved.getBizId());
    assertTrue(retrieved.getPublicAccess());
    assertNotNull(retrieved.getHash());
    assertNotNull(retrieved.getUploadTime());
}
```

#### ✅ 测试8: 自定义元数据

```java

@Test
public void testUploadWithCustomMetadata() {
    Map<String, String> customMetadata = new HashMap<>();
    customMetadata.put("author", "test-author");
    customMetadata.put("version", "1.0");
    customMetadata.put("tags", "test,service,dfs");

    FileUploadRequest request = FileUploadRequest.builder()
            .filename(TEST_FILENAME)
            .metadata(customMetadata)
            .build();

    FileMetadata uploaded = dfsService.upload(request);
    FileMetadata retrieved = dfsService.getMetadata(uploaded.getFileId());

    // 验证自定义元数据完整保存和读取
    assertEquals("test-author", retrieved.getMetadata().get("author"));
    assertEquals("1.0", retrieved.getMetadata().get("version"));
    assertEquals("test,service,dfs", retrieved.getMetadata().get("tags"));
}
```

#### ✅ 测试10: 元数据持久化

```java

@Test
public void testMetadataPersistence() {
    // 上传文件
    FileMetadata uploaded = dfsService.upload(request);

    // 多次查询
    FileMetadata retrieved1 = dfsService.getMetadata(uploaded.getFileId());
    FileMetadata retrieved2 = dfsService.getMetadata(uploaded.getFileId());

    // 验证数据一致性
    assertEquals(uploaded.getFileId(), retrieved1.getFileId());
    assertEquals(uploaded.getFileId(), retrieved2.getFileId());
    assertEquals(uploaded.getHash(), retrieved1.getHash());
    assertEquals(uploaded.getHash(), retrieved2.getHash());
}
```

#### ✅ 测试11: 特殊字符处理

```java

@Test
public void testMetadataWithSpecialCharacters() {
    Map<String, String> customMetadata = new HashMap<>();
    customMetadata.put("description", "文件描述：测试特殊字符 & symbols!");
    customMetadata.put("tags", "中文,English,日本語");
    customMetadata.put("author", "张三 (Zhang San)");

    FileMetadata retrieved = dfsService.getMetadata(uploaded.getFileId());

    // 验证Unicode和特殊字符完整保存
    assertEquals("文件描述：测试特殊字符 & symbols!",
            retrieved.getMetadata().get("description"));
    assertEquals("中文,English,日本語",
            retrieved.getMetadata().get("tags"));
}
```

## 🚀 性能特性

### 查询性能

| 操作    | 复杂度  | S3 请求次数 | 说明                      |
|-------|------|---------|-------------------------|
| 上传    | O(1) | 2 次     | 1次文件上传 + 1次.meta上传      |
| 查询元数据 | O(1) | 1 次     | 直接读取.meta文件             |
| 删除    | O(1) | 3 次     | 1次查询 + 2次删除             |
| 复制    | O(1) | 3 次     | 1次查询 + 1次复制 + 1次.meta上传 |

### 缓存优化建议

虽然当前实现不使用缓存，但对于高并发场景，可以考虑：

```java
// 可选：添加本地缓存层（使用 Caffeine）
Cache<String, FileMetadata> metadataCache = Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .build();

FileMetadata getMetadata(String fileId) {
    // 先查缓存
    FileMetadata cached = metadataCache.getIfPresent(fileId);
    if (cached != null) {
        return cached;
    }

    // 缓存未命中，从 S3 读取
    FileMetadata metadata = readFromS3(fileId);

    // 更新缓存
    metadataCache.put(fileId, metadata);

    return metadata;
}
```

## 📝 容错机制

### 1. .meta 文件丢失

**场景：** .meta 文件被意外删除

**恢复流程：**

```
getMetadata(fileId)
  ↓
.meta 文件不存在 (NoSuchKeyException)
  ↓
遍历 bucket 查找对应的文件对象
  ↓
从 S3 Object Metadata 重建元数据
  ↓
自动保存新的 .meta 文件
  ↓
返回元数据
```

### 2. S3 Object Metadata 损坏

**场景：** Object Metadata 缺失或损坏

**恢复流程：**

```
getMetadata(fileId)
  ↓
从 .meta 文件读取（优先）
  ↓
返回完整元数据
```

### 3. 双重验证

```java
// 可选：验证两个来源的数据一致性
if(DEBUG_MODE){
FileMetadata fromMeta = readFromMetaFile(fileId);
FileMetadata fromObject = readFromObjectMetadata(objectKey);
    
    if(!fromMeta.

getHash().

equals(fromObject.getHash())){
        log.

warn("Metadata inconsistency detected for fileId: {}",fileId);
// 触发数据修复流程
    }
            }
```

## 🔧 配置项

### application.yml

```yaml
loadup:
  dfs:
    default-provider: s3
    providers:
      s3:
        enabled: true
        bucket: your-bucket-name
        endpoint: https://s3.amazonaws.com
        region: us-east-1
        access-key: ${AWS_ACCESS_KEY}
        secret-key: ${AWS_SECRET_KEY}
```

## 📚 依赖项

### pom.xml

```xml
<!-- Jackson for JSON serialization -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
<groupId>com.fasterxml.jackson.datatype</groupId>
<artifactId>jackson-datatype-jsr310</artifactId>
</dependency>

        <!-- AWS SDK for S3 -->
<dependency>
<groupId>software.amazon.awssdk</groupId>
<artifactId>s3</artifactId>
</dependency>
```

## ✨ 优势总结

### 相比单一方案的优势

| 方案                   | 优势                                    | 劣势                    |
|----------------------|---------------------------------------|-----------------------|
| 仅 S3 Object Metadata | ✅ 原子性好                                | ❌ 需要遍历查询<br>❌ 容量限制2KB |
| 仅 .meta 文件           | ✅ 快速查询<br>✅ 无容量限制                     | ❌ 独立维护<br>❌ 可能不一致     |
| **混合方案** ✨           | ✅ 快速查询<br>✅ 无容量限制<br>✅ 双重备份<br>✅ 自动恢复 | ⚠️ 存储成本略高             |

### 核心优势

1. **🚀 高性能**
    - O(1) 查询复杂度
    - 无需遍历 bucket
    - 直接通过 fileId 定位

2. **🛡️ 高可靠**
    - 双重存储保证
    - 自动容错恢复
    - 数据一致性验证

3. **📈 可扩展**
    - 无容量限制
    - 支持复杂元数据
    - 支持自定义字段

4. **🔧 易维护**
    - 清晰的架构设计
    - 完善的测试覆盖
    - 自动修复机制

## 🎓 最佳实践

### 1. 生产环境建议

```java
// 建议添加元数据版本控制
Map<String, String> s3Metadata = new HashMap<>();
s3Metadata.

put("metadata-version","1.0");  // 版本号

// 建议添加校验和
s3Metadata.

put("metadata-checksum",calculateChecksum(metadata));
```

### 2. 监控指标

```java
// 记录关键指标
log.info("Metadata operation - fileId: {}, operation: {}, duration: {}ms",
         fileId, operation, duration);

// 监控 .meta 文件命中率
metrics.

counter("dfs.metadata.cache_hit").

increment();
metrics.

counter("dfs.metadata.cache_miss").

increment();
```

### 3. 清理策略

```java
// 定期清理过期的 .meta 文件
@Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2点
public void cleanupOrphanedMetadata() {
    // 查找没有对应文件对象的 .meta 文件
    // 删除这些孤儿元数据
}
```

## 🔗 相关资源

- [AWS S3 Object Metadata 文档](https://docs.aws.amazon.com/AmazonS3/latest/userguide/UsingMetadata.html)
- [Jackson JSON 处理文档](https://github.com/FasterXML/jackson-docs)
- [LoadUp DFS 设计文档](../README.md)

## 📄 License

Copyright (C) 2026 LoadUp Cloud - GNU General Public License v3.0

---

**作者:** LoadUp Framework Team  
**日期:** 2026-01-06  
**版本:** 1.0.0


# LoadUp DFS API

DFS组件的核心API模块，定义了文件存储的统一接口和数据模型。

## 包含内容

### 核心接口

- `IDfsProvider` - Provider扩展点接口
- `DfsService` - 统一的DFS服务

### 数据模型

- `FileMetadata` - 文件元数据
- `FileUploadRequest` - 上传请求
- `FileDownloadResponse` - 下载响应
- `FileStatus` - 文件状态枚举
- `StorageProvider` - Provider类型枚举

### 配置

- `DfsProperties` - 配置属性
- `DfsAutoConfiguration` - 自动配置

## 依赖

```xml
<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-dfs-api</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 使用

```java
@Autowired
private DfsService dfsService;

// 使用统一的API操作文件
FileMetadata metadata = dfsService.upload(request);
FileDownloadResponse response = dfsService.download(fileId);
boolean success = dfsService.delete(fileId);
```

详见: [主项目文档](../README.md)

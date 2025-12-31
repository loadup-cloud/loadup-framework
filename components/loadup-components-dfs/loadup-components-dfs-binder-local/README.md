# LoadUp DFS Binder - Local

Local文件系统存储Provider实现。

## 特性

- ✅ 本地文件系统存储
- ✅ JSON元数据持久化（.meta目录）
- ✅ MD5哈希计算
- ✅ 自动目录创建（按业务类型和日期）
- ✅ 大文件支持
- ✅ 无需外部依赖

## 依赖

```xml
<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-dfs-binder-local</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 配置

```yaml
loadup:
  dfs:
    default-provider: local
    providers:
      local:
        enabled: true
        base-path: /var/dfs/files  # 文件存储根目录
```

## 存储结构

```
/var/dfs/files/
├── .meta/                    # 元数据目录
│   ├── fileId1.json
│   └── fileId2.json
├── documents/                # 业务分类
│   └── 2025/12/31/          # 日期目录
│       └── uuid-file
└── images/
    └── 2025/12/31/
        └── uuid-file
```

## 使用

```java
// 自动使用Local Provider（如果配置为default）
FileMetadata metadata = dfsService.upload(request);

// 或显式指定
FileMetadata metadata = dfsService.upload(request, "local");
```

## 适用场景

- 单机应用
- 开发测试环境
- 小规模部署
- 无需分布式存储的场景

详见: [主项目文档](../README.md)

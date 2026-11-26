# LoadUp DFS 接入手册

应用通过 `DfsTemplate` 上传、读取与删除对象。宿主选择存储后端，由对应 binder 注册实现。

[架构设计](ARCHITECTURE.md)

## Maven 依赖

导入 Framework BOM，添加 `loadup-components-dfs-api` 和一个 binder。以下示例使用 S3。

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-dfs-api</artifactId>
</dependency>
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-dfs-binder-s3</artifactId>
</dependency>
```

## 配置

通过 `loadup.dfs.binder-type` 选择 `local`、`s3` 或 `database`。自定义 binder 声明自己的标识，后端配置位于 `loadup.dfs.binder.<type>`。

```yaml
loadup:
  dfs:
    binder-type: s3
    binder.s3.bucket: loadup-files
    binder.s3.region: us-east-1
```

## 能力矩阵

| 能力                                 | local | s3 | database |
|--------------------------------------------|------:|---:|---------:|
| 上传、下载、删除、存在性检查、元数据 |    ✓ | ✓ |       ✓ |
| 元数据可跨进程重启保留          |    ✓ | ✓ |       ✓ |
| 预签名下载 URL                     |    ✗ | ✓ |       ✗ |
| 分片上传                           |    ✗ | ✓ |       ✗ |

- [S3](loadup-components-dfs-binder-s3/README.md) 适用于对象存储。
- [Local](loadup-components-dfs-binder-local/README.md) 适用于开发、测试或单节点文件系统。
- [Database](loadup-components-dfs-binder-database/README.md) 适用于过渡期的小文件。

下载响应实现 `AutoCloseable`。调用方使用 try-with-resources 消费数据流。

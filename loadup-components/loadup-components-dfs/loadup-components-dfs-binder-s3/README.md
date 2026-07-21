# LoadUp DFS Binder - S3

S3对象存储Provider实现，兼容AWS S3和MinIO。

## 特性

- ✅ AWS S3支持
- ✅ MinIO兼容
- ✅ 高可用性
- ✅ 无限扩展
- ✅ CDN加速（AWS）

## 依赖

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-dfs-binder-s3</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 配置

### AWS S3

```yaml
loadup:
  dfs:
    default-provider: s3
    providers:
      s3:
        enabled: true
        endpoint: https://s3.amazonaws.com
        region: us-east-1
        access-key: ${AWS_ACCESS_KEY}
        secret-key: ${AWS_SECRET_KEY}
        bucket: my-bucket
```

### MinIO

```yaml
loadup:
  dfs:
    providers:
      s3:
        enabled: true
        endpoint: http://localhost:9000
        region: us-east-1
        access-key: minioadmin
        secret-key: minioadmin
        bucket: dfs-bucket
```

## 使用

```java
// 自动使用S3 Provider（如果配置为default）
FileMetadata metadata = dfsService.upload(request);

// 或显式指定
FileMetadata metadata = dfsService.upload(request, "s3");
```

## 适用场景

- 生产环境
- 大文件存储
- 需要CDN加速
- 高可用要求
- 云原生应用

## 注意事项

- 需要配置AWS credentials或MinIO访问密钥
- 注意S3的费用（请求次数、存储、流量）
- 建议配置生命周期策略管理过期文件

详见: [主项目文档](../README.md)

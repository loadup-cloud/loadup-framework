# DFS S3 binder 接入

用于 AWS S3 或 MinIO、OSS、COS、LocalStack 等 S3 兼容服务。应用从环境或部署机密读取凭证。

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-dfs-binder-s3</artifactId>
</dependency>
```

```yaml
loadup:
  dfs:
    binder-type: s3
    binder.s3.bucket: loadup-files
    binder.s3.region: us-east-1
    binder.s3.endpoint: https://s3.amazonaws.com
    binder.s3.access-key: ${AWS_ACCESS_KEY_ID}
    binder.s3.secret-key: ${AWS_SECRET_ACCESS_KEY}
    binder.s3.path-style-access-enabled: false
```

提前准备 bucket 和访问权限。开发可用 `create-bucket` 初始化 bucket，兼容服务按需调整 endpoint 和 path-style。文件名和自定义数据写入对象元数据。应用可按 [DFS API](../loadup-components-dfs-api/README.md) 请求预签名 GET URL，或发起、上传、完成和取消分片会话。

[架构设计](ARCHITECTURE.md)

# DFS 本地文件 binder 接入

本地文件 binder 适用于开发、测试和单节点部署。运行用户需要对存储根目录有读写权限。

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-dfs-binder-local</artifactId>
</dependency>
```

```yaml
loadup:
  dfs:
    binder-type: local
    binder.local.base-path: /var/lib/loadup-dfs
```

文件内容位于 `<base-path>/objects`，持久化元数据位于 `<base-path>/metadata`。部署时将这两个目录放在应用可见的持久化文件系统。通过 `DfsTemplate` 读取内容，预签名与分片能力见 [DFS 能力矩阵](../README.md#能力矩阵)。

[架构设计](ARCHITECTURE.md)

# DFS API

API 提供 `DfsTemplate` 和 `io.github.loadup.components.dfs.model` 下的不可变文件模型。公共能力包括对象的上传、下载、删除和元数据。预签名下载与分片上传由支持这些能力的 binder 提供，其他 binder 调用相应接口时报告 `UnsupportedOperationException`。

## 接入步骤

在引入 `loadup-framework-bom` BOM 的应用中添加：

```xml
<dependency><groupId>io.github.loadup-cloud</groupId><artifactId>loadup-components-dfs-api</artifactId></dependency>
```

通过 `loadup.dfs.binder-type` 选择 local、s3 或 database；业务层注入 `DfsTemplate`。
运行时还需在应用侧引入一种对应 binder；仅有接口 jar 不会创建实际后端能力。

## 对外契约

- [`DfsTemplate`](src/main/java/io/github/loadup/components/dfs/DfsTemplate.java)

业务代码注入 `DfsTemplate` 后，可调用 `upload(FileUploadRequest)`、`download(fileId)`、
`getMetadata(fileId)` 和 `delete(fileId)`。需要临时下载链接或分片上传时，先确认所选 binder
支持对应能力；当前能力差异见父模块 [README](../README.md)。

## 自动装配

各 binder 按 `loadup.dfs.binder-type` 匹配并注册 `DfsTemplate`。应用可自行提供一个 `DfsTemplate` Bean。

设计与装配路径见 [ARCHITECTURE.md](ARCHITECTURE.md)。

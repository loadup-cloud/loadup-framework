# File Resources

文件资源模块在 DFS 存储之上管理文件的业务元数据、归属、引用和删除生命周期。DFS 决定文件字节存在哪里；本模块提供可被业务引用的稳定文件 ID。适用于单体应用中的附件、头像和导出文件。

## 引入

引入 `loadup-modules-file` 和一个 DFS binder，例如 `loadup-components-dfs-binder-local`。应用需要 MySQL `DataSource`；Flyway 会执行 `V20261002000003__create_file_resource.sql`。可设置 `loadup.file.enabled: false` 停用自动装配。HTTP API 另需 `loadup-modules-file-web`。

```xml
<dependency><groupId>io.github.loadup-cloud</groupId><artifactId>loadup-modules-file</artifactId></dependency>
<dependency><groupId>io.github.loadup-cloud</groupId><artifactId>loadup-components-dfs-binder-local</artifactId></dependency>
```

本地 binder 配置 `loadup.dfs.binder.local.base-path`；生产多节点环境请选择共享存储 binder，并配置其凭据和容量策略。本地启动器示例路径为 `./data/files`。

## 业务 API

注入 `FileResourceService`。`upload(tenantId, ownerId, filename, contentType, size, InputStream)` 上传并记录元数据，调用者关闭输入流。返回的 `FileResource.id` 是业务文件 ID，`storageId` 仅供内部定位 DFS 对象。`get`、`list` 和 `download` 均校验租户及所有者，管理员权限由调用方传入；下载返回的 `FileDownloadResponse` 必须关闭。

业务记录落库后可调用 `attach(tenantId, fileId, actorId, admin, referenceType, referenceId)` 建立引用。业务记录删除时调用 `detach`。有引用的文件不能删除。引用类型和引用 ID 只用于记录关联；业务系统负责确认引用对象的真实性及访问授权，不应把任意用户输入直接传给这两个 API。

`requestDeletion` 将无引用文件置为 `PENDING_DELETE`，事务提交后调用 `cleanup` 删除 DFS 对象并标记 `DELETED`。DFS 清理失败时可调用 `cleanupPending(limit)` 重试。需要自动清理的应用可用已有调度组件定期调用它。没有租户上下文时使用 `__default__`。

Web API 与配置见 [Web 适配说明](../loadup-modules-file-web/README.md)，设计见 [ARCHITECTURE.md](ARCHITECTURE.md)。

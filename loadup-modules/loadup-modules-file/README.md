# File Resources

文件资源模块在 DFS 存储之上管理文件的业务元数据、归属、引用和删除生命周期。DFS 决定文件字节存在哪里；本模块提供可被业务引用的稳定文件 ID。适用于单体应用中的附件、头像和导出文件。

## 引入

引入 `loadup-modules-file-app` 和一个 DFS binder，例如 `loadup-components-dfs-binder-local`。应用需要 MySQL `DataSource`；Flyway 会执行 `V20261002000003__create_file_resource.sql`。可设置 `loadup.modules.file.enabled: false` 停用自动装配。HTTP API 另需 `loadup-modules-file-web`。

```xml
<dependency><groupId>io.github.loadup-cloud</groupId><artifactId>loadup-modules-file-app</artifactId></dependency>
<dependency><groupId>io.github.loadup-cloud</groupId><artifactId>loadup-components-dfs-binder-local</artifactId></dependency>
```

本地 binder 配置 `loadup.dfs.binder.local.base-path`；生产多节点环境请选择共享存储 binder，并配置其凭据和容量策略。本地启动器示例路径为 `./data/files`。

## 业务 API

注入 `FileResourceService`。`upload(tenantId, ownerId, filename, contentType, size, InputStream)` 上传并记录元数据，调用者关闭输入流。返回的 `FileResource.id` 是业务文件 ID，`storageId` 仅供内部定位 DFS 对象。`get`、`list` 和 `download` 均校验租户及所有者，管理员权限由调用方传入；下载返回的 `FileDownloadResponse` 必须关闭。

业务记录落库后可调用 `attach(tenantId, fileId, actorId, admin, referenceType, referenceId)` 建立引用。业务记录删除时调用 `detach`。有引用的文件不能删除。引用类型和引用 ID 只用于记录关联；业务系统负责确认引用对象的真实性及访问授权，不应把任意用户输入直接传给这两个 API。

`requestDeletion` 将无引用文件置为 `PENDING_DELETE`，事务提交后调用 `cleanup` 删除 DFS 对象并标记 `DELETED`。DFS 清理失败时可调用 `cleanupPending(limit)` 重试。需要自动清理的应用可用已有调度组件定期调用它。没有租户上下文时使用 `__default__`。

Web API 与配置见 [Web 适配说明](loadup-modules-file-web/README.md)，设计见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## COLA 模块选择

本目录的 Maven 坐标 `loadup-modules-file` 为聚合 POM。业务接入选择具体 jar，版本由根 BOM 管理：

| 子模块 | 用途 |
| --- | --- |
| [`client`](loadup-modules-file-client/README.md) | 对外 DTO、请求契约；不依赖 Spring 或持久化实现 |
| [`domain`](loadup-modules-file-domain/README.md) | 纯 Java 领域模型、分页值与 Gateway 接口 |
| [`infrastructure`](loadup-modules-file-infrastructure/README.md) | 默认 MyBatis-Flex Gateway、Flyway 迁移与持久化装配 |
| [`app`](loadup-modules-file-app/README.md) | 用例服务、DTO 映射与应用装配；程序化接入入口 |
| [`web`](loadup-modules-file-web/README.md) | 可选 Spring MVC 适配；自动引入 app |
| [`test`](loadup-modules-file-test/README.md) | 自动装配回归源码，不作为生产依赖 |

`FileResourceService` 现在位于 `io.github.loadup.modules.file.app.service`；公开结果位于 `io.github.loadup.modules.file.client.dto`。领域 Gateway 不引用客户端 DTO，也不引用数据库或 Spring API。HTTP 适配使用客户端契约，接口路径及 JSON 字段保持一致。

配置统一归入 `loadup.modules.file`：

```yaml
loadup:
  modules:
    file:
      enabled: true
      web:
        enabled: true
```

仅引入 app 时不注册 Controller；关闭 web 开关保留程序化服务，关闭模块 enabled 开关同时停止默认应用与持久化 Bean 装配。配置开关不控制 Flyway 对已在 classpath 上的脚本执行。不要在已有数据库重复复制迁移脚本。

## 统一接入契约

Java 消费方通过 `client.facade.XxxFacade` 注入公开业务入口；默认应用 Service 直接实现接口。引入 `*-app` 装配业务能力，引入 `*-web` 才提供 Controller，Web 适配不再提供独立 enabled 开关。模块整体启停仍使用 `loadup.modules.file.enabled`。

JSON Controller 显式返回 SuccessResponse，分页保留已有分页报文契约；异常由全局 WebMVC 处理。下载仍为流式响应。请求与 DTO 字段声明 OpenAPI，凭证只写。持久化经 database 组件使用 MyBatis-Flex、Tables 常量和 Spring MapStruct Converter；数据库连接与可信租户来源由消费工程配置。新 schema 迁移与本轮 clean 编译、运行验证仍需本地执行。

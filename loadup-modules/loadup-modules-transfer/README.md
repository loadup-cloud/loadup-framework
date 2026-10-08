# Import / Export Tasks

面向后台业务的一次性导入导出任务：保存任务状态与进度，由 RetryTask/JobRunr 在后台执行具体处理器，并通过文件资源模块保存导入源文件和导出结果。框架不定义业务表的导入规则；消费工程实现 `TransferHandler`。

## 引入

```xml
<dependency><groupId>io.github.loadup-cloud</groupId><artifactId>loadup-modules-transfer</artifactId></dependency>
<dependency><groupId>io.github.loadup-cloud</groupId><artifactId>loadup-components-retrytask-binder-jobrunr</artifactId></dependency>
<dependency><groupId>io.github.loadup-cloud</groupId><artifactId>loadup-components-dfs-binder-local</artifactId></dependency>
```

模块依赖 `loadup-modules-file`、RetryTask facade 和 MySQL `DataSource`；Flyway 执行 `V20261003000002__create_transfer_task.sql`。导入源文件应先由文件资源模块上传。`loadup.transfer.enabled: false` 可关闭自动装配；HTTP 端点由 `loadup-modules-transfer-web` 单独提供。多节点部署须配置共享 DFS binder 和持久 JobRunr storage。

## 处理器

注册一个 Spring `TransferHandler` bean，指定唯一的 `kind()`（`IMPORT`/`EXPORT`）和 `key()`。`process(context, input, output)` 读取导入流或写入导出流，并调用 `context.report(processed,total)` 更新进度。导入处理器可向输出流写校验/错误报告；导出处理器必须输出结果文件。`outputFilename()` 与 `outputContentType()` 用于结果文件元数据。

处理器应按 `context.taskId()` 实现业务写入幂等。任务默认不自动重试，但失败后用户可显式重试，作业故障恢复也可能重放同一任务。不要把单个文件内容或敏感数据塞入任务参数；`options` 最多 30 项，每项值不超过 1000 字符，实际文件保存在 DFS。

## 服务 API

`TransferTaskService.submit(tenantId, ownerId, kind, handlerKey, sourceFileId, options)` 创建任务并排队。`get` 与 `list` 按租户和所有者查询；`retry` 可重发 `QUEUED` 或重试 `FAILED` 任务。结果的 `resultFileId` 可交给文件资源模块下载。导入源文件在处理期间被任务引用，完成或失败后释放。未知处理器、无权访问的源文件及超限文件会被拒绝。

默认输入限制 20 MiB、输出限制 100 MiB；分别配置 `loadup.transfer.max-input-bytes`、`loadup.transfer.max-output-bytes`。输出先写入工作节点的临时文件，随后上传 DFS；临时目录需有足够空间。结果文件的留存由业务应用管理。

本地启动器的 `demo-csv-import` 只校验简单 `value,label` CSV 并生成报告，不写业务表；`demo-csv-export` 生成示例 CSV。接入业务表时替换为自己的处理器。

HTTP 用法见 [Web 适配](../loadup-modules-transfer-web/README.md)，一致性设计见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 导出字段脱敏

MVC 注解不会处理 CSV、Excel 或文件下载。业务 handler 生成文件时逐字段调用 `Masking.mask(value, MaskType)`，消费工程引入 `loadup-commons-masking`。开发启动器的 `demo-csv-export` 演示导出已脱敏 mobile 列。

首版没有通用明文导出开关。若业务确需明文，必须在提交和后台执行时校验租户、授权范围及有效用户状态，并可靠审计；不要把请求中的管理员标记或 ThreadLocal 传播到任务来绕过校验。

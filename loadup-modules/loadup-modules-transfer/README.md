# Import / Export Tasks

面向后台业务的一次性导入导出任务：保存任务状态与进度，由 Scheduler
定时唤醒扫描器领取业务数据库队列并执行处理器，并通过文件资源模块保存导入源文件和导出结果。框架不定义业务表的导入规则；消费工程实现
`TransferHandler`。

## 引入

```xml
<dependency><groupId>io.github.loadup-cloud</groupId><artifactId>loadup-modules-transfer-app</artifactId></dependency>
<dependency><groupId>io.github.loadup-cloud</groupId><artifactId>loadup-components-dfs-binder-local</artifactId></dependency>
```

模块依赖 `loadup-modules-file-app`、Scheduler API 和 PostgreSQL `DataSource`；Flyway 执行
`V20261003000002__create_transfer_task.sql`。导入源文件应先由文件资源模块上传。`loadup.modules.transfer.enabled: false`
可关闭自动装配；HTTP 端点由 `loadup-modules-transfer-web` 单独提供。多节点部署须配置共享 DFS binder，并选择一个 Scheduler
binder；本地 @Scheduled 不需要独立 Server。

## 处理器

注册一个 Spring `TransferHandler` bean，指定唯一的 `kind()`（`IMPORT`/`EXPORT`）和 `key()`。
`process(context, input, output)` 读取导入流或写入导出流，并调用 `context.report(processed,total)`
更新进度。导入处理器可向输出流写校验/错误报告；导出处理器必须输出结果文件。`outputFilename()` 与 `outputContentType()`
用于结果文件元数据。

处理器应按 `context.taskId()` 实现业务写入幂等。任务默认不自动重试，但失败后用户可显式重试，作业故障恢复也可能重放同一任务。不要把单个文件内容或敏感数据塞入任务参数；
`options` 最多 30 项，每项值不超过 1000 字符，实际文件保存在 DFS。

## 服务 API

`TransferTaskService.submit(tenantId, ownerId, kind, handlerKey, sourceFileId, options)` 创建任务并排队。`get` 与 `list`
按租户和所有者查询；`retry` 可重发 `QUEUED` 或重试 `FAILED` 任务。结果的 `resultFileId`
可交给文件资源模块下载。导入源文件在处理期间被任务引用，完成或失败后释放。未知处理器、无权访问的源文件及超限文件会被拒绝。

默认输入限制 20 MiB、输出限制 100 MiB；分别配置 `loadup.modules.transfer.max-input-bytes`、
`loadup.modules.transfer.max-output-bytes`。输出先写入工作节点的临时文件，随后上传 DFS；临时目录需有足够空间。结果文件的留存由业务应用管理。

本地启动器的 `demo-csv-import` 只校验简单 `value,label` CSV 并生成报告，不写业务表；`demo-csv-export` 生成示例
CSV。接入业务表时替换为自己的处理器。

HTTP 用法见 [Web 适配](loadup-modules-transfer-web/README.md)，一致性设计见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 导出字段脱敏

MVC 注解不会处理 CSV、Excel 或文件下载。业务 handler 生成文件时逐字段调用 `Masking.mask(value, MaskType)`，消费工程引入
`loadup-commons-masking`。开发启动器的 `demo-csv-export` 演示导出已脱敏 mobile 列。

首版没有通用明文导出开关。若业务确需明文，必须在提交和后台执行时校验租户、授权范围及有效用户状态，并可靠审计；不要把请求中的管理员标记或
ThreadLocal 传播到任务来绕过校验。

## COLA 模块选择

本目录的 Maven 坐标 `loadup-modules-transfer` 为聚合 POM。业务接入选择具体 jar，版本由根 BOM 管理：

| 子模块                                                               | 用途                                              |
|----------------------------------------------------------------------|---------------------------------------------------|
| [`client`](loadup-modules-transfer-client/README.md)                 | 对外 DTO、请求契约；不依赖 Spring 或持久化实现    |
| [`domain`](loadup-modules-transfer-domain/README.md)                 | 纯 Java 领域模型、分页值与 Gateway 接口           |
| [`infrastructure`](loadup-modules-transfer-infrastructure/README.md) | 默认 Spring JDBC Gateway、Flyway 迁移与持久化装配 |
| [`app`](loadup-modules-transfer-app/README.md)                       | 用例服务、DTO 映射与应用装配；程序化接入入口      |
| [`web`](loadup-modules-transfer-web/README.md)                       | 可选 Spring MVC 适配；自动引入 app                |
| [`test`](loadup-modules-transfer-test/README.md)                     | 自动装配回归源码，不作为生产依赖                  |

`TransferTaskService` 现在位于 `io.github.loadup.modules.transfer.app.service`；公开结果位于
`io.github.loadup.modules.transfer.client.dto`。领域 Gateway 不引用客户端 DTO，也不引用数据库或 Spring API。HTTP
适配使用客户端契约，接口路径及 JSON 字段保持一致。

配置统一归入 `loadup.modules.transfer`：

```yaml
loadup:
  modules:
    transfer:
      enabled: true
      web:
        enabled: true
```

仅引入 app 时不注册 Controller；通过是否引入 web 依赖选择 HTTP 暴露，关闭模块 enabled 开关同时停止默认应用与持久化 Bean
装配。配置开关不控制 Flyway 对已在 classpath 上的脚本执行。不要在已有数据库重复复制迁移脚本。

## 调度配置

应用引入 `loadup-components-scheduler-binder-local`，设置 `loadup.scheduler.binder-type=local` 即使用 Spring @Scheduled
默认每分钟扫描。Transfer 注册名为 `transfer.scan`，业务模块仅依赖 Scheduler API，无 PowerJob 依赖。没有 binder 时任务保持
QUEUED。

使用 PowerJob 时改引入 `loadup-components-scheduler-binder-powerjob`，设置 binder-type=powerjob 并配置原生 Worker。在管理台创建
BUILT_IN / STANDALONE / CRON 任务：processorInfo=`loadupSchedulerProcessor`、jobParams=`transfer.scan`，建议 cron=
`0 * * * * ?`、maxInstanceNum=1、instanceRetryNum=0、taskRetryNum=0。

每次扫描最多 `loadup.modules.transfer.dispatch-batch-size`（默认 20，上限 100）项 QUEUED 任务。扫描器遍历处理，单项失败存为
FAILED，扫描正常返回不代表所有业务任务成功。失败任务通过现有 retry API 重排；RUNNING 不自动重排。业务处理器以 taskId
幂等；Worker 崩溃后的租约、对账和恢复仍是待办。

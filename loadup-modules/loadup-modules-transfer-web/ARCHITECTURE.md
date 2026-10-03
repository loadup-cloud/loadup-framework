# Import / Export Tasks Web 架构

`TransferTaskController` 是 `TransferTaskService` 的可选 MVC 入口。自动配置仅在任务服务、文件资源和 RetryTask binder 可用时创建 Controller。业务处理器由应用注册，不在 Web 模块内解析格式或访问数据库。

租户来自 `TenantUtil`，所有者来自已验证的 `LoadUpUser.userId`。提交时不接受调用方指定所有者；查询和重试在服务层再次校验任务所有权。管理员查询列表可选指定用户，但结果文件下载仍由文件资源模块授权。

Web 层返回任务状态与文件 ID，不直接串流输出。浏览器按任务 ID 轮询状态，成功后再调用结果和文件下载接口，避免长连接承载导入导出工作。

接入见 [README.md](./README.md)。

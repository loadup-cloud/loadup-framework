# Import / Export Tasks 架构

## 边界

`TransferTaskService` 管理任务元数据、处理器选择、进度和结果文件；RetryTask 管理持久队列及工作节点执行；文件资源模块管理文件访问和 DFS 字节。导入导出任务把文件资源作为明确的下层业务能力，避免重复实现所有者校验、上传和下载。

```text
API / 业务提交 → TransferTaskService → transfer_task(+option) → MySQL
                         │                   │
                         ├→ FileResourceService → DFS
                         └→ RetryTaskFacade → JobRunr → TransferHandler
```

## 生命周期与失败语义

提交时校验处理器和输入文件、持久化 `QUEUED` 任务、建立源文件引用，然后注册 RetryTask。注册失败记为 `FAILED` 并释放引用；数据库提交后、注册前若进程异常退出，任务会停留在 `QUEUED`，可调用重发接口。执行时状态变为 `RUNNING`，处理器更新进度；输出经有界临时文件上传到文件资源模块，成功后记为 `SUCCEEDED` 并释放源文件引用。异常记为 `FAILED`，尝试清理未关联的输出，错误细节写入服务端日志，公开任务只返回通用失败信息。

一个任务 ID 对应一个 RetryTask `bizId`；默认 `maxRetries=0`，避免非幂等导入被自动重复执行。手动重试会重置失败任务；JobRunr 对异常运行中的作业的恢复行为仍可能重放处理器，业务处理器必须以任务 ID 做幂等。提交、文件存储和 JobRunr 分属不同事务，不提供跨系统原子提交。工作节点突然终止时可能保留 `RUNNING` 状态；运行中作业恢复与对账需结合生产部署继续完善，见 Roadmap。

## 安全与资源限制

Web 端从认证主体取所有者，从可信租户上下文取租户；普通用户只可见自己的任务，超级管理员可指定所有者查询。导入源文件必须由所有者可读，处理期间通过文件引用防止删除。结果文件属于任务所有者，下载复用 `/api/files/{id}/content` 的权限校验。

任务参数有数量与长度限制，处理器输出受字节上限限制；临时输出位于工作节点本地，适用于一次任务在一个节点上完成。首版不提供任务中途取消、分片处理或自动结果清理。

接入见 [README.md](./README.md)。

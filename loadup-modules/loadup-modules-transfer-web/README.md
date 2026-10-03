# Import / Export Tasks Web

引入 `loadup-modules-transfer-web` 后，在任务服务可用时注册 `/api/transfer-tasks/**`。所有端点需要登录；普通用户只能查询和重试自己的任务，管理员可用 `ownerId` 查询指定用户的列表。设置 `loadup.transfer.web.enabled: false` 可关闭 HTTP 端点。

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| POST | `/api/transfer-tasks/imports` | 提交导入，传 `handlerKey`、`sourceFileId`、`options` |
| POST | `/api/transfer-tasks/exports` | 提交导出，传 `handlerKey`、`options` |
| POST | `/api/transfer-tasks/list` | JSON 分页列表，可传 `ownerId`（管理员） |
| POST | `/api/transfer-tasks/detail` | JSON `{ "id": "..." }` 查询状态与进度 |
| POST | `/api/transfer-tasks/result` | JSON `{ "id": "..." }` 获取结果文件 ID 和下载路径 |
| POST | `/api/transfer-tasks/retry` | JSON `{ "id": "..." }` 重发排队任务或重试失败任务 |

导入前先通过 `/api/files` 上传源文件。结果下载继续使用 `/api/files/{resultFileId}/content`，遵循文件资源模块的权限和二进制响应。任务 JSON 响应使用全局 `{result, data}` 包装；任务失败详情不暴露处理器异常栈。

设计见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

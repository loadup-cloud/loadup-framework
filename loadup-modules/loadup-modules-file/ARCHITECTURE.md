# File Resources 架构

## 边界

`FileResourceService` 管理业务文件 ID、租户、所有者、元数据、引用和删除状态。它通过 `DfsService` 存取字节，不重新实现本地文件、数据库或对象存储 binder。`FileResourceRepository` 隔离 MySQL；可选 Web 模块负责身份与 HTTP 流式传输。模块不依赖 UPMS 或审计中心。

```text
业务/API → FileResourceService ┬→ FileResourceRepository → MySQL
                           └→ DfsService → 选定 DFS binder
```

## 数据与一致性

`file_resource` 记录业务 ID 与 DFS storage ID、租户/所有者、文件名、大小、类型、provider、状态和时间戳；`file_resource_reference` 记录业务引用，唯一键避免同一关联重复。引用增加与删除申请都先锁定文件行，再检查状态或引用计数，从而阻止“删除检查后又加引用”的竞态。

上传先完成 DFS 写入，再写 MySQL；MySQL 失败时尝试补偿删除 DFS 对象。如果补偿删除也失败，原异常附带清理异常，需按 DFS 运维手段清理孤儿对象。删除先在数据库事务内由 `ACTIVE` 变为 `PENDING_DELETE`，提交后清理 DFS，再置 `DELETED`。清理失败保留待处理状态，`cleanupPending` 可重试。若 DFS 对象已不存在，binder 的幂等删除仍可完成状态收敛。跨 MySQL 与 DFS 不承诺原子事务。

## 安全与部署

所有元数据查询按租户过滤；Web 入口从受信认证主体和租户上下文取得 owner/tenant，不接受请求体覆盖。只有所有者和超级管理员可读取或删除文件。Web 下载采用附件模式、`nosniff` 和流式传输；不会生成公开直链。业务引用不自动授予文件读取权限，业务域需要单独决定谁能查看附件。

HTTP 上传受 Spring Multipart 大小限制。对高风险文件类型的病毒扫描、内容审核、下载水印、保留期限和跨租户共享应在消费应用的业务规则中实现；首版不开放公开访问。

接入方式见 [README.md](./README.md)。

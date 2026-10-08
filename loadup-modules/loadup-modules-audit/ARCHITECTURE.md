# Audit Center 架构

## 边界

`loadup-modules-audit` 提供 `AuditWrite`、`AuditService` 和 JDBC 存储，不依赖 UPMS 或 HTTP。`loadup-modules-audit-web` 可选地提供 MVC 自动采集和管理员查询。业务服务可以直接注入 `AuditService` 记录非 HTTP 操作。

首版存储和 Flyway DDL 面向 MySQL；模块引入 Flyway starter 与 MySQL 支持，数据库驱动由接入方提供。

```text
AuditWrite → AuditService → JdbcAuditRepository → audit_event
AuditQuery → AuditService → JdbcAuditRepository → AuditPage
```

## 数据契约

每个事件有唯一 ID、租户 ID、操作者 ID、动作、HTTP 方法、路由模板、结果、traceId 和发生时间。数据库表保留 `id`、`tenant_id`、`created_at`、`updated_at`、`deleted` 五个标准列。查询始终按传入的租户 ID 限定范围，按发生时间与 ID 倒序分页，页大小上限 100。

## 写入与失败

审计表只追加事件。自动采集在 MVC 请求结束后写入；写入失败会记录服务器日志，不改变已经完成的业务响应。因此首版适合后台操作追踪，尚不提供与业务事务原子提交的合规级审计保证。认证过滤器在进入 MVC 前拒绝的请求不会被该 MVC 适配器采集。可靠写入、保留/归档与该类拒绝事件列于 ROADMAP。

接入方式见 [README.md](README.md)。

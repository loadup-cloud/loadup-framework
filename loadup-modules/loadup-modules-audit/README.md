# Audit Center

为后台应用保存不可变的操作审计事件，并提供按租户、操作者、动作、结果和时间范围查询的服务。事件只包含元数据，不保存请求体或凭证。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-modules-audit</artifactId>
</dependency>
```

需要配置 MySQL `DataSource` 和驱动；模块引入 Flyway 并提供 `db/migration/V20261002000001__create_audit_event.sql`。如需自动采集 MVC 操作和查询接口，引入 `loadup-modules-audit-web`。

若将 Flyway 首次接入已有非空数据库，请按应用现有的 Flyway 配置决定是否设置 `spring.flyway.baseline-on-migrate`（使用 LoadUp Database 时为 `loadup.flyway.baseline-on-migrate`）。

## 程序化记录

注入 `AuditService`，调用 `record(new AuditWrite(tenantId, actorId, action, method, path, outcome, traceId))`。仅传入不含密码、令牌、请求体或敏感参数的元数据。`loadup.audit.enabled: false` 可关闭服务自动装配。

`search(AuditQuery)` 强制传入租户上下文，页码从 1 开始，每页最多 100 条。审计表为追加写入，不提供修改、删除接口；保留和归档策略由接入方规划。

设计与失败语义见 [ARCHITECTURE.md](ARCHITECTURE.md)。

# Audit Center

为后台应用保存不可变的操作审计事件，并提供按租户、操作者、动作、结果和时间范围查询的服务。事件只包含元数据，不保存请求体或凭证。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-modules-audit-app</artifactId>
</dependency>
```

需要配置 MySQL `DataSource` 和驱动；模块引入 Flyway 并提供 `db/migration/V20261002000001__create_audit_event.sql`。如需自动采集 MVC 操作和查询接口，引入 `loadup-modules-audit-web`。

若将 Flyway 首次接入已有非空数据库，请按应用现有的 Flyway 配置决定是否设置 `spring.flyway.baseline-on-migrate`（使用 LoadUp Database 时为 `loadup.flyway.baseline-on-migrate`）。

## 程序化记录

注入 `AuditService`，调用 `record(new AuditRecordCommand(tenantId, actorId, action, method, path, outcome, traceId))`。仅传入不含密码、令牌、请求体或敏感参数的元数据。`loadup.modules.audit.enabled: false` 可关闭服务自动装配。

`search(AuditQuery)` 强制传入租户上下文，页码从 1 开始，每页最多 100 条。审计表为追加写入，不提供修改、删除接口；保留和归档策略由接入方规划。

设计与失败语义见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## COLA 模块选择

本目录的 Maven 坐标 `loadup-modules-audit` 为聚合 POM。业务接入选择具体 jar，版本由根 BOM 管理：

| 子模块 | 用途 |
| --- | --- |
| [`client`](loadup-modules-audit-client/README.md) | 对外 DTO、请求契约；不依赖 Spring 或持久化实现 |
| [`domain`](loadup-modules-audit-domain/README.md) | 纯 Java 领域模型、分页值与 Gateway 接口 |
| [`infrastructure`](loadup-modules-audit-infrastructure/README.md) | 默认 MyBatis-Flex Gateway、Flyway 迁移与持久化装配 |
| [`app`](loadup-modules-audit-app/README.md) | 用例服务、DTO 映射与应用装配；程序化接入入口 |
| [`web`](loadup-modules-audit-web/README.md) | 可选 Spring MVC 适配；自动引入 app |
| [`test`](loadup-modules-audit-test/README.md) | 自动装配回归源码，不作为生产依赖 |

`AuditService` 现在位于 `io.github.loadup.modules.audit.app.service`；公开结果位于 `io.github.loadup.modules.audit.client.dto`。领域 Gateway 不引用客户端 DTO，也不引用数据库或 Spring API。HTTP 适配使用客户端契约，接口路径及 JSON 字段保持一致。

配置统一归入 `loadup.modules.audit`：

```yaml
loadup:
  modules:
    audit:
      enabled: true
      web:
        enabled: true
```

仅引入 app 时不注册 Controller；关闭 web 开关保留程序化服务，关闭模块 enabled 开关同时停止默认应用与持久化 Bean 装配。配置开关不控制 Flyway 对已在 classpath 上的脚本执行。不要在已有数据库重复复制迁移脚本。

## 统一接入契约

Java 消费方通过 `client.facade.XxxFacade` 注入公开业务入口；默认应用 Service 直接实现接口。引入 `*-app` 装配业务能力，引入 `*-web` 才提供 Controller，Web 适配不再提供独立 enabled 开关。模块整体启停仍使用 `loadup.modules.audit.enabled`。

JSON Controller 显式返回 SuccessResponse，分页保留已有分页报文契约；异常由全局 WebMVC 处理。下载仍为流式响应。请求与 DTO 字段声明 OpenAPI，凭证只写。持久化经 database 组件使用 MyBatis-Flex、Tables 常量和 Spring MapStruct Converter；数据库连接与可信租户来源由消费工程配置。新 schema 迁移与本轮 clean 编译、运行验证仍需本地执行。

## 入参与分页约定

写操作入参采用业务动作 Command，查询入参采用 Query，分页查询使用 PageQuery 后缀；纯 ID 查询复用公共 IdQuery。Facade 分页统一返回 `PageDTO<T>`，HTTP 分页统一返回 `PageResponse<T>`：`result` 表示结果，`data` 为当前页数组，顶层 `pageInfo` 提供 totalCount/pageIndex/pageSize。不再提供模块专用分页 DTO。完整规则与示例见 [commons-dto](../../loadup-commons/loadup-commons-dto/README.md)。

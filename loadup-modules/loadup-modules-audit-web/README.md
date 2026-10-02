# Audit Center Web Adapter

为审计中心提供按路径配置的 Spring MVC 操作采集，以及管理员查询接口。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-modules-audit-web</artifactId>
</dependency>
```

引入该模块会自动带入审计核心、WebMVC 响应约定和方法授权组件。配置需要采集的路径；默认路径列表为空，不自动记录业务请求：

```yaml
loadup:
  audit:
    web:
      include-paths:
        - /api/upms/*/create
        - /api/upms/*/update
        - /api/auth/login
```

默认只采集配置路径上的 POST、PUT、PATCH、DELETE；可用 `loadup.audit.web.include-methods` 调整。采集结果包括操作者、当前租户、路由模板、成功/失败和 MDC `traceId`；不读取请求体、查询字符串、密码或令牌。HTTP 错误和通用 `FailureResponse` 被记为失败。登录前的操作者可能为空；MVC 之前被安全过滤器拒绝的请求不在采集范围。写入失败仅记录服务器日志，不回滚业务操作。

管理员使用 `POST /api/audit/events/query` 查询，要求 `ROLE_SUPER_ADMIN`，支持 `actorId`、`action`、`outcome`、`from`、`to`、`page`、`size`。查询自动使用当前 `TenantUtil` 租户 ID，不接受客户端指定租户。`loadup.audit.web.enabled: false` 关闭 Web 适配。

实现边界见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

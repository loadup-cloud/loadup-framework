# Audit Center Web Adapter 架构

## 采集路径

`AuditWebAutoConfiguration` 在审计核心服务存在时注册 `AuditCaptureInterceptor`、`AuditResponseAdvice` 和 `AuditController`。拦截器仅注册到 `loadup.audit.web.include-paths`，列表为空时不采集请求；处理完成后再按 `include-methods` 过滤 HTTP 方法。业务服务与 Controller 无需依赖审计模块。

```text
配置的 MVC 路由 → 业务处理 → 响应标记 → afterCompletion → AuditService
管理员查询 → AuditController → AuditService → 租户范围内的分页事件
```

`AuditResponseAdvice` 标记采用 HTTP 200 的 `FailureResponse`；`afterCompletion` 综合异常、HTTP 状态和标记判定结果。操作者来自 Spring Security，租户来自 `TenantUtil`，traceId 来自 MDC；记录路由模板以避免把动态 URL 参数写入审计表。不读取请求体或查询字符串。

## 安全与边界

查询接口使用 `@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")`，仍须由接入方的 Resource Server 保护 `/api/**`。审计写入发生在 MVC 处理后，失败不会更改业务结果。SecurityFilterChain 在请求进入 MVC 前拒绝时不触发拦截器；首版不覆盖该类事件，也不承诺与业务事务原子提交。

配置示例见 [README.md](README.md)。

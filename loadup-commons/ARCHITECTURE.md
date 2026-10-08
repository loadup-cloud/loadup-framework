# Commons 架构

Commons 位于依赖链底部，供技术组件和业务模块复用，不依赖上层模块。

- [DTO](loadup-commons-dto/README.md)：统一响应、分页、`BaseDO` 与 MapStruct 配置。
- [Util](loadup-commons-util/README.md)：JSON、日期、字符串等无业务归属的工具。
- [Log](loadup-commons-log/README.md)：日志格式及 MDC 键约定。

数据库审计字段在 Java/JSON 中统一为 `createdAt`、`updatedAt`，列名为 `created_at`、`updated_at`。通用类型不承载业务授权、HTTP 路由或具体存储决策。

## 分层与调用路径

```text
loadup-commons
  └─ loadup-commons-dto
  └─ loadup-commons-log
  └─ loadup-commons-util
```

聚合 POM 组织模块与版本，运行时依赖由子模块决定。

## 脱敏边界

`loadup-commons-masking` 无 Spring/Jackson 运行依赖；只表达展示规则。响应适配由 WebMVC 承担，业务权限与明文查询由业务模块控制。

## 业务上下文

`loadup-commons-context` 无运行时三方依赖，提供业务执行链共享数据；`commons-util` 的 TenantUtil 复用 TENANT_ID。核心基于 JDK 25 ScopedValue 绑定不可变 ExecutionContext，可选 ServiceTemplate 提供 init/clean。WebMVC 负责请求回调边界，Observability 通过 TaskDecorator 组合业务与标准 Micrometer 传播，核心不依赖它们。

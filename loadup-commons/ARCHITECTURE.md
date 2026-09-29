# Commons 架构

Commons 位于依赖链底部，供技术组件和业务模块复用，不依赖上层模块。

- [DTO](loadup-commons-dto/README.md)：统一响应、分页、`BaseDO` 与 MapStruct 配置。
- [Util](loadup-commons-util/README.md)：JSON、日期、字符串等无业务归属的工具。
- [Log](loadup-commons-log/README.md)：日志格式及 MDC 键约定。
- [Tracer](loadup-commons-tracer/README.md)：OpenTelemetry span、Servlet 请求追踪与上下文传播，复用 Log 的 MDC 约定。

数据库审计字段在 Java/JSON 中统一为 `createdAt`、`updatedAt`，列名为 `created_at`、`updated_at`。通用类型不承载业务授权、HTTP 路由或具体存储决策。

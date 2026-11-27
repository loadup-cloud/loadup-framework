# Commons Log 架构

## 日志契约

`loadup-commons-log` 定义通用日志格式常量、`traceId`、`spanId`、`requestId` 的 MDC 键及 `LogContext`。应用选择 SLF4J 后端。
`LogUtil` 的显式来源类重载用于框架和应用日志，参数化格式及末尾 Throwable 语义沿用 SLF4J。直接调用重载通过 JDK
`StackWalker` 获取调用类。第三方适配可通过 `LogUtil.getLogger` 获取原生 Logger。

## Spring Boot 装配

Runtime 中的 `LoadupLogEnvironmentPostProcessor` 为 console/file 添加最低优先级的格式默认值，应用的 `logging.pattern.*`
优先。文件未配置独立格式时沿用 LoadUp console pattern。结构化输出配置使用 Spring Boot 的 `logging.structured.format.*`。
`LoadupLogAutoConfiguration` 绑定框架日志属性。

## 追踪与服务摘要

Spring Boot Micrometer Tracing 为当前追踪写入标准 MDC 键，日志格式读取这些值。业务日志的 trace 和 span 生命周期由追踪组件负责。

`@ServiceDigest` 是可用于类、方法和组合注解的运行期标记。Observability 负责注册
advisor、计时和调用摘要；摘要契约仅记录调用状态等元数据，参数和返回值留在业务代码。代理拦截与异步完成语义见 [Observability 接入](../../loadup-components/platform/loadup-components-observability/README.md)。

[日志使用方式](README.md) · [Runtime 架构](../../loadup-components/platform/loadup-components-runtime/ARCHITECTURE.md)

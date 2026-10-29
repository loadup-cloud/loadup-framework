# LoadUp Scheduler API

引入 `io.github.loadup-cloud:loadup-components-scheduler-api`（版本由 BOM 管理）。实现 ScheduledTask 并注册 Spring Bean。使用
SchedulerTemplate.trigger (name) 可手动触发；需配置唯一 SchedulerProvider。只引入 API 可作为仅生产业务任务的应用，不会自行启动扫描。

## 能力矩阵

| 能力           | 支持               |
|----------------|--------------------|
| 调度接入       | 已实现，边界见上文 |
| 业务数据持久化 | 由业务组件负责     |

完整接入契约见上级 [Scheduler](../README.md)。

# Scheduler Binder PowerJob 架构

PowerJobSchedulerProvider 同时实现 SchedulerProvider 与官方 BasicProcessor，Bean 名固定 loadupSchedulerProcessor。读取 jobParams 作为本地注册名，调用 SchedulerTemplate.trigger，异常传回原生引擎。Worker 的上下文隔离和观测由 PowerJob 组件提供，业务身份仍从持久化记录重建。Server 的作业定义由运维管理，不与业务截止时间形成一对一映射。

总体职责及扩展契约见上级 [架构](../ARCHITECTURE.md)。

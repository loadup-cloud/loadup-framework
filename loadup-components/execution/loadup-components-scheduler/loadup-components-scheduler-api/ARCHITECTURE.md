# Scheduler API 架构

SchedulerTemplate 是 SmartLifecycle 注册表，构造时验证名称与唯一性；每个任务有独立重叠保护。Provider 只有
start/stop，业务扫描任务只依赖 ScheduledTask，不依赖调度厂商。

总体职责及扩展契约见上级 [架构](../ARCHITECTURE.md)。

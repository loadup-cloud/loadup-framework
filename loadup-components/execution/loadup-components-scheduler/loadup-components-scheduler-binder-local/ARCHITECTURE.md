# Scheduler Binder Local 架构

@EnableScheduling 装配 Spring 定时基础设施；LocalSchedulerProvider 的 @Scheduled 方法读取已绑定
Template。单个扫描器异常不阻止其他扫描器，日志只包含安全错误类型。线程池参数由消费工程使用 Spring 标准配置管理。

总体职责及扩展契约见上级 [架构](../ARCHITECTURE.md)。

# Scheduler Binder Local 架构

@EnableScheduling 装配 Spring 定时基础设施；LocalSchedulerProvider 的 @Scheduled 方法读取已绑定
Template。单个扫描器异常不阻止其他扫描器，日志只包含安全错误类型。线程池参数由消费工程使用 Spring 标准配置管理。

总体职责及扩展契约见上级 [架构](../ARCHITECTURE.md)。

## 虚拟线程评估

LocalSchedulerProvider 使用 Spring 原生 fixed-delay，逐个同步唤醒 scanner。Boot 全局虚拟线程开关会影响自动 TaskScheduler，不能把 pool.size 当成虚拟线程并发上限。
不自动并行化 scanner，不修改任务领取、租约或幂等语义。LocalSchedulerTest 已补两种模式下原生调度与不重叠检查源码，未运行。
消费方应验证扫描持续时间/停机、队列积压和背景身份重建；线程开关不能替代持久任务恢复或并发隔离。

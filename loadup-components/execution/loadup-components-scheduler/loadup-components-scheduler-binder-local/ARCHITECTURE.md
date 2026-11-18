# Local binder 架构

`LocalSchedulerProvider` 使用独立的 Spring `ThreadPoolTaskScheduler`，为每个配置项注册 `CronTrigger`。触发时按任务名回调 `SchedulerTemplate`。
单个任务失败会记录任务名和异常类型。停止或注册失败时取消已建立的触发器并关闭任务线程池。

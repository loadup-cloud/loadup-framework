# Scheduler API 架构

`ScheduledTask` 定义名称和业务执行逻辑。`TaskSchedule` 定义任务名与六字段 Cron，供 binder 消费。
`SchedulerTemplate` 匹配配置和任务 Bean，校验完整任务列表后再启动 binder，并负责同名任务的 JVM 内重叠抑制。

# Loadup Scheduler Binder JobRunr 架构

## 职责与边界

任务调度的具体后端适配模块；由集成方按需引入。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-scheduler-api`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter`
- `org.jobrunr:jobrunr-spring-boot-4-starter`
- `org.springframework.boot:spring-boot-autoconfigure-processor`
- `org.springframework.boot:spring-boot-configuration-processor`

## 实现入口

主要入口文件：

- [`JobRunrSchedulerAutoConfiguration`](src/main/java/io/github/loadup/components/scheduler/jobrunr/autoconfig/JobRunrSchedulerAutoConfiguration.java)
- [`JobRunrSchedulerTemplate`](src/main/java/io/github/loadup/components/scheduler/jobrunr/JobRunrSchedulerTemplate.java)

## 分层与调用路径

`SchedulerTemplate` 接收注册与管理命令，binder 适配到调度引擎，再由 `SchedulerProcessor` 执行业务。

```text
业务代码 → API/facade → 当前 binder → 第三方引擎或基础设施
```
binder 实现框架契约，把实现库及其配置隔离在业务 API 之外。

## 装配规则

- [`JobRunrSchedulerAutoConfiguration`](src/main/java/io/github/loadup/components/scheduler/jobrunr/autoconfig/JobRunrSchedulerAutoConfiguration.java) 是自动配置入口。
  - `@ConditionalOnClass({JobRequestScheduler.class, JobRequest.class})`

## 设计取舍

选择独立 binder，使周期任务实现的依赖留在集成应用；替换底层实现时，业务侧仍使用 `SchedulerTemplate` 契约。

集成方式与配置示例见 [README.md](README.md)。

## 待执行实例清理

变更计划或删除周期任务时，清理 AWAITING、SCHEDULED、ENQUEUED 状态下属于该任务的实例。清理分页会改变结果集，因此下一页偏移量只推进本页保留的记录数，避免删除一页后跳过已前移的任务。回归测试覆盖超过 1000 条待执行实例及不属于目标周期任务的记录。

## 已确认的上游缓存边界

JobRunr 9.0.0 的 `RecurringJob.toScheduledJobs` 即使下一次执行不在当前轮询窗口，也会创建远期实例；`ProcessRecurringJobsTask` 将其时间记入以 recurring id 为键的内存 map。重新加载数据库中的 cron 定义不会清除这个时间，`lastRun > runStartTime` 时直接跳过调度。binder 的数据库清理不能使此内存状态失效。

2026-10-18 使用 `SchedulerTemplateIT#updateCronChangesSchedule` 先等待旧年度任务进入 SCHEDULED，再更新为每 5 秒执行，稳定复现 20 秒超时；原用例立即连续注册/更新，是否失败取决于后台轮询能否插入两次调用之间。9.0.1 发布说明仅修复 starter 模块名，不应假定升级即可修复调度问题。不得以增加等待时间、跳过测试或去掉执行断言代替运行时修复。

已按用户选择维护 [共享补丁运行时](../../loadup-components-jobrunr/ARCHITECTURE.md)，刷新定义时失效变更/删除任务的游标，保留无关任务。两个 binder 排除原 core、共同引入该替代运行时；不通过反射操纵私有 map，也不为更新一个任务重启共享后台服务器。

源码依据：[JobRunr 9.0.0 调度任务](https://github.com/jobrunr/jobrunr/blob/v9.0.0/core/src/main/java/org/jobrunr/server/tasks/zookeeper/ProcessRecurringJobsTask.java)、[RecurringJob 预调度](https://github.com/jobrunr/jobrunr/blob/v9.0.0/core/src/main/java/org/jobrunr/jobs/RecurringJob.java)、[9.0.1 发布说明](https://github.com/jobrunr/jobrunr/releases/tag/v9.0.1)。

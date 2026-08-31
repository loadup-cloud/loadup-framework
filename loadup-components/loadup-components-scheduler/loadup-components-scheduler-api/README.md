# LoadUp Scheduler API

定时任务组件的 **facade 模块**：业务代码只依赖本模块的 `SchedulerTemplate`、`SchedulerProcessor`
及模型类，零 Spring 注解、零引擎依赖，切换底层引擎（JobRunr / Quartz）无需修改业务代码。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-scheduler-api</artifactId>
</dependency>
```

## API 概览

| 类型 | 职责 |
|------|------|
| `SchedulerTemplate` | 业务门面：`register` / `delete` / `trigger` / `updateCron` / `getStatus` |
| `SchedulerProcessor` | SPI：业务实现 `taskName()` + `process(SchedulerContext)`，按任务名分派 |
| `SchedulerProcessorRegistry` | 按 `taskName` 解析处理器，binder 共用，保证解析契约一致 |
| `ScheduleRequest` | 注册入参：`taskName`、`cron`、`args`、`zoneId` |
| `SchedulerContext` | 单次执行的负载：`taskName` + `args` |
| `SchedulerStatus` | 生命周期状态：`SCHEDULED` / `PAUSED` |

## 约束与语义

- `taskName` 全局唯一；重复 `register` 按 `taskName` 幂等（更新 cron 与参数，不产生重复任务）。
- `SchedulerProcessor.process` 抛异常视为本次执行失败，由底层引擎的重试策略处理。
- 一次性/可重试任务请使用 retrytask 组件，本组件只管理周期性（cron）任务。

## 许可证

Apache License 2.0 (Apache-2.0)

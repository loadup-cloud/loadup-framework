# LoadUp Scheduler Binder - JobRunr

基于 JobRunr 9.0.0 官方 `jobrunr-spring-boot-4-starter` 的 binder，实现 `SchedulerTemplate` facade。
与 retrytask binder **共用同一个 JobRunr 引擎**（存储、后台服务器、Dashboard），
适合需要分布式持久化调度、且同时使用 retrytask 的应用。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-scheduler-binder-jobrunr</artifactId>
</dependency>
```

## 配置

```yaml
jobrunr:
  background-job-server:
    enabled: true
    poll-interval-in-seconds: 15
    worker-count: 4
  dashboard:
    enabled: true
    username: admin
    password: change-me
```

底层存储自动使用应用已有的 `DataSource`（JobRunr 自建表与迁移）。cron 使用标准 6/5 段格式，
如 `*/5 * * * * *`；两次执行间隔不得小于 `poll-interval-in-seconds`（最小 5 秒）。

## 行为说明

- 每个任务以 `taskName` 作为 recurring job id，重复注册/更新即覆盖，天然幂等。
- `register` / `updateCron` / `delete` 会清理该任务的待执行实例，保证新 cron 立即生效、
  删除后不会残留孤儿执行。
- 执行通过 `SchedulerJobRequestHandler` 分发到 `SchedulerProcessor`；处理器异常走 JobRunr
  重试策略。

## 版本升级

scheduler 与 retrytask 必须使用同一 BOM 管理的 JobRunr 版本。升级 8.x 的消费应用应按[官方 v9 迁移指南](https://www.jobrunr.io/en/guides/migration/v9/)验证已有任务、存储迁移及应用自定义扩展；本 binder 不使用 v9 删除的 `useMicroMeter` 或 `withDetails` API。

## 许可证

Apache License 2.0 (Apache-2.0)

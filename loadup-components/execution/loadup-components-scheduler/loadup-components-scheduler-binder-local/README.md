# LoadUp Scheduler Binder Local

部署依赖 `io.github.loadup-cloud:loadup-components-scheduler-binder-local`，通过 `loadup.scheduler.binder-type: local`
使用应用进程内的调度器。

```yaml
loadup:
  scheduler:
    binder-type: local
    tasks:
      - name: transfer.scan
        cron: "0/30 * * * * *"
```

| 能力     | 契约                                           |
|----------|------------------------------------------------|
| 定时触发 | 每个配置项独立计算 Cron，使用 JVM 时区         |
| 执行线程 | 同一应用实例内串行执行，长任务可能推迟其他任务 |
| 生命周期 | 应用关闭时取消未来触发，已开始的任务可完成     |
| 多副本   | 每个实例触发自身的任务，业务负责记录领取与幂等 |

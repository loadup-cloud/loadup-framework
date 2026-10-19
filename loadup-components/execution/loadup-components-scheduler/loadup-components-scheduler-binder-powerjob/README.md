# LoadUp Scheduler Binder PowerJob

引入 `io.github.loadup-cloud:loadup-components-scheduler-binder-powerjob`，它传递依赖 PowerJob 薄集成组件。

```yaml
loadup:
  scheduler:
    binder-type: powerjob
powerjob:
  worker:
    enabled: true
    app-name: merchant-app
    server-address: powerjob-server:7700
    protocol: HTTP
    port: 27777
```

先部署 Server 并创建应用。在管理台创建 BUILT_IN / STANDALONE / CRON 任务：processorInfo=`loadupSchedulerProcessor`，jobParams=`delayedtask.scan`，cron=`0 * * * * ?`，建议 maxInstanceNum=1、instanceRetryNum=0、taskRetryNum=0。Transfer 另建任务，jobParams=`transfer.scan`。业务重试由各业务组件管理，避免平台重试与业务重试叠加。

不使用 PowerJob 时不要引入此 binder；若主动保留两个 binder 依赖，必须额外关闭未使用的原生 Worker/Client，binder 选择不会覆盖 PowerJob 官方配置。

## 能力矩阵

| 能力 | 支持 |
|---|---|
| 调度接入 | 已实现，边界见上文 |
| 业务数据持久化 | 由业务组件负责 |

完整接入契约见上级 [Scheduler](../README.md)。

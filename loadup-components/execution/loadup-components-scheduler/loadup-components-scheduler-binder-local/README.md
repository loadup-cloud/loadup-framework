# LoadUp Scheduler Binder Local

引入 `io.github.loadup-cloud:loadup-components-scheduler-binder-local`。

```yaml
loadup:
  scheduler:
    binder-type: local
    local:
      interval: PT1M
      initial-delay: PT1M
```

binder-type 缺省为 local。使用 Spring `@Scheduled` fixedDelay，首次默认等待一分钟，每轮顺序触发全部扫描器。无需 PowerJob
Server；多 Pod 场景所有 Pod 均会扫描，数据库领取负责互斥。

## 能力矩阵

| 能力           | 支持               |
|----------------|--------------------|
| 调度接入       | 已实现，边界见上文 |
| 业务数据持久化 | 由业务组件负责     |

完整接入契约见上级 [Scheduler](../README.md)。

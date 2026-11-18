# LoadUp Scheduler

业务实现 `ScheduledTask`，应用用任务名匹配处理器，用 Cron 定义执行频率。Local 和 PowerJob 共用同一套任务配置。

| 能力 | 契约 |
| ---- | ---- |
| Local | 每个应用实例按 Cron 触发配置的任务 |
| PowerJob | 启动时同步任务定义，Server 按 Cron 调度，Worker 按任务名回调 |
| 自定义平台 | 独立 binder 实现 `SchedulerProvider` |
| 重叠执行 | 同一 JVM 的同名任务只运行一个，集群级幂等由业务负责 |

## 接入

业务模块依赖 `io.github.loadup-cloud:loadup-components-scheduler-api`，版本由 BOM 管理。注册一个有稳定名称的 Spring Bean。

```java
@Component
public class OrderScanner implements ScheduledTask {
    public String name() { return "order.scan"; }
    public void execute() { /* Call the application service. */ }
}
```

部署时将需要的 binder 加入运行依赖。可以同时安装 Local 和 PowerJob binder，然后通过 `binder-type` 切换。

```yaml
loadup:
  scheduler:
    binder-type: local
    tasks:
      - name: order.scan
        cron: "0 */5 * * * *"
```

Cron 包含秒、分、时、日、月和星期六个字段。星期使用 `MON` 至 `SUN`，支持列表和范围。日和星期最多限定一个字段，另一个用 `*` 或 `?`。
Local 使用应用的 JVM 时区，PowerJob 使用 Server 的 JVM 时区。切换后端时保持两者一致。

任务名以字母开头，最长 64 字符，可包含字母、数字、下划线、点和连字符。配置里列出的任务必须有同名 Bean，名称重复、Cron 非法或选定的 binder 缺失都会导致启动失败。未列出的 Bean 可通过 `SchedulerTemplate.trigger(name)` 手动执行。仅引入 API 可用于提供任务 Bean。

现有应用从 `loadup.scheduler.local.interval` 和 `initial-delay` 迁移时，需要为每个任务填写 Cron。Cron 按日历时间触发。已手工创建 PowerJob 任务的部署按 binder 文档迁移。持久化 Task 默认本地轮询，也可配置 `loadup.task.trigger: scheduler` 使用同一个 Scheduler。Task 自动提供每分钟一次的 `loadup-task-scan` 默认计划；同名显式 Cron 优先。

- [Local binder](loadup-components-scheduler-binder-local/README.md)
- [PowerJob binder](loadup-components-scheduler-binder-powerjob/README.md)

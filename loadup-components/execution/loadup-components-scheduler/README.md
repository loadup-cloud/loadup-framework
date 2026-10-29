# LoadUp Scheduler

为数据库扫描器和普通周期任务提供可切换的调度唤醒入口。业务实现 `ScheduledTask`；应用选择一个 binder。Scheduler
不存储订单截止时间，也不为每个业务订单注册引擎任务。

## 能力矩阵

| 能力                | 状态     | 边界                                  |
|---------------------|----------|---------------------------------------|
| Spring `@Scheduled` | 已实现   | 默认本地 binder，每个应用实例均可触发 |
| PowerJob            | 已实现   | 独立 Server，原生任务定义和运维       |
| XXL-JOB / JobRunr   | 待实现   | 通过 SchedulerProvider 扩展           |
| 单机重叠抑制        | 已实现   | 同名任务同一 JVM 同时只执行一次       |
| 集群业务互斥        | 业务负责 | DelayedTask 使用数据库条件领取        |

## 接入

引入 BOM 后，业务代码依赖 `loadup-components-scheduler-api`，应用额外引入 `loadup-components-scheduler-binder-local` 或
`loadup-components-scheduler-binder-powerjob`（groupId 均为 `io.github.loadup-cloud`）。两个 binder 的部署配置见各自
README。

```java
@Component
public class OrderScanner implements ScheduledTask {
    public String name() { return "order.scan"; }
    public void execute() { /* Call the application service. */ }
}
```

任务名为 1–64 位字母、数字、下划线、点或连字符，首字符为字母，禁止重复。异常由 binder 各自处理；本地 binder
记录安全日志并继续其他任务，PowerJob binder 交给原生引擎判断实例失败。只引入 API 不启动调度；不存在 Provider 时不创建
SchedulerTemplate，同时出现多个 Provider 则启动失败。

DelayedTask 注册 `delayedtask.scan`，Transfer 注册 `transfer.scan`。按扫描频率及单次处理时长设置延迟 SLA；分钟轮询不是精确秒级定时器。

# LoadUp DelayedTask

持久化业务截止时间任务，例如创建订单后 30 天仍未支付则关闭。保存业务 ID、任务类型和执行时间，由 Scheduler
每分钟唤醒扫描器；延迟跨重启保留，不为每个订单创建 PowerJob 作业。

## 引入与数据库

引入 BOM 后添加 `io.github.loadup-cloud:loadup-components-delayedtask`，并在执行应用选择一个 Scheduler
binder。注册任务与业务记录必须使用同一个 DataSource 和事务管理器。Flyway 需包含本组件 PostgreSQL
`V20261019000001__create_delayed_task.sql` 和 database Sequence 迁移。业务主键使用统一 IdGenerator，持久化使用 Spring
JDBC。

## 注册与处理

```java
@Transactional
public void createOrder(Order order) {
    orders.save(order);
    delayedTasks.register("order.close", order.id(), LocalDateTime.now(clock).plusDays(30));
}
```

```java
@Component
public class CloseOrderHandler implements DelayedTaskHandler {
    private final OrderFacade orders;
    public CloseOrderHandler(OrderFacade orders) { this.orders = orders; }
    public String taskType() { return "order.close"; }
    public void handle(DelayedTask task) {
        orders.closeIfUnpaidAndExpired(task.bizId());
    }
}
```

Handler 使用 Spring Facade 的独立业务事务重新读取订单，校验当前状态和真实业务截止时间；关闭操作必须幂等。没有需要执行的业务时正常返回即成功。业务数据不放到任务
payload，执行时按 bizId 读取。

`register` 要求已有事务，否则返回 TRANSACTION_REQUIRED。唯一键为 tenantId + taskType + bizId，相同截止时间重复注册返回已有记录，截止时间不同返回
CONFLICT（改期应显式调用 reschedule）。数据库微秒精度归一化。并发重复注册可能触发数据库死锁，由调用方重试整个业务事务；组件不隐藏事务失败。

## 管理 API

| 方法                              | 语义                                                                  |
|-----------------------------------|-----------------------------------------------------------------------|
| register(type, bizId, executeAt)  | 与业务同事务注册                                                      |
| find(type, bizId)                 | 按可信租户查询                                                        |
| cancel(type, bizId)               | 取消待执行、执行中或失败记录，不能撤销已发生副作用                    |
| reschedule(type, bizId, deadline) | PENDING / FAILED / CANCELLED 改期，清零次数；RUNNING / SUCCEEDED 拒绝 |
| retryFailed(type, bizId)          | 仅 FAILED 立即重排，保留原始业务截止时间                              |

## 配置

```yaml
loadup:
  delayedtask:
    enabled: true
    batch-size: 100
    max-attempts: 5
    lease: PT5M
    retry-delay: PT1M
  scheduler:
    binder-type: local
    local:
      interval: PT1M
```

batch-size 与 max-attempts 为 1–1000；lease / retry-delay 为 1 秒–30 天。日期按系统本地时区，所有节点和数据库会话需保持一致。租约无自动续期，必须大于
Handler 的最大处理时间；长任务需调整租约。每轮最多执行 batch-size 项，顺序领取和执行，不提前领取整批。

多租户使用 database 的可信租户配置和 ScopedValue。扫描跨租户，执行时只绑定记录中的 tenantId，不继承调度线程身份。只引入 API
而无 binder 的应用可注册任务，但不会执行，必须由其他共享数据库的执行应用扫描。

## 能力矩阵

| 能力                                   | 状态 / 边界                                               |
|----------------------------------------|-----------------------------------------------------------|
| 长期延时、事务注册、回滚一致性         | 已实现                                                    |
| 到期领取、失败固定间隔重试、次数上限   | 已实现                                                    |
| 租约过期恢复、旧结果隔离               | 已实现；claim token + 有效租约条件回写                    |
| 多实例互斥领取                         | 已实现；短事务内 PostgreSQL `FOR UPDATE SKIP LOCKED` 领取 |
| 取消、改期、手动重试、租户隔离         | 已实现                                                    |
| 至少一次                               | 已实现；业务必须幂等，不保证恰好一次                      |
| 租约续期、管理页面、历史清理、积压告警 | 尚未实现                                                  |

执行建立 `loadup.delayedtask.execute` Observation，低基数标签 task.type（注册 Handler 类型或 unregistered）及 outcome；不记录业务
payload。业务需自行配置失败与积压监控，成功记录保留用于幂等，请勿直接逻辑删除后重新注册同一业务键。

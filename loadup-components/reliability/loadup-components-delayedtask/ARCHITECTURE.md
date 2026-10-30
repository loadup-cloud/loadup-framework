# DelayedTask 架构

## 边界

DelayedTask 管理业务截止时间、持久化状态和失败重试；Scheduler 只负责周期唤醒。组件依赖 Scheduler API，不依赖 PowerJob
Worker / Server。普通业务调用使用内部 Template，无 HTTP 管理接口。

```text
Business transaction → business data + delayed_task
Scheduler binder → delayedtask.scan → recover exhausted → claim → Handler → finish
```

## 数据模型

业务唯一键 tenant_id/task_type/biz_id；execute_at 是业务截止时间，next_execute_at 是本次尝试的最早执行时间。attempts
记录实际领取次数，max_attempts 在注册时快照。claim_token 和 lease_until 标识当前执行权，last_error 仅保存固定安全描述。Spring
JDBC 显式维护标准字段，MapStruct 负责数据表示转换；当前数据对象仍继承共享 BaseDO，待公共基础设施迁移时拆除其 Flex 耦合。

## 状态与事务

PENDING 到期且次数未耗尽可领取为 RUNNING，增加 attempts、生成随机 token 并设置租约。Handler 成功转 SUCCEEDED；失败未耗尽回
PENDING 并延后 next_execute_at，否则 FAILED。最后一次执行宕机则到期清理为 FAILED。过期但尚有次数的 RUNNING 可重新领取。取消转
CANCELLED；改期清零次数并返回 PENDING。

注册不启动额外事务，强制参与同数据源的可写业务事务；使用指定业务键的 `ON CONFLICT` 处理重复登记。扫描器挂起外部事务；过期清理、单条领取和结果确认分别使用
REQUIRES_NEW，Handler 在领取提交后执行。领取以 READ_COMMITTED 和 `FOR UPDATE SKIP LOCKED` 选择一条候选，并在同一短事务更新次数、token
和租约。业务处理没有持有领取事务。

回写必须同时满足 RUNNING、相同 token、租约尚未过期。取消和改期清空 token，使旧执行无法覆盖新状态。这只能隔离任务状态，不会阻止失效
Worker 的外部副作用；超时后并发重放仍依赖业务幂等或业务层 fencing。不存在跨数据库分布式事务保证。

## 扩展和容量

Handler 以固定 taskType
注册，未知类型按失败策略重试。每轮有界、逐条领取，默认一分钟轮询；实际延迟还包含排队与处理耗时。当前没有续租、并行处理、归档或自动容量告警；大积压时需评估索引、批量候选读取开销与处理吞吐，不能用单测替代容量验收。

## 验证

PostgreSQL 集成测试源码覆盖事务回滚、幂等和冲突、租户隔离、期限、失败重试、取消改期、租约围栏和并发领取；迁移后的构建与测试尚未运行。多
Pod 故障演练、时钟漂移和规模压测仍待部署验收。

## 构造与代理安全

Dispatcher 保持可代理，Handler 注册表在构造器委托参数中完成静态校验，再进入仅赋值的私有构造器；校验失败发生于 Object
初始化之前，避免终结器访问半初始化实例。保留重复/空类型拒绝，且不通过 final 类破坏 execute 的 NOT_SUPPORTED
事务代理。无候选时领取返回空结果。

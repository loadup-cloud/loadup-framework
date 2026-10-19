# DelayedTask 架构

## 边界

DelayedTask 管理业务截止时间、持久化状态和失败重试；Scheduler 只负责周期唤醒。组件依赖 Scheduler API，不依赖 PowerJob Worker / Server。普通业务调用使用内部 Template，无 HTTP 管理接口。

```text
Business transaction → business data + delayed_task
Scheduler binder → delayedtask.scan → recover exhausted → claim → Handler → finish
```

## 数据模型

业务唯一键 tenant_id/task_type/biz_id；execute_at 是业务截止时间，next_execute_at 是本次尝试的最早执行时间。attempts 记录实际领取次数，max_attempts 在注册时快照。claim_token 和 lease_until 标识当前执行权，last_error 仅保存固定安全描述。继承 BaseDO 标准字段，Mapper / Tables 由 database processor 生成，MapStruct 负责数据表示转换。

## 状态与事务

PENDING 到期且次数未耗尽可领取为 RUNNING，增加 attempts、生成随机 token 并设置租约。Handler 成功转 SUCCEEDED；失败未耗尽回 PENDING 并延后 next_execute_at，否则 FAILED。最后一次执行宕机则到期清理为 FAILED。过期但尚有次数的 RUNNING 可重新领取。取消转 CANCELLED；改期清零次数并返回 PENDING。

注册不启动额外事务，强制参与业务事务。扫描器挂起外部事务；过期清理、单条领取和结果确认分别使用 REQUIRES_NEW，Handler 在领取提交后执行。领取以 READ_COMMITTED 读取有界候选，再对状态、次数、时间与旧 token 做条件更新；竞争失败继续候选，避免范围锁领取引发不必要的锁升级。业务处理没有持有领取事务。

回写必须同时满足 RUNNING、相同 token、租约尚未过期。取消和改期清空 token，使旧执行无法覆盖新状态。这只能隔离任务状态，不会阻止失效 Worker 的外部副作用；超时后并发重放仍依赖业务幂等或业务层 fencing。不存在跨数据库分布式事务保证。

## 扩展和容量

Handler 以固定 taskType 注册，未知类型按失败策略重试。每轮有界、逐条领取，默认一分钟轮询；实际延迟还包含排队与处理耗时。当前没有续租、并行处理、归档或自动容量告警；大积压时需评估索引、批量候选读取开销与处理吞吐，不能用单测替代容量验收。

## 验证

真实 MySQL 测试覆盖事务回滚、幂等和冲突、跨租户隔离、未来 30 天不提前执行、到期执行、失败重试与次数上限、取消改期、过期领取及旧 token 回写拒绝、并发唯一领取。单测覆盖配置边界、非法输入、中断语义、注册冲突。多 Pod 故障演练、时钟漂移和规模压测仍待部署验收。

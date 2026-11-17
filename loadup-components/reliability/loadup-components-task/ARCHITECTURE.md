# Task 架构

单一 jar、单一 TaskTemplate/TaskHandler 接口、单一 loadup_task 主表。消息载荷与执行时间是任务属性，不设旧组件模式或双状态机。

业务事务：业务写入 + submit，同步提交或回滚。
领取事务：恢复耗尽租约，SKIP LOCKED 领取一条记录，生成新 token、递增 attempts、设置租约，提交。
事务外：调用 Handler。
确认事务：按 id、RUNNING、token 和有效租约条件完成或延后。

TaskDispatcher 挂起调用方事务；领取、确认与人工操作使用独立短事务。数据库时间决定就绪、租约与重试，避免节点时钟差。逐条领取执行，不预领整批。

PENDING → RUNNING → SUCCEEDED；异常返回 PENDING 或耗尽进入 FAILED。过期 RUNNING 可重领或进入 FAILED。取消清空 token；改期和人工重试清零 attempts。旧执行者不能回写新状态，但可能仍产生外部副作用，业务幂等依然必要。

TaskSubmission 显式选择 dedupKey；businessId 只做关联。同一个业务可有多个目标、多代任务。任务 payload、version、type 和业务 ID 在登记后保持不变。去重重用同一记录，不能靠逻辑删除重新消费。

Inbox 是消费端同事务去重能力，不参与任务执行状态机。外部副作用不能因为使用 Inbox 就宣称恰好一次。

TaskWorker 是唯一默认唤醒器；关闭 worker 后由独立工作应用调用 dispatcher。默认串行执行，有界批次，多实例扩展；慢任务与关键投递有容量隔离需求时可分别部署执行节点并设置 task-type 限定领取类型；当前不提供自动优先级或抢占。

任务配置与操作接口不依赖 Scheduler、PowerJob、ORM 或消息中间件。只提供 PostgreSQL JDBC 实现，不预建存储 binder。测试源码与静态审阅不代表实际数据库故障恢复验收。

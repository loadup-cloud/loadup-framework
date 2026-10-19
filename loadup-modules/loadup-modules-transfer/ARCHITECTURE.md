# Transfer 架构

## 分层与职责

TransferTaskService 管理业务任务、文件引用、进度和结果；TransferTaskProcessor 实现原生 BasicProcessor，只调用 service 的有界队列扫描。文件操作复用 FileResourceFacade，持久化通过 TransferGateway 和 MyBatis-Flex。

## 事务与执行

提交在同一业务数据库事务内建立源文件引用并插入 QUEUED 记录；显式重试在事务内 CAS 重排队并保护输入引用。不调用远程调度 API，消除“业务库已提交、远程任务未注册”的双写窗口。文件资源表与任务表必须使用同一事务 DataSource。

PowerJob 按配置触发 transferTaskProcessor，读取跨租户有界候选 ID，再根据数据库中可信租户绑定 TenantUtil，使用 QUEUED → RUNNING 条件更新抢占。RUNNING 不属于可领取条件，重复投递不会并发再次执行同一任务。扫描单个失败不阻断后续项，失败项保留 FAILED 等待人工重试，扫描返回失败供平台观测。

输入输出依旧有界；业务处理器按 taskId 保证副作用幂等。文件字节存储与业务数据库不能原子提交，输出清理和源文件解引用沿用既有补偿；失败状态在清理后发布，避免人工重试与前一次清理竞争。

Worker 崩溃后 RUNNING 任务仍需要租约/对账和显式恢复；未以放开 RUNNING 抢占换取自动重放。业务失败、PowerJob 扫描实例失败、管理 Client 请求超时是不同状态，不做模糊翻译。

## 验证

TransferDispatchTest 验证有界扫描、失败隔离和抢占失败跳过；TransferClaimIT 用真实 MySQL 和两个并发虚拟线程验证仅一个领取成功，以及 FAILED 重排队和 SUCCEEDED 不可领取。运行状态见 ROADMAP。

## 迁移版本顺序

真实空库验收发现 file/transfer 的 BaseDO 补字段脚本原先使用 12 位版本号，数值早于 14 位建表版本。已改为 V20261010000101 和 V20261010000103，确保先建表后补字段。不对已部署库自动修复或重写 flyway_schema_history；已执行旧版迁移的消费工程升级前需单独核对历史。

2026-10-19 定向验收通过：7 项单测、1 项真实 MySQL IT。并发条件更新仅一个成功，RUNNING/SUCCEEDED 不再领取，FAILED 仅显式重排队；QUEUED 的重复重试不重新建立源文件引用。

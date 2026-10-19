# Scheduler Binder PowerJob 架构

PowerJobSchedulerProvider 同时实现 SchedulerProvider 与官方 BasicProcessor，Bean 名固定 loadupSchedulerProcessor。读取 jobParams 作为本地注册名，调用 SchedulerTemplate.trigger，异常传回原生引擎。Worker 的上下文隔离和观测由本 binder 内的切面提供，业务身份仍从持久化记录重建。Server 的作业定义由运维管理，不与业务截止时间形成一对一映射。

总体职责及扩展契约见上级 [架构](../ARCHITECTURE.md)。

## 职责

```text
Consumer application (Boot 4 / Java 25)
 ├─ Business Facade ← Spring BasicProcessor ← Official Worker Starter
 ├─ Optional native PowerJobClient ── management HTTP ─┐
 └─ LoadUp process aspect: ScopedValue + Observation  │
                                                    ▼
                         PowerJob Server (separate JVM / database)
```

实现归属 Scheduler 的 PowerJob binder。官方 Worker 管理调度通信、线程、Processor 查找与执行、心跳和本地存储；Server 管理定义、实例、分发、重试、日志、工作流和告警。LoadUp 不复制这些模型，不维护业务名到引擎 ID 的映射，不重写调度循环。

## 装配

官方 Starter 的自动配置保留原样；LoadUpPowerJobAutoConfiguration 额外绑定 `powerjob.client.*`，需要时创建 Closeable Client，并注册 process 切面。Client 的 appName/serverAddress 可继承 Worker 配置，密码始终显式提供；Client-only 应用可以禁用 Worker。自定义 Client Bean 优先；失败的客户端配置以 POWERJOB 错误枚举报告，不在错误中输出秘密。

组件不隐式启动或创建 Server，不依赖 Server jar、JPA 或业务数据库。运行参数保持官方命名；PowerJobSchedulerAutoConfiguration 根据 loadup.scheduler.binder-type=powerjob 注册扫描入口，LoadUpPowerJobAutoConfiguration 管理原生 Client 和 process 切面。两者均由同一个 binder jar 提供，Worker 配置保持原生。

## 执行作用域

PowerJobProcessorAspect 拦截 Spring BasicProcessor.process，绑定全新不可变 ExecutionContext。标识键由 PowerJobExecutionContext 声明；不继承执行线程残留的业务上下文，不读取任务任意字段当作身份。业务服务根据数据库中的可信记录恢复租户。普通嵌套调用结束和异常退出都恢复上层 ScopedValue。

每次执行建立独立 `loadup.powerjob.process` Observation，停止时标记 success/failure。失败返回值同样计为失败；null 违反原生结果契约，转成规范错误。业务异常原样传回 PowerJob 决定重试；观测错误仅保留固定错误码，避免异常链携带 payload。不同实例标识不进入指标标签，跨进程 Trace 不自动串联。

## 业务任务迁移

旧引擎补丁和 RetryTask 模型已移除。新 Scheduler 仅抽象扫描器唤醒，DelayedTask 独立管理业务截止时间。Gotone 保持发送能力，告警集成由官方 PowerJob 与消费方明确接入。

Transfer 是实际消费方：提交与源文件引用在业务数据库事务中完成，Scheduler 通过本地 @Scheduled 或 PowerJob binder 领取 QUEUED 任务，QUEUED → RUNNING 条件更新抢占；同一个运行中的任务不会被第二个 Worker 再次领取。输出和状态仍由 Transfer 管理，原生任务实例代表一次队列扫描，不代表单个 Transfer 业务任务。

## 验证边界

binder 内单测验证配置校验、Client 覆盖/关闭、结果/异常观测、ScopedValue 与 Observation 恢复及 ScheduledTask 分派。测试不启动独立 Server，不下载 Server jar；不再维护自定义 Server 启动器、Server 专用测试模块或启动 profile。此前真实 Server 运行记录不代表当前仍保留自动化平台验收。

平台通信、动态改期、实例重试、Server 重启恢复、多节点接管及容量由独立部署环境验收。Transfer / DelayedTask 的真实数据库测试仍验证其各自的持久化队列语义，不依赖 PowerJob Server。

# Scheduler 架构

## 职责与依赖

```text
Local @Scheduled ─┐
                  ├─ SchedulerProvider → SchedulerTemplate → ScheduledTask
PowerJob Worker ──┘                                      ├─ DelayedTaskDispatcher
                                                        └─ TransferTaskProcessor
```

采用单后端选择模式。API 只表达任务名、执行与 Provider 生命周期；PowerJob 的工作流、分片、实例管理等原生功能仍使用其原生 SDK，不翻译成框架通用模型。组件不维护远程 jobId 映射，不自动创建/删除平台任务。

## 装配与执行

binder 根据 `loadup.scheduler.binder-type` 创建 Provider。API 自动配置在 binder 之后检查唯一 Provider，收集 ScheduledTask Bean，创建 SmartLifecycle Template。启动绑定 Provider；停止解除绑定。每个任务使用独立 AtomicBoolean 抑制同 JVM 重叠，finally 释放，失败不阻塞下一轮。

本地 fixedDelay 在一轮完成后等待，所有扫描器串行执行；多个实例可同时唤醒。PowerJob 根据原生 jobParams 指定一个任务名。此处不提供集群锁或持久化幂等：业务记录的条件更新、租约和处理器语义属于业务组件。

## 扩展与验证

新增 binder 实现 SchedulerProvider 并独立装配，不修改业务扫描器。XXL-JOB / JobRunr 尚未实现。测试覆盖注册校验、生命周期、异常释放、重叠抑制、实际 @Scheduled 唤醒与 PowerJob Processor 调用同一入口（不启动 Server）。

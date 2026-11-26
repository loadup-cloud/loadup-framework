# 应用可靠性组件接入

消费应用导入 [Framework BOM](../../loadup-framework-bom/README.md)，再按需求选择下表中的工件。此目录的聚合 POM 用于组织 Maven 子模块。

| 组件 | 适用场景 | 接入要点 |
| --- | --- | --- |
| [持久化任务](loadup-components-task/README.md) | 事务内提交任务、租约领取和故障恢复 | 使用宿主数据源，应用队列迁移并注册 Task Handler。 |
| [异步结果](loadup-components-async-result/README.md) | 关联等待方与完成方 | 根据进程范围选择 Local 或 Redis 实现。 |
| [分布式锁](loadup-components-lock/README.md) | 对共享资源进行互斥 | 配置后端与命名空间，明确锁持有时间。 |
| [幂等声明](loadup-components-globalunique/README.md) | 对业务键进行事务内去重 | 部署唯一键表，在业务事务内调用 claim。 |
| [调用容错](loadup-components-resilience4j/README.md) | 熔断、限流和其他调用保护 | 按调用的失败语义配置容错策略。 |

[分类架构](ARCHITECTURE.md) · [全部组件](../README.md)

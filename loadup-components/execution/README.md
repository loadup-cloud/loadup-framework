# 调度与执行组件接入

消费应用导入 [Framework BOM](../../loadup-framework-bom/README.md)，再按需求选择下表中的工件。此目录的聚合 POM 用于组织 Maven 子模块。

| 组件 | 适用场景 | 接入要点 |
| --- | --- | --- |
| [任务调度](loadup-components-scheduler/README.md) | 按 Cron 唤醒已注册的扫描器 | 选择 Local 或 PowerJob binder，配置任务名、Cron 和时区。 |
| [执行流水线](loadup-components-pipeline/README.md) | 组合有顺序的处理步骤 | 按流水线契约定义步骤、输入和上下文。 |

[分类架构](ARCHITECTURE.md) · [全部组件](../README.md)

# 调度与执行组件架构

## 职责与依赖

Scheduler 负责触发，扫描器负责发现待处理记录。持久化任务的领取、租约和重试由业务队列或 Task 组件决定。Pipeline 在一次调用内组织处理步骤，触发来源由调用方决定。

宿主直接依赖所需的组件工件。目录对应 Maven reactor 的聚合层，运行时依赖由各组件 POM 声明。

## 组件设计

- [任务调度](loadup-components-scheduler/ARCHITECTURE.md)
- [执行流水线](loadup-components-pipeline/ARCHITECTURE.md)

[接入说明](README.md)

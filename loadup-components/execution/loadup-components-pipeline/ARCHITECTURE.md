# Pipeline 架构

## 职责

Pipeline 描述并执行有序业务阶段。业务定义通过 Fluent `PipelineBuilder` 构造 `PipelineSpec`，再由 `PipelineExecutor` 解析并调用阶段。
Pipeline 决定步骤顺序；Extension 可在阶段内部选择实现。

组件提供 Spring 执行能力，不绑定厂商 SDK。业务事务仅由显式事务块控制。

## Stage 与上下文约定

常见阶段顺序为 verify → prepare → process → assemble。Builder 允许的定义可以省略开头的 verify/prepare。`PipelineContext` 在阶段间传递
request、model、response 和命名属性，Stage 通过上下文访问共享状态。

Stage 可由 Spring Bean 实现，也可使用内联 Lambda。Bean Stage 从 Spring 上下文解析；Lambda 存入 spec，由执行器适配。Builder 返回类型限制
合法阶段顺序。

`IControlStage` 可提前结束后续阶段；`IFinallyStage` 在清理流程中执行。异常按精确类型、可赋值父类型、可选 bottom handler 路由；标为
overflow 的异常绕过 handler。没有匹配项或 bottom handler 时，异常继续向外传播。

## 事务边界

`startTx(initializer)` 与 `endTx()` 标记一段连续事务阶段。执行器使用 Spring 提供的 `PlatformTransactionManager` 执行该区间；默认 initializer
使用 `PROPAGATION_REQUIRED`。事务边界由定义显式声明，区间外的 Stage 不属于该事务。

## 扩展边界

组件负责阶段顺序、上下文、分派和异常路由，不负责业务状态或实现选择。仅当应用需运行时选择 Pipeline 定义时使用 `IPipelineSelector`；阶段内部
按场景选择实现时使用 Extension。持久化和外部副作用规则由业务模块负责。

组件依赖 Spring 与 LoadUp logging；Extension 集成由 Stage 实现按需提供，Pipeline 不要求引入 Extension 模块。

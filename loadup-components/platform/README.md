# 平台基础组件接入

消费应用导入 [Framework BOM](../../loadup-framework-bom/README.md)，再按需求选择下表中的工件。此目录的聚合 POM 用于组织 Maven 子模块。

| 组件 | 适用场景 | 接入要点 |
| --- | --- | --- |
| [配置中心](loadup-components-configcenter/README.md) | 配置读取、订阅及后端支持的发布 | 选择 Nacos、Apollo 或 XXL-Conf binder，按能力矩阵选择写入方式。 |
| [运行时装配](loadup-components-runtime/README.md) | 共享 Clock、JSON 和日志等基础设施 | 在需要 Spring Boot 自动装配的宿主引入。 |
| [应用观测](loadup-components-observability/README.md) | 指标、追踪和执行上下文传播 | 复用 Boot 管理的 Registry，按任务执行器配置上下文装饰器。 |
| [业务扩展](loadup-components-extension/README.md) | 按业务场景选择扩展实现 | 注册扩展实现并按调用场景路由。 |
| [测试容器](loadup-components-testcontainers/README.md) | 为测试装配数据库等容器 | 在测试作用域引入，按测试选用容器。 |

[分类架构](ARCHITECTURE.md) · [全部组件](../README.md)

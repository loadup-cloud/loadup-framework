# TestContainers 架构

## 职责与启动流程

模块在测试上下文启动共享 Testcontainers，并将连接属性注入 Spring 测试环境；不提供应用客户端，也不管理测试数据生命周期。

支持两种启动入口：

1. `@EnableTestContainers` 声明容器类型，由测试执行监听器启动容器并收集连接属性，再由测试 initializer 注入。
2. `TestContainersPropertyInitializer` 绑定 `loadup.testcontainers`，仅启动全局开关和单类型开关都启用的已配置容器，然后注入连接属性。

类型开关关闭时不注入该容器的连接属性，真实服务地址由应用配置提供。配置模式支持 MySQL、PostgreSQL、MongoDB、Redis、Kafka、
Elasticsearch 与 LocalStack。

## 不变量

- 共享实例的生命周期为 JVM 级别。容器成功启动后，后续调用不改变实例配置；需验证不同配置时使用独立测试进程。
- 注解的 `reuse` 默认开启；Testcontainers 复用还需在用户配置中启用 `testcontainers.reuse.enable=true`。
- LocalStack 遵守与其他共享容器一致的复用和生命周期设置。
- 连接密钥不得写入日志。
- 容器启动失败必须作为失败处理，不得静默切换到真实服务。
- SDK mock 不能证明真实容器成功启动；单元测试与容器集成测试提供不同范围的证据。

## 扩展边界

新增容器类型时，需同步扩展类型选择、配置模型、共享容器启动、连接属性注入和生命周期处理；注入属性应符合对应客户端的 Spring 配置。
不要将生产服务配置或业务模块专用测试夹具放入此模块。

## 测试入口与待验收

配置选择、属性注入、启动失败重试和 SDK 调用有单元测试源码；Elasticsearch 写入、读取和删除索引有容器集成测试源码。单元测试与集成测试
分别由 Surefire 与 Failsafe 测试入口执行。仅看到测试源码或单元测试结果，不能证明 Docker/镜像可用；实际容器验收需在具备 Docker 的环境运行。

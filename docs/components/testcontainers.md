---
id: components-testcontainers
title: LoadUp TestContainers Component
---

> 来源: `loadup-components/loadup-components-testcontainers/README.md`

# LoadUp TestContainers Component

提供企业级的 TestContainers 支持，用于在开发/CI 中快速切换真实容器与本地模拟环境。支持 MySQL/Postgres/MongoDB/Redis/Kafka/Elasticsearch/LocalStack 等常见容器。

关键点：
- 支持三种模式：TestContainers（默认）、真实服务（CI）、混合模式
- 通过配置（profiles 或 env）控制容器启停
- 提供共享容器、初始化器、抽象测试基类，方便在模块测试中复用

快速使用：添加测试依赖并使用 `AbstractMySQLContainerTest` 等基类。

原始模块 README: `loadup-components/loadup-components-testcontainers/README.md`

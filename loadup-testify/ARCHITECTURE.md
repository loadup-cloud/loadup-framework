# Testify 架构

Testify 是测试期能力，由 core、data-engine、assert-engine、Spring Boot starter 和测试模块组成。它可以深度访问框架内部类型，但仅以测试依赖进入消费工程。

- [core](loadup-testify-core/ARCHITECTURE.md) 提供测试契约与执行基础。
- [data-engine](loadup-testify-data-engine/ARCHITECTURE.md) 负责测试数据准备。
- [assert-engine](loadup-testify-assert-engine/ARCHITECTURE.md) 负责响应断言。
- [spring-boot-starter](loadup-testify-spring-boot-starter/ARCHITECTURE.md) 提供 Spring Boot 集成。
- [test](loadup-testify-test/ARCHITECTURE.md) 验证框架行为。

有数据库交互的集成测试使用 Testcontainers 连接真实数据库；单元测试只验证不依赖数据库的逻辑。

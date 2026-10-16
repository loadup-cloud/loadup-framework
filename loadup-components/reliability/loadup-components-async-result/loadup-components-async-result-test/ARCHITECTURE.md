# Async Result Test Architecture

## 职责

提供单元测试及真实 Redis、Kafka、MySQL 集成测试，不是生产业务模块。

## 依赖与运行路径

[整体设计](../ARCHITECTURE.md) 统一定义状态、事务、过期、跨实例回传与故障语义。本模块不定义额外业务状态或响应 envelope。

单元测试不启动外部系统；*IT 由 Failsafe 执行，使用项目 Testcontainers/Testify 支持。测试模块包含本地、CI 两套 profile，三个运行时 jar 的测试集中在此模块。

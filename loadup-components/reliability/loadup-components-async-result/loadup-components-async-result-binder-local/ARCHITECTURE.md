# Async Result Local Binder Architecture

## 职责

Caffeine 保留结果，支持同进程异步回调；进程退出不保留结果。

## 依赖与运行路径

[整体设计](../ARCHITECTURE.md) 统一定义状态、事务、过期、跨实例回传与故障语义。本模块不定义额外业务状态或响应 envelope。

实现 AsyncResultProvider，Caffeine 管理 TTL，Semaphore 控制保留条数；每条记录使用短临界区决定首个终态，不通过分布式锁协调。

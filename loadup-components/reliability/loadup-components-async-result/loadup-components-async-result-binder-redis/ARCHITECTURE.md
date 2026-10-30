# Async Result Redis Binder Architecture

## 职责

复用 RedissonClient，以单键 Lua 写结果并通过实例通道提示等待方，轮询兜底通知丢失。

## 依赖与运行路径

[整体设计](../ARCHITECTURE.md) 统一定义状态、事务、过期、跨实例回传与故障语义。本模块不定义额外业务状态或响应 envelope。

实现 AsyncResultProvider，复用 RedissonClient；StringCodec 和固定 JSON 协议避免 Java 原生反序列化，Lua 保证同键状态比较及写入。Redis
Cluster 无跨键脚本。

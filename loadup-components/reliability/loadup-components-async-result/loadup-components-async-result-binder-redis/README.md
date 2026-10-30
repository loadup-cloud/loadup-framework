# LoadUp Async Result Redis Binder

复用 RedissonClient，以单键 Lua 写结果并通过实例通道提示等待方，轮询兜底通知丢失。

## 引入

坐标：`io.github.loadup-cloud:loadup-components-async-result-binder-redis`，版本由 LoadUp BOM 管理。

## 能力矩阵

| 能力       | 契约                                                                               |
|------------|------------------------------------------------------------------------------------|
| 模块职责   | 复用 RedissonClient，以单键 Lua 写结果并通过实例通道提示等待方，轮询兜底通知丢失。 |
| 配置与示例 | [组件接入手册](../README.md)                                                       |
| 架构边界   | [ARCHITECTURE.md](ARCHITECTURE.md)                                                 |

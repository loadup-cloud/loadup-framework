# Async Result API Architecture

## 职责

业务仅依赖本模块，通过 AsyncResultTemplate 使用同步/异步等待；AsyncResultProvider 是单后端 SPI。

## 依赖与运行路径

[整体设计](../ARCHITECTURE.md) 统一定义状态、事务、过期、跨实例回传与故障语义。本模块不定义额外业务状态或响应 envelope。

业务调用 Template → 注册 Pending → Provider 注册 → AsyncDispatch → Provider 结果查询/通知 → 完成 Future。共享 Micrometer
和应用 ObjectMapper 由构造器注入。

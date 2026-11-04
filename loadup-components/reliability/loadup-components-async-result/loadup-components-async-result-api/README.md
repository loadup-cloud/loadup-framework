# LoadUp Async Result API

业务仅依赖本模块，通过 AsyncResultTemplate 使用同步/异步等待；AsyncResultProvider 是单后端 SPI。

## 引入

坐标：`io.github.loadup-cloud:loadup-components-async-result-api`，版本由 LoadUp BOM 管理。

## 能力矩阵

| 能力       | 契约                                                                                             |
|------------|--------------------------------------------------------------------------------------------------|
| 模块职责   | 业务仅依赖本模块，通过 AsyncResultTemplate 使用同步/异步等待；AsyncResultProvider 是单后端 SPI。 |
| 配置与示例 | [组件接入手册](../README.md)                                                                     |
| 架构边界   | [ARCHITECTURE.md](ARCHITECTURE.md)                                                               |

Spring 装配使用 `loadup-components-runtime` 提供的应用 JSON mapper / Clock；消费者通过参数注入，不创建默认兜底实例。独立测试需显式提供所需 Bean。

# Loadup Components Retrytask Facade 架构

## 职责与边界

后台重试任务的业务契约与接口模块；实现由独立模块提供。

## 实现入口

主要入口文件：

- [`RetryTaskFacade`](src/main/java/io/github/loadup/retrytask/facade/RetryTaskFacade.java)

## 分层与调用路径

业务提交进入 facade，经 JobRunr binder 持久化与执行；通知桥接模块独立订阅失败事件。

```text
业务调用 → RetryTaskFacade → SPI/领域契约 → 运行时实现
```
接口模块固定业务侧依赖方向；实现与基础设施通过独立模块接入。

## 扩展契约

- [`RetryTaskProcessorRegistry`](src/main/java/io/github/loadup/retrytask/facade/RetryTaskProcessorRegistry.java)：由实现方或调用方按接口定义对接。
- [`RetryTaskNotifier`](src/main/java/io/github/loadup/retrytask/facade/RetryTaskNotifier.java)：由实现方或调用方按接口定义对接。
- [`RetryTaskFacade`](src/main/java/io/github/loadup/retrytask/facade/RetryTaskFacade.java)：由实现方或调用方按接口定义对接。
- [`RetryTaskProcessor`](src/main/java/io/github/loadup/retrytask/facade/RetryTaskProcessor.java)：由实现方或调用方按接口定义对接。

## 设计取舍

`RetryTaskFacade` 作为稳定入口；技术选择在运行应用完成，避免 API 层反向依赖具体后端。

集成方式与配置示例见 [README.md](README.md)。

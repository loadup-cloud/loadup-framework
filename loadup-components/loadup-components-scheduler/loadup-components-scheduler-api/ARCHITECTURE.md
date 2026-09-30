# Loadup Scheduler Api 架构

## 职责与边界

任务调度的业务契约与接口模块；实现由独立模块提供。

## 实现入口

主要入口文件：

- [`SchedulerTemplate`](src/main/java/io/github/loadup/components/scheduler/SchedulerTemplate.java)

## 分层与调用路径

`SchedulerTemplate` 接收注册与管理命令，binder 适配到调度引擎，再由 `SchedulerProcessor` 执行业务。

```text
业务调用 → SchedulerTemplate → SPI/领域契约 → 运行时实现
```
接口模块固定业务侧依赖方向；实现与基础设施通过独立模块接入。

## 扩展契约

- [`SchedulerProcessor`](src/main/java/io/github/loadup/components/scheduler/SchedulerProcessor.java)：由实现方或调用方按接口定义对接。
- [`SchedulerProcessorRegistry`](src/main/java/io/github/loadup/components/scheduler/SchedulerProcessorRegistry.java)：由实现方或调用方按接口定义对接。
- [`SchedulerTemplate`](src/main/java/io/github/loadup/components/scheduler/SchedulerTemplate.java)：由实现方或调用方按接口定义对接。

## 设计取舍

`SchedulerTemplate` 作为稳定入口；技术选择在运行应用完成，避免 API 层反向依赖具体后端。

集成方式与配置示例见 [README.md](./README.md)。

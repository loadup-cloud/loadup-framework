# Loadup Components Retrytask Facade

后台重试任务的业务契约与接口模块；实现由独立模块提供。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-retrytask-facade</artifactId>
</dependency>
```

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

## 接入步骤

业务层依赖 facade；运行应用引入 binder-jobrunr，可选 notifier-gotone。
运行时还需在应用侧引入一种对应 binder；仅有接口 jar 不会创建实际后端能力。

## 对外契约

- [`RetryTaskProcessorRegistry`](src/main/java/io/github/loadup/retrytask/facade/RetryTaskProcessorRegistry.java)
- [`RetryTaskNotifier`](src/main/java/io/github/loadup/retrytask/facade/RetryTaskNotifier.java)
- [`RetryTaskFacade`](src/main/java/io/github/loadup/retrytask/facade/RetryTaskFacade.java)
- [`RetryTaskProcessor`](src/main/java/io/github/loadup/retrytask/facade/RetryTaskProcessor.java)

# Loadup Components Retrytask Binder JobRunr

后台重试任务的具体后端适配模块；由集成方按需引入。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-retrytask-binder-jobrunr</artifactId>
</dependency>
```

## 装配与配置

模块提供以下自动配置入口；实际启用条件和配置项以对应类及父模块 README 为准：

- `JobRunrRetryTaskAutoConfiguration`

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

## 接入步骤

业务层依赖 facade；运行应用引入 binder-jobrunr，可选 notifier-gotone。
此 jar 负责接入具体技术实现；业务模块继续面向 API/facade 编程。

## 配置入口

| 前缀 | 配置类 |
|---|---|
| `loadup.retrytask` | [`RetryTaskProperties`](src/main/java/io/github/loadup/retrytask/jobrunr/RetryTaskProperties.java) |

## 自动装配

- [`JobRunrRetryTaskAutoConfiguration`](src/main/java/io/github/loadup/retrytask/jobrunr/autoconfig/JobRunrRetryTaskAutoConfiguration.java)

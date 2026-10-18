# Loadup Components Retrytask Binder JobRunr

基于 BOM 管理的 JobRunr 9.0.0，和 scheduler binder 共用同一引擎。8.x 升级参考[官方迁移指南](https://www.jobrunr.io/en/guides/migration/v9/)。

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

内部边界、依赖与源码入口见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 接入步骤

业务层依赖 facade；运行应用引入 binder-jobrunr，可选 notifier-gotone。
此 jar 负责接入具体技术实现；业务模块继续面向 API/facade 编程。

## 配置入口

| 前缀 | 配置类 |
|---|---|
| `loadup.retrytask` | [`RetryTaskProperties`](src/main/java/io/github/loadup/retrytask/jobrunr/RetryTaskProperties.java) |

## 自动装配

- [`JobRunrRetryTaskAutoConfiguration`](src/main/java/io/github/loadup/retrytask/jobrunr/autoconfig/JobRunrRetryTaskAutoConfiguration.java)

## 共享补丁运行时

与 scheduler binder 共同依赖 [LoadUp JobRunr 运行时](../../loadup-components-jobrunr/README.md)，排除 starter 的原 core，避免同一应用出现两份核心类。此补丁仅处理周期调度缓存失效，重试任务继续使用原 JobRunr 逻辑和数据表，无新增配置。消费工程单独引入 starter 时也应排除原 `org.jobrunr:jobrunr`。上游库许可和对应完整源代码见该运行时文档。

# LoadUp ConfigCenter Binder Local

配置中心的具体后端适配模块；由集成方按需引入。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-configcenter-binder-local</artifactId>
</dependency>
```

## 装配与配置

模块提供以下自动配置入口；实际启用条件和配置项以对应类及父模块 README 为准：

- `LocalConfigCenterAutoConfiguration`

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 接入步骤

通过 `loadup.configcenter.binder-type` 选择 local、nacos 或 apollo；业务层注入 `ConfigCenterTemplate`。
此 jar 负责接入具体技术实现；业务模块继续面向 API/facade 编程。

## 最小配置

```yaml
loadup:
  configcenter:
    binder-type: local
```

同一应用只启用与该值对应的 binder；其他可选项见下方配置类及父模块 README。

## 配置入口

| 前缀 | 配置类 |
|---|---|
| `loadup.configcenter.binder.local` | [`LocalConfigCenterProperties`](src/main/java/io/github/loadup/components/configcenter/local/LocalConfigCenterProperties.java) |

## 自动装配

- [`LocalConfigCenterAutoConfiguration`](src/main/java/io/github/loadup/components/configcenter/local/autoconfig/LocalConfigCenterAutoConfiguration.java)
  - 启用条件：`@ConditionalOnProperty( prefix = "loadup.configcenter", name = "binder-type", havingValue = "local", matchIfMissing = true)`。

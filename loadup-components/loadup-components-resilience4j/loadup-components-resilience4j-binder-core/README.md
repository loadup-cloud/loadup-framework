# Loadup Resilience4j Binder Core

容错的具体后端适配模块；由集成方按需引入。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-resilience4j-binder-core</artifactId>
</dependency>
```

## 装配与配置

模块提供以下自动配置入口；实际启用条件和配置项以对应类及父模块 README 为准：

- `Resilience4jCoreAutoConfiguration`

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

## 接入步骤

业务层依赖 API；集成方引入 binder-core，并配置 `loadup.resilience4j` 与所需 `resilience4j.*` 实例。
此 jar 负责接入具体技术实现；业务模块继续面向 API/facade 编程。

## 配置入口

| 前缀 | 配置类 |
|---|---|
| `resilience4j.timelimiter` | [`TimeLimiterProperties`](src/main/java/io/github/loadup/components/resilience4j/core/TimeLimiterProperties.java) |
| `resilience4j.bulkhead` | [`BulkheadProperties`](src/main/java/io/github/loadup/components/resilience4j/core/BulkheadProperties.java) |
| `resilience4j.ratelimiter` | [`RateLimiterProperties`](src/main/java/io/github/loadup/components/resilience4j/core/RateLimiterProperties.java) |
| `resilience4j.thread-pool-bulkhead` | [`ThreadPoolBulkheadProperties`](src/main/java/io/github/loadup/components/resilience4j/core/ThreadPoolBulkheadProperties.java) |
| `resilience4j.circuitbreaker` | [`CircuitBreakerProperties`](src/main/java/io/github/loadup/components/resilience4j/core/CircuitBreakerProperties.java) |
| `resilience4j.retry` | [`RetryProperties`](src/main/java/io/github/loadup/components/resilience4j/core/RetryProperties.java) |

## 自动装配

- [`Resilience4jCoreAutoConfiguration`](src/main/java/io/github/loadup/components/resilience4j/core/Resilience4jCoreAutoConfiguration.java)
  - 启用条件：`@ConditionalOnProperty(prefix = "loadup.resilience4j", name = "enabled", havingValue = "true", matchIfMissing = true)`。

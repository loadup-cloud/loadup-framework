# Loadup Resilience4j Binder Core 架构

## 职责与边界

容错的具体后端适配模块；由集成方按需引入。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-resilience4j-api`

直接依赖的外部坐标（不含测试与 provided scope）：

- `io.github.resilience4j:resilience4j-spring6`
- `io.github.resilience4j:resilience4j-micrometer`
- `org.springframework.boot:spring-boot-starter-aspectj`
- `org.springframework.boot:spring-boot-autoconfigure`
- `org.springframework.boot:spring-boot-configuration-processor`

## 实现入口

主要入口文件：

- [`Resilience4jCoreAutoConfiguration`](src/main/java/io/github/loadup/components/resilience4j/core/Resilience4jCoreAutoConfiguration.java)

## 分层与调用路径

API 承载调用契约；binder-core 创建 Resilience4j 注册表并按实例配置熔断、重试、限流等策略。

```text
业务代码 → API/facade → 当前 binder → 第三方引擎或基础设施
```
binder 实现框架契约，把实现库及其配置隔离在业务 API 之外。

## 装配规则

- [`Resilience4jCoreAutoConfiguration`](src/main/java/io/github/loadup/components/resilience4j/core/Resilience4jCoreAutoConfiguration.java) 是自动配置入口。
  - `@ConditionalOnClass(CircuitBreakerRegistry.class)`
  - `@ConditionalOnProperty(prefix = "loadup.resilience4j", name = "enabled", havingValue = "true", matchIfMissing = true)`
  - `@ConditionalOnClass(MeterRegistry.class)`
  - `@ConditionalOnMissingBean(name = "circuitBreakerMetrics")`

## 配置归属

- [`TimeLimiterProperties`](src/main/java/io/github/loadup/components/resilience4j/core/TimeLimiterProperties.java) 绑定 `resilience4j.timelimiter`。
- [`BulkheadProperties`](src/main/java/io/github/loadup/components/resilience4j/core/BulkheadProperties.java) 绑定 `resilience4j.bulkhead`。
- [`RateLimiterProperties`](src/main/java/io/github/loadup/components/resilience4j/core/RateLimiterProperties.java) 绑定 `resilience4j.ratelimiter`。
- [`ThreadPoolBulkheadProperties`](src/main/java/io/github/loadup/components/resilience4j/core/ThreadPoolBulkheadProperties.java) 绑定 `resilience4j.thread-pool-bulkhead`。
- [`CircuitBreakerProperties`](src/main/java/io/github/loadup/components/resilience4j/core/CircuitBreakerProperties.java) 绑定 `resilience4j.circuitbreaker`。
- [`RetryProperties`](src/main/java/io/github/loadup/components/resilience4j/core/RetryProperties.java) 绑定 `resilience4j.retry`。

## 设计取舍

选择独立 binder，使容错治理实现的依赖留在集成应用；替换底层实现时，业务侧仍使用 `ResilienceRegistries` 契约。

集成方式与配置示例见 [README.md](./README.md)。

# Loadup Resilience4j API 架构

## 职责与边界

容错的业务契约与接口模块；实现由独立模块提供。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-autoconfigure-processor`
- `org.springframework.boot:spring-boot-configuration-processor`
- `org.springframework.boot:spring-boot-autoconfigure`
- `io.github.resilience4j:resilience4j-annotations`
- `io.github.resilience4j:resilience4j-circuitbreaker`
- `io.github.resilience4j:resilience4j-retry`
- `io.github.resilience4j:resilience4j-ratelimiter`
- `io.github.resilience4j:resilience4j-bulkhead`
- `io.github.resilience4j:resilience4j-timelimiter`

## 实现入口

该模块没有 `src/main/java` 入口；依赖与资源声明以 `pom.xml` 为准。

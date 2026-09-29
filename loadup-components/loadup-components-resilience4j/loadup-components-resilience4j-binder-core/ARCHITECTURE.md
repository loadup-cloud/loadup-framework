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

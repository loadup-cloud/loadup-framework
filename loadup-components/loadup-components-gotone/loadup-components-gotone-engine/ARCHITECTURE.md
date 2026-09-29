# Loadup Gotone Engine 架构

## 职责与边界

多渠道通知的执行引擎模块。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-gotone-api`
- `loadup-components-resilience4j-binder-core`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-autoconfigure`
- `org.slf4j:slf4j-api`

## 实现入口

主要入口文件：

- [`GotoneEngineAutoConfiguration`](src/main/java/io/github/loadup/components/gotone/engine/GotoneEngineAutoConfiguration.java)
- [`DefaultNotificationService`](src/main/java/io/github/loadup/components/gotone/engine/DefaultNotificationService.java)
- [`ResilientNotificationChannelProvider`](src/main/java/io/github/loadup/components/gotone/engine/ResilientNotificationChannelProvider.java)

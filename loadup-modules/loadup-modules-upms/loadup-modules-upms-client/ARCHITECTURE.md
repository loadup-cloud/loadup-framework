# Loadup Modules UPMS Client Layer 架构

## 职责与边界

UPMS 对外 DTO、Command 与 Query 契约。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-commons-util`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.apache.commons:commons-lang3`

## 实现入口

主要入口文件：

- [`AccessCheckService`](src/main/java/io/github/loadup/modules/upms/client/service/AccessCheckService.java)
- [`AuthenticationService`](src/main/java/io/github/loadup/modules/upms/client/service/AuthenticationService.java)
- [`UserQueryService`](src/main/java/io/github/loadup/modules/upms/client/service/UserQueryService.java)

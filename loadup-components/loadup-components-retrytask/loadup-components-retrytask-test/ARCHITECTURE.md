# Loadup Components Retrytask Test 架构

## 职责与边界

后台重试任务的测试模块，承载模块行为和集成验证；不作为生产运行时依赖。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-retrytask-facade`
- `loadup-components-retrytask-binder-jobrunr`
- `loadup-components-retrytask-notifier-gotone`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter`
- `org.springframework.boot:spring-boot-starter-jdbc`
- `org.springframework.boot:spring-boot-starter-json`
- `com.mysql:mysql-connector-j`

## 实现入口

该模块没有 `src/main/java` 入口；依赖与资源声明以 `pom.xml` 为准。

# Loadup Launcher 架构

## 职责与边界

本地集成启动器，用于验证各组件与 UPMS 的组合；集成方应在自己的应用中按需引入模块。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-springdoc`
- `loadup-modules-upms-web`
- `loadup-components-resource-server`
- `loadup-components-authserver-binder-sas`
- `loadup-modules-upms-authserver`
- `loadup-commons-log`
- `loadup-commons-tracer`
- `loadup-components-retrytask-binder-jobrunr`
- `loadup-components-scheduler-binder-jobrunr`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter-web`
- `com.mysql:mysql-connector-j`
- `org.springframework.boot:spring-boot-starter-actuator`
- `io.micrometer:micrometer-registry-prometheus`

## 实现入口

主要入口文件：

- [`DemoService`](src/main/java/io/github/loadup/framework/service/DemoService.java)

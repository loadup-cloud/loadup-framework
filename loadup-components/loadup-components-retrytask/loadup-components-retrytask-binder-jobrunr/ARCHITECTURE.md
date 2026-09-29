# Loadup Components Retrytask Binder JobRunr 架构

## 职责与边界

后台重试任务的具体后端适配模块；由集成方按需引入。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-retrytask-facade`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter`
- `org.jobrunr:jobrunr-spring-boot-4-starter`
- `org.springframework.boot:spring-boot-autoconfigure-processor`
- `org.springframework.boot:spring-boot-configuration-processor`

## 实现入口

主要入口文件：

- [`JobRunrRetryTaskAutoConfiguration`](src/main/java/io/github/loadup/retrytask/jobrunr/autoconfig/JobRunrRetryTaskAutoConfiguration.java)
- [`JobRunrRetryTaskFacade`](src/main/java/io/github/loadup/retrytask/jobrunr/JobRunrRetryTaskFacade.java)

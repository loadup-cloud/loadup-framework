# Loadup Scheduler Quartz Binder 架构

## 职责与边界

任务调度的具体后端适配模块；由集成方按需引入。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-scheduler-api`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter-quartz`

## 实现入口

主要入口文件：

- [`QuartzSchedulerAutoConfiguration`](src/main/java/io/github/loadup/components/scheduler/quartz/autoconfig/QuartzSchedulerAutoConfiguration.java)
- [`QuartzSchedulerTemplate`](src/main/java/io/github/loadup/components/scheduler/quartz/QuartzSchedulerTemplate.java)

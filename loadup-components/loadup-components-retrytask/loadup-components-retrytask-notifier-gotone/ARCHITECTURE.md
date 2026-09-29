# Loadup Components Retrytask Notifier Gotone 架构

## 职责与边界

RetryTask 与 Gotone 通知组件之间的可选告警适配模块。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-retrytask-facade`
- `loadup-components-gotone-api`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-autoconfigure`
- `org.slf4j:slf4j-api`
- `org.springframework.boot:spring-boot-configuration-processor`

## 实现入口

主要入口文件：

- [`RetryTaskGotoneNotifierAutoConfiguration`](src/main/java/io/github/loadup/retrytask/notifier/gotone/RetryTaskGotoneNotifierAutoConfiguration.java)

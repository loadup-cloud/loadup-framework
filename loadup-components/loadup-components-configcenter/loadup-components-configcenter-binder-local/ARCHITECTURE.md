# LoadUp ConfigCenter Binder Local 架构

## 职责与边界

配置中心的具体后端适配模块；由集成方按需引入。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-configcenter-api`
- `loadup-commons-util`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter`

## 实现入口

主要入口文件：

- [`LocalConfigCenterAutoConfiguration`](src/main/java/io/github/loadup/components/configcenter/local/autoconfig/LocalConfigCenterAutoConfiguration.java)
- [`LocalConfigCenterProvider`](src/main/java/io/github/loadup/components/configcenter/local/LocalConfigCenterProvider.java)

# LoadUp ConfigCenter Components API 架构

## 职责与边界

配置中心的业务契约与接口模块；实现由独立模块提供。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-autoconfigure-processor`
- `org.springframework.boot:spring-boot-starter`
- `org.springframework.boot:spring-boot-configuration-processor`

## 实现入口

主要入口文件：

- [`ConfigCenterAutoConfiguration`](src/main/java/io/github/loadup/components/configcenter/autoconfig/ConfigCenterAutoConfiguration.java)
- [`ConfigCenterProvider`](src/main/java/io/github/loadup/components/configcenter/ConfigCenterProvider.java)
- [`ConfigCenterTemplate`](src/main/java/io/github/loadup/components/configcenter/ConfigCenterTemplate.java)
- [`DefaultConfigCenterTemplate`](src/main/java/io/github/loadup/components/configcenter/DefaultConfigCenterTemplate.java)

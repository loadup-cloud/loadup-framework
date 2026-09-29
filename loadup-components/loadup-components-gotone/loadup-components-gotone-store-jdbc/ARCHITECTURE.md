# Loadup Gotone Store JDBC 架构

## 职责与边界

多渠道通知的可选 JDBC 持久化模块。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-gotone-api`
- `loadup-components-database`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter-flyway`
- `org.flywaydb:flyway-mysql`
- `tools.jackson.core:jackson-databind`
- `org.springframework.boot:spring-boot-autoconfigure`
- `org.slf4j:slf4j-api`

## 实现入口

主要入口文件：

- [`GotoneStoreJdbcAutoConfiguration`](src/main/java/io/github/loadup/components/gotone/store/config/GotoneStoreJdbcAutoConfiguration.java)
- [`JdbcChannelConfigProvider`](src/main/java/io/github/loadup/components/gotone/store/config/JdbcChannelConfigProvider.java)
- [`JdbcServiceConfigProvider`](src/main/java/io/github/loadup/components/gotone/store/config/JdbcServiceConfigProvider.java)

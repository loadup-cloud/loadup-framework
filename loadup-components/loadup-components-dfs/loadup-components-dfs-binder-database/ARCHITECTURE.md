# Loadup Dfs Binder Database 架构

## 职责与边界

文件存储的具体后端适配模块；由集成方按需引入。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-dfs-api`
- `loadup-components-database`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter-flyway`
- `org.flywaydb:flyway-mysql`
- `tools.jackson.core:jackson-databind`
- `org.springframework.boot:spring-boot-autoconfigure`

## 实现入口

主要入口文件：

- [`DatabaseDfsAutoConfiguration`](src/main/java/io/github/loadup/components/dfs/database/autoconfig/DatabaseDfsAutoConfiguration.java)
- [`DatabaseDfsProvider`](src/main/java/io/github/loadup/components/dfs/database/DatabaseDfsProvider.java)

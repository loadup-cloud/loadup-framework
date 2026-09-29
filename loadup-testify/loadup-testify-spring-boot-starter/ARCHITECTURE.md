# Testify Spring Boot Starter 架构

## 职责与边界

Testify 与 Spring Boot 的自动装配入口。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-testify-core`
- `loadup-testify-data-engine`
- `loadup-testify-assert-engine`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter`
- `org.springframework.boot:spring-boot-starter-jdbc`
- `tools.jackson.dataformat:jackson-dataformat-yaml`
- `org.slf4j:slf4j-api`

## 实现入口

主要入口文件：

- [`TestifyAutoConfiguration`](src/main/java/io/github/loadup/testify/starter/config/TestifyAutoConfiguration.java)

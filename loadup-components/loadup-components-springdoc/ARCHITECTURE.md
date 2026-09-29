# LoadUp Components SpringDoc 架构

## 职责与边界

提供 OpenAPI 与 Knife4j 的自动配置。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `com.github.xiaoymin:knife4j-openapi3-jakarta-spring-boot-starter`
- `org.springframework.boot:spring-boot-autoconfigure`
- `org.springframework.boot:spring-boot-configuration-processor`
- `org.slf4j:slf4j-api`

## 实现入口

主要入口文件：

- [`SpringDocAutoConfiguration`](src/main/java/io/github/loadup/components/springdoc/autoconfigure/SpringDocAutoConfiguration.java)

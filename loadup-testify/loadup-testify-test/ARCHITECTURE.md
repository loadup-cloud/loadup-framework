# Testify Demo 架构

## 职责与边界

Testify 测试框架的测试模块，承载模块行为和集成验证；不作为生产运行时依赖。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter`
- `org.springframework.boot:spring-boot-starter-jdbc`
- `com.mysql:mysql-connector-j`

## 实现入口

主要入口文件：

- [`OrderService`](src/main/java/io/github/loadup/testify/test/service/OrderService.java)
- [`UserService`](src/main/java/io/github/loadup/testify/test/service/UserService.java)

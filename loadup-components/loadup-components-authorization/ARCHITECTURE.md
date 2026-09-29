# LoadUp Components Authorization 架构

## 职责与边界

提供Spring Security 方法级授权能力的独立模块。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter-security`
- `org.springframework:spring-context`
- `org.springframework.boot:spring-boot-autoconfigure`
- `org.slf4j:slf4j-api`
- `com.github.spotbugs:spotbugs-annotations`

## 实现入口

主要入口文件：

- [`AuthorizationAutoConfiguration`](src/main/java/io/github/loadup/components/authorization/config/AuthorizationAutoConfiguration.java)

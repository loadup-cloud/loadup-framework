# LoadUp Components AuthServer API 架构

## 职责与边界

OAuth2 授权服务器与令牌签发的业务契约与接口模块；实现由独立模块提供。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot`
- `org.springframework.security:spring-security-oauth2-jose`
- `org.springframework.security:spring-security-oauth2-authorization-server`
- `org.springframework.security:spring-security-core`

## 实现入口

该模块没有 `src/main/java` 入口；依赖与资源声明以 `pom.xml` 为准。

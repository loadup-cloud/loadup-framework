# LoadUp JWT Resource Server 架构

## 职责与边界

提供资源服务器 JWT 校验能力的独立模块。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-commons-dto`
- `loadup-components-authorization`

直接依赖的外部坐标（不含测试与 provided scope）：

- `tools.jackson.core:jackson-databind`
- `org.springframework.boot:spring-boot-starter-security`
- `org.springframework.security:spring-security-oauth2-resource-server`
- `org.springframework.security:spring-security-oauth2-jose`

## 实现入口

主要入口文件：

- [`ResourceServerAutoConfiguration`](src/main/java/io/github/loadup/components/resourceserver/ResourceServerAutoConfiguration.java)

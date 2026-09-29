# LoadUp Components AuthServer Binder SAS 架构

## 职责与边界

OAuth2 授权服务器与令牌签发的具体后端适配模块；由集成方按需引入。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-authserver-api`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.apache.commons:commons-lang3`
- `org.springframework.boot:spring-boot-starter-security-oauth2-authorization-server`

## 实现入口

主要入口文件：

- [`SasAuthServerAutoConfiguration`](src/main/java/io/github/loadup/components/authserver/sas/SasAuthServerAutoConfiguration.java)

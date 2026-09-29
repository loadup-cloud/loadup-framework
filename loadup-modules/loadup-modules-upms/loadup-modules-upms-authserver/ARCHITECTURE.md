# LoadUp UPMS Authorization Server Adapter 架构

## 职责与边界

UPMS 与授权服务器之间的可选认证适配模块。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-modules-upms-app`
- `loadup-components-authserver-api`
- `loadup-commons-dto`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter-security`
- `org.springframework.boot:spring-boot-starter-webmvc`
- `org.springframework.security:spring-security-oauth2-jose`

## 实现入口

主要入口文件：

- [`UpmsAuthServerAutoConfiguration`](src/main/java/io/github/loadup/modules/upms/authserver/UpmsAuthServerAutoConfiguration.java)
- [`UpmsAuthenticationProvider`](src/main/java/io/github/loadup/modules/upms/authserver/UpmsAuthenticationProvider.java)
- [`UpmsTokenController`](src/main/java/io/github/loadup/modules/upms/authserver/UpmsTokenController.java)

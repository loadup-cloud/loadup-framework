# Loadup Modules UPMS Client Layer

UPMS 对外 DTO、Command 与 Query 契约。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-upms-client</artifactId>
</dependency>
```

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

## 接入步骤

接入 Spring MVC 时引入 web 与所需 app/infra；需要登录签发时增加 upms-authserver。
运行应用需要按需组合 app、infrastructure 与 web 适配模块。

## 对外契约

- [`AccessCheckService`](src/main/java/io/github/loadup/modules/upms/client/service/AccessCheckService.java)
- [`AuthenticationService`](src/main/java/io/github/loadup/modules/upms/client/service/AuthenticationService.java)
- [`UserQueryService`](src/main/java/io/github/loadup/modules/upms/client/service/UserQueryService.java)

## 展示与明文 DTO

`UserDetailDTO` 的 realName/email/mobile 带 `@Masked`，只影响 WebMVC 响应；内部服务返回的对象仍有原值。`UserSensitiveQuery` 使用目标 ID 和枚举 `SensitiveReadPurpose`；独立 `UserSensitiveDTO` 仅由经过授权和可靠审计的 app 服务返回。明文 DTO 的 toString 不包含个人字段。

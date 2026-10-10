# Loadup Modules UPMS Client Layer 架构

## 职责与边界

UPMS 对外 DTO、Command 与 Query 契约。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-commons-util`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.apache.commons:commons-lang3`

## 实现入口

主要入口文件：

- [`AccessCheckFacade`](src/main/java/io/github/loadup/modules/upms/client/facade/AccessCheckFacade.java)
- [`AuthenticationFacade`](src/main/java/io/github/loadup/modules/upms/client/facade/AuthenticationFacade.java)
- [`UserQueryFacade`](src/main/java/io/github/loadup/modules/upms/client/facade/UserQueryFacade.java)

## 分层与调用路径

Web → App → Domain Gateway → Infrastructure；认证适配连接 UPMS 凭证校验与通用 AuthServer。

```text
业务调用 → AuthenticationService → SPI/领域契约 → 运行时实现
```
接口模块固定业务侧依赖方向；实现与基础设施通过独立模块接入。

## 扩展契约

- [`AccessCheckFacade`](src/main/java/io/github/loadup/modules/upms/client/facade/AccessCheckFacade.java)：由实现方或调用方按接口定义对接。
- [`AuthenticationFacade`](src/main/java/io/github/loadup/modules/upms/client/facade/AuthenticationFacade.java)：由实现方或调用方按接口定义对接。
- [`UserQueryFacade`](src/main/java/io/github/loadup/modules/upms/client/facade/UserQueryFacade.java)：由实现方或调用方按接口定义对接。

## 设计取舍

`AuthenticationService` 作为稳定入口；技术选择在运行应用完成，避免 API 层反向依赖具体后端。

集成方式与配置示例见 [README.md](README.md)。

## 敏感输出契约

仅展示 DTO 依赖纯 Java `commons-masking` 注解，client 不依赖 WebMVC/Jackson 脱敏实现。查询入参、Command 与原值 DTO 不使用展示注解，避免入参或签名内容被修改。普通与明文输出类型分离，禁止通过权限动态切换同一 DTO 的 serializer。

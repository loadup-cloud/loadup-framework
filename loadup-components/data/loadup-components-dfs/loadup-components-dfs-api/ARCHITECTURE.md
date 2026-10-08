# Loadup Dfs Components Api 架构

## 职责与边界

文件存储的业务契约与接口模块；实现由独立模块提供。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-autoconfigure`

## 实现入口

主要入口文件：

- [`DfsAutoConfiguration`](src/main/java/io/github/loadup/components/dfs/autoconfig/DfsAutoConfiguration.java)
- [`DefaultDfsService`](src/main/java/io/github/loadup/components/dfs/DefaultDfsService.java)
- [`DfsProvider`](src/main/java/io/github/loadup/components/dfs/DfsProvider.java)
- [`DfsService`](src/main/java/io/github/loadup/components/dfs/DfsService.java)

## 分层与调用路径

`DefaultDfsService` 把对象操作委派给唯一的 `DfsProvider`；预签名与分片操作取决于 provider 能力。

```text
业务调用 → DfsService → SPI/领域契约 → 运行时实现
```
接口模块固定业务侧依赖方向；实现与基础设施通过独立模块接入。

## 装配规则

- [`DfsAutoConfiguration`](src/main/java/io/github/loadup/components/dfs/autoconfig/DfsAutoConfiguration.java) 是自动配置入口。
  - `@ConditionalOnSingleCandidate(DfsProvider.class)`

## 扩展契约

- [`DfsService`](src/main/java/io/github/loadup/components/dfs/DfsService.java)：由实现方或调用方按接口定义对接。
- [`DfsProvider`](src/main/java/io/github/loadup/components/dfs/DfsProvider.java)：由实现方或调用方按接口定义对接。

## 配置归属

- [`DfsProperties`](src/main/java/io/github/loadup/components/dfs/DfsProperties.java) 绑定 `loadup.dfs`。

## 设计取舍

`DfsService` 作为稳定入口；技术选择在运行应用完成，避免 API 层反向依赖具体后端。

集成方式与配置示例见 [README.md](README.md)。

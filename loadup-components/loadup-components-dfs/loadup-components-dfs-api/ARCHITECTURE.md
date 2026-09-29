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

# Loadup Dfs Binder S3 架构

## 职责与边界

文件存储的具体后端适配模块；由集成方按需引入。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-dfs-api`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-autoconfigure`
- `software.amazon.awssdk:s3`
- `com.github.spotbugs:spotbugs-annotations`

## 实现入口

主要入口文件：

- [`S3DfsAutoConfiguration`](src/main/java/io/github/loadup/components/dfs/s3/autoconfig/S3DfsAutoConfiguration.java)
- [`S3DfsProvider`](src/main/java/io/github/loadup/components/dfs/s3/S3DfsProvider.java)

# Loadup Cache Components API 架构

## 职责与边界

缓存的业务契约与接口模块；实现由独立模块提供。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-autoconfigure-processor`
- `org.springframework.boot:spring-boot-configuration-processor`
- `org.springframework.boot:spring-boot-starter-cache`
- `tools.jackson.core:jackson-databind`

## 实现入口

主要入口文件：

- [`LoadupCacheAutoConfiguration`](src/main/java/io/github/loadup/components/cache/autoconfig/LoadupCacheAutoConfiguration.java)

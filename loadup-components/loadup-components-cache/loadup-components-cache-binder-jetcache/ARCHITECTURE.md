# Loadup Cache Binder JetCache 架构

## 职责与边界

缓存的具体后端适配模块；由集成方按需引入。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-cache-api`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter-cache`
- `org.springframework.boot:spring-boot-starter-data-redis`
- `com.alicp.jetcache:jetcache-anno`
- `com.alicp.jetcache:jetcache-redis-springdata`
- `com.github.ben-manes.caffeine:caffeine`
- `tools.jackson.core:jackson-databind`

## 实现入口

主要入口文件：

- [`JetCacheCacheAutoConfiguration`](src/main/java/io/github/loadup/components/cache/jetcache/autoconfig/JetCacheCacheAutoConfiguration.java)

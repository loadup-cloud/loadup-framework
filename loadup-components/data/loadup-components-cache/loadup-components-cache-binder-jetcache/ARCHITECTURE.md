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

## 分层与调用路径

Spring Cache 调用进入当前 `CacheManager`，由选中的 binder 处理缓存读写；切换后端不改变业务调用。

```text
业务代码 → API/facade → 当前 binder → 第三方引擎或基础设施
```
binder 实现框架契约，把实现库及其配置隔离在业务 API 之外。

## 装配规则

- [`JetCacheCacheAutoConfiguration`](src/main/java/io/github/loadup/components/cache/jetcache/autoconfig/JetCacheCacheAutoConfiguration.java) 是自动配置入口。
  - `@ConditionalOnClass({SimpleCacheManager.class, CacheBuilderTemplate.class})`
  - `@ConditionalOnProperty(prefix = "loadup.cache", name = "type", havingValue = CacheBackendType.JETCACHE)`
  - `@ConditionalOnMissingBean(com.alicp.jetcache.CacheManager.class)`
  - `@ConditionalOnMissingBean(CacheManager.class)`

## 配置归属

- [`JetCacheCacheProperties`](src/main/java/io/github/loadup/components/cache/jetcache/JetCacheCacheProperties.java) 绑定 `loadup.cache.binder.jetcache`。

## 设计取舍

选择独立 binder，使 Spring Cache 的实现依赖留在集成应用；替换底层实现时，业务侧仍使用 `CacheManager` 契约。

集成方式与配置示例见 [README.md](README.md)。

## Spring Cache 加载与生命周期

配置的缓存名称在 Spring CacheManager 初始化阶段注册，避免提前预热后被初始化流程覆盖。显式预热遵循同一缓存创建规则。

带 loader 的读取将 JetCache 包装的 Spring `Cache.ValueRetrievalException` 恢复为原异常，保留 loader 和原始 cause；其他 JetCache 异常继续按后端异常传播。真实 Redis 与本地缓存集成测试分别验证加载、过期及预热行为。

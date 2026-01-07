# Caffeine Cache 配置覆盖功能实现总结

## 变更概述

实现了 Caffeine Cache 的配置覆盖功能，支持：

1. 使用 `spring.cache.caffeine.spec` 设置全局默认配置
2. 使用 `loadup.cache.cache-configs.[cacheName]` 为特定缓存设置自定义配置
3. 自定义配置会覆盖全局默认配置

## 修改的文件

### 1. LoadUpCaffeineCacheManager.java

**位置**: `loadup-components-cache-binder-caffeine/src/main/java/com/github/loadup/components/cache/caffeine/config/`

**变更**:

- 完善了 JavaDoc 注释，明确说明配置覆盖机制
- 添加了 `getCustomCaffeineSpecs()` 方法，用于获取所有自定义配置（测试时使用）
- `createCaffeineCache()` 方法会优先检查自定义配置，如果没有则使用父类的默认配置

**关键逻辑**:

```java

@Override
protected Cache createCaffeineCache(String name) {
    // 1. 先检查是否有自定义配置
    Caffeine<Object, Object> customCaffeine = customCaffeineSpecs.get(name);

    if (customCaffeine != null) {
        // 2. 如果有，使用自定义配置
        return new CaffeineCache(name, customCaffeine.build(), isAllowNullValues());
    }

    // 3. 否则，使用 spring.cache.caffeine.spec 的默认配置
    return super.createCaffeineCache(name);
}
```

### 2. CaffeineCacheAutoConfiguration.java

**位置**: `loadup-components-cache-binder-caffeine/src/main/java/com/github/loadup/components/cache/caffeine/config/`

**变更**:

- 添加了 `@Value("${spring.cache.caffeine.spec:}")` 注入，读取全局默认配置
- 在 `defaultCacheManager()` 方法中，通过 `setCacheSpecification()` 设置默认配置
- 更新日志输出，明确标识自定义配置会覆盖默认配置

**关键逻辑**:

```java

@Value("${spring.cache.caffeine.spec:}")
private String cacheSpec;

public CacheManager defaultCacheManager() {
    LoadUpCaffeineCacheManager cacheManager = new LoadUpCaffeineCacheManager();

    // 1. 设置全局默认配置
    if (cacheSpec != null && !cacheSpec.isEmpty()) {
        cacheManager.setCacheSpecification(cacheSpec);
        log.info("Applied default Caffeine cache spec: {}", cacheSpec);
    }

    // 2. 注册自定义配置（会覆盖默认配置）
    Map<String, CacheConfigs> cacheConfigs = getCacheConfigs();
    if (cacheConfigs != null && !cacheConfigs.isEmpty()) {
        cacheConfigs.forEach((cacheName, cacheConfig) -> {
            if (cacheBindingCfg.getBinderForCache(cacheName) == BinderEnum.CacheBinder.CAFFEINE) {
                Caffeine<Object, Object> customCaffeine = buildCustomCaffeine(cacheConfig);
                cacheManager.registerCustomCache(cacheName, customCaffeine);
                log.info("Configured custom Caffeine cache: name={}, ... (overrides default spec)", ...);
            }
        });
    }

    return cacheManager;
}
```

## 新增的文件

### 1. CACHE_CONFIGURATION_GUIDE.md

**位置**: `loadup-components-cache-binder-caffeine/`

**内容**:

- 详细的配置指南
- 使用示例
- 配置优先级说明
- 时间单位格式说明
- 最佳实践建议
- 监控和调试方法

### 2. CaffeineCacheManagerConfigTest.java

**位置**: `loadup-components-cache-binder-caffeine/src/test/java/com/github/loadup/components/cache/caffeine/config/`

**内容**:

- 测试 CacheManager 正确注入
- 测试使用默认配置创建缓存
- 测试使用自定义配置创建缓存
- 测试自定义配置的缓存规格
- 测试多个缓存可以共存

### 3. README.md (更新)

**位置**: `loadup-components-cache-binder-caffeine/`

**变更**:

- 添加了配置覆盖功能的说明
- 更新了配置示例
- 添加了配置优先级说明
- 添加了最佳实践建议

## 配置示例

### 场景 1: 所有缓存使用相同配置

```yaml
spring:
  cache:
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m

loadup:
  cache:
    binder: caffeine
```

### 场景 2: 大部分缓存使用默认配置，少数缓存特殊处理

```yaml
spring:
  cache:
    caffeine:
      spec: maximumSize=500,expireAfterWrite=10m  # 默认配置

loadup:
  cache:
    binder: caffeine
    cache-configs:
      userCache: # 自定义配置，覆盖默认
        expire-after-write: 30m
        maximum-size: 10000
      productCache: # 自定义配置，覆盖默认
        expire-after-write: 1h
        maximum-size: 5000
      # 其他未配置的缓存使用默认配置
```

### 场景 3: 防止缓存雪崩

```yaml
spring:
  cache:
    caffeine:
      spec: maximumSize=500,expireAfterWrite=10m

loadup:
  cache:
    cache-configs:
      userCache:
        expire-after-write: 30m
        enable-random-expiration: true
        random-offset-seconds: 300  # 增加 0-5 分钟的随机偏移
```

## 工作原理

1. **应用启动时**:
    - `CaffeineCacheAutoConfiguration` 创建 `LoadUpCaffeineCacheManager` Bean
    - 如果配置了 `spring.cache.caffeine.spec`，通过 `setCacheSpecification()` 设置为默认配置
    - 读取 `loadup.cache.cache-configs`，为每个配置的缓存名称注册自定义 `Caffeine` 构建器

2. **获取缓存时**:
    - 当代码调用 `cacheManager.getCache(cacheName)` 时
    - `LoadUpCaffeineCacheManager` 检查是否有该缓存名称的自定义配置
    - 如果有，使用自定义配置创建缓存（覆盖默认配置）
    - 如果没有，使用 `spring.cache.caffeine.spec` 的默认配置创建缓存

3. **配置优先级** (从高到低):
    - `loadup.cache.cache-configs.[cacheName]` - 特定缓存的自定义配置
    - `spring.cache.caffeine.spec` - 全局默认配置
    - Caffeine 内置默认值

## 优势

1. **灵活性**: 可以为不同的缓存设置不同的配置，而不需要创建多个 CacheManager
2. **简洁性**: 大部分缓存使用默认配置，只需为特殊缓存设置自定义配置
3. **兼容性**: 完全兼容 Spring Boot 标准的 `spring.cache.caffeine.spec` 配置
4. **可维护性**: 配置集中管理，易于理解和维护

## 测试建议

1. 运行 `CaffeineCacheManagerConfigTest` 验证基本功能
2. 启用 DEBUG 日志查看配置应用情况:
   ```yaml
   logging:
     level:
       com.github.loadup.components.cache: DEBUG
   ```
3. 观察日志输出:
   ```
   Applied default Caffeine cache spec: maximumSize=1000,expireAfterWrite=300s
   Configured custom Caffeine cache: name=userCache, maxSize=10000, expireAfterWrite=30m (overrides default spec)
   ```

## 注意事项

1. `spring.cache.caffeine.spec` 使用 Caffeine 的标准 spec 格式
2. `loadup.cache.cache-configs` 使用 LoadUp 框架的自定义格式（支持更多功能如随机过期偏移）
3. 配置在应用启动时加载，运行时修改配置需要重启应用
4. 未在 `cache-configs` 中配置的缓存会使用 `spring.cache.caffeine.spec` 的默认配置

## 后续改进建议

1. 支持动态修改配置（可能需要引入配置中心）
2. 添加缓存监控指标（如命中率、大小等）
3. 支持更复杂的缓存策略（如基于权重的驱逐策略）
4. 添加缓存预热功能


# Loadup Cache Test 架构

## 职责与边界

缓存的测试模块，承载模块行为和集成验证；不作为生产运行时依赖。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-cache-api`
- `loadup-components-cache-binder-redis`
- `loadup-components-cache-binder-caffeine`
- `loadup-components-cache-binder-jetcache`

## 实现入口

该模块没有 `src/main/java` 入口；依赖与资源声明以 `pom.xml` 为准。

# LoadUp Components Cache API

## 📋 概述

LoadUp Components Cache API 是缓存组件的核心API模块，定义了统一的缓存接口规范。

## 🎯 功能特性

- 统一的缓存操作接口
- 缓存配置模型
- 缓存注解定义
- 缓存事件接口

## 📦 Maven 依赖

```xml

<dependency>
    <groupId>com.github.loadup.framework</groupId>
    <artifactId>loadup-components-cache-api</artifactId>
</dependency>
```

## 🚀 主要接口

### 1. CacheBinder

缓存绑定器接口，定义基本的缓存操作：

```java
public interface CacheBinder {
    void put(String key, Object value);

    Object get(String key);

    void remove(String key);

    void clear();

    boolean exists(String key);
}
```

### 2. CacheBinding

缓存绑定接口，提供高级缓存操作：

```java
public interface CacheBinding {
    <T> T get(String key, Class<T> type);

    void put(String key, Object value, long timeout);

    void putIfAbsent(String key, Object value);
    // ...
}
```

### 3. 缓存注解

提供声明式缓存注解：

```java

@Cacheable(key = "user:#{id}")
public User getUserById(Long id) {
    // ...
}

@CacheEvict(key = "user:#{id}")
public void deleteUser(Long id) {
    // ...
}
```

## 📝 使用示例

```java

@Service
public class UserService {

    @Autowired
    private CacheBinding cacheBinding;

    public User getUser(Long id) {
        String key = "user:" + id;
        User user = cacheBinding.get(key, User.class);
        if (user == null) {
            user = loadFromDatabase(id);
            cacheBinding.put(key, user, 3600);
        }
        return user;
    }
}
```

## 📄 许可证

GNU General Public License v3.0 (GPL-3.0)

```
Copyright (C) 2025 LoadUp Framework

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
```

详见 [LICENSE](../../../LICENSE) 文件。

---

**最后更新**: 2025-12-30

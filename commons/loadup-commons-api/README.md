# LoadUp Commons API

## 📋 概述

LoadUp Commons API 提供了框架的核心API接口和基础定义。

## 🎯 功能特性

- 通用API接口定义
- 基础响应模型
- 异常体系
- 通用工具接口

## 📦 Maven 依赖

```xml

<dependency>
    <groupId>com.github.loadup.framework</groupId>
    <artifactId>loadup-commons-api</artifactId>
</dependency>
```

## 🚀 主要内容

### 1. 响应模型

提供统一的响应模型：

```java
public class Result<T> {
    private int    code;
    private String message;
    private T      data;
    // ...
}
```

### 2. 异常定义

统一的异常体系：

```java
public class BusinessException extends RuntimeException {
    private int    code;
    private String message;
    // ...
}
```

### 3. 通用接口

定义通用的接口规范。

## 📝 使用示例

```java
// 返回成功响应
return Result.success(data);

// 返回失败响应
return Result.

error("操作失败");

// 抛出业务异常
throw new

BusinessException("业务处理失败");
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

详见 [LICENSE](../../LICENSE) 文件。

---

**最后更新**: 2025-12-30

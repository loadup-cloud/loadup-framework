# LoadUp Commons Lang

## 📋 概述

LoadUp Commons Lang 提供了常用的语言增强工具和通用功能。

## 🎯 功能特性

- 字符串工具类
- 日期时间工具类
- 集合工具类
- 对象工具类
- JSON工具类
- 加密解密工具

## 📦 Maven 依赖

```xml
<dependency>
    <groupId>com.github.loadup.framework</groupId>
    <artifactId>loadup-commons-lang</artifactId>
</dependency>
```

## 🚀 主要工具类

### 1. StringUtils

字符串操作工具：

```java
// 判空
StringUtils.isEmpty(str);
StringUtils.

isNotEmpty(str);

// 格式化
StringUtils.

format("Hello {}",name);

// 驼峰转换
StringUtils.

toCamelCase("user_name");
StringUtils.

toSnakeCase("userName");
```

### 2. DateUtils

日期时间工具：

```java
// 格式化
DateUtils.format(date, "yyyy-MM-dd HH:mm:ss");

// 解析
DateUtils.

parse("2025-12-30","yyyy-MM-dd");

// 计算
DateUtils.

addDays(date, 7);
DateUtils.

daysBetween(startDate, endDate);
```

### 3. CollectionUtils

集合操作工具：

```java
// 判空
CollectionUtils.isEmpty(list);
CollectionUtils.

isNotEmpty(list);

// 转换
CollectionUtils.

toList(array);
CollectionUtils.

toSet(list);
```

### 4. JsonUtils

JSON操作工具：

```java
// 对象转JSON
String json = JsonUtils.toJson(object);

// JSON转对象
User user = JsonUtils.fromJson(json, User.class);

// JSON转列表
List<User> users = JsonUtils.fromJsonList(json, User.class);
```

### 5. CryptoUtils

加密解密工具：

```java
// MD5
String md5 = CryptoUtils.md5("password");

// SHA256
String sha256 = CryptoUtils.sha256("data");

// AES加密
String encrypted = CryptoUtils.aesEncrypt(data, key);
String decrypted = CryptoUtils.aesDecrypt(encrypted, key);
```

## 📝 使用示例

```java
import com.github.loadup.commons.lang.StringUtils;
import com.github.loadup.commons.lang.DateUtils;

// 字符串操作
if (StringUtils.isNotEmpty(username)) {
    String formatted = StringUtils.format("Hello, {}!", username);
}

// 日期操作
Date tomorrow = DateUtils.addDays(new Date(), 1);
String dateStr = DateUtils.format(tomorrow, "yyyy-MM-dd");
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

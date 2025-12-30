# LoadUp Commons Util

## 📋 概述

LoadUp Commons Util 提供了通用的工具类和辅助功能。

## 🎯 功能特性

- HTTP工具类
- 文件操作工具
- 验证工具类
- ID生成器
- 树形结构工具
- Excel导入导出

## 📦 Maven 依赖

```xml

<dependency>
    <groupId>com.github.loadup.framework</groupId>
    <artifactId>loadup-commons-util</artifactId>
</dependency>
```

## 🚀 主要工具类

### 1. HttpUtils

HTTP请求工具：

```java
// GET请求
String response = HttpUtils.get("https://api.example.com/data");

// POST请求
String response = HttpUtils.post("https://api.example.com/submit", jsonData);

// 带参数的请求
Map<String, String> params = new HashMap<>();
params.

put("key","value");

String response = HttpUtils.get(url, params);
```

### 2. FileUtils

文件操作工具：

```java
// 读取文件
String content = FileUtils.readFileToString(file);

// 写入文件
FileUtils.

writeStringToFile(file, content);

// 复制文件
FileUtils.

copyFile(srcFile, destFile);

// 删除文件
FileUtils.

deleteFile(file);
```

### 3. ValidateUtils

数据验证工具：

```java
// 邮箱验证
boolean isEmail = ValidateUtils.isEmail("user@example.com");

// 手机号验证
boolean isMobile = ValidateUtils.isMobile("13800138000");

// 身份证验证
boolean isIdCard = ValidateUtils.isIdCard("110101199001011234");

// URL验证
boolean isUrl = ValidateUtils.isUrl("https://www.example.com");
```

### 4. IdGenerator

ID生成器：

```java
// 生成UUID
String uuid = IdGenerator.uuid();

// 生成雪花ID
long snowflakeId = IdGenerator.snowflake();

// 生成短ID
String shortId = IdGenerator.shortId();
```

### 5. TreeUtils

树形结构工具：

```java
// 列表转树形结构
List<TreeNode> tree = TreeUtils.buildTree(nodeList);

// 查找节点
TreeNode node = TreeUtils.findNode(tree, nodeId);

// 获取所有子节点
List<TreeNode> children = TreeUtils.getAllChildren(node);
```

### 6. ExcelUtils

Excel操作工具：

```java
// 导出Excel
ExcelUtils.export(dataList, User .class, "users.xlsx");

// 导入Excel
List<User> users = ExcelUtils.importExcel(file, User.class);

// 自定义导出
ExcelWriter writer = ExcelUtils.getWriter();
writer.

write(dataList);
writer.

flush();
```

## 📝 使用示例

```java
import com.github.loadup.commons.util.*;

// HTTP请求
String response = HttpUtils.get("https://api.example.com/users");

        // 文件操作
        String content = FileUtils.readFileToString(new File("data.txt"));

        // 数据验证
if(ValidateUtils.

        isEmail(email)){
        // 发送邮件
        }

        // 生成ID
        String orderId = IdGenerator.uuid();
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


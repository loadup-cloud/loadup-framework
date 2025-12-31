# LoadUp BOM (Bill of Materials)

## 📋 概述

LoadUp BOM 是 LoadUp Framework 的依赖管理模块，提供统一的依赖版本管理。

## 🎯 功能特性

- 统一管理所有框架组件的版本
- 统一管理第三方依赖的版本
- 避免版本冲突
- 简化依赖配置

## 📦 使用方法

### 在父 POM 中引入

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.github.loadup.framework</groupId>
            <artifactId>loadup-framework-bom</artifactId>
            <version>${loadup.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 使用托管的依赖

引入 BOM 后，可以不指定版本直接使用：

```xml

<dependencies>
    <!-- LoadUp 组件 -->
    <dependency>
        <groupId>com.github.loadup.framework</groupId>
        <artifactId>loadup-commons-lang</artifactId>
        <!-- 版本由 BOM 管理，不需要指定 -->
    </dependency>

    <!-- 第三方依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <!-- 版本由 BOM 管理 -->
    </dependency>
</dependencies>
```

## 📚 管理的依赖

### LoadUp Framework 组件

- loadup-commons-api
- loadup-commons-dto
- loadup-commons-lang
- loadup-commons-util
- loadup-components-database
- loadup-components-cache
- loadup-components-scheduler
- 等等...

### 第三方依赖

- Spring Boot
- Spring Cloud
- MyBatis
- Redis
- 等等...

## 🔧 最佳实践

1. **统一版本管理**: 在父 POM 中引入 BOM
2. **简化配置**: 子模块不需要指定版本号
3. **避免冲突**: 所有依赖版本经过兼容性验证

## 📄 许可证

GNU General Public License v3.0 (GPL-3.0)

```
Copyright (C) 2025 LoadUp Framework

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
```

详见 [LICENSE](../LICENSE) 文件。

---

**最后更新**: 2025-12-30

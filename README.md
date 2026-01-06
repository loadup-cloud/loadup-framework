# LoadUp Framework

LoadUp Framework 是一个基于 Spring Boot 的微服务开发框架，提供了一系列可复用的组件和最佳实践。

## 目录

- [项目结构](#项目结构)
- [技术栈](#技术栈)
- [快速开始 - 使用 BOM](#快速开始---使用-bom)
- [所有可用组件](#所有可用组件)
- [使用场景示例](#使用场景示例)
- [开发规范](#开发规范)
  - [代码格式化](#代码格式化) 🔥 **新增**
- [版本升级](#版本升级)
- [常见问题](#常见问题)
- [如何贡献](#如何贡献)
- [许可证](#许可证)

## 项目结构

```
loadup-framework/
├── bom/                    # 依赖版本管理 (Bill of Materials)
├── commons/               # 通用工具类
│   ├── commons-api       # API相关定义
│   ├── commons-dto      # 数据传输对象
│   ├── commons-lang     # 基础工具类
│   └── commons-util     # 通用工具类
└── components/           # 功能组件
    ├── cache            # 缓存组件 (支持 Redis/Caffeine)
    ├── captcha          # 验证码组件
    ├── database         # 数据库组件
    ├── extension        # 扩展机制
    ├── liquibase        # 数据库版本管理
    ├── scheduler        # 调度组件 (支持 Quartz/XXL-Job)
    └── tracer          # 链路追踪
```

## 技术栈

- Java 17
- Spring Boot 3.1.2
- Spring Cloud 2022.0.4
- COLA 4.3.2
- Maven (依赖管理)

---

## 快速开始 - 使用 BOM

LoadUp Framework 提供了 BOM (Bill of Materials) 来统一管理所有组件的版本，让您的项目可以方便地引入框架组件。

### 步骤 1: 配置 Maven Repository

在项目的 `pom.xml` 或 `settings.xml` 中配置 GitHub Packages 仓库：

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/loadup-cloud/loadup-packages</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

### 步骤 2: 在项目中引入 BOM

在您的项目 `pom.xml` 的 `<dependencyManagement>` 部分添加：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.github.loadup.framework</groupId>
            <artifactId>loadup-framework-bom</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 步骤 3: 添加所需组件（无需指定版本）

引入 BOM 后，您可以在 `<dependencies>` 部分添加所需的组件，**无需指定版本号**：

```xml
<dependencies>
    <!-- Commons 组件 -->
    <dependency>
        <groupId>com.github.loadup.commons</groupId>
        <artifactId>loadup-commons-api</artifactId>
    </dependency>

    <!-- 数据库组件 -->
    <dependency>
        <groupId>com.github.loadup.components</groupId>
        <artifactId>loadup-components-database</artifactId>
    </dependency>

    <!-- Redis 缓存 -->
    <dependency>
        <groupId>com.github.loadup.components</groupId>
        <artifactId>loadup-components-cache-binder-redis</artifactId>
    </dependency>
</dependencies>
```

### BOM 的优势

✅ **统一版本管理** - 所有组件版本由 BOM 统一管理，避免版本冲突

✅ **简化依赖声明** - 引入组件时无需指定版本号，降低维护成本

✅ **易于升级** - 只需修改 BOM 版本号即可升级所有相关组件

✅ **确保兼容性** - 所有组件版本经过测试，确保互相兼容

---

## 所有可用组件

### Commons 模块

|      artifactId       |        说明         |
|-----------------------|-------------------|
| `loadup-commons-api`  | API 公共组件，包含通用接口定义 |
| `loadup-commons-dto`  | DTO 公共组件，包含数据传输对象 |
| `loadup-commons-lang` | 语言工具组件，提供基础工具类    |
| `loadup-commons-util` | 通用工具类组件           |

### Components - 数据库

|          artifactId           |        说明         |
|-------------------------------|-------------------|
| `loadup-components-database`  | 数据库访问组件           |
| `loadup-components-liquibase` | Liquibase 数据库版本管理 |

### Components - 缓存

|                artifactId                 |       说明        |
|-------------------------------------------|-----------------|
| `loadup-components-cache-api`             | 缓存抽象层 API       |
| `loadup-components-cache-binder-caffeine` | Caffeine 本地缓存实现 |
| `loadup-components-cache-binder-redis`    | Redis 分布式缓存实现   |

### Components - 调度器

|              artifactId              |       说明        |
|--------------------------------------|-----------------|
| `loadup-components-scheduler-api`    | 调度器抽象层 API      |
| `loadup-components-scheduler-quartz` | Quartz 调度器实现    |
| `loadup-components-scheduler-xxljob` | XXL-Job 分布式调度实现 |

### Components - 其他

|          artifactId           |             说明             |
|-------------------------------|----------------------------|
| `loadup-components-extension` | 扩展点机制                      |
| `loadup-components-captcha`   | 验证码生成与验证                   |
| `loadup-components-tracer`    | 分布式链路追踪 (基于 OpenTelemetry) |

---

## 使用场景示例

### 场景 1: 构建 Web 应用

```xml
<dependencies>
    <!-- 基础 API -->
    <dependency>
        <groupId>com.github.loadup.commons</groupId>
        <artifactId>loadup-commons-api</artifactId>
    </dependency>

    <!-- 数据库访问 -->
    <dependency>
        <groupId>com.github.loadup.components</groupId>
        <artifactId>loadup-components-database</artifactId>
    </dependency>

    <!-- Redis 缓存 -->
    <dependency>
        <groupId>com.github.loadup.components</groupId>
        <artifactId>loadup-components-cache-binder-redis</artifactId>
    </dependency>

    <!-- 验证码 -->
    <dependency>
        <groupId>com.github.loadup.components</groupId>
        <artifactId>loadup-components-captcha</artifactId>
    </dependency>
</dependencies>
```

### 场景 2: 构建定时任务服务

```xml
<dependencies>
    <!-- 基础工具 -->
    <dependency>
        <groupId>com.github.loadup.commons</groupId>
        <artifactId>loadup-commons-util</artifactId>
    </dependency>

    <!-- XXL-Job 分布式调度 -->
    <dependency>
        <groupId>com.github.loadup.components</groupId>
        <artifactId>loadup-components-scheduler-xxljob</artifactId>
    </dependency>

    <!-- 数据库 -->
    <dependency>
        <groupId>com.github.loadup.components</groupId>
        <artifactId>loadup-components-database</artifactId>
    </dependency>
</dependencies>
```

### 场景 3: 构建微服务

```xml
<dependencies>
    <!-- 完整的 Commons 支持 -->
    <dependency>
        <groupId>com.github.loadup.commons</groupId>
        <artifactId>loadup-commons-api</artifactId>
    </dependency>
    <dependency>
        <groupId>com.github.loadup.commons</groupId>
        <artifactId>loadup-commons-dto</artifactId>
    </dependency>

    <!-- 数据库与数据库版本管理 -->
    <dependency>
        <groupId>com.github.loadup.components</groupId>
        <artifactId>loadup-components-database</artifactId>
    </dependency>
    <dependency>
        <groupId>com.github.loadup.components</groupId>
        <artifactId>loadup-components-liquibase</artifactId>
    </dependency>

    <!-- 分布式追踪 -->
    <dependency>
        <groupId>com.github.loadup.components</groupId>
        <artifactId>loadup-components-tracer</artifactId>
    </dependency>

    <!-- 扩展点机制 -->
    <dependency>
        <groupId>com.github.loadup.components</groupId>
        <artifactId>loadup-components-extension</artifactId>
    </dependency>
</dependencies>
```

### 完整示例项目配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>my-application</artifactId>
    <version>1.0.0</version>

    <properties>
        <java.version>${java.version}</java.version>
        <loadup.framework.version>1.0.0-SNAPSHOT</loadup.framework.version>
    </properties>

    <!-- 引入 LoadUp Framework BOM -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.github.loadup.framework</groupId>
                <artifactId>loadup-framework-bom</artifactId>
                <version>${loadup.framework.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!-- 使用 LoadUp Framework 组件，无需指定版本 -->
    <dependencies>
        <dependency>
            <groupId>com.github.loadup.commons</groupId>
            <artifactId>loadup-commons-api</artifactId>
        </dependency>

        <dependency>
            <groupId>com.github.loadup.components</groupId>
            <artifactId>loadup-components-database</artifactId>
        </dependency>

        <dependency>
            <groupId>com.github.loadup.components</groupId>
            <artifactId>loadup-components-cache-binder-redis</artifactId>
        </dependency>
    </dependencies>

    <!-- 配置仓库 -->
    <repositories>
        <repository>
            <id>github</id>
            <url>https://maven.pkg.github.com/loadup-cloud/loadup-packages</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
</project>
```

---

## 开发规范

### 模块命名规范

- **通用模块**: `loadup-commons-*`
- **组件模块**: `loadup-components-*`
- **业务模块**: `loadup-modules-*`

### 版本号规范

- **SNAPSHOT版本**: `x.y.z-SNAPSHOT` (开发版本)
- **发布版本**: `x.y.z` (正式版本)
- **内部测试版本**: `x.y.z-alpha` / `x.y.z-beta`

### 代码规范

- 使用 **Spotless** 进行代码格式化
- 遵循 **Google Java Style Guide** 编码风格
- 所有公共 API 必须有完整的 **JavaDoc**
- 编写单元测试，确保代码质量

#### 代码格式化

本项目使用 Spotless 自动格式化代码（配置继承自 `loadup-dependencies`）。

首次使用请安装 Git hooks：

```bash
./install-git-hooks.sh
```

安装后，每次 push 前会自动检查代码格式。手动格式化：

```bash
./spotless.sh apply   # 格式化代码
./spotless.sh check   # 检查格式
```

> **📝 注意**: Spotless 插件配置在 `loadup-dependencies` parent POM 中，所有子项目自动继承。

详细说明: [CODE_FORMATTING.md](CODE_FORMATTING.md)

---

## 版本升级

当需要升级 LoadUp Framework 版本时，只需修改 BOM 的版本号：

```xml
<dependency>
    <groupId>com.github.loadup.framework</groupId>
    <artifactId>loadup-framework-bom</artifactId>
    <version>1.1.0-SNAPSHOT</version> <!-- 修改这里 -->
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

所有使用的 LoadUp Framework 组件都会自动升级到对应版本。

---

## 常见问题

### Q: 为什么要使用 BOM？

**A:** BOM 提供了以下好处：

1. **统一版本管理** - 避免不同组件版本不兼容的问题
2. **简化配置** - 无需为每个依赖指定版本号
3. **便于升级** - 只需修改一处即可升级所有组件
4. **减少错误** - 降低版本冲突的风险

### Q: 可以覆盖 BOM 中的版本吗？

**A:** 可以。如果需要使用特定版本的组件，可以在依赖声明中显式指定版本号：

```xml
<dependency>
    <groupId>com.github.loadup.components</groupId>
    <artifactId>loadup-components-cache-api</artifactId>
    <version>1.0.1-SNAPSHOT</version> <!-- 覆盖 BOM 中的版本 -->
</dependency>
```

但不建议这样做，除非有特殊需求。

### Q: BOM 和 Parent POM 有什么区别？

**A:**

- **BOM** 只提供依赖版本管理，不会继承任何配置
- **Parent POM** 会继承所有配置（插件、属性、依赖等）

使用 BOM（通过 `<scope>import</scope>`）更灵活，不会强制继承不需要的配置。

### Q: 如何查看可用的组件列表？

**A:** 查看本文档的 [所有可用组件](#所有可用组件) 章节，或查看 `bom/pom.xml` 文件。

---

## 如何贡献

我们欢迎所有形式的贡献！

1. **Fork** 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交变更 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 **Pull Request**

### 贡献指南

- 遵循项目的代码规范
- 为新功能添加单元测试
- 更新相关文档
- 确保所有测试通过

---

## 构建和发布

### 构建项目

```bash
mvn clean install
```

### 发布到 GitHub Packages

```bash
mvn clean deploy
```

### 仅发布 BOM

```bash
mvn clean deploy -pl bom
```

---

## 注意事项

1. ✅ 确保在 `settings.xml` 中配置了 GitHub Packages 的认证信息
2. ✅ BOM 只管理 LoadUp Framework 自身的组件版本，不管理第三方依赖
3. ✅ 建议在项目中使用 BOM 来统一管理所有 LoadUp Framework 组件的版本
4. ⚠️ 避免混用不同版本的 LoadUp Framework 组件
5. ⚠️ 不建议覆盖 BOM 中的版本（除非有特殊需求）

---

## 许可证

本项目采用 [GNU General Public License v3.0 (GPL-3.0)](LICENSE) 许可证。

```
Copyright (C) 2025 LoadUp Framework

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

---

## 联系方式

如有问题或建议，请通过以下方式联系我们：

- 提交 [Issue](https://github.com/loadup-cloud/loadup-framework/issues)
- 创建 [Pull Request](https://github.com/loadup-cloud/loadup-framework/pulls)

---

**© 2025 LoadUp Framework. All rights reserved.**

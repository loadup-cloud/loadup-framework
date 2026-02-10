# LoadUp Framework

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-green.svg)](https://spring.io/projects/spring-boot)
[![MyBatis-Flex](https://img.shields.io/badge/MyBatis--Flex-1.11.5-orange.svg)](https://mybatis-flex.com/)
[![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE)

LoadUp Framework 是一个基于 Spring Boot 3.4.3 的**企业级微服务开发框架**，采用 **Monorepo（单仓库）多模块架构**，提供可复用的基础组件和最佳实践，帮助团队快速构建高质量的企业应用。

## 📚 目录

- [核心特性](#核心特性)
- [架构概览](#架构概览)
- [项目结构](#项目结构)
- [技术栈](#技术栈)
- [快速开始](#快速开始)
- [所有可用组件](#所有可用组件)
- [使用场景示例](#使用场景示例)
- [架构设计](#架构设计)
- [开发规范](#开发规范)
- [测试策略](#测试策略)
- [部署指南](#部署指南)
- [常见问题](#常见问题)
- [如何贡献](#如何贡献)
- [许可证](#许可证)

---

## 🎯 核心特性

- ✅ **模块化设计** - 清晰的模块边界，单一职责原则
- ✅ **依赖倒置** - 高层模块不依赖低层模块，都依赖抽象
- ✅ **可扩展性** - 插件化架构，支持业务定制扩展
- ✅ **开箱即用** - 自动配置，最小化配置原则
- ✅ **企业级** - 高性能、高可用、可观测、安全
- ✅ **统一版本管理** - BOM 统一管理所有依赖版本
- ✅ **分层架构** - 严格的分层架构和依赖规则

---

## 📊 架构概览

### 整体架构图

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         LoadUp Framework                                 │
│                      (Monorepo 单仓库架构)                                │
└──────────────────────────────────────────────────────────────────────────┘

        ┌───────────────────────────────────────────────────┐
        │         🚀 应用层 (Application Layer)              │
        │                                                    │
        │    loadup-application  (Spring Boot 启动器)        │
        └──────────────────┬─────────────────────────────────┘
                           │ 依赖所有需要的模块
                           ▼
        ┌─────────────────────────────────────────────────────────┐
        │         🎯 业务模块层 (Business Modules Layer)         │
        │                                                          │
        │  modules/                                                │
        │    └── loadup-modules-upms/  (用户权限管理系统)         │
        │         ├── api/            (对外接口)                   │
        │         ├── application/    (应用服务)                   │
        │         ├── domain/         (领域模型)                   │
        │         ├── infrastructure/ (基础设施)                   │
        │         └── test/           (测试)                       │
        └────────────────────┬────────────────────────────────────┘
                             │ 依赖
                             ▼
        ┌──────────────────────────────────────────────────────────┐
        │      🌐 中间件层 (Middleware Layer)                       │
        │      (独立服务，提供横切关注点)                            │
        │                                                           │
        │  middleware/                                              │
        │    ├── loadup-gateway/     (API 网关)                     │
        │    └── loadup-testify/     (测试框架)                     │
        └────────────────────┬────────────────────────────────────┘
                             │ 可以依赖
                             ▼
        ┌────────────────────────────────────────────────────────────────┐
        │         🔧 技术组件层 (Technical Components Layer)            │
        │         (可复用的技术能力，零业务逻辑)                         │
        │                                                                 │
        │  components/                                                    │
        │  ├── 🛡️ 安全组件: authorization, captcha                      │
        │  ├── 💾 核心组件: database, cache, extension                  │
        │  ├── 📁 数据组件: dfs, flyway                                 │
        │  ├── 📡 通信组件: gotone (邮件/短信/推送/Webhook)              │
        │  ├── ⏰ 调度组件: scheduler                                   │
        │  ├── 📊 观测组件: tracer                                      │
        │  └── 🧪 测试组件: testcontainers                              │
        └────────────────────┬────────────────────────────────────────┘
                             │ 依赖
                             ▼
        ┌────────────────────────────────────────────────────────────┐
        │         🧱 基础设施层 (Infrastructure - Commons)            │
        │         (最底层的通用基础能力)                              │
        │                                                              │
        │  commons/                                                    │
        │    ├── loadup-commons-api/   (通用接口、异常、枚举)         │
        │    ├── loadup-commons-dto/   (通用 DTO、BaseDO、Result)     │
        │    └── loadup-commons-util/  (工具类：JSON、日期、加密等)   │
        └────────────────────┬────────────────────────────────────────┘
                             │ 依赖
                             ▼
        ┌────────────────────────────────────────────────────────────┐
        │         📦 依赖管理层 (Dependencies - BOM)                  │
        │                                                              │
        │  loadup-dependencies/  (统一版本管理)                       │
        └──────────────────────────────────────────────────────────────┘
```

### 依赖流向规则

```
Application (应用层)
    ↓ 可依赖所有
┌───────────────────┐
│ Modules  Middleware│
└────────┬───────────┘
         ↓ 可依赖
    Components (组件层)
         ↓ 必须依赖
    Commons (基础设施)
         ↓ 必须依赖
   Dependencies (BOM)

❌ 禁止:
   - Commons 依赖 Components
   - Components 循环依赖
   - Modules 直接依赖 Modules
```

---

---

## 📂 项目结构

LoadUp Framework 采用清晰的 **6 层分层架构**，共 19 个模块：

### 1️⃣ 依赖管理层 (1 个模块)

```
📦 loadup-dependencies/  - BOM 依赖版本管理
```

**职责**: 统一管理所有第三方依赖版本，确保版本一致性

### 2️⃣ 基础设施层 (3 个模块)

```
🧱 commons/
├── loadup-commons-api/   - 通用接口、异常、枚举
├── loadup-commons-dto/   - 通用 DTO、BaseDO、Result
└── loadup-commons-util/  - 工具类集合 (JSON、日期、加密等)
```

**特点**: 零业务逻辑，纯基础能力

### 3️⃣ 技术组件层 (11 个组件)

```
🔧 components/
├── 🛡️ 安全组件 (2)
│   ├── loadup-components-authorization   - 权限校验、RBAC
│   └── loadup-components-captcha         - 验证码生成与校验
├── 💾 核心组件 (3)
│   ├── loadup-components-database        - 数据库访问 (MyBatis-Flex)
│   ├── loadup-components-cache           - 缓存抽象 (Redis/Caffeine)
│   └── loadup-components-extension       - 扩展点机制
├── 📁 数据组件 (2)
│   ├── loadup-components-dfs             - 分布式文件存储 (S3/本地/数据库)
│   └── loadup-components-flyway          - 数据库版本管理
├── 📡 通信组件 (1)
│   └── loadup-components-gotone          - 通知服务
│        ├── api/                         (核心接口)
│        ├── core/                        (核心实现)
│        ├── starter/                     (自动配置)
│        └── channels/                    (渠道实现)
│             ├── email/                  (SMTP 邮件)
│             ├── sms/                    (阿里云/华为云/云片短信)
│             ├── push/                   (FCM 推送)
│             └── webhook/                (钉钉/企业微信/飞书)
├── ⏰ 调度组件 (1)
│   └── loadup-components-scheduler       - 任务调度 (Quartz/XXL-Job/PowerJob)
├── 📊 观测组件 (1)
│   └── loadup-components-tracer          - 链路追踪 (OpenTelemetry/SkyWalking)
└── 🧪 测试组件 (1)
    └── loadup-components-testcontainers  - 测试容器集成
```

**特点**: 可独立使用、自动配置、支持多种实现、零业务逻辑

### 4️⃣ 业务模块层 (1 个模块)

```
🎯 modules/
└── loadup-modules-upms/  - 用户权限管理系统 (DDD 架构)
     ├── api/            - 对外接口 (Controller, DTO)
     ├── application/    - 应用服务层
     ├── domain/         - 领域模型层
     ├── infrastructure/ - 基础设施层 (Mapper, DO)
     └── test/           - 测试
```

**功能**: 用户管理、角色管理、权限管理、部门管理、菜单管理

### 5️⃣ 中间件层 (2 个中间件)

```
🌐 middleware/
├── loadup-gateway/   - API 网关
│    ├── core/       (核心引擎、Action 机制)
│    ├── facade/     (路由管理、RouteConfig)
│    ├── starter/    (自动配置)
│    └── test/       (测试)
└── loadup-testify/  - 测试框架
     ├── assert-engine/  (断言引擎)
     ├── data-engine/    (数据引擎)
     ├── mock-engine/    (Mock 引擎)
     ├── core/           (核心)
     ├── starter/        (自动配置)
     └── test/           (测试)
```

**Gateway 功能**: 动态路由、认证鉴权、签名验签、请求响应包装、链路追踪

**Testify 功能**: 数据驱动测试、Mock 引擎、断言引擎、TestContainers 集成

### 6️⃣ 应用层 (1 个应用)

```
🚀 loadup-application/  - Spring Boot 应用启动器
```

**职责**: 应用入口、组装模块、运行时配置、生成可执行 JAR

---

## 🛠️ 技术栈

## 🛠️ 技术栈

| 技术领域 | 技术选型 | 版本 | 说明 |
|--------|---------|------|------|
| 编程语言 | Java | 21 | LTS 长期支持版本 |
| 应用框架 | Spring Boot | 3.4.3 | 企业级应用框架 |
| 持久层框架 | MyBatis-Flex | 1.11.5 | 类型安全的 ORM 框架 |
| 数据库 | MySQL | 8.0+ | 关系型数据库 |
| 缓存 | Redis (Redisson) | - | 分布式缓存 |
| 本地缓存 | Caffeine | - | 高性能本地缓存 |
| 服务治理 | Dubbo | 3.2.8 | RPC 框架 |
| 链路追踪 | OpenTelemetry | 1.57.0 | 分布式追踪 |
| 任务调度 | Quartz/XXL-Job/PowerJob | - | 定时任务 |
| 认证授权 | JWT | - | 无状态认证 |
| API 文档 | OpenAPI (Swagger) | v3 | 接口文档 |
| 测试框架 | JUnit 5, Mockito, Testcontainers | - | 单元/集成测试 |
| 构建工具 | Maven | 3.6+ | 项目构建 |
| 代码格式化 | Spotless | - | 统一代码风格 |

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
            <groupId>io.github.loadup-cloud</groupId>
            <artifactId>bom</artifactId>
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
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-commons-api</artifactId>
    </dependency>

    <!-- 数据库组件 -->
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-components-database</artifactId>
    </dependency>

    <!-- Redis 缓存 -->
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
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
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-commons-api</artifactId>
    </dependency>

    <!-- 数据库访问 -->
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-components-database</artifactId>
    </dependency>

    <!-- Redis 缓存 -->
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-components-cache-binder-redis</artifactId>
    </dependency>

    <!-- 验证码 -->
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-components-captcha</artifactId>
    </dependency>
</dependencies>
```

### 场景 2: 构建定时任务服务

```xml
<dependencies>
    <!-- 基础工具 -->
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-commons-util</artifactId>
    </dependency>

    <!-- XXL-Job 分布式调度 -->
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-components-scheduler-xxljob</artifactId>
    </dependency>

    <!-- 数据库 -->
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-components-database</artifactId>
    </dependency>
</dependencies>
```

### 场景 3: 构建微服务

```xml
<dependencies>
    <!-- 完整的 Commons 支持 -->
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-commons-api</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-commons-dto</artifactId>
    </dependency>

    <!-- 数据库与数据库版本管理 -->
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-components-database</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-components-liquibase</artifactId>
    </dependency>

    <!-- 分布式追踪 -->
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-components-tracer</artifactId>
    </dependency>

    <!-- 扩展点机制 -->
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
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
                <groupId>io.github.loadup-cloud</groupId>
                <artifactId>bom</artifactId>
                <version>${loadup.framework.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!-- 使用 LoadUp Framework 组件，无需指定版本 -->
    <dependencies>
        <dependency>
            <groupId>io.github.loadup-cloud</groupId>
            <artifactId>loadup-commons-api</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.loadup-cloud</groupId>
            <artifactId>loadup-components-database</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.loadup-cloud</groupId>
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

---

## 🏗️ 架构设计

### 设计理念

- **模块化设计**: 清晰的模块边界，单一职责原则
- **依赖倒置**: 高层模块不依赖低层模块，都依赖抽象
- **可扩展性**: 插件化架构，支持业务定制扩展
- **开箱即用**: 自动配置，最小化配置原则
- **企业级**: 高性能、高可用、可观测、安全

### 分层架构

```
┌─────────────────────────────────────────┐
│         表现层 (Presentation)             │  Controller, REST API
├─────────────────────────────────────────┤
│          应用层 (Application)             │  Service, 业务编排
├─────────────────────────────────────────┤
│           领域层 (Domain)                 │  Entity, VO, Repository Interface
├─────────────────────────────────────────┤
│        基础设施层 (Infrastructure)         │  Mapper, Cache, External API
├─────────────────────────────────────────┤
│         数据层 (Data)                     │  MySQL, Redis
└─────────────────────────────────────────┘
```

### 核心设计模式

#### 1. 策略模式 (Strategy Pattern)
- **应用场景**: ID 生成策略、缓存实现策略、调度器实现策略
- **优势**: 运行时动态选择算法，易于扩展

#### 2. 模板方法模式 (Template Method Pattern)
- **应用场景**: BaseMapper 通用 CRUD、测试基类
- **优势**: 复用通用流程，子类只需实现特定步骤

#### 3. 观察者模式 (Observer Pattern)
- **应用场景**: 审计回调、Spring Event 事件驱动
- **优势**: 松耦合的事件通知机制

#### 4. 代理模式 (Proxy Pattern)
- **应用场景**: Gateway 路由代理、AOP 拦截、Testify Mock
- **优势**: 透明的功能增强

#### 5. 工厂模式 (Factory Pattern)
- **应用场景**: Spring Bean 工厂、插件工厂
- **优势**: 解耦对象创建和使用

### API 设计规范

#### RESTful API 规范

```
GET    /api/{module}/{resource}           # 列表查询
GET    /api/{module}/{resource}/{id}      # 单个查询
POST   /api/{module}/{resource}           # 创建
PUT    /api/{module}/{resource}/{id}      # 更新
DELETE /api/{module}/{resource}/{id}      # 删除
```

#### 统一响应格式

```json
{
  "code": "0",
  "message": "success",
  "data": { ... },
  "timestamp": "2026-02-10T10:00:00Z"
}
```

#### 分页查询

**请求参数**:
```java
public class PageQuery {
    private Integer pageNum = 1;   // 页码（从 1 开始）
    private Integer pageSize = 10; // 每页大小
    private String sortBy;         // 排序字段
    private String sortOrder;      // 排序方向（asc/desc）
}
```

**响应格式**:
```json
{
  "code": "0",
  "message": "success",
  "data": {
    "records": [ ... ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10
  }
}
```

### 数据库设计规范

#### 表命名规范

- **系统表前缀**: `sys_`（如 `sys_config`）
- **业务表前缀**: `t_`（如 `t_user`, `t_order`）
- **关联表**: `{table1}_{table2}`（如 `user_role`）
- **命名风格**: snake_case（小写 + 下划线）

#### 基础审计字段（所有表必须包含）

```sql
id              VARCHAR(64)  NOT NULL PRIMARY KEY COMMENT '主键ID',
created_by      VARCHAR(64)  COMMENT '创建人',
created_at      DATETIME     COMMENT '创建时间',
updated_by      VARCHAR(64)  COMMENT '更新人',
updated_at      DATETIME     COMMENT '更新时间'
```

#### 索引规范

- **主键**: 使用字符串 ID（由 ID 生成器生成）
- **唯一索引**: `uk_{column_name}`
- **普通索引**: `idx_{column_name}`
- **联合索引**: `idx_{table}_{column1}_{column2}`

### 安全设计

#### 认证与授权

- **认证方式**: JWT Token
- **Token 存储**: Header: `Authorization: Bearer {token}`
- **权限控制**: 基于 RBAC（角色 + 权限）
- **数据权限**: 支持多级数据范围（全部/部门/本人）

#### 敏感信息保护

- 密码字段必须标注 `@JsonIgnore`
- 敏感信息脱敏（手机号、身份证、邮箱）
- 日志中禁止打印 Token 和密码

#### SQL 注入防护

- 严禁字符串拼接 SQL
- 使用 MyBatis-Flex QueryWrapper 或参数化查询
- 对用户输入进行严格校验

### 性能优化策略

#### 多级缓存

```
Request → 本地缓存 (Caffeine) → 分布式缓存 (Redis) → 数据库
```

**缓存模式**:
- Cache-Aside（旁路缓存）
- Read-Through（读穿透）
- Write-Through（写穿透）

#### 数据库优化

- **连接池**: HikariCP（默认）
- **批量操作**: `insertBatch` / `updateBatch`
- **分页查询**: MyBatis-Flex Page 对象
- **慢查询监控**: p6spy 日志监控

#### 并发控制

- 乐观锁（version 字段）
- 分布式锁（Redis/Redisson）
- 限流（Resilience4j RateLimiter）

### 可观测性

#### 日志

- **框架**: SLF4J + Logback
- **级别**: ERROR < WARN < INFO < DEBUG < TRACE
- **格式**: JSON 格式（方便日志采集）
- **MDC**: 请求 ID、用户 ID、租户 ID

#### 链路追踪

- **框架**: OpenTelemetry
- **集成**: Jaeger/Zipkin
- **采样率**: 可配置

#### 指标监控

- Spring Boot Actuator
- Micrometer + Prometheus
- 自定义业务指标

---

## 🧪 测试策略

## 🧪 测试策略

### 测试金字塔

```
       ┌──────────────┐
       │  End-to-End  │  (少量, Testify 集成测试)
       └──────────────┘
      ┌────────────────┐
      │  Integration   │  (适量, Testcontainers)
      └────────────────┘
    ┌──────────────────┐
    │   Unit Tests     │  (大量, JUnit + Mockito)
    └──────────────────┘
```

### 测试工具

- **单元测试**: JUnit 5 + Mockito + AssertJ
- **集成测试**: Testcontainers (MySQL, Redis)
- **接口测试**: Testify (YAML 驱动)
- **覆盖率**: JaCoCo

### 测试示例

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserMapper userMapper;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    void testCreateUser() {
        // Given
        UserDO user = UserDO.builder()
            .username("test")
            .email("test@example.com")
            .build();
        when(userMapper.insert(any())).thenReturn(1);
        
        // When
        userService.createUser(user);
        
        // Then
        verify(userMapper, times(1)).insert(user);
    }
}
```

---

## 🚀 部署指南

### 容器化部署

```yaml
# docker-compose.yml
version: '3.8'
services:
  app:
    image: loadup-framework:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/loadup
    depends_on:
      - mysql
      - redis
  
  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=root
      - MYSQL_DATABASE=loadup
  
  redis:
    image: redis:7-alpine
```

### Kubernetes 部署

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: loadup-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: loadup
  template:
    metadata:
      labels:
        app: loadup
    spec:
      containers:
      - name: loadup
        image: loadup-framework:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
```

### 配置管理

- **配置中心**: Nacos / Spring Cloud Config
- **环境隔离**: dev / test / staging / prod
- **敏感配置**: 加密存储

---

## 📋 开发规范

## 📋 开发规范

### 模块命名规范

- **通用模块**: `loadup-commons-*`
- **组件模块**: `loadup-components-*`
- **业务模块**: `loadup-modules-*`
- **中间件模块**: `loadup-gateway-*`, `loadup-testify-*`

### 版本号规范

- **SNAPSHOT版本**: `x.y.z-SNAPSHOT` (开发版本)
- **发布版本**: `x.y.z` (正式版本)
- **内部测试版本**: `x.y.z-alpha` / `x.y.z-beta`

### 代码规范

- 使用 **Spotless** 进行代码格式化
- 遵循 **Google Java Style Guide** 编码风格
- 所有公共 API 必须有完整的 **JavaDoc**
- 编写单元测试，确保代码质量

### 代码格式化

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

### Git 提交规范

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type 类型**:
- `feat`: 新功能
- `fix`: 修复 Bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具链相关

**示例**:
```
feat(database): 添加 MyBatis-Flex 集成

- 实现 BaseMapper 通用 CRUD
- 支持审计字段自动填充
- 集成 p6spy 日志监控

Closes #123
```

---

## ❓ 常见问题

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
    <groupId>io.github.loadup-cloud</groupId>
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

**A:** 查看本文档的 [所有可用组件](#所有可用组件) 章节，或查看 `loadup-dependencies/pom.xml` 文件。

### Q: 组件、模块、中间件如何选择？

**A:** 

| 类型 | 定位 | 特点 | 举例 |
|------|------|------|------|
| **组件** | 可复用技术能力 | 零业务逻辑、自动配置、支持多实现 | database, cache, gotone |
| **模块** | 业务功能实现 | 包含业务逻辑、DDD架构 | upms (用户权限) |
| **中间件** | 独立横切服务 | 提供基础设施能力 | gateway (网关), testify (测试) |

### Q: 如何扩展新的组件？

**A:** 参考现有组件的实现模式：

1. 创建 API 模块（定义接口）
2. 创建实现模块（具体实现）
3. 创建 Starter 模块（自动配置）
4. 添加到 BOM 管理

### Q: 如何贡献代码？

**A:** 参考 [如何贡献](#如何贡献) 章节。

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

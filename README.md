# LoadUp Framework

LoadUp Framework 是一个基于 Spring Boot 的微服务开发框架，提供了一系列可复用的组件和最佳实践。

## 项目结构

```
loadup-framework/
├── bom/                    # 依赖版本管理
├── commons/               # 通用工具类
│   ├── commons-api       # API相关定义
│   ├── commons-dto      # 数据传输对象
│   ├── commons-lang     # 基础工具类
│   └── commons-util     # 通用工具类
└── components/           # 功能组件
    ├── cache            # 缓存组件
    ├── database         # 数据库组件
    ├── gateway          # 网关组件
    ├── liquibase        # 数据库版本管理
    ├── scheduler        # 调度组件
    ├── tracer          # 链路追踪
    └── extension        # 扩展机制
```

## 技术栈

- Java 17
- Spring Boot 3.1.2
- Spring Cloud 2022.0.4
- COLA 4.3.2

## 开发规范

### 模块命名规范

- 通用模块: loadup-commons-*
- 组件模块: loadup-components-*
- 业务模块: loadup-modules-*

### 版本号规范

- SNAPSHOT版本：x.y.z-SNAPSHOT
- 发布版本：x.y.z
- 内部测试版本：x.y.z-alpha/beta

### 代码规范

- 使用Spotless进行代码格式化
- 遵循阿里巴巴Java开发手册
- 所有公共API必须有完整的JavaDoc

## 组件使用指南

### 缓存组件

支持Redis和Caffeine两种实现，可以通过配置灵活切换。

### 网关组件

提供统一的网关接入层，支持动态路由、限流、认证等功能。

### 链路追踪

基于OpenTelemetry提供全链路追踪能力。

## 如何贡献

1. Fork 本仓库
2. 创建特性分支
3. 提交变更
4. 创建Pull Request

## 许可证

[Apache License 2.0](LICENSE.txt)

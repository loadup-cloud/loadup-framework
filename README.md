# LoadUp Framework

LoadUp 是面向 Spring Boot 应用的可复用框架与 SDK。项目通过 BOM 统一依赖版本，集成方按需引入技术组件和通用业务模块，在自己的应用中组合所需能力。

[阅读文档](https://loadup-docs.laysan.site/) · [技术组件](loadup-components/README.md) · [UPMS](loadup-modules/loadup-modules-upms/README.md) · [路线图](ROADMAP.md)

## 项目概况

- **按需集成**：缓存、认证、文件存储等组件可以独立选择；提供多种后端的能力通过 binder 选择实现。
- **遵循 Spring 生态**：业务接口使用 Spring MVC Controller；缓存等能力优先采用已有的标准接口。
- **通用业务能力**：UPMS 提供用户、角色、权限和部门管理，以及 RBAC 与资源属性授权能力。
- **统一版本管理**：`loadup-dependencies` BOM 管理 LoadUp 模块及相关依赖版本。

LoadUp 是供其他应用消费的 SDK。`loadup-application` 用于本地集成验证，不是生产部署单元。

## 能力与目录

| 目录 | 内容 |
| --- | --- |
| [`loadup-dependencies`](loadup-dependencies/README.md) | 统一依赖版本的 BOM |
| [`loadup-commons`](loadup-commons/README.md) | DTO、工具、日志和链路追踪 |
| [`loadup-components`](loadup-components/README.md) | Web MVC、认证授权、缓存、数据库、文件存储、通知、调度等技术组件 |
| [`loadup-modules`](loadup-modules/README.md) | 可复用业务模块，当前包含 UPMS |
| [`loadup-testify`](loadup-testify/README.md) | 集成测试辅助能力 |
| [`loadup-application`](loadup-application/README.md) | 本地集成验证启动器 |

模块的接入方式、配置项和设计说明见各目录的 README 与 ARCHITECTURE 文档，也可在[文档站](https://loadup-docs.laysan.site/docs/)按能力查找。

## 快速接入

项目当前使用 **Java 25**、**Spring Boot 4.1.1** 和 **Maven**。在消费工程中先导入 BOM：

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.loadup-cloud</groupId>
      <artifactId>loadup-dependencies</artifactId>
      <version>0.0.2-SNAPSHOT</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

然后按需添加具体模块。例如，为 Spring MVC 业务接口引入统一响应与错误处理：

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-webmvc</artifactId>
</dependency>
```

其他模块无需重复声明受 BOM 管理的版本。当前版本为 `SNAPSHOT`，接入前需要确保 BOM 和所选模块已安装在本地 Maven 仓库，或已发布到你使用的制品仓库。具体配置请参阅[组件文档](loadup-components/README.md)。

## 架构边界

依赖方向为 `commons → components → modules → 集成方应用`。业务模块按需提供 Spring MVC Web 适配；技术组件的不同后端通过独立 binder 接入。项目不会要求集成方使用 `loadup-application` 作为自己的部署入口。

## 许可证

本项目采用 [Apache License 2.0](LICENSE)。

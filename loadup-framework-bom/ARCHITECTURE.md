# BOM 架构

`loadup-framework-bom` 是纯 POM 模块，用于管理 framework 的发布坐标与第三方依赖版本；消费工程在 `dependencyManagement` 中
import。它不引入运行时代码，也不为应用选择组件或 binder。

该 BOM 独立继承 Spring Boot 的依赖管理 POM，避免框架根聚合 POM 与 BOM 相互引用。新增外部依赖的版本应集中声明在这里；各子模块引用同项目坐标时不重复写版本。

本模块没有 Java 源文件，因此 License 插件的文件头更新、检查和移除目标均跳过。

本 BOM 只显式声明 Maven Deploy 插件；`spotless.skip=true` 与 `enforcer.skip=true` 让 reactor 中直接调用的质量检查跳过 BOM 本身。
构建插件的其他配置由各仓库父 POM 管理，BOM import 不传递这些跳过属性。

## 分层与调用路径

```text
loadup-framework-bom
```

聚合 POM 组织模块与版本，运行时依赖由子模块决定。

集成方式与配置示例见 [README.md](README.md)。

## Boot 运行时依赖管理

移除框架的 vertx.version 覆盖与 Vert.x BOM 导入，Vert.x 使用上游依赖的默认解析版本。Netty 和 Log4j 恢复 Spring Boot
继承的依赖管理，不额外导入补偿 BOM，避免旧第三方 BOM 把 Boot 运行时依赖降级。不在测试模块单独降级 Lettuce 或固定单个 Netty
jar。

## 拆仓后的 BOM 所有权

| BOM | 所属仓库 | 管理范围 |
|---|---|---|
| `loadup-framework-bom` | loadup-framework | commons、components、框架第三方依赖 |
| `loadup-modules-bom` | loadup-modules | 通用业务 JAR 与配套 Framework BOM |
| `loadup-testify-bom` | loadup-testify | Testify core、starter 与配套 Framework BOM |
| `loadup-pay-bom` | loadup-pay | Pay 制品与配套 Framework、Modules BOM |

各 BOM 是 `packaging=pom` 的独立发布制品，不继承所属仓库 parent。Framework BOM 的本仓库制品版本取自自身 `${project.version}`；
Modules、Testify BOM 各自导入配套 Framework，Pay BOM 导入配套 Framework 与 Modules。Framework BOM 不反向导入这些上层产品，导入链保持无环。
各 parent 导入自身同版本 BOM；Pay、Modules parent 额外导入内部测试所需的 Testify BOM，公开运行时 BOM 不导入 Testify。
parent 负责构建插件与处理器路径，BOM 提供依赖版本；import 不传递插件或 properties，普通 JAR 的传递依赖也不导出版本管理清单。

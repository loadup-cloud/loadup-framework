# BOM 架构

`loadup-framework-bom` 是纯 POM 模块，用于管理 framework 的发布坐标与第三方依赖版本；消费工程在 `dependencyManagement` 中
import。它不引入运行时代码，也不为应用选择组件或 binder。

该 BOM 独立继承 Spring Boot 的依赖管理 POM，避免框架根聚合 POM 与 BOM 相互引用。新增外部依赖的版本应集中声明在这里；各子模块引用同项目坐标时不重复写版本。

本模块没有 Java 源文件，因此 License 插件的文件头更新、检查和移除目标均跳过。

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
| `loadup-modules-bom` | loadup-modules | 当前发布的通用业务 JAR |
| `loadup-testify-bom` | loadup-testify | Testify core 与 starter |

各 BOM 是 `packaging=pom` 的独立发布制品，不需要独立 Git 仓库。消费工程显式导入所用产品族的 BOM。
新 modules / Testify BOM 不继承导入它们的仓库父 POM，也不导入 framework 或彼此的 BOM，避免模型循环和跨产品版本覆盖。
父 POM 负责构建配置，并通过导入所属 BOM 为子模块提供版本；普通 JAR 依赖不会把其父 POM 的 dependencyManagement 导出给消费方。

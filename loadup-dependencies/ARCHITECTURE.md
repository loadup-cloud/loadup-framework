# BOM 架构

`loadup-dependencies` 是纯 POM 模块，用于集中管理 LoadUp 发布坐标与第三方依赖版本；消费工程在 `dependencyManagement` 中
import。它不引入运行时代码，也不为应用选择组件或 binder。

该 BOM 独立继承 Spring Boot 的依赖管理 POM，避免框架根聚合 POM 与 BOM 相互引用。新增外部依赖的版本应集中声明在这里；各子模块引用同项目坐标时不重复写版本。

本模块没有 Java 源文件，因此 License 插件的文件头更新、检查和移除目标均跳过。

## 分层与调用路径

```text
loadup-dependencies
```

聚合 POM 组织模块与版本，运行时依赖由子模块决定。

集成方式与配置示例见 [README.md](README.md)。

## Boot 运行时依赖管理

移除框架的 vertx.version 覆盖与 Vert.x BOM 导入，Vert.x 使用上游依赖的默认解析版本。Netty 和 Log4j 恢复 Spring Boot
继承的依赖管理，不额外导入补偿 BOM，避免旧第三方 BOM 把 Boot 运行时依赖降级。不在测试模块单独降级 Lettuce 或固定单个 Netty
jar。

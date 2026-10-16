# LoadUp Components SpringDoc

为 Spring MVC 应用提供 OpenAPI 3 文档与 Scalar API 页面。组件独立于 `loadup-components-webmvc`，按需引入。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-springdoc</artifactId>
</dependency>
```

启动后访问 `/scalar` 查看接口，访问 `/v3/api-docs` 获取 OpenAPI JSON。页面和文档路径会随应用的 context path 变化。

## 配置

```yaml
loadup:
  springdoc:
    title: My API
    version: 1.0.0
    jwt-enabled: true
springdoc:
  api-docs:
    path: /v3/api-docs
  packages-to-scan: com.example.api
scalar:
  enabled: true
  path: /scalar
```

`loadup.springdoc.enabled` 只控制 LoadUp 提供的 OpenAPI 元数据 Bean；关闭接口文档使用 `springdoc.api-docs.enabled: false`，关闭页面使用 `scalar.enabled: false`。`jwt-enabled` 会在文档中添加 BearerAuth 安全方案及全局授权要求，供 Scalar 输入令牌。公开接口可按应用需求覆盖其 OpenAPI 安全声明。

运行时 `/api/**` 响应由 `loadup-components-webmvc` 包装为 `result`、`data`；SpringDoc 默认从 Controller 返回类型推导模型，阅读和调试接口时应以实际响应报文为准。页面与 JSON 文档的访问权限由集成方的 Spring Security 配置决定。

内部装配见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## Jackson 兼容性

当前使用 SpringDoc 3.1.1，支持 Spring Boot 4 与业务侧 Jackson 3 共存；其 Swagger Core 2.2.55 仍使用 Jackson 2 进行 OpenAPI 模型解析和文档序列化，因此组件并非纯 Jackson 3 依赖。

| 用途 | 坐标 | BOM 版本 |
| --- | --- | --- |
| 业务 JSON | `tools.jackson.core:jackson-core` / `jackson-databind` | 3.2.3 |
| SpringDoc / Swagger 内部 JSON | `com.fasterxml.jackson.core:jackson-core` / `jackson-databind` | 2.21.7 |
| 共享注解 | `com.fasterxml.jackson.core:jackson-annotations` | 2.22 |

两代 core/databind 的包名和坐标不同，可以共存。不要排除 SpringDoc 传递引入的 Jackson 2，或将它的 ObjectMapper 替换成 Jackson 3。Jackson 3 继续使用 `com.fasterxml.jackson.annotation` 注解；该命名空间不代表业务 JSON 退回 Jackson 2。业务的 Jackson 3 Module、序列化器配置不会自动成为 Swagger 内部 Jackson 2 的配置，定制 Schema 仍使用 SpringDoc / Swagger 的扩展点。

Jackson 2 由 LoadUp BOM 的 `jackson-2-bom.version=2.21.7` 统一管理，包括 YAML、JSR310 等模块；消费方不需要单独给 core 添加版本。官方兼容说明见 [SpringDoc](https://springdoc.org/)，上游两代 Jackson 依赖背景见 [SpringDoc #3169](https://github.com/springdoc/springdoc-openapi/issues/3169)。

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

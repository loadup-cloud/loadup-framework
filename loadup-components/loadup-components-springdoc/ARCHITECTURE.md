# LoadUp Components SpringDoc 架构

## 职责与边界

组件依赖 `org.springdoc:springdoc-openapi-starter-webmvc-scalar`，由 SpringDoc 扫描 Spring MVC Controller 并生成 `/v3/api-docs`，由 Scalar 展示 `/scalar`。它不参与请求路由、鉴权或响应包装；这些分别属于 Spring MVC、Security 组件和 `loadup-components-webmvc`。

```text
Controller / OpenAPI 注解 → SpringDoc → OpenAPI JSON → Scalar 页面
```

## 自动装配

[`SpringDocAutoConfiguration`](src/main/java/io/github/loadup/components/springdoc/autoconfigure/SpringDocAutoConfiguration.java) 在 `OpenAPI` 类存在且 `loadup.springdoc.enabled=true` 时生效。它根据 [`SpringDocProperties`](src/main/java/io/github/loadup/components/springdoc/properties/SpringDocProperties.java) 创建元数据 Bean；应用自定义 `OpenAPI` Bean 时自动退让。

`jwt-enabled=true` 注册 HTTP Bearer JWT 安全方案并设为文档的全局安全要求。此项仅影响 OpenAPI 描述与 Scalar 的令牌输入，不改变服务端安全规则。对登录、注册等公开操作，应在接口文档中单独声明无需授权。

## 配置与扩展

- `loadup.springdoc.*`：LoadUp 的标题、版本、联系人、许可证和 JWT 描述。
- `springdoc.*`：文档生成、扫描范围及 `/v3/api-docs` 路径，由 SpringDoc 负责。
- `scalar.*`：页面启用状态及 `/scalar` 路径，由 Scalar starter 负责。

应用可提供自己的 `OpenAPI` Bean 或 SpringDoc 定制器。文档页面与 JSON 端点是否公开由集成方 SecurityFilterChain 决定，组件不放行路径。SpringDoc 按 Controller 声明推导响应模型，而 `loadup-components-webmvc` 在运行时包装 `/api/**` 响应；如需精确的报文模型，集成方可使用 SpringDoc 定制器描述 `result`、`data`。

接入方式见 [README.md](./README.md)。

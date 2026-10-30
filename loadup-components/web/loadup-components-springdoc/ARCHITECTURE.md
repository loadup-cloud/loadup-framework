# LoadUp Components SpringDoc 架构

## 职责与边界

组件依赖 `org.springdoc:springdoc-openapi-starter-webmvc-scalar`，由 SpringDoc 扫描 Spring MVC Controller 并生成
`/v3/api-docs`，由 Scalar 展示 `/scalar`。它不参与请求路由、鉴权或响应包装；这些分别属于 Spring MVC、Security 组件和
`loadup-components-webmvc`。

```text
Controller / OpenAPI 注解 → SpringDoc → OpenAPI JSON → Scalar 页面
```

## 自动装配

[
`SpringDocAutoConfiguration`](src/main/java/io/github/loadup/components/springdoc/autoconfigure/SpringDocAutoConfiguration.java)
在 `OpenAPI` 类存在且 `loadup.springdoc.enabled=true` 时生效。它根据 [
`SpringDocProperties`](src/main/java/io/github/loadup/components/springdoc/properties/SpringDocProperties.java) 创建元数据
Bean；应用自定义 `OpenAPI` Bean 时自动退让。

`jwt-enabled=true` 注册 HTTP Bearer JWT 安全方案并设为文档的全局安全要求。此项仅影响 OpenAPI 描述与 Scalar
的令牌输入，不改变服务端安全规则。对登录、注册等公开操作，应在接口文档中单独声明无需授权。

## 配置与扩展

- `loadup.springdoc.*`：LoadUp 的标题、版本、联系人、许可证和 JWT 描述。
- `springdoc.*`：文档生成、扫描范围及 `/v3/api-docs` 路径，由 SpringDoc 负责。
- `scalar.*`：页面启用状态及 `/scalar` 路径，由 Scalar starter 负责。

应用可提供自己的 `OpenAPI` Bean 或 SpringDoc 定制器。文档页面与 JSON 端点是否公开由集成方 SecurityFilterChain
决定，组件不放行路径。SpringDoc 按 Controller 声明推导响应模型，而 `loadup-components-webmvc` 在运行时包装 `/api/**`
响应；如需精确的报文模型，集成方可使用 SpringDoc 定制器描述 `result`、`data`。

接入方式见 [README.md](README.md)。

## Jackson 职责边界

SpringDoc 3.1.1 通过 Swagger Core 2.2.55 使用 `com.fasterxml.jackson` 的 Jackson 2 模型解析器和序列化器。Boot 4 的 HTTP
消息转换及 LoadUp 业务 JSON 使用 `tools.jackson` 的 Jackson 3；LoadUp 不创建第二套业务 ObjectMapper，也不让 Swagger 的
mapper 接管 MVC。

BOM 独立管理两代依赖：Jackson 3 为 3.2.3，Jackson 2 BOM 为 2.21.7；共享 annotations 显式固定为 2.22。保留上游 Jackson 2
依赖，统一升级其 core/databind/dataformat/datatype，避免单独替换 core 或排除依赖引起文档生成失败。上游迁移到 Jackson 3
后再评估移除旧依赖，不通过强制替换包名实现迁移。

`SpringDocHttpIT` 使用真实嵌入式服务器验证 `/v3/api-docs` 元数据、Schema、安全配置和业务 JSON
请求/响应；它验证两代依赖共存，不等同于所有业务自定义序列化规则都能被 OpenAPI 自动推导。

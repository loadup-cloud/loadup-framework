# LoadUp Web MVC 架构

## 职责与边界

提供Spring MVC 响应与错误处理能力的独立模块。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-commons-dto`
- `loadup-commons-util`
- `loadup-components-observability`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter-webmvc`

## 实现入口

主要入口文件：

- [`LoadUpWebMvcAutoConfiguration`](src/main/java/io/github/loadup/components/webmvc/LoadUpWebMvcAutoConfiguration.java)
- [`ApiErrorController`](src/main/java/io/github/loadup/components/webmvc/ApiErrorController.java)

## 分层与调用路径

```text
Servlet 请求 → Spring MVC Controller → ApiResponseAdvice → result/data JSON
             ↘ /error 转发       → ApiErrorController → result/data JSON
Boot HTTP Observation → traceId 响应头；ApiResponseAdvice → 业务结果计数
Boot ObjectMapper ← JsonMapperBuilderCustomizer ← JsonUtil 日期规则
DTO / JsonUtil   ← SmartInitializingSingleton   ← Boot ObjectMapper
```

`ApiPathMatcher` 只匹配 `/api` 和 `/api/**`（考虑 Servlet context path）。
`ApiResponseAdvice` 对 JSON 与字符串响应设置 HTTP 200；已有 `IResponse` 保持原报文，
`PageDTO` 使用分页报文，字节响应不包装。`ApiErrorController` 接管错误路径，
将业务 API 的 400/401/403/404 等状态映射到 `FailureResponse`；非 API 请求保留原 HTTP 状态。

## 设计边界

组件只处理 MVC 报文与 Jackson 约定，不承担身份认证或授权。Spring Security 过滤器提前写出的
认证错误由 Resource Server 自己使用相同 `FailureResponse` 格式，因此不经过 MVC Advice。
自动配置声明在 Boot 错误自动配置之前，并在已有 `ErrorController` 时退让，避免 `/error` 重复映射。

## 装配规则

- [`LoadUpWebMvcAutoConfiguration`](src/main/java/io/github/loadup/components/webmvc/LoadUpWebMvcAutoConfiguration.java) 是自动配置入口。
  - `@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)`
  - `@ConditionalOnMissingBean(ApiResponseAdvice.class)`
  - `@ConditionalOnMissingBean(ErrorController.class)`

集成方式与配置示例见 [README.md](./README.md)。

## Jackson 3 响应脱敏

```text
Boot JsonMapper ──rebuild + private module──> ApiMaskingJson
                                               ↓
ServerHttpMessageConvertersCustomizer(order=100) → MVC JacksonJsonHttpMessageConverter
Controller → ApiResponseAdvice(result/data) → @Masked property serializer → JSON
全局 JsonUtil / DTO / RestClient / 缓存 → 原 Boot Mapper
```

`ApiMaskingJson` 是普通包装 Bean，内部 Mapper 与 SimpleModule 均不作为 Bean 注册；否则 Boot 会把模块装配到全局 JSON 和出站调用。服务端转换器也不注册为全局 Bean，而由服务器专用 customizer 安装。Jackson 3 `ValueSerializerModifier` 为带注解的 String 属性安装 serializer；不通过 JSON tree 重写数据，因此保留 JsonView 与其他 MVC 序列化提示。

MVC 输入使用同一转换器，但模块只修改 serializer，反序列化不变。String converter 的报文包装使用响应专用 Mapper。二进制、手工拼接 JSON 和 Map 内无注解的值不在自动处理范围。没有 KMS RPC、权限 ThreadLocal 或原对象突变。

错误注解直接使序列化失败；不得捕获后返回原 DTO 明文。`ApiMaskingJsonTest`、`ApiMaskingMvcTest` 覆盖隔离、嵌套与已包装响应、输入原值、JsonView 和错误声明。

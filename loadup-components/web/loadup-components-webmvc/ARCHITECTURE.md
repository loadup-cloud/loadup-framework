# LoadUp Web MVC 架构

## 职责与边界

提供Spring MVC 响应与错误处理能力的独立模块。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-commons-dto`
- `loadup-commons-util`
- `loadup-commons-json`
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

`LoadUpWebMvcAutoConfiguration` 使用 `WebMvcConfigurer#configurePathMatch` 为 `io.github.loadup.modules` 下的 Controller
统一添加 `/api`。各业务模块只声明相对路径；文档、Actuator、错误 Controller 和消费工程自有包不在匹配范围内。
`ApiResponseAdvice` 对 JSON 与字符串响应设置 HTTP 200；已有 `IResponse` 保持原报文，
`PageDTO` 使用分页报文，字节响应不包装。`ApiErrorController` 接管错误路径，
将业务 API 的 400/401/403/404 等状态映射到 `FailureResponse`；非 API 请求保留原 HTTP 状态。

## 设计边界

组件只处理 MVC 报文与 Jackson 约定，不承担身份认证或授权。Spring Security 过滤器提前写出的
认证错误由 Resource Server 自己使用相同 `FailureResponse` 格式，因此不经过 MVC Advice。
自动配置声明在 Boot 错误自动配置之前，并在已有 `ErrorController` 时退让，避免 `/error` 重复映射。

## 装配规则

- [`LoadUpWebMvcAutoConfiguration`](src/main/java/io/github/loadup/components/webmvc/LoadUpWebMvcAutoConfiguration.java)
  是自动配置入口。
    - `@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)`
    - `@ConditionalOnMissingBean(ApiResponseAdvice.class)`
    - `@ConditionalOnMissingBean(ErrorController.class)`

集成方式与配置示例见 [README.md](README.md)。

## Jackson 3 响应脱敏

```text
Boot JsonMapper ──rebuild + private module──> ApiMaskingJson
                                               ↓
ServerHttpMessageConvertersCustomizer(order=100) → MVC JacksonJsonHttpMessageConverter
Controller → ApiResponseAdvice(result/data) → @Masked property serializer → JSON
全局 JsonUtil / DTO / RestClient / 缓存 → 原 Boot Mapper
```

`ApiMaskingJson` 是普通包装 Bean，内部 Mapper 与 SimpleModule 均不作为 Bean 注册；否则 Boot 会把模块装配到全局 JSON
和出站调用。服务端转换器也不注册为全局 Bean，而由服务器专用 customizer 安装。Jackson 3 `ValueSerializerModifier` 为带注解的
String 属性安装 serializer；不通过 JSON tree 重写数据，因此保留 JsonView 与其他 MVC 序列化提示。

MVC 输入使用同一转换器，但模块只修改 serializer，反序列化不变。String converter 的报文包装使用响应专用 Mapper。二进制、手工拼接
JSON 和 Map 内无注解的值不在自动处理范围。没有 KMS RPC、权限 ThreadLocal 或原对象突变。

错误注解直接使序列化失败；不得捕获后返回原 DTO 明文。`ApiMaskingJsonTest`、`ApiMaskingMvcTest` 覆盖隔离、嵌套与已包装响应、输入原值、JsonView
和错误声明。

## 业务执行上下文生命周期

`ExecutionContextFilter` 在 Boot HTTP Observation 和 Security 之前通过 ServiceTemplate 绑定不可变 ExecutionContext，作用域由
JDK 25 ScopedValue 自动结束；Servlet 重新分派通过 `ExecutionContext.class.getName()` 请求属性恢复显式绑定。可信租户适配器使用相同属性并包裹自己的
FilterChain；临时覆盖不回写父请求。嵌套 error 分派沿用当前作用域。业务数据不会写到 MDC、响应或指标中；Filter 不承担认证、授权或租户解析。

## 失败响应契约

`/api` 下的 CommonException 保留 ResultCode，并返回 HTTP 200 的 FailureResponse（result、data）；输入绑定和 Validation 错误返回
PARAM_ILLEGAL，未知异常返回 SYS_ERROR。异常日志只记录类型，不记录可能携带请求明文的消息或异常链。Spring Security
异常交回认证/鉴权过滤器，非 API 协议端点保留原协议行为。

## 内部请求大小限制

SDK 默认不启用；需要时显式配置：

```yaml
loadup:
  webmvc:
    request-body:
      path-prefix: /api/internal/
      max-size: 1MB
```

max-size 为正整数字节大小，必须小于 Integer.MAX_VALUE；path-prefix 是以 / 开始、结束的目录前缀。
RequestBodyLimitAdvice 在认证过滤器之后、MVC 请求转换之前对已匹配路由执行限制，包含 context path 或编码拼写时仍按 MVC 映射判断。
先检查声明长度，再最多读取 limit + 1 字节，完整检查未知长度、虚假长度与 JSON 尾随空白后才交给消息转换器。
以 UTF-8 等实际传输字节计数，不按字符数；有界缓存会增加每个并发请求的内存，应用仍需约束并发、连接池和网络读取时间。

超限是传输拒绝：HTTP 413 + PARAM_ILLEGAL FailureResponse，不执行控制器；ApiResponseAdvice 不把 413 改为 200。
其他小报文的参数/业务错误保持原 HTTP 200，前缀外的回调、文件流与认证入口保留原协议。
这不是全站上传限制，也不代替容器、反向代理、网关的请求时间/并发/大小约束；无 @RequestBody 的直接 Servlet 流读取不在该 Advice 范围。
ConfigMap 更新不会自动更新此配置，默认重启生效。RequestBodyLimitAdviceTest 已补回归源码，未执行构建或测试。

## 原生协议响应

在 Controller 类或方法上标注 @ProtocolResponse，明确该入口负责原生正文、HTTP 状态和内容类型。
ProtocolResponseInterceptor 在参数绑定/调用前记录已匹配协议入口，覆盖 direct sendError；
API 包装器和业务异常信封不处理此类请求，协议判断保留到错误分派，不把渠道 failure/非 2xx 或 JSON 原文改为成功信封。
标记只控制表示层，不修改 Resource Server、安全过滤链、签名、权限或租户来源；普通 /api 字符串接口继续统一包装。
未匹配到 handler 的 URI 仍按原错误入口规则处理，标记不是按路径关闭全部错误处理的配置。
ProtocolResponseTest 已补文本/JSON 原文、非 2xx、方法/类标记、普通 API 和错误分派回归源码，未执行。

## MVC 线程模型

组件沿用 Boot 原生 Servlet 执行模型，不创建请求线程池。消费方可用 spring.threads.virtual.enabled 试验虚拟线程，保持连接池和业务并发预算。
MvcThreadModelTest 使用独立配置名，在回环地址启动真实 Servlet 容器，分别检查平台/虚拟请求线程、并发合成身份与 ScopedValue 隔离、异常后清理。
这是线程契约测试源码，不装配业务数据库/渠道，不证明真实 JWT 或金融等价。测试未运行，默认启用决策仍需消费方压测与发布验收。

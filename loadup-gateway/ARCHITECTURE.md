# LoadUp Gateway 当前架构

## 执行边界

```
HTTP → Spring Security（/api/**）→ SCG MVC RouterFunction
                                    ├─ ServiceMethodCatalog → Spring 代理 Service 方法
                                    └─ HandlerFunctions.http() → 上游 HTTP
                       ↑
RouteSource → 完整文档校验与编译 → ManagedRouteRegistry 原子快照
```

Gateway 是嵌入式库。Service 目标是单应用主路径，不创建 Controller；HTTP 目标使用 SCG MVC 原生代理。两个目标共享来源、路由匹配、入口授权、版本诊断。路由运行时不反射查找方法，也不把 HTTP 响应包装为业务对象。

| 能力 | Service 目标 | HTTP 目标 |
|------|--------------|-----------|
| 动态路由、入口授权、签名、限流、指标 | 支持 | 支持 |
| 参数绑定与 Bean Validation | 支持 | 由上游负责 |
| Spring 事务与方法级 `@PreAuthorize` | 保留代理语义 | 由上游负责 |
| 状态与正文 | 业务结果序列化为 JSON；`void` 为 204 | 保持上游 HTTP 语义 |
| 路径和请求头过滤器 | 不适用 | StripPrefix、RewritePath、SetRequestHeader |
| 熔断 | 未提供 | 需可选 `CircuitBreakerFactory` |
| 总超时与自动重试 | 未提供 | 未提供；由上游客户端或应用策略配置 |

## 模块

| 模块 | 当前职责 |
|------|----------|
| `loadup-gateway-api` | `@GatewayExpose`、不可变路由文档、`RouteSource` SPI、变更事件 |
| `loadup-gateway-webmvc` | Service 方法目录、SCG MVC 路由编译、快照、入口授权、HMAC 签名、只读诊断 |
| `loadup-gateway-security-jwt` | 可选 Spring Security OAuth2 资源服务器与 JWT 权限转换 |
| `loadup-gateway-starter` | 默认文件来源和核心自动装配 |
| `repository-yaml-plugin` | 严格解析版本化 YAML、文件监听与周期校验；由 starter 引入 |
| `loadup-gateway-source-configcenter` | 可选 ConfigCenter Template 适配，监听与轮询 |
| `loadup-gateway-filter-bucket4j` | 可选 `RateLimit`，默认节点内 Caffeine 配额，可替换共享 Bucket4j 存储 |
| 旧 facade/proxy/store | 已从 Maven 聚合与 BOM 移除；新 starter 只依赖托管路由链 |

## 路由生命周期

`RouteSource.loadCurrent()` 返回完整 `RouteDocument`。引擎先校验 schema、ID、方法、路径、目标、access 和受支持的过滤器，再用 SCG MVC `GatewayRouterFunctions.route(id)` 编译完整候选表。任何一条无效都拒绝候选版本；已发布的 `RouterFunction` 和文档元数据通过一个 `AtomicReference` 同时切换。请求保留进入时取得的路由函数。启动无有效配置时失败；刷新失败时保留旧版本并记录 `lastError`。

编译时还会拒绝与 `spring.cloud.gateway.server.webmvc.routes` / `routesMap` 中已声明 ID 重名的托管路由。Java DSL 自定义 RouterFunction 的 ID 不在属性模型内，需由集成方自行保持唯一。

外部文件以 SHA-256 内容摘要为 revision；文件事件触发立即读取，周期轮询弥补丢失事件。配置中心来源通过监听与周期读取收敛，仍以完整快照发布。自定义来源只需实现 `RouteSource`，并在数据变化时发布 `RouteSourceChangedEvent`；可周期调用 `refresh()` 作为补偿。

## Service 方法契约

只有 `@Service` Bean 的公共实例方法且显式标注 `@GatewayExpose` 才进入方法目录。`bean + method` 在编译路由时解析；重复标识和代理不可调用的方法导致启动失败。调用始终针对 Spring 代理，保留事务与方法级安全。支持路径、查询、请求头、JSON body 绑定及 Bean Validation；单个复杂参数可省略 `@RequestBody`，其他参数须显式标注。默认 JSON body 上限 1 MiB；普通返回值为 JSON，`void` 为 204。错误返回 HTTP 400/401/403/500 与请求 ID。

## HTTP 和策略

HTTP 目标采用 SCG MVC `HandlerFunctions.http()` 与 `BeforeFilterFunctions.uri()`；`lb://` 使用 SCG 的 `lb()` filter，并要求集成方提供 Spring Cloud LoadBalancer。当前托管配置提供 `StripPrefix`；HTTP `CircuitBreaker` 使用 SCG MVC 过滤器并要求 `CircuitBreakerFactory`。可选 Bucket4j 模块提供 Service 与 HTTP 通用的 `RateLimit`。SCG MVC 5.0.3 原生 Bucket4j 过滤器在 Service 响应上写只读响应头会失败，因此适配器使用同一 Bucket4j 配额 API 实现短路，放行响应不添加剩余额度响应头。其他过滤器可通过 `ManagedFilterAdapter` 增加；目标类型通过 `TargetHandlerAdapter` 注册，重复类型在启动时失败。托管 access 支持 `public`、`authenticated`、`authority`，并可通过 `signature: true` 显式叠加签名校验（AND）；路由缺少策略会拒绝发布。JWT 资源服务器来自可选 `loadup-gateway-security-jwt`，安全链仅匹配 `/api/**`；有无效 Bearer 时由资源服务器返回认证失败。请求计时、刷新计数和快照年龄写入 Micrometer；只读 Actuator 端点展示 revision、路由摘要、上次刷新时间/耗时/结果、快照年龄和最近错误，不展示目标 URI 或密钥。

HTTP 托管路由还支持 SCG MVC 原生 `RewritePath(regexp, replacement)` 与 `SetRequestHeader(name, value)`。两者只适用于 HTTP 目标；参数形状、正则语法和请求头名称在发布快照时校验。正则替换中的捕获组引用由 SCG 在请求匹配时解释。

## 请求签名

`X-App-Id` 指向 `loadup.gateway.security.app-secrets` 中的密钥；`X-Timestamp` 为 Unix 秒，允许前后 300 秒；`X-Nonce` 在同一 app 下五分钟内不可重复；`X-Signature` 为小写十六进制 HMAC-SHA256。签名输入依次为 `METHOD`、原始 URI 路径与查询串、时间戳、nonce、正文 SHA-256 十六进制摘要，字段之间以 `\n` 分隔。缺失、过期、签名错误和重放返回 401；正文超过 `loadup.gateway.security.max-signed-body-bytes`（默认 1 MiB）返回 413。签名验证后 HTTP handler 仍转发原正文，签名四个请求头不转发给上游。

默认 `LocalSignatureNonceStore` 仅在单节点生效，且有容量上限。多实例部署签名路由时必须提供具备跨节点原子 `reserve` 语义的 `SignatureNonceStore` Bean；JWT 校验与签名校验相互独立，组合时两者都要通过。密钥只放应用安全配置，不写在路由文档中。

## 现有限制

托管配置尚未覆盖 SCG 的全部过滤器、RPC 目标、统一超时策略及分布式限流的现成存储绑定。数据库可以通过 `RouteSource` 自定义接入，当前没有已发布版本表的内置实现。更完整的目标和阶段门槛见 [DETAILED_DESIGN.md](DETAILED_DESIGN.md)。

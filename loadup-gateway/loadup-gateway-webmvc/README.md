# LoadUp Gateway WebMVC

基于 Spring Cloud Gateway Server MVC 的托管路由执行层。通常只需引入 `loadup-gateway-starter`；业务模块只引入 `loadup-gateway-api` 并使用 `@GatewayExpose` 标注入口方法。

## 能力矩阵

| 能力 | 当前状态 |
|------|----------|
| Service 方法路由及 Spring 代理调用 | 支持 |
| JSON body、路径、查询、请求头、Bean Validation | 支持明确子集 |
| SCG MVC 原生 HTTP 代理 | 支持 |
| 版本化路由原子刷新 | 支持 |
| `public` / `authenticated` / `authority` | 支持 |
| 可选 JWT 资源服务器 | 另引 `loadup-gateway-security-jwt`，再启用安全配置 |
| 路由指标与只读 Actuator 诊断 | Actuator 可选 |
| `StripPrefix` 与 HTTP `CircuitBreaker` | 支持；熔断需 `CircuitBreakerFactory` |
| `RateLimit` | 可选 `loadup-gateway-filter-bucket4j`；默认节点内配额 |
| HMAC 请求签名 | `access.signature: true`；默认节点内 nonce，支持自定义共享存储 |
| RPC、统一超时与内置分布式限流存储 | 待实现 |

当前托管路由只允许 `/api/**` 路径。Service 请求体默认上限 1 MiB，可通过 `loadup.gateway.limits.service-body-bytes` 调整。安全链只匹配 `/api/**`，其他端点由应用自行配置。详见 [../ARCHITECTURE.md](../ARCHITECTURE.md)。

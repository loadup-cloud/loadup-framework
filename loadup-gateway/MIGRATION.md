# Gateway 迁移到 SCG MVC 托管路由

旧 `RouteStore` / `ProxyProcessor` / `GatewayRequest` 执行链及其 facade、HTTP/Bean/RPC 代理、旧数据库路由模块已从 Maven 聚合和 BOM 移除。集成方改引 `loadup-gateway-starter`；业务模块只需 `loadup-gateway-api`。使用项目内置 JWT 验证时，另引 `loadup-gateway-security-jwt`；限流另引 `loadup-gateway-filter-bucket4j`。

## 路由数据

旧 CSV/数据库路由需转为 `schemaVersion: 1` 的完整 YAML 文档。Service 目标使用 `target: {type: service, bean: ..., method: ...}`，目标方法必须标注 `@GatewayExpose`；HTTP 目标使用 `target: {type: http, uri: ...}`，不再经过自研 `ProxyProcessor`。旧 `enabled: false` 行在新文档中直接删除；每次发布整份路由文档。旧请求/响应模板和统一响应包装不自动迁移，HTTP 保持上游原始语义。

| 旧 `securityCode` | 新配置 |
|------------------|--------|
| `OFF` | `access: {type: public}` |
| `default` | `access: {type: authenticated}` |
| `signature` | `access: {type: public, signature: true}`；需要 JWT 时用 `authenticated` 加 `signature: true` |
| `internal` | 不自动映射；显式配置可信身份及 `authority` 策略 |

## 不重新部署地修改路由

默认 `classpath:gateway-routes.yml` 适合开发和首次启动；生产应设置 `loadup.gateway.source.file.path` 为 jar 外文件，或引入 `loadup-gateway-source-configcenter` 并选择配置中心 binder。仓库的本地启动器支持 `GATEWAY_ROUTES_PATH` 环境变量指定外部文件。新版本必须是完整、有效的文档；无效更新保留上一已发布版本。只读 `gatewayRoutes` Actuator 端点可查看当前 revision、刷新结果和错误。

## 迁移校验

逐条确认新 `id`、`order`、路径、方法、目标和 `access`；检查业务方法是否通过 Spring 代理暴露，确认原有授权规则没有被放宽。HTTP 路由检查路径改写、上游状态与正文；Service 路由检查 JSON 入参、参数错误和方法级授权。多实例签名路由须提供共享的 `SignatureNonceStore`，多实例限流须提供共享的 Bucket4j `AsyncProxyManager`。完整配置与当前限制见 [README.md](README.md) 和 [ARCHITECTURE.md](ARCHITECTURE.md)。

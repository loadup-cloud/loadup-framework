# LoadUp Gateway

嵌入 Spring Boot MVC 应用的入口组件。单应用把 HTTP 请求直接路由到显式暴露的 Service 方法；分布式应用使用 Spring Cloud Gateway Server MVC 原生 HTTP handler 转发。路由可从 jar 外文件或可选配置中心来源热更新。

## 引入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-gateway-starter</artifactId>
</dependency>
```

在业务模块引入 `loadup-gateway-api`，为允许调用的方法加 `@GatewayExpose`。入口方法可使用 `@PathVariable`、`@RequestParam`、`@RequestHeader`、`@RequestBody`；方法上的 `@PreAuthorize` 仍通过 Spring 代理执行。

## 配置

```yaml
loadup:
  gateway:
    source:
      file:
        path: ./config/gateway-routes.yml
    route-refresh-interval: 5
    limits:
      service-body-bytes: 1048576
```

版本化路由文档使用 `schemaVersion: 1`，每条路由声明 `id`、`order`、`path`、`methods`、`target` 和 `access`。单应用与 HTTP 转发的完整配置见 [QUICKSTART.md](QUICKSTART.md)。

`access.type` 支持 `public`、`authenticated`、`authority`。启用网关 JWT 资源服务器需额外引入 `loadup-gateway-security-jwt`，设置 `loadup.gateway.security.enabled=true` 与密钥、issuer 或 JWKS 配置。应用也可提供自己的 Spring Security 链。

`access.signature: true` 可与上述策略叠加（AND）。配置 `loadup.gateway.security.app-secrets.<appId>`；签名协议、正文上限和多实例 nonce 存储契约见 [ARCHITECTURE.md](ARCHITECTURE.md)。

`lb://` 目标需由集成方引入 `spring-cloud-starter-loadbalancer` 并配置服务发现；缺少 LoadBalancer 时路由发布失败。

HTTP 路由可按顺序声明 `StripPrefix(parts)`、`RewritePath(regexp, replacement)`、`SetRequestHeader(name, value)`。这些过滤器直接调用 SCG MVC 实现；参数形状或正则语法错误使候选版本拒绝发布。

可选 `loadup-gateway-filter-bucket4j` 增加 `RateLimit` 路由过滤器；默认使用节点内 Caffeine 配额，多实例共用配额需提供共享的 Bucket4j `AsyncProxyManager`。HTTP 路由可配置 `CircuitBreaker`，需由应用提供 Spring Cloud `CircuitBreakerFactory`。

文件更新采用完整快照校验与原子切换；无效文件保留上一有效版本。YAML 编辑器可使用 [路由 JSON Schema](plugins/repository-yaml-plugin/src/main/resources/gateway-routes-schema.json)。配置中心来源需另加 `loadup-gateway-source-configcenter` 和一个 ConfigCenter binder，设置 `loadup.gateway.source.type=configcenter`、`loadup.gateway.source.configcenter.key`。也可自定义 `RouteSource` Bean。Actuator 存在时可显式暴露只读 `gatewayRoutes` 端点。

详见 [ARCHITECTURE.md](ARCHITECTURE.md)、[迁移说明](MIGRATION.md) 与 [DETAILED_DESIGN.md](DETAILED_DESIGN.md)。

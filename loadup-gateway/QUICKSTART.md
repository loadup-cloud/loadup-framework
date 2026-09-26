# LoadUp Gateway 接入示例

## 单应用：Service 方法直接对外

业务模块引入 `loadup-gateway-api`，运行应用引入 `loadup-gateway-starter`；版本由 `loadup-dependencies` BOM 管理。应用提供普通 Spring Service，不需要 Controller：

```java
import io.github.loadup.gateway.api.GatewayExpose;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class GreetingService {
    @GatewayExpose
    public Greeting greet(@PathVariable("name") String name) {
        return new Greeting("Hello, " + name);
    }

    public record Greeting(String message) {}
}
```

在 jar 外创建 `./config/gateway-routes.yml`：

```yaml
schemaVersion: 1
routes:
  - id: greeting
    order: 10
    path: /api/greetings/{name}
    methods: [GET]
    target: { type: service, bean: greetingService, method: greet }
    access: { type: public }
```

应用配置：

```yaml
loadup:
  gateway:
    source:
      file:
        path: ./config/gateway-routes.yml
```

启动后请求 `GET /api/greetings/Ada`，得到 `{"message":"Hello, Ada"}`。修改外部路由文件中的路径或策略后，网关监听文件变化并周期复核，无需重新打包。新增 Java 方法仍需发布应用代码。

## 分布式入口：HTTP 目标

在同一份文件中增加路由，目标 URI 指向上游 HTTP 服务：

```yaml
  - id: orders
    order: 20
    path: /api/orders/**
    methods: [GET, POST]
    target: { type: http, uri: "http://127.0.0.1:8081" }
    access: { type: public }
    filters:
      - { name: RewritePath, args: { regexp: "/api/orders/(?<segment>.*)", replacement: "/${segment}" } }
      - { name: SetRequestHeader, args: { name: X-Gateway, value: loadup } }
```

`GET /api/orders/42` 转发为上游的 `GET /42`；上游状态、响应头与正文保持 HTTP 代理语义。需要服务发现时将 URI 改为 `lb://order-service`，并引入 `spring-cloud-starter-loadbalancer`。配置变更依然通过外部文件生效。

## 接入授权与诊断

需要 JWT 时额外引入 `loadup-gateway-security-jwt`，设置 `loadup.gateway.security.enabled=true`，并提供 `secret`、`issuer-uri` 或 `jwk-set-uri`。将路由 `access.type` 改为 `authenticated` 或 `authority`；`authority` 同时设置 `anyOf`。已有自定义 `SecurityFilterChain` 的应用可以自行完成认证。请求签名用 `access.signature: true` 叠加，协议见 [ARCHITECTURE.md](ARCHITECTURE.md)。

若集成方引入 Actuator 并显式暴露 `gatewayRoutes` 端点，可查看当前 revision、最近刷新结果和错误。无效文件不会替换当前有效快照；首次启动文件无效则启动失败。多实例部署应使用共享的配置中心来源，或确保每台实例获得同一份外部文件，并分别核对已发布 revision。

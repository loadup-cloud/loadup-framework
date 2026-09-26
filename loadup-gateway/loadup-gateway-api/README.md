# Gateway API

`loadup-gateway-api` 是业务模块接入嵌入式 Gateway 的轻量依赖，提供 `@GatewayExpose`、不可变路由文档及 `RouteSource` SPI，不会启动 HTTP 网关。

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-gateway-api</artifactId>
</dependency>
```

只有显式标注 `@GatewayExpose` 的 `@Service` 公共方法可以成为 `service` 目标。标注本身不生成路由，路径、HTTP 方法和访问策略由外部版本化路由文档决定。自定义来源实现 `RouteSource.loadCurrent()` 返回完整 `RouteDocument`；变化时发布 `RouteSourceChangedEvent`，引擎会重新读取并原子发布有效版本。

# Gateway Starter

`loadup-gateway-starter` 组合 SCG MVC 执行层和默认文件来源。集成方在 Spring Boot Servlet 应用中引入此坐标即可使用 Service 路由与 HTTP 转发。

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-gateway-starter</artifactId>
</dependency>
```

默认读取 `classpath:gateway-routes.yml`；需要运行时热更新时配置 `loadup.gateway.source.file.path` 为 jar 外文件。设置 `loadup.gateway.enabled=false` 可关闭自动装配。配置中心来源需另加可选模块并设置 `loadup.gateway.source.type=configcenter`。

完整接入示例、路由格式与安全配置见 [../README.md](../README.md)。

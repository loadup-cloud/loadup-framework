# Gateway ConfigCenter 路由来源

通过 LoadUp ConfigCenter `ConfigCenterTemplate` 读取版本化 YAML 路由文档；监听变更并周期复查，适合多实例共享配置。此模块不依赖具体 Nacos 或 Apollo binder。

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-gateway-source-configcenter</artifactId>
</dependency>
```

同时引入一个 `loadup-components-configcenter-binder-*`，并配置：

```yaml
loadup:
  gateway:
    source:
      type: configcenter
      configcenter:
        key: gateway-routes
    route-refresh-interval: 5
```

该 key 的值必须是含 `schemaVersion: 1` 和完整 `routes` 列表的 YAML 文档。通知丢失后仍会通过周期读取收敛；无效版本不替换节点的有效快照。

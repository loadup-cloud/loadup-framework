# Gateway Bucket4j Filter

为托管路由增加基于 Bucket4j 的 `RateLimit` filter，适用于 Service 和 HTTP 目标。单节点默认使用 Caffeine 存储；多实例部署应提供自己的 Bucket4j `AsyncProxyManager<String>` Bean，保证所有节点共享配额。

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-gateway-filter-bucket4j</artifactId>
</dependency>
```

```yaml
filters:
  - name: RateLimit
    args: { capacity: 100, periodSeconds: 60, key: principal }
```

`key` 可选 `ip` 或 `principal`，默认 `ip`；`periodSeconds` 范围为 1–86400。`principal` 缺失时返回 403。超过配额时返回 429 并携带 `X-RateLimit-Remaining: 0`。为兼容 Service 响应，放行响应不添加该响应头。配额查询最多等待 5 秒。完整路由示例见 [../README.md](../README.md)。

---
id: components-tracer
title: LoadUp Tracer Component
---

> 来源: `loadup-components/loadup-components-tracer/README.md`

# LoadUp Tracer Component

LoadUp Tracer 基于 OpenTelemetry，为 Spring Boot 应用提供自动化的分布式追踪能力。

主要功能：

- 自动化配置与零入侵集成
- 注解式追踪 `@Traced`、类级跟踪、手动 Span 管理
- Web 请求和异步任务的上下文传递
- MDC 集成（自动记录 TraceId）
- 支持 Jaeger/Zipkin/Grafana Tempo 等后端
- 性能与并发测试结果、最佳实践与调优建议

快速配置示例：

```yaml
loadup:
  tracer:
    enabled: true
    enable-web-tracing: true
    enable-async-tracing: true
    otlp-endpoint: http://localhost:4317
```

原始详细文档与 API 说明已迁移到 docs，完整内容保留于原始模块：

- 原始路径: `loadup-components/loadup-components-tracer/README.md`

(你可以在 docs 站点中搜索 `Tracer` 获取使用示例、配置项和调试建议。)

# loadup-components 概览

`loadup-components` 包含可复用的中间件与适配器实现，供业务模块复用。

常见子模块与说明：

- `loadup-components-cache`：缓存抽象与实现（Caffeine / Redis 绑定器）
  - README: `loadup-components/loadup-components-cache/README.md`
- `loadup-components-database`：数据库访问与工具，包含 migration 与 schema 示例
  - README: `loadup-components/loadup-components-database/README.md`
- `loadup-components-dfs`：分布式文件存储支持（local / s3 等绑定器）
  - README: `loadup-components/loadup-components-dfs/README.md`
- `loadup-components-scheduler`：任务调度（Quartz / XXL-Job / SimpleJob 等）
  - README: `loadup-components/loadup-components-scheduler/README.md`
- `loadup-components-tracer`：分布式追踪（OpenTelemetry / Jaeger 集成）

## 如何使用

组件通常以 `starter` 或 `api` + `binder` 的形式提供：
1. 在项目中引入对应 artifact（BOM 管理下无需指定版本）。
2. 根据 README 中的示例，配置 application.yml 并启用 starter。

更多细节参见对应模块的 README（仓库内）。

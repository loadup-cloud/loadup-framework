# LoadUp Components Cache API

Cache 组件的 facade 模块：启用 Spring Cache 注解（`@EnableCaching`）、承载统一的
`loadup.cache.*` 配置（`type` / `default-ttl` / 按 cache name 的 TTL、空值与随机过期），
并提供跨 binder 共享的 JSON 值编解码器 `CacheJsonCodec` 与防雪崩语义 `RandomExpiration`。

本模块**不提供手写缓存 CRUD 门面**——业务代码直接使用 Spring Cache 标准注解，底层实现由
`-binder-{caffeine|redis|jetcache}` 提供。Maven 坐标与用法见上层
[loadup-components-cache](../README.md)。

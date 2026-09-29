# Cache Redis Binder 架构

`RedisCacheAutoConfiguration` 在 `loadup.cache.type=redis` 且 Redis 类可用时创建 Spring Data Redis `RedisCacheManager`。它复用应用现有 `RedisConnectionFactory`，不自行创建第二套连接配置，因此旧的 `loadup.cache.redis.host/port` 配置不生效。

值序列化使用基于共享 Jackson 配置的 JSON codec，key 使用字符串序列化；默认 key 前缀由 `loadup.cache.binder.redis.key-prefix` 控制。公共 `LoadupCacheProperties` 决定默认 TTL、每个 cache 的 TTL、随机过期范围与空值策略。其他 binder 由不同模块实现，应用选择一个与 `loadup.cache.type` 匹配的 binder。

缓存连接和可用性由消费应用负责；Redis 服务异常会按 Spring Data Redis 的异常语义向调用方传播。上层业务通过标准 Spring Cache API 使用，不依赖本模块类型。

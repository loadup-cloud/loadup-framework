# LoadUp Lock 架构

## 目标与边界

给应用服务临界区提供同步互斥执行。使用 Redisson core，组件只统一配置、键、异常、事务入口约束及观测，不复制 Redis Lua、续期线程或锁协议。首版只有一个实现，采用单一 jar；分类位于 reliability，业务按需引入。

```text
应用服务入口（尚未开启事务）
  → LockTemplate.execute
    → Redisson RLock.tryLock
    → 业务回调 / TransactionTemplate / 另一个事务 Bean
    → 事务完成
    → RLock.unlock
```

## 公开类型

- LockKey：显式业务/租户/资源，提供 global 和 tenant 工厂；namespace 为模板级应用隔离。
- LockOptions：不可变等待/租期策略，WATCHDOG 无显式租期、FIXED 必填正租期。
- LockTemplate：同步 execute/run 回调，支持 checked exception，中断使用标准 InterruptedException。
- RedissonLockTemplate：单一实现，原生客户端和共享 Registry 构造器注入，不拥有客户端生命周期。
- LockProperties / LockAutoConfiguration：enabled 默认关闭；复用应用客户端或从原生 YAML 资源创建并交给 Spring 关闭。
- LockUnavailableException：仅表示 tryLock 返回 false，区分客户端故障。

键各段 UTF-8 Base64URL 编码，固定 g/t 范围段避免命名冲突及任意 hash tag；全局锁与租户锁不同，namespace 不默认跨应用共享。字符/长度前置校验，toString 不打印资源标识。编码不提供机密性。

## 状态与异常

等待/获取异常时不执行回调、不自行释放未知所有权。成功获取后立即建立 AutoCloseable HeldLock，用 JDK try-with-resources 调用原生 unlock。业务返回/异常和 Error 路径都会释放；释放失败不会被 isHeldByCurrentThread 检查吞掉，也不强制解锁。主异常保留，释放异常为 suppressed；只有释放失败时直接抛出。

调用线程已中断时在连接 Redis 前失败；等待中断恢复 interrupt flag 并抛出。Redisson 获取/释放原始异常保留，不增加业务自动重试。

默认 WATCHDOG 使用无显式 lease 的 tryLock 重载；FIXED 使用显式 lease 重载。watchdog 有效性依赖原生客户端/Redis 运行状态，租期丢失不会取消回调。所有权有效性和故障语义遵循固定 Redisson 版本，并须在真实部署验收。重入采用同客户端/线程语义，嵌套保持相同租期策略。

## 线程与事务

回调不切换线程；虚拟线程与平台线程均使用其逻辑线程身份获取和释放。不能跨线程释放，不能把异步实际执行或流式消费延后到回调结束后。

通过 Spring TransactionSynchronizationManager 拒绝已开启的命令式事务，避免外层事务在释放锁后提交。实际事务必须在临界区内完成，代理自调用问题由应用编排解决。此约束不检测自建 JDBC/JTA 事务、响应式事务或其他跨线程事务，集成方保持同一边界。模板本身不开始事务。

## 配置与装配

BOM 固定 Redisson core 4.8.0，客户端配置由原生 Config.fromYAML 或应用 Bean 管理，不引入 Redisson starter、不复制 TLS/连接池/拓扑配置。明确 supplied client 时忽略 resource；多客户端要求 Primary 或自定义模板。只有 enabled=true、resource 配置存在且没有客户端时创建客户端；缺少客户端与资源的默认装配失败。

原生 starter 已存在时自动配置排序位于其后。组件创建的 Bean 使用 destroyMethod=shutdown；模板不 shutdown 复用实例。消费方的自定义 LockTemplate 会使默认模板退让，未配置 resource 时也不会创建无用客户端。

## 观测与能力边界

共享 MeterRegistry 可选；预注册有限结果标签的获取与持有 Timer、释放失败 Counter。获取 outcome 统一 success/failure，reason 只表达有限失败原因；不使用动态业务键标签。计时记录 RuntimeException 用 LogUtil 记录，不阻断正常锁生命周期；组件不创建新的 Registry/Tracer。

Lock 与 GlobalUnique/Outbox/Scheduler 职责独立。租约锁不能保证 exactly-once，不能代替数据库约束、幂等、状态条件更新；要求防止旧持有者写入时由资源验证 fencing token。首版不提供注解切面、后端 SPI、HTTP 接口、管理页面或默认全服务加锁。

## 验证

单测覆盖超时/中断不执行业务、原始获取异常、固定/自动续期重载、主异常与释放异常、所有权丢失、事务前置拒绝、键隔离、配置退让及有限标签。真实 Redis IT 使用独立客户端，覆盖虚拟线程竞争、隔离、重入后续期及释放后获取。未执行编译/测试；静态/格式检查不替代真实 Redis、TLS/集群故障转移与生产故障演练。

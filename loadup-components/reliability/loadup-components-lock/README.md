# LoadUp Lock

基于 Redisson 的同步分布式互斥锁。提供类型化锁键、回调执行、显式等待/租期策略及共享 Micrometer 指标；Redis 锁实现和续期交给 Redisson。单一 jar，按需引入，不默认接入所有 Service。

## 引入

引入 LoadUp BOM 后：

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-lock</artifactId>
</dependency>
```

Java 25 / Spring Boot 4，BOM 管理 Redisson 4.8.0。组件使用 Redisson core，不引入其 Spring Boot starter，不重复映射 Spring Data Redis 配置。

## 配置与客户端

默认关闭。已有 `RedissonClient` Bean 时直接复用；存在多个客户端时使用 Spring `@Primary`，或显式创建模板并注入指定客户端。

```yaml
spring:
  application:
    name: payments
loadup:
  lock:
    enabled: true
    namespace: ${spring.application.name}
    wait-timeout: 3s
    lease-mode: watchdog
```

`namespace` 必填，是共享 Redis 中的应用/部署环境隔离标识；需要跨应用互斥时显式使用同一个 namespace。Redis 连接、认证、TLS、拓扑和 watchdog 超时由原生 Redisson 客户端配置管理。

### 组件创建客户端

没有已有客户端时，显式提供原生 Redisson YAML 资源：

```yaml
loadup:
  lock:
    enabled: true
    namespace: payments-prod
    redisson-config: file:/etc/payments/redisson-lock.yaml
    wait-timeout: 3s
    lease-mode: watchdog
```

本地开发的 `redisson-lock.yaml` 示例：

```yaml
singleServerConfig:
  address: "redis://127.0.0.1:6379"
lockWatchdogTimeout: 30000
```

生产 TLS 使用 Redisson 原生 `rediss://` 和对应 SSL 配置；凭据由外部受控配置或应用创建客户端时提供，不提交到仓库。组件只关闭自己创建的客户端，复用客户端由其创建者管理。已有客户端时忽略 redisson-config。`LockTemplate` 可由应用显式替换；无需模板时保持 enabled=false。

| 参数 | 默认值 | 契约 |
|---|---|---|
| enabled | false | 是否装配 |
| namespace | 无 | 必填；不隐式采用全局共享前缀 |
| wait-timeout | 3s | 竞争等待时间，允许 0 立即尝试 |
| lease-mode | WATCHDOG | WATCHDOG / FIXED |
| lease-time | 无 | FIXED 必填，WATCHDOG 不允许设置 |
| redisson-config | 无 | 仅缺少 RedissonClient 时创建客户端 |

时间以毫秒传给 Redisson，正值必须至少 1ms，超出 long 毫秒范围拒绝；更细的精度按 toMillis 截断。wait-timeout 控制锁竞争等待，不是涵盖 DNS、连接/命令超时的端到端截止时间，这些参数由原生客户端控制。

## 使用

```java
public OrderDTO submit(OrderCommand command) throws InterruptedException {
    var key = LockKey.tenant("order-submit", trustedTenantId, command.orderId());
    return lockTemplate.execute(key, () -> executeBusiness(command));
}
```

租户必须来自可信认证/授权范围。全局资源显式使用 `LockKey.global("configuration", resourceId)`。key 按 namespace/business/scope/tenant/resource 分段进行 Base64URL 编码，区分全局与租户，避免分隔符和 Redis Cluster hash tag 注入；编码不是加密，不保存秘密数据。

`execute` 支持返回值及原始 checked exception；`run(key, Runnable)` 用于无返回值。回调在当前线程执行，平台/虚拟线程使用同一语义。原生高级能力仍可通过 RedissonClient 获得 RLock，Redis 名称通过 `key.redisName(namespace)` 获取。

### 每次调用选择租期

```java
var options = LockOptions.fixed(Duration.ofSeconds(1), Duration.ofSeconds(20));
return lockTemplate.execute(key, options, () -> executeBusiness(command));
```

- WATCHDOG 调用 `tryLock(wait, unit)`，续期由 Redisson 管理；它不限制业务执行时长。
- FIXED 调用 `tryLock(wait, lease, unit)`，到期自动释放，关闭 watchdog；执行超时可能出现旧执行者与新执行者重叠。
- 同一线程/客户端按 Redisson 语义可重入，每层成功获取对应一次释放；嵌套同一锁保持相同租期策略。

## 异常与释放契约

| 结果 | 行为 |
|---|---|
| 获得锁 | 执行业务，退出/异常时释放 |
| 等待超时 | 抛 LockUnavailableException，不执行回调 |
| 中断 | 恢复线程中断标记并抛 InterruptedException，不执行回调 |
| Redis 操作异常 | 原始异常向上抛出，不伪装为竞争超时 |
| 回调异常 | 保留原始异常 |
| 释放失败/锁已失效 | 正常业务也报失败；已有业务异常时将释放异常添加为 suppressed |

不通过 isHeldByCurrentThread 判断后静默跳过释放，也不 forceUnlock。获取结果未知时不执行回调、不盲目重试业务；由 Redisson 租期恢复及应用幂等处理未知结果。同步锁范围在回调返回时结束，不能返回 Future/Publisher 后让实际业务在其他线程继续执行；流式响应/延迟消费也不在该范围内。

## 与事务及 ServiceTemplate 集成

模板拒绝在已开启的 Spring 命令式事务中获得锁。正确顺序为：获得锁 → 开启事务 → 业务写入 → 提交/回滚 → 释放锁。

```java
return lockTemplate.execute(key,
    () -> transactionTemplate.execute(status -> writeOrder(command)));
```

或者在未标注 Transactional 的编排入口内调用另一个 Spring Bean 的 Transactional 方法。不要在同类自调用上假设 Spring 代理生效。事务提交后的异步监听/消息投递不自动包含在锁范围；可靠副作用使用 Outbox。

ServiceTemplate 用于入口生命周期，LockTemplate 只用于需要互斥的临界区，可以显式组合；普通 Service 不自动套锁。

## 能力矩阵

| 能力 | 首版契约 |
|---|---|
| 同步可重入互斥 | Redisson RLock，同线程获取/释放 |
| 多实例竞争 | 共享 Redis、namespace 与锁键 |
| 等待与中断 | 有界竞争等待、支持中断、超时明确失败 |
| 自动续期/固定租期 | 显式策略，使用原生实现 |
| 资源释放 | try-with-resources，保留主异常及 suppressed |
| Spring 事务 | 已有事务拒绝，锁包裹实际提交 |
| 指标 | 共享 MeterRegistry，可不提供 |
| 注解/SpEL、读写/公平锁 | 首版不封装；高级原生接口按需使用 |
| Fencing、业务幂等、exactly-once | 不提供；由受保护资源及业务契约实现 |

锁控制当前并发，GlobalUnique 控制持久唯一声明，数据库唯一约束与状态条件更新保证最终数据正确性。租期/网络故障可能导致失去锁的执行者继续工作；收单状态更新必须保留数据库约束，要求 fencing 时受保护资源必须验证 token。Outbox 自己的租约不替换为此锁。

## 指标与日志

注入 Boot 管理的 MeterRegistry；不创建静态 Registry。不提供 Registry 时仍可使用锁。

| 指标 | 类型 | 标签 |
|---|---|---|
| loadup.lock.acquire | Timer | outcome=success/failure，reason=none/timeout/interrupted/error |
| loadup.lock.hold | Timer | outcome=success/failure，涵盖回调及释放 |
| loadup.lock.release.failures | Counter | 无动态标签 |

不把锁键、租户、订单、完整资源 ID 加入标签；LogUtil 仅记录指标写入失败，不打印业务键。原始 Redis/业务异常交由应用错误处理，指标失败不作为业务重试依据。

## 验证

已编写配置与生命周期单测，以及基于项目 Testcontainers/Testify 测试依赖的真实 Redis IT：独立客户端竞争、租户/全局隔离、虚拟线程、释放后再次获取及重入/watchdog 续期。尚未运行，不代表完成生产验收。

在依赖已安装、Docker 可用时，由用户执行：

```bash
mvn clean test -pl :loadup-components-lock -Dtest=LockTemplateTest,LockKeyOptionsTest,LockAutoConfigurationTest -Dskip.spotless=true -Dskip.spotbugs=true
mvn clean verify -pl :loadup-components-lock -Dit.test=LockTemplateIT -Dskip.unit.tests=true -Dskip.spotless=true -Dskip.spotbugs=true
```

部署环境的故障转移、长暂停、断网、固定租期过期及事务边界验证保留在 ROADMAP。设计见 [ARCHITECTURE.md](ARCHITECTURE.md)。

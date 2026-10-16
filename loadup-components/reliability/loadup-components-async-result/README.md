# LoadUp Async Result

通过关联 ID 等待 MQ、Outbox、Webhook 或本地异步处理的业务结果。支持同步方法返回和 CompletableFuture；Local 后端用于同进程，Redis 后端支持结果到达任意实例。组件不发送 MQ 消息，不决定支付等业务的最终状态。

## 能力矩阵

| 能力 | 契约 |
|---|---|
| 关联 | 可信 tenantId + operation + businessId + 每次随机 UUID correlationId |
| 同步 / 异步 | execute / executeAsync，共用容量、超时和清理逻辑 |
| 单结果 | 首个终态生效；按编码后 payload 或错误码判断重复/冲突 |
| Redis 多实例 | 先存结果再通知原实例；丢失通知由查询兜底 |
| 超时 / 取消 | 只终止本地等待，不撤销业务，不删除已注册结果 |
| 幂等 / 恰好一次 | 业务负责；不自动重发、不保证消费者恰好一次 |
| 结果保留 | 从注册开始计算 TTL，完成或重复回调不续期 |
| 事务保护 | execute 与 executeAsync 在活动事务入口均拒绝执行 |
| 传输 | 保留原生 Kafka/RabbitMQ/HTTP API；不要求引入统一 MQ 框架 |

## 引入

先导入 LoadUp BOM，业务依赖 API，应用选择一个 binder：

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-async-result-api</artifactId>
</dependency>
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-async-result-binder-redis</artifactId>
</dependency>
```

本地模式改为 `loadup-components-async-result-binder-local`。Redis 模式由应用提供唯一候选或 @Primary RedissonClient；组件复用客户端，不创建或关闭它。Redis 连接、ACL、TLS 和命令超时由原生客户端配置。

```yaml
loadup:
  components:
    async-result:
      enabled: true
      binder-type: redis
      namespace: payment-prod
      wait-timeout: 5s
      max-wait-timeout: 30s
      max-pending: 10000
      result-retention: 15m
      fallback-poll-interval: 500ms
      max-result-bytes: 65536
      max-retained: 100000
```

默认 enabled=false，binder-type=local，启用时 namespace 必填。max-pending 同时限制每个 Template 的本地等待数量和仍在执行的发送回调；等待超时不会立即归还尚未退出的回调执行许可，防止阻塞回调无限累积。max-retained 仅限制 Local 存储条数，满时拒绝注册，不驱逐正在等待的记录。Redis 的存储预算由 TTL、应用流量和 Redis 容量共同控制，不提供全局条数配额。结果大小按业务 payload 的 UTF-8 字节数计算；MQ 和 HTTP 入口还应配置自身载荷上限。

result-retention 必须不小于 max-wait-timeout，最长 7 天；同步等待最长 1 天，实际应远小于入口 HTTP 超时。结果保留期必须覆盖正常业务处理和允许晚到的窗口。过期后回调返回 UNKNOWN，不重新创建记录。

## Service 同步等待

以下为接入示例；应用注入 AsyncResultTemplate 和自己的消息发送器：

```java
public PaymentResultDTO submit(PaymentCreateCommand command) {
    var options = new AsyncResultOptions(
            trustedTenantId, "payment.submit.v1", command.orderId(), Duration.ofSeconds(5));
    return asyncResult.execute(options, PaymentResultDTO.class, key -> {
        // Put all AsyncReplyKey fields in the message envelope or headers.
        return paymentPublisher.send(command, key);
    });
}
```

发送回调返回 `CompletionStage<Void>`，表示客户端/Broker 发送确认，业务结果另行回传。仅有同步发送 API 时，成功调用后返回 `CompletableFuture.completedFuture(null)`；该阶段的完成不表示消费者已完成。

回调在组件虚拟线程执行；调用入口捕获 ScopedValue ExecutionContext 及已注册的 Micrometer ThreadLocal 上下文。不要依赖自动传播 Spring Security 身份，权限检查放在入口，消费者身份另行建立。回调中的事务由应用显式开启；网络操作必须有自身超时，组件不能强制撤销任意用户回调。超时预算包含注册、发送及等待；超时前尚未开始的回调不会再主动执行，已开始的发送仍可能完成。

## 结果接收

```java
AsyncCompletionStatus status = asyncResult.complete(replyKey, paymentResult);
// Or map an external failure to a known local enum first.
asyncResult.completeFailure(replyKey, PaymentResultCodeEnum.CHANNEL_REJECTED);
```

结果接收端验证消息来源、租户、operation 版本和 DTO，再调用组件。不要根据客户端提供的 tenantId 或 correlationId 直接授权。成功结果只接受调用方声明的 Class 类型，不携带 Java 类名或使用多态反序列化协议；泛型集合可以放在具体 DTO 中。

| complete 返回 | 消费建议 |
|---|---|
| ACCEPTED | 新结果已保存，可 ACK |
| DUPLICATE | 相同结果已保存，可 ACK |
| CONFLICT | 不覆盖；记录业务冲突，按接入方规则告警或死信 |
| UNKNOWN | 未注册或过期；按业务订单查询和晚到策略处理 |
| 抛存储异常 | 保存结果不确定；保留重试能力，不直接当作业务失败 |

失败回传仅保存 ResultCode 枚举的稳定 code，不传远端异常消息。调用方收到 AsyncResultFailureException，外层规范码为 ASYNC_RESULT_REMOTE_FAILURE，getRemoteCode() 保留原码。业务需要原业务码时，显式匹配自己已知的枚举后抛 CommonException；组件不按类名重建远端枚举，也不把未知字符串包装成 ResultCode。

## 超时和 HTTP

`executeAsync` 返回 CompletableFuture，Spring MVC 可将其映射为 SuccessResponse 后异步返回；响应 envelope 仍属于 Controller。Future 的普通 continuation 可能在完成线程执行，需要长时间处理时使用显式执行器的 thenApplyAsync 等方法。

ASYNC_RESULT_TIMEOUT 只表示本次等待超时；业务可按订单状态将其映射为 `SuccessResponse.of(processingDTO)`，返回 orderId 与 PROCESSING，后续查询数据库中的最终结果。发送失败、ACK 超时或连接中断也可能存在“消息已发送但确认丢失”的情况，不应自动重复扣款。取消 Future、线程中断、应用退出均不向消费者下发业务撤销命令。

## Outbox 与提交顺序

入口先注册等待，回调调用独立事务 Service，保存业务与 Outbox，事务返回后等待后台处理结果。不能在等待外层包 @Transactional，也不能让消费者在事务提交前报告成功。

```java
return asyncResult.execute(options, PaymentResultDTO.class, key -> {
    transactionalOrderService.createAndPublish(command, key);
    return CompletableFuture.completedFuture(null);
});
```

消费者需要可靠回传结果时，可将结果事件与业务变更一起写入其 Outbox，再通过 MQ 发送。现有 Outbox 的完成状态表示本地处理器完成，不自动等同于最终 MQ 消费成功。业务数据库是最终状态来源，Redis 协调结果不能替代订单表。

## 观测与验证

共享 MeterRegistry 记录 loadup.async_result.pending、loadup.async_result.wait（outcome）、loadup.async_result.completions（outcome）与 loadup.async_result.read_failures；不把订单号、租户或 correlationId 用作标签。不打印结果 payload、凭证及 Jackson 异常消息。内部协议无日期字段；业务 DTO 日期格式沿用应用的 Jackson 配置和本地时区契约。

测试及命令见 [test README](loadup-components-async-result-test/README.md)，包括 Local 并发、Redis 两客户端、Kafka request/reply 和真实 MySQL Outbox 提交顺序。测试编写不代表已执行，运行结果以根 ROADMAP 为准。内部设计见 [ARCHITECTURE.md](ARCHITECTURE.md)。

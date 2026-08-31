# LoadUp RetryTask 组件

以 **JobRunr 为底座的分布式重试/后台任务组件**。业务代码只依赖薄 facade（`RetryTaskFacade` +
`RetryTaskProcessor`），不感知 JobRunr 的存储、状态机与调度细节；切换底层引擎时业务代码零修改。

## 引入

先引入 `loadup-dependencies` BOM，业务模块只依赖 facade，集成方工程按需加 binder：

```xml
<!-- 业务模块：只写 facade -->
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-retrytask-facade</artifactId>
</dependency>

<!-- 集成方工程：选择引擎 -->
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-retrytask-binder-jobrunr</artifactId>
</dependency>
```

## 使用

实现处理器（`bizType` 唯一，抛异常即触发重试）：

```java
@Component
public class OrderSyncProcessor implements RetryTaskProcessor {

    @Override
    public String bizType() {
        return "order-sync";
    }

    @Override
    public void process(RetryTaskContext context) throws Exception {
        // context.args() 携带注册时的 Map<String,String> 参数
        syncOrder(context.bizId());
    }
}
```

注册任务（按 `bizType + bizId` 幂等）：

```java
@Autowired
private RetryTaskFacade retryTaskFacade;

// 立即执行
retryTaskFacade.register(RetryTaskRequest.of("order-sync", "order-123", Map.of("source", "trade")));

// 指定时间执行
retryTaskFacade.register(RetryTaskRequest.schedule("order-sync", "order-123", Instant.now().plusMinutes(5)));
```

其他操作：`delete(bizType, bizId)` 取消任务；`reset(bizType, bizId)` 失败后按原参数立即重跑；
`getStatus(bizType, bizId)` 查询 `PENDING / PROCESSING / SUCCEEDED / FAILED / DELETED`。

## 配置

```yaml
loadup:
  retrytask:
    biz-types:                 # 按业务类型覆盖重试次数
      order-sync:
        max-retries: 5

jobrunr:                       # JobRunr 官方 starter 属性（BOM 统一版本）
  background-job-server:
    enabled: true              # 生产必须显式开启；默认关闭只注册不执行
    poll-interval-in-seconds: 15
    worker-count: 4
  dashboard:
    enabled: true              # 可选：8000 端口管理台
    username: admin
    password: change-me
  jobs:
    default-number-of-retries: 10
    retry-back-off-time-seed: 3
```

底层存储自动使用应用已有的 `DataSource`（JobRunr 自建表与迁移，无需 Flyway 脚本）；也支持
`jobrunr.database.table-prefix` 等官方属性。

## 能力矩阵（契约）

| 能力 | facade | JobRunr binder |
|------|--------|----------------|
| 注册/立即执行 | `register` | ✓ |
| 定时执行 | `register(scheduleAt)` | ✓ |
| 幂等（bizType+bizId） | `register` 重复调用 | ✓（确定性 jobId） |
| 失败重试 + 指数退避 | 处理器抛异常 | ✓（`max-retries` / seed） |
| 取消 | `delete` | ✓（DELETED 状态） |
| 失败后重跑 | `reset` | ✓（按原参数） |
| 状态查询 | `getStatus` | ✓ |
| 集群心跳 / 死任务找回 | 不感知 | ✓（JobRunr 内置） |
| 管理台 / 监控 | 不感知 | ✓（`jobrunr.dashboard.enabled`） |
| 优先级队列 / 自定义退避策略 | 不提供 | ✗（JobRunr OSS 无此能力，Pro 提供） |

## 实现与决策

设计决策、binder 结构、幂等与替换语义见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

## 许可证

Apache License 2.0 (Apache-2.0)

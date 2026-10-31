# LoadUp Scheduler Binder PowerJob

引入 `io.github.loadup-cloud:loadup-components-scheduler-binder-powerjob`，binder 内直接集成官方 Worker 和可选管理
Client，无需额外的 LoadUp PowerJob 组件。

```yaml
loadup:
  scheduler:
    binder-type: powerjob
powerjob:
  worker:
    enabled: true
    app-name: merchant-app
    server-address: powerjob-server:7700
    protocol: HTTP
    port: 27777
```

先部署 Server 并创建应用。在管理台创建 BUILT_IN / STANDALONE / CRON 任务：processorInfo=`loadupSchedulerProcessor`
，jobParams=`delayedtask.scan`，cron=`0 * * * * ?`，建议 maxInstanceNum=1、instanceRetryNum=0、taskRetryNum=0。Transfer
另建任务，jobParams=`transfer.scan`。业务重试由各业务组件管理，避免平台重试与业务重试叠加。

不使用 PowerJob 时不要引入此 binder；若主动保留两个 binder 依赖，必须额外关闭未使用的原生 Worker/Client，binder 选择不会覆盖
PowerJob 官方配置。

## 部署与配置

先部署独立 PowerJob Server 和数据库，通过管理台创建 namespace / app，为应用分配 appName 和应用密码。Server 的 Spring/JPA
运行时不放入业务应用类路径；Server 版本与 Worker/Client 一致。Server 管理口（默认 7700）与 HTTP 传输口（默认
10010）职责不同，Worker 端口也需要 Server 能访问。

```yaml
powerjob:
  worker:
    enabled: true
    app-name: merchant-app
    server-address: powerjob-server:7700 # No http:// prefix
    protocol: HTTP
    port: 27777
    store-strategy: DISK
    allow-lazy-connect-server: false
  client:
    enabled: false # Enable only when this application manages or triggers jobs
```

Worker 的配置、线程限制、发现、心跳、存储和关闭使用官方 `powerjob.worker.*`。多 Pod 下每个 Worker 需要独立的本地工作目录，按官方
`-Dpowerjob.worker.workspace=/...` 设置；DISK 存储也不能代替业务幂等或数据恢复。

可选管理 Client：

```yaml
powerjob:
  client:
    enabled: true
    password: ${POWERJOB_APP_PASSWORD}
    # server-address / app-name default to the worker settings.
    # Client-only applications may set both explicitly with worker.enabled=false.
```

启用后注入官方 `PowerJobClient`；Spring 创建时认证、关闭时调用 close。消费工程可提供自己的同类型 Bean 替换默认
Client。Client 不启用时不要求应用密码；Worker 的连接校验与 Client 的管理认证不同。密码使用 Secret 注入，生产网络需做好管理
API、传输端口隔离和 TLS/访问控制。框架不自动创建 namespace/app，不向业务应用注入管理员凭据。

## 开发 Processor

```java
@Component("settlementProcessor")
public class SettlementProcessor implements BasicProcessor {
    private final SettlementFacade settlements;
    public SettlementProcessor(SettlementFacade settlements) { this.settlements = settlements; }
    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        settlements.reconcile();
        return new ProcessResult(true);
    }
}
```

在 PowerJob 管理台创建 BUILT_IN / STANDALONE 任务，processorInfo 为 `settlementProcessor`，设置 CRON、API 或其他原生时间策略。配置按
jobId 管理，修改原定义即可动态改期。原生 SDK 返回 ResultDTO，业务必须检查 success 后使用 data；runJob 的返回值是
instanceId，不是业务请求号。

Processor 调用业务 Facade；不要把 Controller、线程中的登录态或原始 HTTP 请求作为任务数据。业务元数据应显式存储/传递，在
Processor 验证并绑定可信租户。框架不会从任意 jobParams 自动授予租户或用户身份。

## 能力矩阵

| 能力                        | 契约                                                                                |
|-----------------------------|-------------------------------------------------------------------------------------|
| Scheduler 扫描入口          | loadupSchedulerProcessor 按 jobParams 分派 ScheduledTask                            |
| Worker 生命周期             | 官方 Starter，原生配置与自动装配                                                    |
| 管理 Client                 | 可选原生 Bean、启动认证、关闭释放、允许覆盖                                         |
| 动态路由到处理器 / 调度策略 | 官方 Spring Processor / Server / SDK                                                |
| 执行观测                    | Spring 管理的 BasicProcessor.process 建立 `loadup.powerjob.process` Observation     |
| 指标标签                    | processor 类名、outcome；不以 jobId、instanceId、租户或 payload 作标签              |
| 执行链上下文                | 每次调用建立独立 ScopedValue，含 jobId / instanceId / taskId / retryTimes，退出恢复 |
| 返回 false / null / 抛异常  | false 保留并记录失败；null 转规范错误；异常原样抛回引擎，观测不保留敏感消息         |
| 分布式上下文                | 不自动传播提交线程的 Trace、SecurityContext 或任意业务变量                          |
| 告警、工作流、重试历史      | 使用原生 Server 能力，不另建执行记录或告警系统                                      |

Observation 使用共享 ObservationRegistry，消费工程引入 Actuator/Observability 后由标准 `management.*` 导出指标和追踪；无
Registry 时使用 NOOP。Spring AOP 的正常代理约束适用，默认使用普通、非 final 的 Processor
类。原生方法级处理器和动态容器内部产生的对象不保证经过该切面；MapReduce 的 map/reduce 回调也不等同于
BasicProcessor.process。

## 关键运行语义

- 管理 HTTP 就绪不等于有可用 Worker；无 Worker 时触发可能直接失败。
- jobId 是定义身份，instanceId 是执行身份；jobName 不视为业务唯一键。
- 删除定义是逻辑删除（99），查询可能返回已删定义；不代表撤回已产生的业务效果。
- 实例重试与任务重试是两层语义，副作用使用业务幂等；runJob 超时不表示服务端未接受。
- 普通 cron 扫描周期为 15 秒，不承诺秒级实时性；独立任务时区、退避与重试策略按原生能力设计。
- 多 Server、多 Worker、k8s 故障切换和容量验证仍需在生产部署环境验收。

## 验证边界

binder 保留 9 项单测，覆盖 Client 配置、生命周期、上下文隔离、观测和 Scheduler 扫描入口；当前 JaCoCo 行 / 分支覆盖率均为
100%。不启动 PowerJob Server、不下载 Server jar，也不依赖为此启动的 MySQL 容器。Testcontainers 官方及社区模块目录未找到
PowerJob 专用模块，本项目不维护自定义 Server 启动器。

独立的 scheduler-test、powerjob-it profile 和 Server 下载依赖已移除。平台通信、动态调度、引擎重试及故障切换需要在独立部署环境验收，单元测试不能替代这些验收。原生
powerjob.worker / powerjob.client 配置及错误码保持不变。

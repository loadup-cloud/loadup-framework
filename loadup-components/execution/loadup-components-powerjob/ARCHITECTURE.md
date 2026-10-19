# PowerJob 集成架构

## 职责

```text
Consumer application (Boot 4 / Java 25)
 ├─ Business Facade ← Spring BasicProcessor ← Official Worker Starter
 ├─ Optional native PowerJobClient ── management HTTP ─┐
 └─ LoadUp process aspect: ScopedValue + Observation  │
                                                    ▼
                         PowerJob Server (separate JVM / database)
```

仅一个实现，采用单 jar。官方 Worker 管理调度通信、线程、Processor 查找与执行、心跳和本地存储；Server 管理定义、实例、分发、重试、日志、工作流和告警。LoadUp 不复制这些模型，不维护业务名到引擎 ID 的映射，不重写调度循环。

## 装配

官方 Starter 的自动配置保留原样；LoadUpPowerJobAutoConfiguration 额外绑定 `powerjob.client.*`，需要时创建 Closeable Client，并注册 process 切面。Client 的 appName/serverAddress 可继承 Worker 配置，密码始终显式提供；Client-only 应用可以禁用 Worker。自定义 Client Bean 优先；失败的客户端配置以 POWERJOB 错误枚举报告，不在错误中输出秘密。

组件不隐式启动或创建 Server，不依赖 Server jar、JPA 或业务数据库。运行参数保持官方命名，未添加 loadup.scheduler/binder-type 或第二套 Worker 配置。

## 执行作用域

PowerJobProcessorAspect 拦截 Spring BasicProcessor.process，绑定全新不可变 ExecutionContext。标识键由 PowerJobExecutionContext 声明；不继承执行线程残留的业务上下文，不读取任务任意字段当作身份。业务服务根据数据库中的可信记录恢复租户。普通嵌套调用结束和异常退出都恢复上层 ScopedValue。

每次执行建立独立 `loadup.powerjob.process` Observation，停止时标记 success/failure。失败返回值同样计为失败；null 违反原生结果契约，转成规范错误。业务异常原样传回 PowerJob 决定重试；观测错误仅保留固定错误码，避免异常链携带 payload。不同实例标识不进入指标标签，跨进程 Trace 不自动串联。

## 业务任务迁移

旧 scheduler/retrytask/jobrunr 模块、公共翻译接口、两个引擎 binder、自维护补丁与专用告警桥均移除。Gotone 保持发送能力，告警集成由官方 PowerJob 与消费方明确接入。

Transfer 是实际消费方：提交与源文件引用在业务数据库事务中完成，定时 Processor 领取 QUEUED 任务，QUEUED → RUNNING 条件更新抢占；同一个运行中的任务不会被第二个 Worker 再次领取。输出和状态仍由 Transfer 管理，原生任务实例代表一次队列扫描，不代表单个 Transfer 业务任务。

## 验证边界

组件单测验证配置校验、覆盖/关闭、结果/异常观测、ScopedValue 与 Observation 恢复。独立 `loadup-components-powerjob-test` 由根 `powerjob-it` profile 启用，使用真实 Server / MySQL 与生产 Starter、Client、切面验证手动执行、动态改期、自动重试、实例恢复、删除和重启恢复。Transfer 的真实 MySQL 用例验证并发领取和显式重排队。

PoC 已验证 Server Java 21 / Worker Java 25；Server Java 25、多节点故障接管、网络分区、升级备份和性能 SLA 不在已通过范围。Java 25 下 Netty 原生访问与 Fastjson Unsafe 警告仍需跟进上游，不通过隐藏日志宣称解决。

# LoadUp Scheduler Test

独立真实环境回归套件，使用生产 PowerJob 组件连接官方 5.1.7 Server 与临时 MySQL。默认 reactor 不启动外部调度平台；按需启用：

```bash
export POWERJOB_SERVER_JAVA=/absolute/path/to/jdk-21/bin/java
mvnd -Ppowerjob-it -pl :loadup-components-scheduler-test -am clean verify \
  -Dtest=PowerJobIntegrationTest -Dit.test=PowerJobRuntimeIT \
  -Dsurefire.failIfNoSpecifiedTests=false -Dfailsafe.failIfNoSpecifiedTests=false \
  -Dskip.spotbugs=true -Dskip.enforcer=true -Dtestcontainers.reuse.enable=false
```

需要 Java 25、mvnd、Docker 和制品下载网络。Server jar 通过 BOM 定位并复制到 target/server，不加入 Worker 类路径。Server 默认使用测试 JVM 的 java.home；推荐用环境变量单独指定已验证的 Java 21。测试使用随机凭据，创建独立 namespace/app，分配端口，退出清理容器/进程/临时配置；工作目录与报告均在 target，禁止提交凭据与运行产物。

## 能力矩阵

| 用例 | 验收 |
|---|---|
| 手动执行 | 原生管理 Client Bean、参数值和成功实例 |
| 动态改期 | 保持 jobId，从远期 cron 修改并实际触发 |
| 原生实例重试 | 首次失败后成功，实际调用次数匹配 |
| 终态恢复 | retryInstance 将 FAILED 恢复为成功 |
| Server 重启 | Worker 在线后执行持久定义 |
| 删除 | 定义状态为 DELETED |
| LoadUp 执行作用域 | 真实 Worker 通过 Spring 代理调用，实例 ID 上下文匹配 |

真实 Server 最新回归为 **4 tests, 0 failures, 0 errors, 0 skipped**，耗时 82.47 秒；以生产组件、Spring 管理 Client、实际 Worker 调用链验证。新组件单测在 2026-10-19 补齐后为 7 项通过、行/分支覆盖率均 100%。未执行多节点或 k8s 环境验收。完整范围见根 ROADMAP 和 target/failsafe-reports。

测试套件已迁至 Scheduler 聚合目录，powerjob-it profile 保持显式启用；包含本地扫描入口通过真实 PowerJob binder 唤醒的回归。

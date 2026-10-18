# LoadUp Components JobRunr

JobRunr 9.0.0 的最小补丁运行时，修复远期周期任务更新 cron 后仍受旧执行时间缓存阻塞的问题。由 scheduler 与 retrytask 的 JobRunr binder 共同依赖，沿用原有 API、任务 ID、数据库表和官方 Spring Boot starter。

## 能力矩阵

| 能力 | 契约 |
|---|---|
| 周期定义刷新 | 每个后台服务器重载定义时，清除已变更/删除任务的执行时间缓存 |
| 无关任务 | 保留定义未变化任务的缓存，避免重复调度 |
| 实现边界 | 只修改上游 `ProcessRecurringJobsTask` 中一次重载赋值，其他上游类和资源不变 |
| 后台执行 | 继续使用官方 JobRunr；scheduler 与 retrytask 共享同一引擎 |
| 核心类来源 | 替换原 `org.jobrunr:jobrunr`，不允许两份 core jar 同时存在 |
| 源码分发 | 附带完整且对应的 sources jar、上游许可与修改说明 |

## 接入

通常只需引入 scheduler/retrytask 的 JobRunr binder，它们已排除原 core 并引入本模块，无新增配置。

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-jobrunr</artifactId>
</dependency>
```

版本由 LoadUp BOM 管理。若消费工程单独引入官方 starter 或 core，必须排除 `org.jobrunr:jobrunr`，确保只保留本替换运行时；本模块的 upstream core 依赖为 provided，不传递给消费方。不得依赖 classpath 顺序覆盖重复类。集成 IT 会检查核心类资源只有一份，且与补丁 helper 来自同一个运行时。

## 构建与验证

```bash
mvnd -B -ntp -T 1 clean verify \
  -pl :loadup-dependencies,:loadup-components-scheduler-test,:loadup-components-retrytask-test -am \
  '-Dtest=RecurringJobCacheInvalidatorTest,*Scheduler*Test,*RetryTask*Test,GotoneRetryTaskNotifierTest' \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dit.test=JobRunrDistributionIT,SchedulerTemplateIT,QuartzSchedulerTemplateIT,RetryTaskFacadeIT \
  -Dfailsafe.failIfNoSpecifiedTests=false -Dskip.spotbugs=true
```

必须带 `clean`。构建下载 BOM 管理的原始 core 和 sources，保留原始 multi-release 资源，应用严格匹配的单点源码替换并重新编译该类。版本约束阻止未经补丁审查升级 upstream。匹配失败立即终止，不能静默退回未修复版本。

## 升级与撤销

上游修复发布后，先使用确定性的“旧年度任务已入队 → 改为每 5 秒 → 实际执行”回归验证；再同时移除两个 binder 的替换依赖及原 core exclusion、删除本模块与 BOM/聚合声明。不要在同一应用混用两份运行时。版本升级须重新审查核心源码、原始运行依赖、许可、完整源码产物与回归。

## 许可

该 jar 包含上游 JobRunr 库，按其 LGPL-3.0-or-later 条款标识，不能整体视为 LoadUp 的 Apache-2.0 代码。新增 helper 的 Apache-2.0 文件头单独保留。上游许可及修改说明随 jar 放在 `META-INF`；完整对应源代码通过同坐标的 `sources` classifier 提供。构建材料及补丁逻辑均在本目录。

设计与维护边界见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 验证记录

2026-10-18 定向合并 clean verify：31 项单测、21 项 scheduler/Quartz/retrytask IT 通过；补丁模块最终独立 clean verify：3 项单测、2 项制品 IT 通过，SpotBugs 0 告警，Spotless/License/Enforcer 通过。helper 行/分支覆盖率均为 100%。制品 IT 比对原 core 的全部非目录条目，除目标补丁类和 manifest 外逐字节相同，并检查完整源码、许可与 multi-release 标记。未执行完整 reactor、远程 CI 或多节点部署验收。

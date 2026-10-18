# LoadUp Scheduler Test

Scheduler 组件的集成测试与单元测试模块（不发布给业务方使用）。

## 覆盖范围

- `SchedulerTemplateIT`：JobRunr binder 端到端契约（MySQL TestContainer）——
  注册/幂等/触发/cron 更新/删除/未知任务状态。
- `QuartzSchedulerTemplateIT`：Quartz binder 端到端契约（内存 JobStore）——
  注册/触发/cron 更新/删除/未知任务状态。
- `DefaultSchedulerProcessorRegistryTest`：处理器注册表单元测试。

## 运行

```bash
# 仅单元测试
mvnd clean test -pl :loadup-dependencies,:loadup-components-scheduler-test -am

# 含集成测试（需要 Docker，自动启动 MySQL 容器）
mvnd clean verify -pl :loadup-dependencies,:loadup-components-scheduler-test -am
```

## 测试配置

- `application-test.yml`：本地测试配置（JobRunr 5 秒轮询、Quartz 内存 JobStore）。
- `application-ci.yml`：CI 配置（禁用容器复用，降低日志噪音）。

## 许可证

Apache License 2.0 (Apache-2.0)

## 接入步骤

此模块用于验证组件契约，通常只在本项目的测试构建中使用；应用接入请选同级 API 与实现模块。
需要单独执行时，可在仓库根目录使用 `mvnd clean test -pl :loadup-dependencies,:loadup-components-scheduler-test -am`；外部服务和容器要求以测试类及测试配置为准。

设计边界与装配路径见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 远期 cron 热更新回归

2026-10-18 调查 CI run 39026729412：11 项 IT 中仅 JobRunr 的 `updateCronChangesSchedule` 超时。原始 6 项 JobRunr IT 在本地一次通过；补充“等待旧年度实例已被后台安排”的前置条件后，定向真实 MySQL IT 稳定出现相同的 20 秒超时。用例保留执行断言，不跳过；此问题由共享补丁运行时修复，故障机制详见 [JobRunr 架构](../loadup-components-scheduler-binder-jobrunr/ARCHITECTURE.md)。

定向回归（需要 Docker；移除补丁时可复现原故障）：

```bash
mvnd -B -ntp -T 1 clean verify -pl :loadup-dependencies,:loadup-components-scheduler-test -am \
  -Dskip.unit.tests=true '-Dit.test=SchedulerTemplateIT#updateCronChangesSchedule' \
  -Dfailsafe.failIfNoSpecifiedTests=false -Dskip.spotbugs=true
```

2026-10-18 引入共享补丁后，JobRunr 7 项 IT（含核心类唯一来源检查）、Quartz 5 项 IT 及本模块 13 项单测全部通过；retrytask 的 9 项真实 MySQL IT 同批通过。补丁模块另有 3 项单测和 2 项制品 IT 通过。未触发远程 CI；现有两个 scheduler IT 仍使用固定 test profile。

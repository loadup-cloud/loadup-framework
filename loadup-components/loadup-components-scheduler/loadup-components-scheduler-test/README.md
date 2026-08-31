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
mvn test -pl loadup-components/loadup-components-scheduler/loadup-components-scheduler-test

# 含集成测试（需要 Docker，自动启动 MySQL 容器）
mvn verify -pl loadup-components/loadup-components-scheduler/loadup-components-scheduler-test
```

## 测试配置

- `application-test.yml`：本地测试配置（JobRunr 5 秒轮询、Quartz 内存 JobStore）。
- `application-ci.yml`：CI 配置（禁用容器复用，降低日志噪音）。

## 许可证

Apache License 2.0 (Apache-2.0)

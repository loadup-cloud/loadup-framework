# LoadUp Components JobRunr Architecture

## 问题与修复点

JobRunr 9.0.0 会提前生成远期任务，并将下一次执行时间缓存于 `ProcessRecurringJobsTask.recurringJobRuns`。仅更新持久化 cron 并清理待执行实例不能使该内存时间失效；例如年度计划改为每 5 秒执行后，后台仍可能判断已经调度到明年而跳过新计划。

每个后台服务器在发现 recurring definitions 更新时，调用 `RecurringJobCacheInvalidator.refresh`。helper 对比任务 ID、createdAt（创建/重新注册代次）、schedule expression 和 zoneId；只保留完全一致定义的游标，其余清除。这样多节点分别通过正常存储更新检测刷新，无反射、共享可变全局状态、数据库写入或服务器重启。真正的多节点竞争仍需部署验收。

清除内存游标后，原 JobRunr 调度逻辑照常读取数据库中 AWAITING/SCHEDULED/ENQUEUED/PROCESSING 状态，保留其防重复执行机制；binder 原有待执行实例清理继续工作。补丁不取消已经 PROCESSING 的业务，也不承诺更新和后台调度之间具有事务原子性。

## 制品结构

```text
官方 core jar + 官方 sources jar（固定 9.0.0）
  → 解包原类/资源，排除原 ProcessRecurringJobsTask.class
  → 单点替换 sources 中定义重载语句，缺失则失败
  → 只编译修改的任务类和 LoadUp helper
  → 一个替换运行时 jar + 完整对应 sources jar
```

完整上游 source 放在 target/generated-sources/jobrunr，并由 build-helper 标记为生成源码目录，使 IDEA 可以解析上游 API。Maven Compiler 明确只编译修改的任务类和 helper，其余原始类沿用上游二进制。仓库只维护 POM 中的严格替换点和新增 helper，不复制维护整个上游源码树。原始 dashboard、服务描述符、multi-release Jackson 3 类均保留，manifest 保留 `Multi-Release: true` 和 `org.jobrunr.core` 模块名。源码附件从独立 target/distribution-sources 目录打包，包含原始全部源码、修改后的任务、helper 和许可说明，不将 helper 复制回生成源码根目录造成 IDE 重复定义。

替换模块以 provided 引用原 core 供编译，并显式保留 core 原本的 SLF4J/ASM 运行依赖；两个 binder 排除 starter 带入的原 core。消费者得到单一运行时，避免同名类由加载顺序决定。版本由 BOM 管理；模块额外固定补丁已审查的 upstream 基线，并由 Enforcer 校验。上游版本变化必须同步审查补丁和依赖，不能自动应用到未知版本。

## 验证边界

- helper 单测覆盖无关任务新增、定义未变、cron/时区变更、删除、同 ID 重建与首次空刷新。
- `SchedulerTemplateIT` 等待旧年度实例已被安排后更新 cron，保留实际执行断言，并检查运行时核心类唯一来源。
- Quartz IT 和 retrytask IT 验证共享运行时没有破坏其他适配路径。
- 现有覆盖率工具只收集 src/main/java 对应手写类，因此继续门禁新增 helper，不把从上游解包的数百个第三方类冒充本项目代码。JaCoCo/SpotBugs 同样只分析新增 helper，避免 multi-release 上游类重名和第三方覆盖率混入；上游生成源码保留其原许可，不套用 LoadUp 文件头，新增 helper 仍执行常规 License 门禁；未降低其他组件的门禁。

上游源码依据：[ProcessRecurringJobsTask 9.0.0](https://github.com/jobrunr/jobrunr/blob/v9.0.0/core/src/main/java/org/jobrunr/server/tasks/zookeeper/ProcessRecurringJobsTask.java)。运行证据与未验收事项见根 ROADMAP。

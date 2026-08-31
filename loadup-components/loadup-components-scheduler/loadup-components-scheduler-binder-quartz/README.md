# LoadUp Scheduler Binder - Quartz

[![Java](https://img.shields.io/badge/java-17%2B-blue)]()
[![Spring Boot](https://img.shields.io/badge/spring--boot-3.x-green)]()
[![Quartz](https://img.shields.io/badge/quartz-2.x-orange)]()
[![License](https://img.shields.io/badge/license-Apache--2.0-blue)]()

## 📋 概述

基于 Quartz 的企业级调度实现，支持分布式集群、任务持久化和完整的任务管理功能。**推荐用于生产环境**。

## ✨ 特性

- ✅ **功能强大**: 企业级调度框架，功能完整
- ✅ **分布式集群**: 支持多实例集群部署
- ✅ **任务持久化**: 支持将任务信息持久化到数据库
- ✅ **完整管理**: 支持暂停、恢复、手动触发等操作
- ✅ **动态更新**: 支持动态更新 Cron 表达式
- ✅ **高可用**: 集群模式下任务自动故障转移
- ⚠️ **需要数据库**: 集群模式需要数据库支持

## 🎯 适用场景

- ✅ 生产环境
- ✅ 分布式集群部署
- ✅ 需要任务持久化
- ✅ 需要完整任务管理功能
- ✅ 高可用要求
- ✅ 企业级应用

## 📦 依赖

```xml

<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-scheduler-binder-quartz</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

        <!-- 集群模式需要数据库驱动 -->
<dependency>
<groupId>mysql</groupId>
<artifactId>mysql-connector-java</artifactId>
</dependency>
```

## 🔧 配置

### 内存模式（开发/测试环境）

```yaml
loadup:
  scheduler:
    type: quartz

spring:
  quartz:
    job-store-type: memory
    properties:
      org.quartz.threadPool.threadCount: 5
```

### 集群模式（生产环境）

```yaml
loadup:
  scheduler:
    type: quartz

spring:
  quartz:
    job-store-type: jdbc
    jdbc:
      initialize-schema: always  # 首次启动时创建表
    properties:
      # 调度器配置
      org.quartz.scheduler.instanceName: LoadUpScheduler
      org.quartz.scheduler.instanceId: AUTO

      # JobStore 配置
      org.quartz.jobStore.class: org.quartz.impl.jdbcjobstore.JobStoreTX
      org.quartz.jobStore.driverDelegateClass: org.quartz.impl.jdbcjobstore.StdJDBCDelegate
      org.quartz.jobStore.tablePrefix: QRTZ_
      org.quartz.jobStore.isClustered: true
      org.quartz.jobStore.clusterCheckinInterval: 20000

      # 线程池配置
      org.quartz.threadPool.class: org.quartz.simpl.SimpleThreadPool
      org.quartz.threadPool.threadCount: 10
      org.quartz.threadPool.threadPriority: 5
      org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread: true

# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/scheduler_db?useSSL=false
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### PostgreSQL 配置

```yaml
spring:
  quartz:
    properties:
      org.quartz.jobStore.driverDelegateClass: org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
```

## 💻 使用示例

### 基本使用

```java

@Component
public class QuartzTasks {

    @DistributedScheduler(name = "dailyReport", cron = "0 0 9 * * ?")
    public void generateDailyReport() {
        log.info("Generating daily report...");
        // 业务逻辑
    }
}
```

### 完整任务管理

```java

@Service
public class TaskManagementService {

    @Autowired
    private SchedulerBinding schedulerBinding;

    // 注册任务
    public void registerTask(String name, String cron) {
        SchedulerTask task = SchedulerTask.builder()
                .taskName(name)
                .cron(cron)
                .description("动态任务")
                .enabled(true)
                .build();

        schedulerBinding.registerTask(task);
    }

    // 暂停任务
    public void pauseTask(String taskName) {
        schedulerBinding.pauseTask(taskName);
    }

    // 恢复任务
    public void resumeTask(String taskName) {
        schedulerBinding.resumeTask(taskName);
    }

    // 立即触发任务
    public void triggerTask(String taskName) {
        schedulerBinding.triggerTask(taskName);
    }

    // 更新 Cron 表达式
    public void updateCron(String taskName, String newCron) {
        schedulerBinding.updateTaskCron(taskName, newCron);
    }

    // 删除任务
    public void deleteTask(String taskName) {
        schedulerBinding.unregisterTask(taskName);
    }
}
```

## 🗄️ 数据库表结构

Quartz 需要以下数据库表（自动创建）：

- `QRTZ_JOB_DETAILS` - 作业详细信息
- `QRTZ_TRIGGERS` - 触发器信息
- `QRTZ_SIMPLE_TRIGGERS` - 简单触发器
- `QRTZ_CRON_TRIGGERS` - Cron 触发器
- `QRTZ_BLOB_TRIGGERS` - Blob 触发器
- `QRTZ_CALENDARS` - 日历信息
- `QRTZ_PAUSED_TRIGGER_GRPS` - 暂停的触发器组
- `QRTZ_FIRED_TRIGGERS` - 已触发的触发器
- `QRTZ_SCHEDULER_STATE` - 调度器状态
- `QRTZ_LOCKS` - 锁表（集群用）

## 🔍 功能对比

| 功能     | Quartz | 说明          |
|--------|--------|-------------|
| 动态注册   | ✅      | 完全支持        |
| 动态注销   | ✅      | 完全支持        |
| 暂停/恢复  | ✅      | 完全支持        |
| 手动触发   | ✅      | 完全支持        |
| Cron更新 | ✅      | 动态更新，无需重启   |
| 分布式    | ✅      | 支持集群部署      |
| 持久化    | ✅      | 任务信息持久化到数据库 |
| 故障转移   | ✅      | 自动故障转移      |
| 外部依赖   | ⚠️     | 集群模式需要数据库   |

## 🚀 集群部署

### 1. 准备数据库

```sql
CREATE
DATABASE scheduler_db DEFAULT CHARACTER
SET utf8mb4;
```

### 2. 配置多个实例

每个实例使用相同的配置，Quartz 会自动协调：

```yaml
# 应用实例 1
server:
  port: 8081

loadup:
  scheduler:
    type: quartz

spring:
  quartz:
    job-store-type: jdbc
    properties:
      org.quartz.scheduler.instanceId: AUTO
      org.quartz.jobStore.isClustered: true
```

```yaml
# 应用实例 2
server:
  port: 8082

# 其他配置相同...
```

### 3. 启动多个实例

```bash
# 启动实例 1
java -jar app.jar --server.port=8081

# 启动实例 2
java -jar app.jar --server.port=8082
```

## ⚙️ 高级配置

### 线程池优化

```yaml
spring:
  quartz:
    properties:
      org.quartz.threadPool.threadCount: 20  # 根据任务量调整
      org.quartz.threadPool.threadPriority: 5
```

### 数据源优化

```yaml
spring:
  quartz:
    properties:
      org.quartz.dataSource.myDS.maxConnections: 10
```

### Misfire 处理策略

```yaml
spring:
  quartz:
    properties:
      org.quartz.jobStore.misfireThreshold: 60000  # 60秒
```

## ⚠️ 注意事项

1. **数据库要求**: 集群模式需要数据库支持
2. **时钟同步**: 集群节点需要时钟同步
3. **网络连接**: 确保所有节点能访问数据库
4. **表初始化**: 首次启动需要创建 Quartz 表
5. **唯一实例 ID**: 使用 AUTO 自动生成唯一 ID

## 🐛 故障排查

### 任务未执行

- 检查数据库连接
- 查看 `QRTZ_TRIGGERS` 表状态
- 检查 Cron 表达式
- 查看应用日志

### 集群不工作

- 确认 `isClustered` 设置为 true
- 检查所有节点时钟是否同步
- 查看 `QRTZ_SCHEDULER_STATE` 表
- 确认数据库连接正常

### 性能问题

- 调整线程池大小
- 优化数据库连接池
- 检查任务执行时间
- 考虑任务拆分

## 📚 相关文档

- [主 README](../README.md) - Scheduler 组件完整文档
- [API 文档](../loadup-components-scheduler-api/README.md)
- [Quartz 官方文档](http://www.quartz-scheduler.org/documentation/)
- [配置说明](../README.md#配置说明)

## 📄 许可证

Apache License 2.0 (Apache-2.0)

详见 [LICENSE](../../../LICENSE) 文件。

---

**最后更新**: 2025-12-30

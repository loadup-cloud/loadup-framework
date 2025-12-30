# LoadUp Scheduler Component

统一的调度任务组件，提供统一的API接口，支持多种调度框架的底层实现。

## 📋 目录

- [功能特性](#功能特性)
- [架构设计](#架构设计)
- [模块说明](#模块说明)
- [快速开始](#快速开始)
- [使用示例](#使用示例)
- [配置说明](#配置说明)
- [测试说明](#测试说明)
- [扩展开发](#扩展开发)
- [最佳实践](#最佳实践)
- [许可证](#许可证)

---

## 功能特性

- 🎯 **统一API**: 提供统一的调度任务接口，屏蔽底层实现差异
- 🔌 **多框架支持**: 支持 SimpleJob、Quartz、PowerJob、XXL-Job 等多种调度框架
- 🚀 **Spring Boot 3**: 基于 Spring Boot 3.x 和 Spring 6.x 构建
- 🔄 **SPI机制**: 利用 Spring Boot 的 SPI 机制实现自动配置
- 📝 **注解驱动**: 使用 `@DistributedScheduler` 注解声明定时任务
- 🎨 **灵活切换**: 通过配置文件即可切换不同的调度实现
- ✅ **测试完整**: 70%+ 代码覆盖率，包含单元测试和集成测试

---

## 架构设计

### 分层架构

```
┌─────────────────────────────────────────┐
│         应用层 (Application)              │
│  @DistributedScheduler 注解式任务声明      │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         API层 (Unified API)              │
│  SchedulerBinding - 统一业务接口          │
│  SchedulerTask - 任务模型                │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│       Binder层 (Implementation)          │
│  SchedulerBinder - 适配器接口             │
└─────────────────────────────────────────┘
                    ↓
┌──────────┬──────────┬──────────┬─────────┐
│SimpleJob │  Quartz  │ XXL-Job  │PowerJob │
│  实现     │   实现    │   实现    │  实现   │
└──────────┴──────────┴──────────┴─────────┘
```

### 核心组件

#### 1. SchedulerBinder（调度器绑定器接口）

定义统一的调度操作接口：

- `registerTask()` - 注册任务
- `unregisterTask()` - 注销任务
- `pauseTask()` - 暂停任务
- `resumeTask()` - 恢复任务
- `triggerTask()` - 手动触发任务
- `updateTaskCron()` - 更新Cron表达式
- `taskExists()` - 检查任务是否存在

#### 2. SchedulerBinding（调度器绑定接口）

统一的业务API，委托给具体的 Binder 实现。

#### 3. SchedulerTask（调度任务模型）

完整的任务描述模型：

- `taskName` - 任务名称（唯一标识）
- `cron` - Cron表达式
- `description` - 任务描述
- `taskGroup` - 任务分组
- `method` - 执行方法
- `targetBean` - 目标Bean
- `enabled` - 是否启用
- `priority` - 优先级
- `timeoutMillis` - 超时时间
- `maxRetries` - 最大重试次数
- `parameters` - 扩展参数

#### 4. SchedulerTaskRegistry（任务注册表）

负责扫描和管理所有定时任务：

- 扫描 `@DistributedScheduler` 注解
- 维护任务注册表
- 与 SchedulerBinding 集成

---

## 模块说明

### loadup-components-scheduler-api

**核心API模块**，定义了调度任务的统一接口和模型。

**主要类：**

- `SchedulerBinder` - 调度器绑定器接口
- `SchedulerBinding` - 调度器绑定接口
- `DefaultSchedulerBinding` - 默认绑定实现
- `SchedulerTask` - 调度任务模型
- `@DistributedScheduler` - 分布式调度注解
- `SchedulerTaskRegistry` - 任务注册表
- `SchedulerAutoConfiguration` - 自动配置

### loadup-components-scheduler-binder-simplejob

**SimpleJob 实现**，基于 Spring TaskScheduler 的轻量级实现。

**特点：**

- ✅ 轻量级，无需外部依赖
- ✅ 支持动态任务注册
- ✅ 适合单实例应用
- ❌ 不支持分布式调度
- ❌ 不支持任务暂停/恢复

**适用场景：** 单机应用、开发测试环境

### loadup-components-scheduler-binder-quartz

**Quartz 实现**，企业级调度框架。

**特点：**

- ✅ 功能强大，支持分布式集群
- ✅ 支持持久化调度信息
- ✅ 完整的任务管理功能（暂停、恢复、触发）
- ✅ 支持动态更新Cron表达式
- ⚠️ 需要数据库支持（集群模式）

**适用场景：** 生产环境、需要集群部署的应用

### loadup-components-scheduler-binder-xxljob

**XXL-Job 实现**，轻量级分布式任务调度平台。

**特点：**

- ✅ 轻量级分布式调度
- ✅ 提供可视化管理界面
- ✅ 支持任务分片
- ⚠️ 需要部署 XXL-Job Admin 控制台
- ⚠️ 任务管理需通过控制台完成

**适用场景：** 需要可视化管理的分布式应用

### loadup-components-scheduler-binder-powerjob

**PowerJob 实现**，新一代分布式任务调度平台。

**特点：**

- ✅ 新一代分布式调度框架
- ✅ 支持多种任务类型
- ✅ 强大的可视化管理
- ⚠️ 需要部署 PowerJob Server
- ⚠️ 任务管理需通过控制台完成

**适用场景：** 复杂的分布式调度场景

### loadup-components-scheduler-test

**测试模块**，包含完整的测试套件。

**测试覆盖：**

- ✅ 单元测试：6类 / 41个测试方法
- ✅ 实现测试：2类 / 25个测试方法
- ✅ 集成测试：2类 / 7个测试方法
- ✅ 代码覆盖率：70%+（JaCoCo配置）

---

## 快速开始

### 1. 添加依赖

根据需要选择一个调度实现：

#### 使用 SimpleJob（默认，推荐开发环境）

```xml
<dependency>
    <groupId>com.github.loadup.framework</groupId>
    <artifactId>loadup-components-scheduler-binder-simplejob</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 使用 Quartz（推荐生产环境）

```xml
<dependency>
    <groupId>com.github.loadup.framework</groupId>
    <artifactId>loadup-components-scheduler-binder-quartz</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 使用 XXL-Job

```xml
<dependency>
    <groupId>com.github.loadup.framework</groupId>
    <artifactId>loadup-components-scheduler-binder-xxljob</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 使用 PowerJob

```xml
<dependency>
    <groupId>com.github.loadup.framework</groupId>
    <artifactId>loadup-components-scheduler-binder-powerjob</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置文件

在 `application.yml` 中配置调度类型：

```yaml
loadup:
  scheduler:
    type: simplejob  # 可选: simplejob, quartz, xxljob, powerjob
```

### 3. 创建定时任务

```java
import com.github.loadup.components.scheduler.annotation.DistributedScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MyScheduledTasks {

    @DistributedScheduler(name = "dailyReport", cron = "0 0 9 * * ?")
    public void generateDailyReport() {
        log.info("Generating daily report...");
        // 业务逻辑
    }

    @DistributedScheduler(name = "dataSync", cron = "0 */10 * * * ?")
    public void syncData() {
        log.info("Syncing data...");
        // 数据同步逻辑
    }
}
```

---

## 使用示例

### 基础使用 - 注解方式

```java
@Component
public class ScheduledTasks {

    // 每天凌晨1点执行
    @DistributedScheduler(name = "cleanupTask", cron = "0 0 1 * * ?")
    public void cleanup() {
        // 清理逻辑
    }

    // 每5分钟执行
    @DistributedScheduler(name = "heartbeat", cron = "0 */5 * * * ?")
    public void heartbeat() {
        // 心跳检测
    }

    // 每周一上午9点执行
    @DistributedScheduler(name = "weeklyReport", cron = "0 0 9 ? * MON")
    public void generateWeeklyReport() {
        // 生成周报
    }
}
```

### 高级使用 - 动态管理

```java
@Service
public class TaskManagementService {

    @Autowired
    private SchedulerBinding schedulerBinding;

    // 动态注册任务
    public void createTask(String name, String cron) {
        SchedulerTask task = SchedulerTask.builder()
                .taskName(name)
                .cron(cron)
                .description("Dynamically created task")
                .enabled(true)
                .priority(5)
                .build();
        
        schedulerBinding.registerTask(task);
    }

    // 暂停任务（仅Quartz支持）
    public void pauseTask(String taskName) {
        if (schedulerBinding.taskExists(taskName)) {
            schedulerBinding.pauseTask(taskName);
        }
    }

    // 恢复任务（仅Quartz支持）
    public void resumeTask(String taskName) {
        schedulerBinding.resumeTask(taskName);
    }

    // 立即触发任务（仅Quartz支持）
    public void triggerTask(String taskName) {
        schedulerBinding.triggerTask(taskName);
    }

    // 更新Cron表达式（仅Quartz支持）
    public void updateCron(String taskName, String newCron) {
        schedulerBinding.updateTaskCron(taskName, newCron);
    }

    // 删除任务
    public void deleteTask(String taskName) {
        schedulerBinding.unregisterTask(taskName);
    }
}
```

### 查询任务信息

```java
@Service
public class TaskQueryService {

    @Autowired
    private SchedulerTaskRegistry taskRegistry;

    // 获取所有任务
    public Collection<SchedulerTask> getAllTasks() {
        return taskRegistry.findAllTasks();
    }

    // 查询特定任务
    public SchedulerTask getTask(String taskName) {
        return taskRegistry.findByTaskName(taskName);
    }

    // 检查任务是否存在
    public boolean exists(String taskName) {
        return taskRegistry.containsTask(taskName);
    }

    // 获取任务数量
    public int getTaskCount() {
        return taskRegistry.getTaskCount();
    }
}
```

---

## 配置说明

### SimpleJob 配置

最简单的配置，无需额外配置：

```yaml
loadup:
  scheduler:
    type: simplejob  # 或省略，默认为 simplejob
```

### Quartz 配置

#### 内存模式（开发环境）

```yaml
loadup:
  scheduler:
    type: quartz

spring:
  quartz:
    job-store-type: memory
```

#### 集群模式（生产环境）

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
      org.quartz.scheduler.instanceName: LoadUpScheduler
      org.quartz.scheduler.instanceId: AUTO
      org.quartz.jobStore.class: org.quartz.impl.jdbcjobstore.JobStoreTX
      org.quartz.jobStore.driverDelegateClass: org.quartz.impl.jdbcjobstore.StdJDBCDelegate
      org.quartz.jobStore.tablePrefix: QRTZ_
      org.quartz.jobStore.isClustered: true
      org.quartz.jobStore.clusterCheckinInterval: 20000
      org.quartz.threadPool.class: org.quartz.simpl.SimpleThreadPool
      org.quartz.threadPool.threadCount: 10
      org.quartz.threadPool.threadPriority: 5
```

### XXL-Job 配置

```yaml
loadup:
  scheduler:
    type: xxljob

xxl:
  job:
    admin:
      addresses: http://localhost:8080/xxl-job-admin
    executor:
      appname: loadup-executor
      address:  # 可选，自动检测
      ip:       # 可选，自动检测
      port: 9999
      logpath: /data/applogs/xxl-job/jobhandler
      logretentiondays: 30
    accessToken:  # 可选，访问令牌
```

### PowerJob 配置

```yaml
loadup:
  scheduler:
    type: powerjob

powerjob:
  worker:
    enabled: true
    server-address: 127.0.0.1:7700
    app-name: loadup-app
    port: 27777
    protocol: http
    store-strategy: disk
    max-result-length: 8096
```

### Cron 表达式说明

Cron 表达式格式：`秒 分 时 日 月 周 [年]`

**常用示例：**

- `0 0 12 * * ?` - 每天中午12点执行
- `0 */10 * * * ?` - 每10分钟执行一次
- `0 0 9-17 * * MON-FRI` - 工作日9点到17点，每小时执行
- `0 0 0 1 * ?` - 每月1号凌晨执行
- `0 0 0 ? * SUN` - 每周日凌晨执行
- `0 0 1 * * ?` - 每天凌晨1点执行
- `0 0/30 * * * ?` - 每30分钟执行

**在线工具：**

- [Cron表达式生成器](https://cron.qqe2.com/)
- [Cron表达式验证器](https://crontab.guru/)

---

## 测试说明

### 测试架构

本组件包含完整的测试套件，覆盖率达70%+。

#### 测试类型

**1. 单元测试（6类/41方法）**

- `SchedulerTaskTest` - 模型测试
- `DefaultSchedulerBindingTest` - 绑定层测试
- `SchedulerTaskRegistryTest` - 注册表测试
- `SchedulerBinderTest` - API接口测试
- `SchedulerAutoConfigurationTest` - 自动配置测试

**2. 实现测试（2类/25方法）**

- `SimpleJobSchedulerBinderTest` - SimpleJob实现测试
- `QuartzSchedulerBinderTest` - Quartz实现测试

**3. 集成测试（2类/7方法）**

- `SimpleJobSchedulerIntegrationTest` - SimpleJob集成测试
- `QuartzSchedulerIntegrationTest` - Quartz集成测试

### 运行测试

#### 方式一：Maven命令

```bash
# 运行所有测试
mvn test

# 只运行单元测试
mvn test -Dtest='!*Integration*'

# 只运行集成测试
mvn test -Dtest='*Integration*'

# 生成覆盖率报告
mvn clean test jacoco:report

# 查看报告
open target/site/jacoco/index.html
```

#### 方式二：使用测试脚本

```bash
cd loadup-components-scheduler-test
./run-tests.sh
```

#### 方式三：IDE运行

- IntelliJ IDEA: 右键点击测试类 → Run
- 查看覆盖率: Run with Coverage

### 测试配置

测试使用的配置文件：`loadup-components-scheduler-test/src/test/resources/application.properties`

```properties
# 测试配置
loadup.scheduler.type=simplejob
logging.level.com.github.loadup.components.scheduler=DEBUG
```

---

## 扩展开发

### 支持新的调度框架

如需支持其他调度框架，按以下步骤操作：

#### 1. 创建新的 binder 模块

```
loadup-components-scheduler-binder-custom/
├── pom.xml
└── src/main/java/
    └── com/github/loadup/components/scheduler/custom/
        ├── binder/
        │   └── CustomSchedulerBinder.java
        └── config/
            └── CustomSchedulerAutoConfiguration.java
```

#### 2. 实现 SchedulerBinder 接口

```java
public class CustomSchedulerBinder implements SchedulerBinder {

    @Override
    public String getName() {
        return "custom";
    }

    @Override
    public boolean registerTask(SchedulerTask task) {
        // 实现任务注册逻辑
        return true;
    }

    @Override
    public boolean unregisterTask(String taskName) {
        // 实现任务注销逻辑
        return true;
    }

    // 实现其他方法...
}
```

#### 3. 创建自动配置类

```java
@AutoConfiguration
@ConditionalOnProperty(
    prefix = "loadup.scheduler",
    name = "type",
    havingValue = "custom"
)
public class CustomSchedulerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SchedulerBinder.class)
    public SchedulerBinder customSchedulerBinder() {
        return new CustomSchedulerBinder();
    }
}
```

#### 4. 注册自动配置

创建文件：`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

```
com.github.loadup.components.scheduler.custom.config.CustomSchedulerAutoConfiguration
```

#### 5. 配置使用

```yaml
loadup:
  scheduler:
    type: custom
```

---

## 最佳实践

### 1. 任务命名规范

```java
// ✅ 推荐：使用有意义的名称
@DistributedScheduler(name = "user-data-sync", cron = "0 */5 * * * ?")

// ❌ 不推荐：使用无意义的名称
@DistributedScheduler(name = "task1", cron = "0 */5 * * * ?")
```

### 2. Cron表达式管理

```java
// ✅ 推荐：使用配置文件管理Cron表达式
@Value("${schedule.data-sync.cron:0 */5 * * * ?}")
private String dataSyncCron;

@DistributedScheduler(name = "dataSync", cron = "${schedule.data-sync.cron}")
public void syncData() {
    // ...
}
```

### 3. 任务幂等性

```java
// ✅ 推荐：确保任务可重复执行
@DistributedScheduler(name = "dataImport", cron = "0 0 2 * * ?")
public void importData() {
    // 检查是否已执行
    if (isAlreadyProcessedToday()) {
        return;
    }
    // 执行导入逻辑
    doImport();
    // 记录执行状态
    markAsProcessed();
}
```

### 4. 异常处理

```java
// ✅ 推荐：妥善处理异常
@DistributedScheduler(name = "reportGeneration", cron = "0 0 9 * * ?")
public void generateReport() {
    try {
        doGenerateReport();
    } catch (Exception e) {
        log.error("Failed to generate report", e);
        // 发送告警通知
        alertService.sendAlert("Report generation failed", e);
    }
}
```

### 5. 性能优化

```java
// ✅ 推荐：使用异步执行长时间任务
@DistributedScheduler(name = "heavyTask", cron = "0 0 * * * ?")
public void scheduleHeavyTask() {
    // 触发异步任务
    asyncTaskService.executeHeavyTask();
}

@Async
public void executeHeavyTask() {
    // 实际的耗时操作
}
```

### 6. 环境隔离

```yaml
# 开发环境
spring:
  profiles: dev
loadup:
  scheduler:
    type: simplejob

---
# 生产环境
spring:
  profiles: prod
loadup:
  scheduler:
    type: quartz
```

### 7. 监控和日志

```java
@DistributedScheduler(name = "dataBackup", cron = "0 0 3 * * ?")
public void backupData() {
    long startTime = System.currentTimeMillis();
    log.info("Starting data backup...");
    
    try {
        doBackup();
        long duration = System.currentTimeMillis() - startTime;
        log.info("Data backup completed in {} ms", duration);
        
        // 记录指标
        metricService.recordTaskDuration("dataBackup", duration);
    } catch (Exception e) {
        log.error("Data backup failed", e);
        throw e;
    }
}
```

---

## 功能对比

| 功能     | SimpleJob | Quartz | XXL-Job   | PowerJob   |
|--------|-----------|--------|-----------|------------|
| 动态注册   | ✅         | ✅      | ❌         | ❌          |
| 动态注销   | ✅         | ✅      | ❌         | ❌          |
| 暂停/恢复  | ❌         | ✅      | ⚠️        | ⚠️         |
| 手动触发   | ❌         | ✅      | ⚠️        | ⚠️         |
| Cron更新 | ❌         | ✅      | ⚠️        | ⚠️         |
| 分布式    | ❌         | ✅      | ✅         | ✅          |
| 任务分片   | ❌         | ❌      | ✅         | ✅          |
| 可视化管理  | ❌         | ❌      | ✅         | ✅          |
| 外部依赖   | ❌         | 可选(DB) | 必需(Admin) | 必需(Server) |
| 学习成本   | 低         | 中      | 中         | 中          |
| 适用场景   | 单机/开发     | 生产环境   | 分布式       | 复杂分布式      |

⚠️ 表示需要通过管理控制台操作

---

## 注意事项

1. **任务名称唯一性**: 同一应用中任务名称必须唯一
2. **Cron表达式验证**: 确保Cron表达式语法正确
3. **时区问题**: 默认使用服务器时区，注意跨时区部署
4. **单实例限制**: SimpleJob 仅适用于单实例应用
5. **数据库要求**: Quartz集群模式需要数据库支持
6. **外部服务**: XXL-Job和PowerJob需要独立部署管理服务
7. **同时只能使用一种**: 一个应用只能选择一种调度实现

---

## 故障排查

### 常见问题

**Q1: 任务没有执行？**

- 检查 Cron 表达式是否正确
- 确认任务方法是否被正确扫描（检查日志）
- 验证调度器是否正常启动

**Q2: 切换调度器后任务不工作？**

- 清理旧的任务数据
- 重启应用
- 检查新调度器的配置是否正确

**Q3: Quartz 集群模式无法工作？**

- 确认数据库连接正常
- 检查 Quartz 表是否创建
- 验证集群配置参数

**Q4: 任务执行异常？**

- 检查任务方法是否抛出异常
- 查看详细日志
- 确认任务执行权限

---

## 许可证

本项目采用 MIT 许可证。详见 LICENSE 文件。

---

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 联系方式

- 项目地址: [LoadUp Framework](https://github.com/loadup-cloud/loadup-framework)
- 问题反馈: [Issues](https://github.com/loadup-cloud/loadup-framework/issues)

---

**📝 最后更新: 2025-12-30**

### loadup-components-scheduler-api

核心API模块，定义了调度任务的统一接口和模型：

- `SchedulerBinder`: 调度器绑定器接口
- `SchedulerBinding`: 调度器绑定接口
- `SchedulerTask`: 调度任务模型
- `@DistributedScheduler`: 分布式调度注解

### loadup-components-scheduler-binder-simplejob

基于 Spring TaskScheduler 的简单实现，适用于单实例应用：

- 轻量级，无需外部依赖
- 适合简单的定时任务场景
- 不支持分布式调度

### loadup-components-scheduler-binder-quartz

基于 Quartz 的实现，支持集群调度：

- 功能强大，支持分布式
- 支持持久化调度信息
- 完整的任务管理功能（暂停、恢复、触发等）

### loadup-components-scheduler-binder-xxljob

基于 XXL-Job 的实现：

- 轻量级分布式任务调度平台
- 提供可视化管理界面
- 需要部署 XXL-Job Admin 控制台

### loadup-components-scheduler-binder-powerjob

基于 PowerJob 的实现：

- 新一代分布式任务调度平台
- 支持多种任务类型
- 需要部署 PowerJob Server

## 快速开始

### 1. 添加依赖

根据需要选择一个调度实现：

#### 使用 SimpleJob（默认）

```xml

<dependency>
    <groupId>com.github.loadup.framework</groupId>
    <artifactId>loadup-components-scheduler-binder-simplejob</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 使用 Quartz

```xml

<dependency>
    <groupId>com.github.loadup.framework</groupId>
    <artifactId>loadup-components-scheduler-binder-quartz</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 使用 XXL-Job

```xml

<dependency>
    <groupId>com.github.loadup.framework</groupId>
    <artifactId>loadup-components-scheduler-binder-xxljob</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 使用 PowerJob

```xml

<dependency>
    <groupId>com.github.loadup.framework</groupId>
    <artifactId>loadup-components-scheduler-binder-powerjob</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置文件

在 `application.yml` 中配置调度类型：

```yaml
loadup:
  scheduler:
    type: simplejob  # 可选: simplejob, quartz, xxljob, powerjob
```

#### Quartz 配置示例

```yaml
loadup:
  scheduler:
    type: quartz

spring:
  quartz:
    job-store-type: jdbc  # 使用数据库存储
    properties:
      org.quartz.scheduler.instanceName: LoadUpScheduler
      org.quartz.scheduler.instanceId: AUTO
      org.quartz.jobStore.class: org.quartz.impl.jdbcjobstore.JobStoreTX
      org.quartz.jobStore.driverDelegateClass: org.quartz.impl.jdbcjobstore.StdJDBCDelegate
      org.quartz.jobStore.tablePrefix: QRTZ_
      org.quartz.jobStore.isClustered: true
```

#### XXL-Job 配置示例

```yaml
loadup:
  scheduler:
    type: xxljob

xxl:
  job:
    admin:
      addresses: http://localhost:8080/xxl-job-admin
    executor:
      appname: loadup-executor
      port: 9999
```

#### PowerJob 配置示例

```yaml
loadup:
  scheduler:
    type: powerjob

powerjob:
  worker:
    server-address: 127.0.0.1:7700
    app-name: loadup-app
```

### 3. 使用示例

创建定时任务：

```java
import com.github.loadup.components.scheduler.annotation.DistributedScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MyScheduledTask {

    @DistributedScheduler(name = "dailyReport", cron = "0 0 9 * * ?")
    public void generateDailyReport() {
        log.info("Generating daily report...");
        // 业务逻辑
    }

    @DistributedScheduler(name = "dataSync", cron = "0 */10 * * * ?")
    public void syncData() {
        log.info("Syncing data...");
        // 数据同步逻辑
    }
}
```

### 4. 高级用法

#### 使用 SchedulerBinding 进行动态管理

```java
import com.github.loadup.components.scheduler.api.SchedulerBinding;
import com.github.loadup.components.scheduler.model.SchedulerTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskManagementService {

    @Autowired
    private SchedulerBinding schedulerBinding;

    // 动态注册任务
    public void registerTask() {
        SchedulerTask task = SchedulerTask.builder()
                .taskName("dynamicTask")
                .cron("0 0 12 * * ?")
                .description("Dynamic scheduled task")
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

    // 更新任务Cron表达式
    public void updateTaskCron(String taskName, String newCron) {
        schedulerBinding.updateTaskCron(taskName, newCron);
    }

    // 注销任务
    public void unregisterTask(String taskName) {
        schedulerBinding.unregisterTask(taskName);
    }
}
```

## Cron 表达式说明

Cron 表达式格式：`秒 分 时 日 月 周 [年]`

常用示例：

- `0 0 12 * * ?` - 每天中午12点执行
- `0 */10 * * * ?` - 每10分钟执行一次
- `0 0 9-17 * * MON-FRI` - 工作日9点到17点，每小时执行
- `0 0 0 1 * ?` - 每月1号凌晨执行
- `0 0 0 ? * SUN` - 每周日凌晨执行

## 功能对比

| 功能     | SimpleJob | Quartz | XXL-Job | PowerJob |
|--------|-----------|--------|---------|----------|
| 动态注册   | ✅         | ✅      | ❌       | ❌        |
| 暂停/恢复  | ❌         | ✅      | ⚠️      | ⚠️       |
| 手动触发   | ❌         | ✅      | ⚠️      | ⚠️       |
| Cron更新 | ❌         | ✅      | ⚠️      | ⚠️       |
| 分布式    | ❌         | ✅      | ✅       | ✅        |
| 可视化管理  | ❌         | ❌      | ✅       | ✅        |
| 外部依赖   | ❌         | 可选     | 必需      | 必需       |

⚠️ 表示需要通过管理控制台操作

## 扩展支持

如需支持其他调度框架，可以实现以下接口：

1. 实现 `SchedulerBinder` 接口
2. 创建自动配置类，使用 `@ConditionalOnProperty` 指定类型
3. 在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 中注册

示例结构：

```
loadup-components-scheduler-binder-custom/
├── src/main/java/
│   └── com/github/loadup/components/scheduler/custom/
│       ├── binder/
│       │   └── CustomSchedulerBinder.java
│       └── config/
│           └── CustomSchedulerAutoConfiguration.java
└── src/main/resources/
    └── META-INF/spring/
        └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

## 注意事项

1. **SimpleJob** 适合单实例应用，不支持分布式场景
2. **Quartz** 支持集群，但需要数据库支持（使用 JDBC JobStore）
3. **XXL-Job** 和 **PowerJob** 需要部署独立的管理服务器
4. 同一应用只能选择一种调度实现
5. 任务名称（taskName）必须唯一

## 许可证

本项目采用 MIT 许可证。详见 LICENSE 文件。


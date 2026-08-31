# LoadUp Scheduler API

[![Java](https://img.shields.io/badge/java-17%2B-blue)]()
[![Spring Boot](https://img.shields.io/badge/spring--boot-3.x-green)]()
[![License](https://img.shields.io/badge/license-Apache--2.0-blue)]()

## 📋 概述

Scheduler 组件的核心 API 模块，定义了统一的调度任务接口和模型，是整个 Scheduler 组件的基础。

## 🎯 主要功能

- **统一接口定义**: 定义了 `SchedulerBinder` 和 `SchedulerBinding` 接口
- **任务模型**: 提供完整的 `SchedulerTask` 任务描述模型
- **注解支持**: 提供 `@DistributedScheduler` 注解用于声明式任务定义
- **任务注册表**: 内置 `SchedulerTaskRegistry` 管理所有定时任务
- **自动配置**: 基于 Spring Boot 4.x 的自动配置机制

## 🏗️ 核心组件

### 1. SchedulerBinder（调度器绑定器接口）

定义统一的调度操作接口：

```java
public interface SchedulerBinder {
    String getName();
    boolean registerTask(SchedulerTask task);
    boolean unregisterTask(String taskName);
    boolean pauseTask(String taskName);
    boolean resumeTask(String taskName);
    boolean triggerTask(String taskName);
    boolean updateTaskCron(String taskName, String newCron);
    boolean taskExists(String taskName);
}
```

### 2. SchedulerBinding（调度器绑定接口）

业务层统一 API，委托给具体的 Binder 实现。

### 3. SchedulerTask（调度任务模型）

完整的任务描述模型，包含以下属性：

- `taskName` - 任务名称（唯一标识）
- `cron` - Cron 表达式
- `description` - 任务描述
- `taskGroup` - 任务分组
- `method` - 执行方法
- `targetBean` - 目标 Bean
- `enabled` - 是否启用
- `priority` - 优先级
- `timeoutMillis` - 超时时间
- `maxRetries` - 最大重试次数
- `parameters` - 扩展参数

### 4. @DistributedScheduler（分布式调度注解）

用于声明式定义定时任务：

```java
@Component
public class MyTasks {
    @DistributedScheduler(name = "dailyReport", cron = "0 0 9 * * ?")
    public void generateDailyReport() {
        // 任务逻辑
    }
}
```

### 5. SchedulerTaskRegistry（任务注册表）

负责扫描和管理所有定时任务：

- 扫描 `@DistributedScheduler` 注解
- 维护任务注册表（ConcurrentHashMap）
- 在 `ContextRefreshedEvent` 时延迟注册任务
- 使用 beanName 确保任务名唯一性

## 📦 依赖

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-scheduler-api</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 🔧 使用示例

### 定义任务

```java
@Component
public class ScheduledTasks {

    @DistributedScheduler(name = "cleanupTask", cron = "0 0 1 * * ?")
    public void cleanup() {
        // 每天凌晨1点执行
    }

    @DistributedScheduler(
        name = "dataSync",
        cron = "0 */10 * * * ?",
        description = "数据同步任务",
        priority = 5
    )
    public void syncData() {
        // 每10分钟执行
    }
}
```

### 动态管理任务

```java
@Service
public class TaskService {

    @Autowired
    private SchedulerBinding schedulerBinding;

    public void registerNewTask() {
        SchedulerTask task = SchedulerTask.builder()
            .taskName("dynamicTask")
            .cron("0 */5 * * * ?")
            .description("动态创建的任务")
            .enabled(true)
            .build();

        schedulerBinding.registerTask(task);
    }
}
```

## 📚 相关文档

- [主 README](../README.md) - Scheduler 组件完整文档
- [快速开始](../README.md#快速开始)
- [配置说明](../README.md#配置说明)
- [使用示例](../README.md#使用示例)

## 📄 许可证

Apache License 2.0 (Apache-2.0)

详见 [LICENSE](../../../LICENSE) 文件。

---

**最后更新**: 2025-12-30

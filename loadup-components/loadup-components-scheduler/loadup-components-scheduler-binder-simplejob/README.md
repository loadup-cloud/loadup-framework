# LoadUp Scheduler Binder - SimpleJob

[![Java](https://img.shields.io/badge/java-17%2B-blue)]()
[![Spring Boot](https://img.shields.io/badge/spring--boot-3.x-green)]()
[![License](https://img.shields.io/badge/license-GPL--3.0-blue)]()

## 📋 概述

基于 Spring TaskScheduler 的轻量级调度实现，是 Scheduler 组件的默认实现。无需额外依赖，开箱即用。

## ✨ 特性

- ✅ **轻量级**: 无需外部依赖，基于 Spring 内置调度器
- ✅ **动态任务**: 支持动态任务注册和注销
- ✅ **简单易用**: 配置简单，适合快速开发
- ✅ **开箱即用**: 默认实现，无需额外配置
- ❌ **单机限制**: 不支持分布式调度
- ❌ **功能有限**: 不支持任务暂停/恢复

## 🎯 适用场景

- ✅ 单机应用
- ✅ 开发测试环境
- ✅ 简单定时任务
- ✅ 快速原型开发
- ❌ 分布式集群
- ❌ 复杂任务管理

## 📦 依赖

```xml

<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-scheduler-binder-simplejob</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 🔧 配置

### 基础配置

```yaml
loadup:
  scheduler:
    type: simplejob  # 或省略，默认为 simplejob
```

### application.properties 配置

```properties
# 使用 SimpleJob（默认）
loadup.scheduler.type=simplejob
# 日志级别
logging.level.io.github.loadup.components.scheduler=INFO
```

## 💻 使用示例

### 基本使用

```java

@Component
public class MyTasks {

    @DistributedScheduler(name = "simpleTask", cron = "0 */5 * * * ?")
    public void executeTask() {
        System.out.println("Task executed at: " + LocalDateTime.now());
    }
}
```

### 动态任务管理

```java

@Service
public class TaskService {

    @Autowired
    private SchedulerBinding schedulerBinding;

    // 动态注册任务
    public void addTask(String name, String cron) {
        SchedulerTask task = SchedulerTask.builder()
                .taskName(name)
                .cron(cron)
                .enabled(true)
                .build();

        schedulerBinding.registerTask(task);
    }

    // 删除任务
    public void removeTask(String name) {
        schedulerBinding.unregisterTask(name);
    }

    // 检查任务是否存在
    public boolean taskExists(String name) {
        return schedulerBinding.taskExists(name);
    }
}
```

## 🔍 功能对比

| 功能     | SimpleJob | 说明          |
|--------|-----------|-------------|
| 动态注册   | ✅         | 支持动态注册任务    |
| 动态注销   | ✅         | 支持动态注销任务    |
| 暂停/恢复  | ❌         | 不支持（使用注销代替） |
| 手动触发   | ❌         | 不支持         |
| Cron更新 | ❌         | 需要先注销再注册    |
| 分布式    | ❌         | 仅支持单机       |
| 持久化    | ❌         | 任务信息存储在内存中  |
| 外部依赖   | ❌         | 无需外部依赖      |
| 集群支持   | ❌         | 不支持集群部署     |

## ⚠️ 限制说明

1. **单机限制**: 只能在单实例应用中使用
2. **重启丢失**: 应用重启后需要重新注册动态任务
3. **功能有限**: 不支持暂停、恢复、手动触发等高级功能
4. **无持久化**: 任务信息不持久化，存储在内存中

## 🚀 切换到其他实现

当需要更强大的功能时，可以轻松切换到其他实现：

### 切换到 Quartz（推荐生产环境）

```xml
<!-- 移除 simplejob 依赖，添加 quartz 依赖 -->
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-scheduler-binder-quartz</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

```yaml
loadup:
  scheduler:
    type: quartz
```

### 切换到 XXL-Job

```xml

<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-scheduler-binder-xxljob</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

```yaml
loadup:
  scheduler:
    type: xxljob
```

## 📚 相关文档

- [主 README](../README.md) - Scheduler 组件完整文档
- [API 文档](../loadup-components-scheduler-api/README.md)
- [Quartz 实现](../loadup-components-scheduler-binder-quartz/README.md)
- [XXL-Job 实现](../loadup-components-scheduler-binder-xxljob/README.md)

## 📄 许可证

GNU General Public License v3.0 (GPL-3.0)

详见 [LICENSE](../../../LICENSE) 文件。

---

**最后更新**: 2025-12-30

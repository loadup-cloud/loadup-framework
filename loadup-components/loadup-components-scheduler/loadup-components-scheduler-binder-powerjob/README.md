# LoadUp Scheduler Binder - PowerJob

[![Java](https://img.shields.io/badge/java-17%2B-blue)]()
[![Spring Boot](https://img.shields.io/badge/spring--boot-3.x-green)]()
[![PowerJob](https://img.shields.io/badge/powerjob-4.x-orange)]()
[![License](https://img.shields.io/badge/license-Apache--2.0-blue)]()

## 📋 概述

基于 PowerJob 的新一代分布式任务调度实现，支持多种任务类型、工作流编排和强大的可视化管理功能。

## ✨ 特性

- ✅ **新一代架构**: 采用现代化分布式架构设计
- ✅ **多任务类型**: 支持单机、广播、Map/MapReduce 等多种任务类型
- ✅ **工作流编排**: 支持 DAG 工作流
- ✅ **动态调度**: 支持固定频率、固定延迟、Cron 等多种调度策略
- ✅ **在线日志**: 实时查看任务执行日志
- ✅ **强大的可视化**: 功能丰富的 Web 管理界面
- ⚠️ **需要 Server**: 需要部署 PowerJob Server
- ⚠️ **管理方式**: 任务管理需通过控制台完成

## 🎯 适用场景

- ✅ 复杂的分布式调度场景
- ✅ 需要工作流编排
- ✅ 需要多种任务类型
- ✅ 大规模任务调度
- ✅ 需要强大的监控和管理
- ⚠️ 需要额外部署 PowerJob Server

## 🏗️ 架构说明

```
┌─────────────────────────────────────┐
│    PowerJob Server (调度中心)          │
│   - 任务调度                          │
│   - 工作流编排                         │
│   - 执行监控                          │
│   - 日志管理                          │
└─────────────────────────────────────┘
                 ↓ ↓ ↓
    ┌────────────┴─┴────────────┐
    ↓                           ↓
┌─────────┐              ┌─────────┐
│ Worker 1 │              │ Worker 2 │
│ (应用实例)│              │ (应用实例)│
└─────────┘              └─────────┘
```

## 📦 依赖

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-scheduler-binder-powerjob</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 🔧 配置

### 1. 部署 PowerJob Server

#### 使用 Docker

```bash
docker run -d \
  --name powerjob-server \
  -p 7700:7700 -p 10086:10086 \
  -e PARAMS="--spring.profiles.active=product --spring.datasource.url=jdbc:mysql://localhost:3306/powerjob?useUnicode=true&characterEncoding=UTF-8" \
  tjqq/powerjob-server:latest
```

#### 手动部署

```bash
# 下载
wget https://github.com/PowerJob/PowerJob/releases/download/v4.3.0/powerjob-server-4.3.0.jar

# 启动
java -jar powerjob-server-4.3.0.jar
```

访问: http://localhost:7700
默认账号: admin/powerjob123

### 2. 应用配置

```yaml
loadup:
  scheduler:
    type: powerjob

powerjob:
  worker:
    enabled: true
    server-address: 127.0.0.1:7700   # PowerJob Server 地址
    app-name: loadup-app             # 应用名称
    port: 27777                      # Worker 端口
    protocol: http                   # 通信协议: http/akka
    store-strategy: disk             # 存储策略: disk/memory
    max-result-length: 8096          # 最大结果长度
    max-appended-wf-context-length: 8192  # 工作流上下文最大长度
```

### application.properties 配置

```properties
# 调度器类型
loadup.scheduler.type=powerjob

# PowerJob Worker 配置
powerjob.worker.enabled=true
powerjob.worker.server-address=127.0.0.1:7700
powerjob.worker.app-name=loadup-app
powerjob.worker.port=27777
powerjob.worker.protocol=http
powerjob.worker.store-strategy=disk
```

## 💻 使用示例

### 1. 基本任务

```java
@Component
public class PowerJobTasks {

    private static final Logger log = LoggerFactory.getLogger(PowerJobTasks.class);

    @DistributedScheduler(name = "basicTask", cron = "0 */5 * * * ?")
    public void executeBasicTask() {
        log.info("执行基本任务");
        // 业务逻辑
    }
}
```

### 2. Map 任务（分片）

```java
@Component
public class MapTaskHandler {

    @DistributedScheduler(name = "mapTask", cron = "0 0 2 * * ?")
    public ProcessResult executeMapTask(TaskContext context) {
        // 根节点执行，生成子任务
        if (context.getJobParams() == null) {
            List<SubTask> subTasks = generateSubTasks();
            return new ProcessResult(true, "子任务生成完成", subTasks);
        }

        // 子任务执行
        return processSubTask(context.getJobParams());
    }

    private List<SubTask> generateSubTasks() {
        // 生成子任务列表
        List<SubTask> subTasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            subTasks.add(new SubTask("task-" + i, "params-" + i));
        }
        return subTasks;
    }
}
```

### 3. MapReduce 任务

```java
@Component
public class MapReduceTaskHandler {

    @DistributedScheduler(name = "mapReduceTask", cron = "0 0 3 * * ?")
    public ProcessResult executeMapReduce(TaskContext context) {
        if (context.getJobParams() == null) {
            // Map 阶段
            return map();
        } else if (context.isLastTask()) {
            // Reduce 阶段
            return reduce(context);
        } else {
            // 执行子任务
            return process(context.getJobParams());
        }
    }
}
```

### 4. 广播任务

```java
@Component
public class BroadcastTaskHandler {

    @DistributedScheduler(name = "broadcastTask", cron = "0 0 1 * * ?")
    public ProcessResult executeBroadcast(TaskContext context) {
        // 在所有 Worker 上执行
        log.info("Worker {} 执行广播任务", context.getWorkerId());

        // 每个 Worker 的业务逻辑
        clearLocalCache();

        return new ProcessResult(true, "广播任务完成");
    }
}
```

## 🔍 功能对比

| 功能     | PowerJob | 说明                  |
|--------|----------|---------------------|
| 动态注册   | ❌        | 需要在控制台配置            |
| 动态注销   | ❌        | 需要在控制台操作            |
| 暂停/恢复  | ⚠️       | 通过控制台操作             |
| 手动触发   | ⚠️       | 通过控制台触发             |
| Cron更新 | ⚠️       | 通过控制台更新             |
| 分布式    | ✅        | 完全支持                |
| 多任务类型  | ✅        | 单机/广播/Map/MapReduce |
| 工作流    | ✅        | 支持 DAG 工作流          |
| 在线日志   | ✅        | 实时查看执行日志            |
| 可视化管理  | ✅        | 强大的 Web 管理界面        |
| 外部依赖   | ⚠️       | 需要部署 Server         |

## 🎨 控制台功能

### 应用管理

- 应用注册
- Worker 列表
- Worker 状态监控
- 健康检查

### 任务管理

- 任务配置
- 调度策略设置
- 任务类型选择
- 执行器选择
- 参数配置

### 工作流管理

- DAG 可视化编排
- 节点依赖配置
- 流程控制
- 条件判断

### 实例管理

- 任务实例列表
- 执行状态查询
- 在线日志查看
- 停止/重试操作

### 调度策略

- **CRON**: Cron 表达式
- **FIXED_RATE**: 固定频率
- **FIXED_DELAY**: 固定延迟
- **API**: API 触发

### 任务类型

- **STANDALONE**: 单机任务
- **BROADCAST**: 广播任务
- **MAP**: Map 任务
- **MAP_REDUCE**: MapReduce 任务

## 📋 工作流示例

### 1. 在控制台创建工作流

1. 登录 PowerJob 控制台
2. 进入"工作流管理"
3. 创建新工作流
4. 添加节点并配置依赖关系

### 2. 配置工作流节点

```java
@Component
public class WorkflowTasks {

    // 节点1: 数据准备
    @DistributedScheduler(name = "prepareData")
    public ProcessResult prepareData(TaskContext context) {
        log.info("准备数据");
        return new ProcessResult(true, "数据准备完成");
    }

    // 节点2: 数据处理
    @DistributedScheduler(name = "processData")
    public ProcessResult processData(TaskContext context) {
        log.info("处理数据");
        return new ProcessResult(true, "数据处理完成");
    }

    // 节点3: 数据清理
    @DistributedScheduler(name = "cleanupData")
    public ProcessResult cleanupData(TaskContext context) {
        log.info("清理数据");
        return new ProcessResult(true, "数据清理完成");
    }
}
```

## 🚀 集群部署

### 1. 部署 PowerJob Server 集群

```yaml
# Server 实例 1
server:
  port: 7700

# Server 实例 2
server:
  port: 7701
```

### 2. 配置 Worker 集群

所有 Worker 使用相同配置：

```yaml
powerjob:
  worker:
    server-address: server1:7700,server2:7701
    app-name: loadup-app  # 相同的 app-name
    port: 27777
```

### 3. 配置负载均衡

PowerJob 自动实现负载均衡，支持：

- 随机
- 轮询
- 最少使用
- 本地优先

## ⚙️ 高级配置

### 自定义存储策略

```yaml
powerjob:
  worker:
    store-strategy: memory  # 内存模式，适合临时数据
```

### 配置通信协议

```yaml
powerjob:
  worker:
    protocol: akka  # 使用 Akka 协议，性能更好
```

### 日志配置

```yaml
powerjob:
  worker:
    max-result-length: 16384  # 增加日志长度限制
```

## ⚠️ 注意事项

1. **依赖 Server**: 必须先部署 PowerJob Server
2. **网络连接**: 确保 Worker 能访问 Server
3. **端口开放**: Worker 端口需要对 Server 开放
4. **任务管理**: 大部分管理操作需要通过控制台完成
5. **AppName 唯一**: 不同应用使用不同的 app-name
6. **数据库要求**: Server 需要 MySQL 数据库

## 🐛 故障排查

### Worker 未注册

- 检查 Server 地址配置
- 确认网络连接正常
- 查看应用启动日志
- 检查端口是否被占用

### 任务未执行

- 确认任务已在控制台配置
- 检查任务是否已启动
- 查看执行实例日志
- 确认调度策略配置正确

### 工作流失败

- 查看各节点执行状态
- 检查节点依赖关系
- 查看实例日志
- 确认节点任务正常

## 📚 相关文档

- [主 README](../README.md) - Scheduler 组件完整文档
- [API 文档](../loadup-components-scheduler-api/README.md)
- [PowerJob 官方文档](https://www.powerjob.tech/)
- [配置说明](../README.md#配置说明)

## 📄 许可证

Apache License 2.0 (Apache-2.0)

详见 [LICENSE](../../../LICENSE) 文件。

---

**最后更新**: 2025-12-30

# LoadUp Scheduler Binder - XXL-Job

[![Java](https://img.shields.io/badge/java-17%2B-blue)]()
[![Spring Boot](https://img.shields.io/badge/spring--boot-3.x-green)]()
[![XXL-Job](https://img.shields.io/badge/xxl--job-2.x-orange)]()
[![License](https://img.shields.io/badge/license-Apache--2.0-blue)]()

## 📋 概述

基于 XXL-Job 的轻量级分布式任务调度实现，提供可视化管理界面和任务分片功能。

## ✨ 特性

- ✅ **分布式调度**: 轻量级分布式任务调度
- ✅ **可视化管理**: 提供 Web 管理控制台
- ✅ **任务分片**: 支持任务分片执行
- ✅ **动态调度**: 动态修改任务状态、启停、重试
- ✅ **执行器管理**: 自动注册、在线检测
- ✅ **调度日志**: 完整的任务执行日志
- ⚠️ **需要 Admin**: 需要部署 XXL-Job Admin 控制台
- ⚠️ **管理方式**: 任务管理需通过控制台完成

## 🎯 适用场景

- ✅ 需要可视化管理的分布式应用
- ✅ 需要任务分片功能
- ✅ 需要查看任务执行日志
- ✅ 团队协作开发
- ✅ 运维人员管理任务
- ⚠️ 需要额外部署 XXL-Job Admin

## 🏗️ 架构说明

```
┌─────────────────────────────────────┐
│      XXL-Job Admin (调度中心)          │
│   - 任务配置                          │
│   - 调度管理                          │
│   - 执行日志                          │
└─────────────────────────────────────┘
                 ↓ ↓ ↓
    ┌────────────┴─┴────────────┐
    ↓                           ↓
┌─────────┐              ┌─────────┐
│ 执行器 1  │              │ 执行器 2  │
│ (应用实例)│              │ (应用实例)│
└─────────┘              └─────────┘
```

## 📦 依赖

```xml

<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-scheduler-binder-xxljob</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 🔧 配置

### 1. 部署 XXL-Job Admin

下载并启动 XXL-Job Admin：

```bash
# 下载
wget https://github.com/xuxueli/xxl-job/releases/download/2.4.0/xxl-job-admin-2.4.0.jar

# 启动（默认端口 8080）
java -jar xxl-job-admin-2.4.0.jar
```

访问: http://localhost:8080/xxl-job-admin
默认账号: admin/123456

### 2. 应用配置

```yaml
loadup:
  scheduler:
    type: xxljob

xxl:
  job:
    admin:
      addresses: http://localhost:8080/xxl-job-admin
    executor:
      appname: loadup-executor  # 执行器名称
      address:                  # 可选，自动获取
      ip:                       # 可选，自动获取
      port: 9999               # 执行器端口
      logpath: /data/applogs/xxl-job/jobhandler
      logretentiondays: 30     # 日志保留天数
    accessToken:               # 可选，访问令牌
```

### application.properties 配置

```properties
# 调度器类型
loadup.scheduler.type=xxljob
# XXL-Job Admin 地址
xxl.job.admin.addresses=http://localhost:8080/xxl-job-admin
# 执行器配置
xxl.job.executor.appname=loadup-executor
xxl.job.executor.port=9999
xxl.job.executor.logpath=/data/applogs/xxl-job/jobhandler
xxl.job.executor.logretentiondays=30
# 访问令牌（可选）
xxl.job.accessToken=your-access-token
```

## 💻 使用示例

### 1. 定义任务处理器

```java

@Component
public class XxlJobTasks {

    private static final Logger log = LoggerFactory.getLogger(XxlJobTasks.class);

    @DistributedScheduler(name = "dataSync", cron = "0 */10 * * * ?")
    public void syncData() {
        log.info("开始同步数据...");
        // 业务逻辑
    }

    // 分片任务示例
    @DistributedScheduler(name = "shardingTask", cron = "0 0 2 * * ?")
    public void shardingTask() {
        // 获取分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        log.info("分片任务执行: {}/{}", shardIndex, shardTotal);
        // 根据分片参数处理数据
    }
}
```

### 2. 在 Admin 控制台配置任务

1. 登录 XXL-Job Admin 控制台
2. 进入"执行器管理"，确认执行器已注册
3. 进入"任务管理"，配置任务参数
4. 启动任务

### 3. 查看执行日志

在 Admin 控制台可以查看：

- 任务执行状态
- 执行日志
- 执行时间
- 成功/失败次数

## 🔍 功能对比

| 功能     | XXL-Job | 说明             |
|--------|---------|----------------|
| 动态注册   | ❌       | 需要在控制台配置       |
| 动态注销   | ❌       | 需要在控制台操作       |
| 暂停/恢复  | ⚠️      | 通过控制台操作        |
| 手动触发   | ⚠️      | 通过控制台触发        |
| Cron更新 | ⚠️      | 通过控制台更新        |
| 分布式    | ✅       | 完全支持           |
| 任务分片   | ✅       | 支持任务分片         |
| 可视化    | ✅       | 提供 Web 管理界面    |
| 执行日志   | ✅       | 详细的执行日志        |
| 外部依赖   | ⚠️      | 需要部署 Admin 控制台 |

## 🎨 控制台功能

### 执行器管理

- 自动注册执行器
- 在线状态检测
- 执行器地址管理

### 任务管理

- 任务配置
- 执行策略设置
- Cron 表达式配置
- 路由策略选择

### 调度日志

- 任务执行历史
- 执行详细日志
- 失败重试记录
- 性能统计

### 路由策略

- **FIRST**: 第一个
- **LAST**: 最后一个
- **ROUND**: 轮询
- **RANDOM**: 随机
- **CONSISTENT_HASH**: 一致性 HASH
- **LEAST_FREQUENTLY_USED**: 最不经常使用
- **LEAST_RECENTLY_USED**: 最近最久未使用
- **FAILOVER**: 故障转移
- **BUSYOVER**: 忙碌转移
- **SHARDING_BROADCAST**: 分片广播

## 📋 任务分片示例

```java

@Component
public class ShardingTasks {

    @DistributedScheduler(name = "userDataProcess", cron = "0 0 2 * * ?")
    public void processUserData() {
        // 获取分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        // 根据分片处理数据
        List<User> users = userService.getUsersBySharding(shardIndex, shardTotal);

        for (User user : users) {
            processUser(user);
        }

        XxlJobHelper.log("分片 {}/{} 处理完成，处理用户数: {}",
                shardIndex, shardTotal, users.size());
    }
}
```

## 🚀 集群部署

### 1. 部署 XXL-Job Admin 集群

```yaml
# Admin 实例 1
server:
  port: 8080

# Admin 实例 2
server:
  port: 8081
```

### 2. 配置执行器集群

所有执行器实例使用相同配置：

```yaml
xxl:
  job:
    admin:
      addresses: http://admin1:8080/xxl-job-admin,http://admin2:8081/xxl-job-admin
    executor:
      appname: loadup-executor  # 相同的 appname
      port: 9999
```

### 3. 在控制台配置路由策略

选择合适的路由策略，如：

- 轮询（ROUND）
- 分片广播（SHARDING_BROADCAST）
- 故障转移（FAILOVER）

## ⚙️ 高级配置

### 自定义执行器端口

```yaml
xxl:
  job:
    executor:
      port: ${random.int[10000,19999]}  # 随机端口
```

### 配置访问令牌

```yaml
xxl:
  job:
    accessToken: your-secret-token
```

Admin 端也需要配置相同的 token。

### 日志配置

```yaml
xxl:
  job:
    executor:
      logpath: /var/log/xxl-job
      logretentiondays: 7  # 保留 7 天
```

## ⚠️ 注意事项

1. **依赖 Admin**: 必须先部署 XXL-Job Admin 控制台
2. **网络连接**: 确保执行器能访问 Admin
3. **端口开放**: 执行器端口需要对 Admin 开放
4. **任务管理**: 大部分管理操作需要通过控制台完成
5. **时钟同步**: 集群节点需要时钟同步
6. **AppName 唯一**: 不同应用使用不同的 appname

## 🐛 故障排查

### 执行器未注册

- 检查 Admin 地址配置
- 确认网络连接正常
- 查看应用启动日志
- 检查端口是否被占用

### 任务未执行

- 确认任务已在控制台配置
- 检查任务是否已启动
- 查看调度日志
- 确认 Cron 表达式正确

### 分片不工作

- 确认路由策略为"分片广播"
- 检查执行器数量
- 查看执行器注册状态
- 确认所有执行器都在线

## 📚 相关文档

- [主 README](../README.md) - Scheduler 组件完整文档
- [API 文档](../loadup-components-scheduler-api/README.md)
- [XXL-Job 官方文档](https://www.xuxueli.com/xxl-job/)
- [配置说明](../README.md#配置说明)

## 📄 许可证

Apache License 2.0 (Apache-2.0)

详见 [LICENSE](../../../LICENSE) 文件。

---

**最后更新**: 2025-12-30

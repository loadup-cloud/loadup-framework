# Loadup UPMS Test 架构

## 职责与边界

用户与权限管理的测试模块，承载模块行为和集成验证；不作为生产运行时依赖。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `com.zaxxer:HikariCP`
- `com.mysql:mysql-connector-j`

## 实现入口

该模块没有 `src/main/java` 入口；依赖与资源声明以 `pom.xml` 为准。

## 分层与调用路径

Web → App → Domain Gateway → Infrastructure；认证适配连接 UPMS 凭证校验与通用 AuthServer。

```text
测试用例 → 测试配置/容器 → 被测 API 与实现 → 契约断言
```

测试模块处于依赖链末端，用真实装配验证调用路径；不向业务代码暴露新契约。

## 设计取舍

以公开契约验证组件接入；环境相关的启动与数据准备留在测试模块。

集成方式与配置示例见 [README.md](README.md)。

## 启动与数据生命周期

测试启动器只开启 Boot 自动配置，不再手动扫描 `io.github.loadup.components.database`，也不重复声明 UPMS 的 Mapper
扫描。正式自动配置负责装配顺序与条件判断，真实数据库不会替换为 Mock。

本地与 CI 共用 `application.yml` 的 SQL 初始化：先重置 UPMS 测试表及 Sequence 分配元数据，再执行 database 提供的
`V2026101201__sequence_allocation.sql`。测试容器数据可清理；这些初始化脚本不得用于生产数据库。

`mybatis-flex.global-config` 属于 database 的受保护配置，测试 profile 不提供覆盖项。该保护持续生效；不能为了通过测试而关闭保护或替换框架默认
ID 策略。`UserServiceIT` 通过真实自动装配、数据初始化和服务调用构成此次启动失败的回归入口。

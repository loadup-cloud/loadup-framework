# 示例与回归模块架构

## 职责

test 模块位于依赖图最上层，组合 Testify starter、Spring Boot Test、Testcontainers、MySQL driver 和示例服务。它验证组件行为，不是框架对外发布的业务 API。

## 分层

src/main 下的 Application/UserService/OrderService/User/Order 是测试夹具；src/test 下的 TestApplication、JUnit 用例和资源建立验证环境。schema.sql 使用 JDBC 示例字段而非完整 COLA 业务模块模型，不能据此放宽生产表约束。

## 验证边界

ScenarioAssertTest 验证声明式比较；TestifyAutoConfigurationTest 验证条件装配；两个 IT 覆盖 Spring、真实 JDBC、用例输入和数据库副作用。它们不证明所有数据库方言、HTTP 序列化、并行隔离或生产容量正确。

## 数据生命周期

初始化脚本重建示例表，测试清理后准备数据并调用业务。批量案例在测试中逐条解析，JUnit 负责展示 case 名称。共享容器复用与整表清理是不同层面的策略，CI 应串行执行该示例或隔离数据库。

## 维护要求

变化按风险选择单测或 IT；使用 mvnd clean 和明确的 -Dtest 列表，不默认全量 reactor。新增案例应覆盖真实边界而非复制实现。此轮只改文档，运行状态不能从源码存在推断。执行说明见 [README.md](README.md)。

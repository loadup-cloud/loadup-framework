# 技术组件架构

## 定位与依赖方向

技术组件提供可复用的框架能力，位于 commons 与业务 modules 之间。消费工程通过 BOM 按需引入具体组件；`loadup-components` 本身是源码聚合 POM，不是运行时依赖。

```text
commons → components → modules → 消费工程
```

## 功能分类与职责

| 分类 | 定位 | 组件职责 |
|---|---|---|
| `security` | 安全与身份 | authorization：方法授权；authserver：OAuth2 令牌签发；resource-server：JWT 校验；captcha：验证码；kms：OpenBao 密钥管理；signature：数字签名及请求验签 |
| `web` | Web 接入 | webmvc：JSON 报文和错误处理；springdoc：OpenAPI 与 Scalar |
| `data` | 数据与存储 | cache：缓存；database：数据库集成；dfs：文件存储 |
| `integration` | 外部集成 | http：出站 HTTP；gotone：多渠道通知 |
| `reliability` | 可靠性 | async-result：异步业务结果等待与跨实例回传；lock：同步分布式互斥；outbox：可靠事务事件；globalunique：幂等控制；resilience4j：熔断、重试、限流 |
| `execution` | 任务与执行 | Scheduler：可切换本地 / PowerJob 唤醒（PowerJob 能力归属 binder）；DelayedTask（reliability）：持久化业务截止任务；pipeline：流水线编排 |
| `platform` | 平台基础 | configcenter：配置中心；extension：扩展机制；observability：Micrometer 指标与追踪；testcontainers：测试容器 |

## 分类目录与 Maven 边界

分类目录只负责源码导航，不承担自动装配、共享配置、运行接口或依赖约束。分类下不存在新的聚合 Parent，根 components POM 直接通过 `security/loadup-components-kms` 等路径聚合原组件。

- artifactId、版本归属、Java 包、配置前缀与自动配置文件保持原契约。
- 所有组件及其子模块的 Parent 都是根 `loadup-parent`，`relativePath` 显式指向根 POM。
- 分类不对应部署单元，也不意味着同类组件必须一起引入。
- 横向协作仍通过明确的公开 API 或 binder；目录分类不代替真实依赖边界。

## 组件内部结构

一次选择一个后端的组件使用 API + binder；多个渠道共存的 Gotone 使用 API + engine + 可选 store + binders；只有一个实现的组件采用单 jar。迁移分类不改变这些内部结构，也不把公共逻辑提升到分类目录。

## 文档与开发工具

每个 Maven 模块保留 README 和 ARCHITECTURE。组件根 README 提供分类索引；文档站同步器按 Maven 模块名保留原页面 URL，新增目录层级不会隐藏组件主页面，子实现页面继续作为技术细节隐藏在侧栏之外。

源码引用、定向 Maven 路径及根 Parent 的相对路径随目录迁移更新。优先采用 `-pl :artifactId` 的选择方式减少路径耦合。CodeGraph 与 IDE 的本地索引应在迁移后刷新。

## 验证边界

组件质量按运行时 Maven jar 分别计量，行、分支覆盖率各 ≥90%；测试 jar 不作为生产代码分母。根 POM 为每个运行时组件提供显式 `component-coverage-check` execution，组件通过 `loadup.coverage.skip=false` 加入门禁。手写 DTO、配置、异常及适配器全部计入，根 JaCoCo 配置仅排除指定的 APT/MapStruct 输出。

内部 `loadup-coverage` POM 由 `component-coverage` profile 加入 reactor，通过依赖确保在全部组件和兄弟测试模块之后执行。运行时 jar 使用 compile scope（代码与数据均聚合）；测试模块使用 test scope（仅数据）。Enforcer 在 `post-integration-test` 检查预期 exec 非空，JaCoCo 在 `verify` 原生合并并生成聚合 HTML/XML/CSV。每个组件独立校验前另检查其 classes 目录及全局 exec，防止 JaCoCo 因缺少输入而静默跳过。

JaCoCo `check` 面向当前项目的 classes，不直接校验 `report-aggregate` 的 XML。因此完整验收采用两次 Maven 调用：第一步 `clean install` 完成测试及聚合；第二步仅显式执行输入门禁及原生 `check`，共享同一批产物，不再编译或测试。`--fail-at-end` 保留多个 jar 的失败信息；总平均覆盖率不能替代逐 jar 双 90% 门禁。

两步之间不能 clean、编译或修改源码。原生插件不提供原 Python 工具的逐手写源码字节码完整性检查或陈旧数据强制拒绝机制；以第一步完整 clean 构建成功及同批产物作为前提，不能把单独重跑 check 或部分模块结果当作整组验收。测试/组件新增时维护聚合依赖和预期文件清单，不自动发现。覆盖率不能替代断言，应覆盖公开契约、异常传播、事务、资源生命周期及真实后端行为。完整配置与手工验收步骤见 [Coverage](../loadup-coverage/README.md)。

MySQL 测试中，拥有 Flyway 清库或持久作业执行器的组件使用 `reuse=false` 的独立容器，防止清库删掉其他组件的表、后台执行器消费其他测试作业，或迁移历史相互干扰。采用模拟仓储的单元测试只验证编排，不作为数据库 IT。

目录迁移以 Java 文件内容摘要、artifact/Parent 契约、模块聚合路径、文档映射与链接检查作为静态证据。构建和运行验证由消费工程按项目约定执行，不把静态检查当成编译或测试通过。

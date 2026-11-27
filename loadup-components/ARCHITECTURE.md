# 技术组件架构

## 定位与依赖方向

技术组件提供可复用的框架能力，位于 commons 与业务 modules 之间。消费工程通过 BOM 按需引入具体组件；`loadup-components`
本身是源码聚合 POM，不是运行时依赖。

```text
commons → components → modules → 消费工程
```

## 功能分类与职责

| 分类                                      | 组件                                                                |
|-------------------------------------------|---------------------------------------------------------------------|
| [数据与存储](data/ARCHITECTURE.md)        | KV 与缓存、数据库、数据库处理器、文件存储                           |
| [调度与执行](execution/ARCHITECTURE.md)   | 任务调度、执行流水线                                                |
| [应用集成](integration/ARCHITECTURE.md)   | HTTP 客户端、RPC                                                    |
| [平台基础](platform/ARCHITECTURE.md)      | 配置中心、运行时装配、应用观测、业务扩展、测试容器                  |
| [应用可靠性](reliability/ARCHITECTURE.md) | 持久化任务、异步结果、分布式锁、幂等声明、调用容错                  |
| [安全与身份](security/ARCHITECTURE.md)    | OAuth2 与 JWT、方法授权、服务身份、密钥与机密管理、报文签名、验证码 |
| [Web 接入](web/ARCHITECTURE.md)           | WebMVC、接口文档                                                    |

## Maven 与运行边界

父 reactor 通过七个分类 POM 聚合具体组件。组件继承的 Maven Parent 由各自 POM 声明，依赖通过 BOM
对齐。运行期选择以具体组件的依赖、自动配置和宿主设置为准。

## 组件内部结构

一次选择一个后端的组件使用 API + binder。只有一个实现的组件采用单一 JAR。Gotone 由 Modules 提供，
Payment Channel 由 Pay 提供。两者通过各自 BOM 发布。

## 文档与开发工具

每个 Maven 模块维护 README 和 ARCHITECTURE。组件根 README 提供分类索引，文档站按工件名保留组件与实现的独立页面。

源码引用、定向 Maven 路径及根 Parent 的相对路径随目录迁移更新。优先采用 `-pl :artifactId` 的选择方式减少路径耦合。CodeGraph
与 IDE 的本地索引应在迁移后刷新。

## 验证边界

组件质量按运行时 Maven jar 分别计量，行、分支覆盖率各 ≥90%；测试 jar 不作为生产代码分母。根 POM 为每个运行时组件提供显式
`component-coverage-check` execution，组件通过 `loadup.coverage.skip=false` 加入门禁。手写 DTO、配置、异常及适配器全部计入，根
JaCoCo 配置仅排除指定的 APT/MapStruct 输出。

内部 `loadup-coverage` POM 由 `component-coverage` profile 加入 reactor，通过依赖确保在全部组件和兄弟测试模块之后执行。运行时
jar 使用 compile scope（代码与数据均聚合）；测试模块使用 test scope（仅数据）。Enforcer 在 `post-integration-test` 检查预期
exec 非空，JaCoCo 在 `verify` 原生合并并生成聚合 HTML/XML/CSV。每个组件独立校验前另检查其 classes 目录及全局 exec，防止
JaCoCo 因缺少输入而静默跳过。

JaCoCo `check` 面向当前项目的 classes，不直接校验 `report-aggregate` 的 XML。因此完整验收采用两次 Maven 调用：第一步
`clean install` 完成测试及聚合；第二步仅显式执行输入门禁及原生 `check`，共享同一批产物，不再编译或测试。`--fail-at-end` 保留多个
jar 的失败信息；总平均覆盖率不能替代逐 jar 双 90% 门禁。

两步之间不能 clean、编译或修改源码。原生插件不提供原 Python 工具的逐手写源码字节码完整性检查或陈旧数据强制拒绝机制；以第一步完整
clean 构建成功及同批产物作为前提，不能把单独重跑 check
或部分模块结果当作整组验收。测试/组件新增时维护聚合依赖和预期文件清单，不自动发现。覆盖率不能替代断言，应覆盖公开契约、异常传播、事务、资源生命周期及真实后端行为。完整配置与手工验收步骤见 [Coverage](../loadup-coverage/README.md)。

MySQL 测试中，拥有 Flyway 清库或持久作业执行器的组件使用 `reuse=false`
的独立容器，防止清库删掉其他组件的表、后台执行器消费其他测试作业，或迁移历史相互干扰。采用模拟仓储的单元测试只验证编排，不作为数据库
IT。

目录迁移以 Java 文件内容摘要、artifact/Parent 契约、模块聚合路径、文档映射与链接检查作为静态证据。构建和运行验证由消费工程按项目约定执行，不把静态检查当成编译或测试通过。

`platform/loadup-components-runtime` 承接 Clock、日志、JSON 与容器访问工具的 Spring Boot 装配；commons 保留纯基础能力。

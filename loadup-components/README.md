# 技术组件

LoadUp 组件通过功能分类目录组织源码。消费工程引入 `loadup-framework-bom` BOM 后，按需选择组件坐标；多后端组件再选择
binder。分类聚合 POM 组织 Maven 子模块，具体工件决定运行时能力。

## 功能分类

| 分类 | 能力 |
| --- | --- |
| [数据与存储](data/README.md) | KV 与缓存、数据库、数据库处理器、文件存储 |
| [调度与执行](execution/README.md) | 任务调度、执行流水线 |
| [应用集成](integration/README.md) | HTTP 客户端、RPC |
| [平台基础](platform/README.md) | 配置中心、运行时装配、应用观测、业务扩展、测试容器 |
| [应用可靠性](reliability/README.md) | 持久化任务、异步结果、分布式锁、幂等声明、调用容错 |
| [安全与身份](security/README.md) | OAuth2 与 JWT、方法授权、服务身份、密钥与机密管理、报文签名、验证码 |
| [Web 接入](web/README.md) | WebMVC、接口文档 |

分类目录设有 reactor 聚合 POM。消费应用选用具体的组件或 binder 工件，Java 包名和配置前缀由组件维护。以下示例引入 WebMVC。

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-webmvc</artifactId>
</dependency>
```

从仓库根目录做定向开发时，推荐用 Maven artifact 选择器，如 `-pl :loadup-components-webmvc`，避免命令绑定目录深度。需要构建或测试时遵循根
AGENTS 的构建纪律，不默认运行全 reactor。

能力边界与目录决策见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 测试与覆盖率

单元测试使用 `*Test`；集成测试使用 `*IT` / `*IntegrationTest`，数据库及 Redis 等后端使用真实 Testcontainers。完整验收需要
Docker，在仓库根目录依次执行：

```bash
# 一次 clean 构建，完成测试并合并本次执行数据。
mvnd -B -ntp -T 1 -Pcomponent-coverage clean install \
  -DskipTests=false -Dskip.unit.tests=false -Dskip.integration.tests=false \
  -Dspotless.skip=true -Dspotbugs.skip=true -Dtestcontainers.reuse.enable=false
# 直接调用插件，不经过编译/测试生命周期；不要在这两步之间 clean 或修改源码。
mvnd -B -ntp -T 1 -Pcomponent-coverage -pl '!loadup-framework-bom' --fail-at-end \
  enforcer:enforce@component-coverage-inputs \
  jacoco:check@component-coverage-check
```

仅第一步成功后执行第二步。第二步排除独立继承 Spring Boot BOM 的 `loadup-framework-bom`，其余项目按父 POM 的命名 execution
执行；只有运行时组件 jar 开启检查。这里跳过格式及 SpotBugs 用于测试验收，不能替代完整质量检查；CI 另外执行质量插件。

`loadup-coverage` 是 profile 启用的内部 Maven 聚合模块，使用 JaCoCo 原生 `merge` / `report-aggregate` 及逐 jar `check`，无需
JaCoCo CLI 或 Python 覆盖率脚本。先收齐全部组件及兄弟测试模块的 unit / IT 数据，再按每个 jar 的实际 classes 校验行、分支覆盖率均
≥90%；手写 DTO、配置、异常均计入，只排除根 POM 明列的 MyBatis-Flex / MapStruct 生成类型。没有分支的 jar 不产生分支阈值违规。Enforcer
要求当前测试清单中的每份 exec 存在且非空，并在逐 jar 检查前确认 classes 目录和全局数据存在。

聚合报告在 `loadup-coverage/target/site/jacoco-aggregate/index.html`，同目录有 `jacoco.xml` / `jacoco.csv`，每个依赖 jar
有独立页面；全局执行数据为 `loadup-coverage/target/jacoco-components.exec`。现有各模块本地报告保留，但本地 exec
可能不包含兄弟模块测试，不能用来替代完整组件门禁。

新增或移除组件时，同时维护该模块的 `loadup.coverage.skip`、聚合 POM 的依赖清单和预期 unit / IT
文件清单。跳过测试、只运行部分模块或使用历史产物均不属于完整验收；详细限制见 [覆盖率架构](../loadup-coverage/ARCHITECTURE.md)
。原生配置绑定与运行效果需按消费应用验收。

历史基线：2026-10-16 同一次完整本地运行的 **48/48 个运行时 jar 行、分支均 ≥90%**，653 项单测、148 项 IT
通过。该历史结果不能证明当前原生配置或新提交已验收。
新增 AsyncResult 的 3 个运行时 jar 在独立定向运行中均达到行/分支 ≥90%，21 项单测及 4 项 Redis/Kafka/MySQL IT
通过；测试现位于各 binder。该结果不与前述 48 个 jar 合并宣称为同一次全量验收。

`platform/loadup-components-runtime` 承接 Clock、日志、JSON 与容器访问工具的 Spring Boot 装配；commons 保留纯基础能力。

# 技术组件

LoadUp 组件通过功能分类目录组织源码。消费工程引入 `loadup-dependencies` BOM 后，按需选择组件坐标；多后端组件再选择 binder。分类目录不是 Maven 模块，组件坐标、Java 包名、配置前缀和 API 保持不变。

## 功能分类

| 目录 | 职责 | 组件 |
|---|---|---|
| `security/` | 安全与身份 | [authorization](security/loadup-components-authorization/README.md)、[authserver](security/loadup-components-authserver/README.md)、[resource-server](security/loadup-components-resource-server/README.md)、[captcha](security/loadup-components-captcha/README.md)、[kms](security/loadup-components-kms/README.md)、[signature](security/loadup-components-signature/README.md) |
| `web/` | Web 接入 | [webmvc](web/loadup-components-webmvc/README.md)、[springdoc](web/loadup-components-springdoc/README.md) |
| `data/` | 数据与存储 | [cache](data/loadup-components-cache/README.md)、[database](data/loadup-components-database/README.md)、[dfs](data/loadup-components-dfs/README.md) |
| `integration/` | 外部集成 | [http](integration/loadup-components-http/README.md)、[gotone](integration/loadup-components-gotone/README.md) |
| `reliability/` | 可靠性 | [DelayedTask](reliability/loadup-components-delayedtask/README.md)、[async-result](reliability/loadup-components-async-result/README.md)、[lock](reliability/loadup-components-lock/README.md)、[outbox](reliability/loadup-components-outbox/README.md)、[globalunique](reliability/loadup-components-globalunique/README.md)、[resilience4j](reliability/loadup-components-resilience4j/README.md) |
| `execution/` | 任务与执行 | [Scheduler](execution/loadup-components-scheduler/README.md)、[pipeline](execution/loadup-components-pipeline/README.md) |
| `platform/` | 平台基础 | [configcenter](platform/loadup-components-configcenter/README.md)、[extension](platform/loadup-components-extension/README.md)、[observability](platform/loadup-components-observability/README.md)、[testcontainers](platform/loadup-components-testcontainers/README.md) |

## 目录与接入

```text
loadup-components/
├── security/       authorization, authserver, resource-server, captcha, kms, signature
├── web/            webmvc, springdoc
├── data/           cache, database, dfs
├── integration/    http, gotone
├── reliability/    delayedtask, async-result, lock, outbox, globalunique, resilience4j
├── execution/      scheduler, pipeline
└── platform/       configcenter, extension, observability, testcontainers
```

每个组件目录仍使用 `loadup-components-{name}`，内部 API / binder / engine / store / test 结构沿用该组件的设计。所有 Maven 模块直接继承根 `loadup-parent`，分类目录没有额外 POM 或发布坐标。

组件选择与参数以对应 README 为准；分类仅帮助定位，不表示必须整组引入。

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-webmvc</artifactId>
</dependency>
```

从仓库根目录做定向开发时，推荐用 Maven artifact 选择器，如 `-pl :loadup-components-webmvc`，避免命令绑定目录深度。需要构建或测试时遵循根 AGENTS 的构建纪律，不默认运行全 reactor。

能力边界与目录决策见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 测试与覆盖率

单元测试使用 `*Test`；集成测试使用 `*IT` / `*IntegrationTest`，数据库及 Redis 等后端使用真实 Testcontainers。完整验收需要 Docker，在仓库根目录依次执行：

```bash
# 一次 clean 构建，完成测试并合并本次执行数据。
mvnd -B -ntp -T 1 -Pcomponent-coverage clean install \
  -DskipTests=false -Dskip.unit.tests=false -Dskip.integration.tests=false \
  -Dskip.spotless=true -Dskip.spotbugs=true -Dtestcontainers.reuse.enable=false
# 直接调用插件，不经过编译/测试生命周期；不要在这两步之间 clean 或修改源码。
mvnd -B -ntp -T 1 -Pcomponent-coverage -pl '!loadup-dependencies' --fail-at-end \
  enforcer:enforce@component-coverage-inputs \
  jacoco:check@component-coverage-check
```

仅第一步成功后执行第二步。第二步排除独立继承 Spring Boot BOM 的 `loadup-dependencies`，其余项目按父 POM 的命名 execution 执行；只有运行时组件 jar 开启检查。这里跳过格式及 SpotBugs 用于测试验收，不能替代完整质量检查；CI 另外执行质量插件。

`loadup-coverage` 是 profile 启用的内部 Maven 聚合模块，使用 JaCoCo 原生 `merge` / `report-aggregate` 及逐 jar `check`，无需 JaCoCo CLI 或 Python 覆盖率脚本。先收齐全部组件及兄弟测试模块的 unit / IT 数据，再按每个 jar 的实际 classes 校验行、分支覆盖率均 ≥90%；手写 DTO、配置、异常均计入，只排除根 POM 明列的 MyBatis-Flex / MapStruct 生成类型。没有分支的 jar 不产生分支阈值违规。Enforcer 要求当前测试清单中的每份 exec 存在且非空，并在逐 jar 检查前确认 classes 目录和全局数据存在。

聚合报告在 `loadup-coverage/target/site/jacoco-aggregate/index.html`，同目录有 `jacoco.xml` / `jacoco.csv`，每个依赖 jar 有独立页面；全局执行数据为 `loadup-coverage/target/jacoco-components.exec`。现有各模块本地报告保留，但本地 exec 可能不包含兄弟模块测试，不能用来替代完整组件门禁。

新增或移除组件时，同时维护该模块的 `loadup.coverage.skip`、聚合 POM 的依赖清单和预期 unit / IT 文件清单。跳过测试、只运行部分模块或使用历史产物均不属于完整验收；详细限制见 [覆盖率架构](../loadup-coverage/ARCHITECTURE.md)。本次原生配置迁移未运行构建或检查，等待手工验收。

历史基线：2026-10-16 同一次完整本地运行的 **48/48 个运行时 jar 行、分支均 ≥90%**，653 项单测、148 项 IT 通过。该历史结果不能证明当前原生配置或新提交已验收。
新增 AsyncResult 的 3 个运行时 jar 在独立定向运行中均达到行/分支 ≥90%，21 项单测及 4 项 Redis/Kafka/MySQL IT 通过，报告见 [AsyncResult Test](reliability/loadup-components-async-result/loadup-components-async-result-test/README.md)。该结果不与前述 48 个 jar 合并宣称为同一次全量验收。

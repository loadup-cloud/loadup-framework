# 技术组件

LoadUp 组件通过功能分类目录组织源码。消费工程引入 `loadup-dependencies` BOM 后，按需选择组件坐标；多后端组件再选择 binder。分类目录不是 Maven 模块，组件坐标、Java 包名、配置前缀和 API 保持不变。

## 功能分类

| 目录 | 职责 | 组件 |
|---|---|---|
| `security/` | 安全与身份 | [authorization](security/loadup-components-authorization/README.md)、[authserver](security/loadup-components-authserver/README.md)、[resource-server](security/loadup-components-resource-server/README.md)、[captcha](security/loadup-components-captcha/README.md)、[kms](security/loadup-components-kms/README.md)、[signature](security/loadup-components-signature/README.md) |
| `web/` | Web 接入 | [webmvc](web/loadup-components-webmvc/README.md)、[springdoc](web/loadup-components-springdoc/README.md) |
| `data/` | 数据与存储 | [cache](data/loadup-components-cache/README.md)、[database](data/loadup-components-database/README.md)、[dfs](data/loadup-components-dfs/README.md) |
| `integration/` | 外部集成 | [http](integration/loadup-components-http/README.md)、[gotone](integration/loadup-components-gotone/README.md) |
| `reliability/` | 可靠性 | [async-result](reliability/loadup-components-async-result/README.md)、[lock](reliability/loadup-components-lock/README.md)、[outbox](reliability/loadup-components-outbox/README.md)、[globalunique](reliability/loadup-components-globalunique/README.md)、[resilience4j](reliability/loadup-components-resilience4j/README.md) |
| `execution/` | 任务与执行 | [PowerJob](execution/loadup-components-powerjob/README.md)、[pipeline](execution/loadup-components-pipeline/README.md) |
| `platform/` | 平台基础 | [configcenter](platform/loadup-components-configcenter/README.md)、[extension](platform/loadup-components-extension/README.md)、[observability](platform/loadup-components-observability/README.md)、[testcontainers](platform/loadup-components-testcontainers/README.md) |

## 目录与接入

```text
loadup-components/
├── security/       authorization, authserver, resource-server, captcha, kms, signature
├── web/            webmvc, springdoc
├── data/           cache, database, dfs
├── integration/    http, gotone
├── reliability/    async-result, lock, outbox, globalunique, resilience4j
├── execution/      powerjob, pipeline
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

单元测试使用 `*Test`；集成测试使用 `*IT`，数据库及 Redis 等后端使用真实 Testcontainers。运行全部组件测试需要 Docker，执行范围包括组件的独立测试模块及构建依赖：

```bash
mvnd clean verify -pl "$(python3 tools/component-coverage.py --list-modules)" -am --fail-at-end \
  -Dskip.spotless=true -Dskip.spotbugs=true
mvnd -N dependency:get -Dartifact=org.jacoco:org.jacoco.cli:0.8.15:jar:nodeps -Dtransitive=false
python3 tools/component-coverage.py --jacoco-cli /absolute/path/to/org.jacoco.cli-0.8.15-nodeps.jar
```

上述命令从仓库根目录执行。CLI jar 位于本机 Maven 仓库中；使用 Maven 输出的实际路径。只有 `verify` 成功后才执行覆盖率门禁。跳过格式与静态分析用于收集测试结果，不能替代发布检查。

验收要求是每个组件行、分支覆盖率均 ≥90%。工具进一步逐运行时 jar 检查：合并所有组件的单测与 IT 执行数据，解决测试位于兄弟 `*-test` 模块时报告漏计的问题。手写 DTO、配置及异常全部计入，只排除不在 `src/main/java` 的生成代码。无分支的 jar 分支指标视为不适用；缺失编译产物、陈旧执行数据或未达标时返回非零状态。`--report-only` 仅供诊断，不能用作验收门禁。

报告写入 `loadup-components/target/coverage/summary.md` 和 `summary.json`，各 jar 的 `html/index.html` 列出未覆盖源码。2026-10-16 本地完整组件运行及正式覆盖率门禁已通过：**48/48 个运行时 jar 行、分支均 ≥90%**，653 项单测、148 项 IT 全部通过，无失败、错误或跳过。该结果来自同一次完整运行，不使用分批历史报告替代验收。License 与 Spotless 检查通过；SpotBugs 静态验收后续已完成，远端 CI 和真实供应商环境验收仍见根 ROADMAP。

新增 AsyncResult 的 3 个运行时 jar 在独立定向运行中均达到行/分支 ≥90%，21 项单测及 4 项 Redis/Kafka/MySQL IT 通过，报告见 [AsyncResult Test](reliability/loadup-components-async-result/loadup-components-async-result-test/README.md)。该结果不与前述 48 个 jar 合并宣称为同一次全量验收。

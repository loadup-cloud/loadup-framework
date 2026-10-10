# LoadUp — 项目协作指南

本文件从 Git 历史中的原版恢复，并以当前仓库结构和约定更新。具体组件契约以对应模块的 `README.md` 和 `ARCHITECTURE.md` 为准；`docs/` 是面向读者的 Hextra 文档站，不作为代码实现的依据。

<!-- CODEGRAPH_START -->
## CodeGraph

仓库根目录存在 `.codegraph/` 时，定位或理解代码应先使用 CodeGraph，再使用 grep/find 或直接读文件。优先调用 `codegraph_explore`；工具不可用时运行 `codegraph explore "<symbol names or question>"`。若无 `.codegraph/`，跳过此步骤。
<!-- CODEGRAPH_END -->

---

## 项目定位

LoadUp 是一个**被消费的框架/SDK**，通过 `loadup-dependencies` BOM 对外提供能力。
集成方在自有 Spring Boot / Spring Cloud 项目中引入 BOM，按需使用组件和业务模块。

- `loadup-application` 仅为集成测试验证器和本地开发启动器，**不是生产部署单元**
- `loadup-modules/` 下的 UPMS、审计、数据字典、文件资源等是**可复用业务能力**，不是测试代码
- 单体应用通过 Spring MVC Controller 暴露接口；外部 API 调用规划为独立 HTTP 客户端组件

---

## 构建纪律

- 默认不构建、不运行测试；由用户在本地执行。优先通过源码、依赖元数据和现有日志验证。
- 只有用户要求，或变更无法通过其他方式验证时才构建。使用最窄的 `-pl` 模块、目标测试类及适当跳过参数（如 `-DskipTests`、`-Dskip.spotless=true`、`-Dskip.spotbugs=true`）。
- 必须带 `clean`，避免 MyBatis-Flex、MapStruct 等注解处理器在增量编译时抛出 `FilerException`。
- 未经用户要求，不启动全 reactor 构建或测试。若确有必要，说明原因并让用户决定。

---

## 项目结构

```
loadup-cloud/
├── loadup-dependencies/        # BOM，统一依赖版本
├── loadup-commons/             # 最底层通用基础
│   ├── loadup-commons-context/ # JDK 25 ScopedValue 只读执行链上下文
│   ├── loadup-commons-dto/     # 通用响应、DTO 与 BaseDO
│   ├── loadup-commons-util/    # 工具类：JsonUtil、StringUtils、DateUtils
│   ├── loadup-commons-log/     # 统一日志格式与 trace MDC 约定
├── loadup-components/          # 技术组件，分类目录不增加 Maven 层级
│   ├── security/       # authorization, authserver, resource-server, captcha, kms, signature
│   ├── web/            # webmvc, springdoc
│   ├── data/           # cache, database, dfs
│   ├── integration/    # http, gotone
│   ├── reliability/    # lock, outbox, globalunique, resilience4j
│   ├── execution/      # scheduler, retrytask, pipeline
│   └── platform/       # configcenter, extension, observability, testcontainers
├── loadup-modules/             # 通用业务聚合，各自包含 COLA 分层与可选 web 子模块
│   ├── loadup-modules-upms/    # 用户与权限管理、账号安全
│   ├── loadup-modules-audit/   # 审计中心
│   ├── loadup-modules-dictionary/ # 数据字典
│   ├── loadup-modules-file/    # 文件资源
│   ├── loadup-modules-notification/ # 站内通知
│   └── loadup-modules-transfer/ # 导入导出任务
├── loadup-testify/             # 集成测试框架
├── loadup-application/         # 集成测试启动器（非生产部署单元）
├── frontend/                   # Vue 3 管理端
└── docs/                       # 独立的 Hugo + Hextra 文档站
```

---

## 模块依赖方向（严格单向）

```
loadup-dependencies (BOM)
        ↑
   loadup-commons/*
        ↑
  loadup-components/{category}/*   ← 通过 API/binder 模式解耦横向依赖
        ↑
   loadup-modules/*     ← 模块间依赖应明确且尽量减少
        ↑
loadup-application

loadup-testify  → 仅 test scope，深度依赖框架内部类型
```

业务模块通过可选 `*-web` 适配模块提供 Spring MVC Controller；业务服务不依赖 HTTP 客户端实现。

---

## 组件设计规范

> 项目概况见 [README.md](README.md)，待办事项见 [ROADMAP.md](ROADMAP.md)，组件设计以对应 `ARCHITECTURE.md` 为准。
> 核心原则：底层 OSS + 薄集成；业务侧 API 尽量采用业界标准接口（Spring Cache、S3、OpenTelemetry、Spring MVC 等）；自创接口仅限标准表达不了的语义；每个组件 README 必须维护能力矩阵契约表。

### 单后端选择模式（Mode A）

适用于一次选一种后端，例如 Cache / Captcha / ConfigCenter / DFS / Scheduler。

```
loadup-components-{domain}/
├── pom.xml                    # 聚合 POM（packaging: pom）
├── {domain}-api/              # {Domain}Provider SPI + {Domain}Template 业务 API
├── {domain}-binder-{impl}/    # 每个后端一个 binder 模块
└── {domain}-test/             # 集成测试
```

- 业务代码只引入 `-api`，注入 `{Domain}Template`
- 集成方通过加 `-binder-{impl}` 的 pom 依赖 + yml 配置 `binder-type` 切换后端
- 装配机制：`@ConditionalOnSingleCandidate(Provider.class)` 自动创建 Template
- 示例：cache、captcha、configcenter、dfs、scheduler

### 多后端共存模式（Mode B）

适用于多个 Provider 同时活跃、运行时数据驱动路由。仅 Gotone。

```
loadup-components-{domain}/
├── pom.xml                    # 聚合 POM
├── {domain}-api/              # SPI + Template + 存储 SPI（Optional）
├── {domain}-engine/           # 纯发送引擎（零存储，零 DB 依赖）
├── {domain}-store-jdbc/       # 默认存储实现（MyBatis-Flex，可选）
├── {domain}-binder-{impl}/    # 每个渠道一个 binder 模块
└── {domain}-test/
```

- Provider 通过 `List<{Domain}Provider>` 注入到 Engine 中收集
- 存储 SPIs（`ServiceConfigProvider` / `ChannelConfigProvider` / `RecordHandler`）全部 `Optional`
- 引擎不依赖任何存储与 DB

### 单一 jar 模式（只有一个实现）

```
loadup-components-{name}/
├── pom.xml                    # 独立 jar
└── src/main/java/...
```

- 示例：signature、database、authorization、pipeline

**判断标准**：如果一个组件有多于一种后端实现 → Mode A（单后端）或 Mode B（多后端共存）。反之 → 单一 jar。

---

## 业务模块内部分层（COLA 4.0）

```
loadup-modules-{mod}/
├── {mod}-client/          # Facade、DTO、Command、Query、消费方 SPI
├── {mod}-domain/          # 纯 POJO + Gateway 接口 + 枚举（零 Spring 注解）
├── {mod}-infrastructure/  # DO extends BaseDO、Mapper、GatewayImpl、Converter
├── {mod}-app/             # @Service 业务编排、AutoConfiguration
├── {mod}-web/             # 可选 Controller 适配；按 Maven 依赖装配
└── {mod}-test/            # 集成测试 + 单元测试（parent = 根 loadup-parent）
```

**domain 层铁律**：无 `@Table`、无 `@Service`、无任何 Spring / ORM 注解。

---

## 硬性禁止项

| #  | 禁止行为                                     | 正确做法                                                                |
|----|------------------------------------------|---------------------------------------------------------------------|
| 1  | Java 文件头写 `/*- #%L ... #L% */` License 块 | 标准 Apache-2.0 模板：`mvn license:update-file-header` 后执行 `mvn spotless:apply`（模板空行含尾随空格需对齐），verify 阶段 `check-file-header` + `spotless:check` 双校验 |
| 2  | 将 HTTP 映射直接写在业务 Service 中 | 在可选 Web 适配模块中使用 Spring MVC Controller 调用 Service |
| 3  | 集成测试中用 `@MockBean` 替代 DB                 | `@EnableTestContainers(ContainerType.MYSQL)` 启动真实容器                      |
| 4  | `@Autowired` 字段注入                        | 构造器注入：显式 `public XxxService(XxxGateway gw) { this.gw = gw; }`                               |
| 5  | 字符串拼接 SQL                                | MyBatis-Flex `QueryWrapper`                                              |
| 6  | `@Table` 放在 domain 层                     | DO（`XxxDO extends BaseDO`）只放在 `infrastructure.dataobject`                   |
| 7  | 子模块 `<parent>` 指向模块自身 pom                | 所有子模块 `<parent>` 统一指向根 `loadup-parent`                                    |
| 8  | 子模块内写 `<version>` 引用同项目模块                | 版本由 `loadup-dependencies` BOM 统一管理                                       |
| 9  | 表主键 `BIGINT AUTO_INCREMENT`              | 主键 `VARCHAR(64)`，业务层用 `UUID.randomUUID()` 赋值                               |
| 10 | 无必要的业务模块横向依赖                         | 保持清晰依赖方向，跨模块能力优先通过公开接口集成                                       |
| 11 | Java 文件中写中文注释或 Javadoc                   | 注释/Javadoc 统一使用英文；中文只允许出现在 `*.md` 文档文件中                                     |
| 12 | `BOOLEAN`/`BOOL` 类型                       | 统一使用 `TINYINT`（0/1）                                                    |
| 13 | DO 中重复定义 id/createdAt/updatedAt/tenantId/deleted | 这些字段在 `BaseDO` 中已定义                                              |
| 14 | Mapper 中写额外 SQL 方法                       | 用 `QueryWrapper` 在 GatewayImpl 中操作                                       |
| 15 | 新增三方依赖不在 BOM 中声明                         | 版本管理集中在 `loadup-dependencies/pom.xml`                                     |
| 16 | 使用 Lombok（`@Data`/`@Getter`/`@Slf4j`/`@Builder` 等） | 写显式 Java 代码；DTO/Command/Query 优先使用 Java `record`；日志打印使用 `LogUtil.info/warn/error(Source.class, ...)`；仅第三方日志适配需要 Logger 时使用 `LogUtil.getLogger` |

---

## 数据库表规范

创建与更新时间统一命名：Java 属性和 JSON 字段使用 `createdAt`、`updatedAt`，数据库列使用 `created_at`、`updated_at`。领域对象、DTO 和查询排序字段遵循同一命名，不使用 `createdTime`、`updatedTime`。

每张表必须包含 5 个标准字段：

```sql
id         VARCHAR(64)  NOT NULL PRIMARY KEY,
tenant_id  VARCHAR(64),
created_at DATETIME     NOT NULL,
updated_at DATETIME     NOT NULL,
deleted    TINYINT      NOT NULL DEFAULT 0
```

- Flyway 迁移脚本命名：`V{n}__{description}.sql`，放 `src/main/resources/db/migration/`

---

## 命名约定

| 类型            | 规则                                 | 示例                              |
|----------------|--------------------------------------|---------------------------------|
| 数据库映射对象     | `XxxDO extends BaseDO`              | `ConfigItemDO`                  |
| 对外 DTO        | `XxxDTO`                            | `ConfigItemDTO`                 |
| 写操作入参        | `XxxCreateCommand` / `XxxUpdateCommand` | `ConfigItemCreateCommand`  |
| 查询入参         | `XxxQuery`                          | `ConfigItemQuery`               |
| 业务 API        | `XxxTemplate`                       | `CacheTemplate`                 |
| SPI 接口        | `XxxProvider`                       | `CacheProvider`                 |
| Provider 实现    | `{Impl}XxxProvider`                 | `CaffeineCacheProvider`         |
| Provider 配置    | `{Impl}XxxConfig`                   | `CaffeineCacheConfig`           |
| 顶层配置          | `XxxProperties`                     | `CacheProperties`               |
| Template 实现    | `DefaultXxxTemplate`                | `DefaultCacheTemplate`          |
| AutoConfig      | `XxxAutoConfiguration`              | `CacheAutoConfiguration`        |
| Binder AutoConfig | `{Impl}XxxAutoConfiguration`      | `CaffeineCacheAutoConfiguration`|
| Gateway 接口    | `XxxGateway`                        | `ConfigItemGateway`             |
| Gateway 实现    | `XxxGatewayImpl`                    | `ConfigItemGatewayImpl`         |
| Service         | `XxxService`（直接 `@Service`，无 impl）| `ConfigItemService`          |

---

## 包命名（根包：`io.github.loadup.modules.{mod}`）

| 层                 | 包路径                          |
|-------------------|------------------------------|
| client DTO        | `.client.dto`                |
| client Command    | `.client.command`            |
| client Facade     | `.client.facade`             |
| client Query      | `.client.query`              |
| client SPI        | `.client.spi`                |
| domain model      | `.domain.model`              |
| domain gateway    | `.domain.gateway`            |
| infra DO          | `.infrastructure.dataobject` |
| infra Mapper      | `.infrastructure.mapper`     |
| infra GatewayImpl | `.infrastructure.repository` |
| infra Converter   | `.infrastructure.converter`  |
| app Service       | `.app.service`               |
| app AutoConfig    | `.app.autoconfigure`         |

---

## MapStruct 规范

所有对象映射统一使用 MapStruct，且只允许 Spring 组件模式，不允许 `default`/instance 模式
（`Mappers.getMapper(...)`、手动 `new XxxConverterImpl()`）。

- 共享配置统一使用 `loadup-commons-dto` 的 `LoadUpMapStructConfig`
  （`io.github.loadup.commons.mapping`），固定配置：
  `componentModel = "spring"`、`unmappedTargetPolicy = ERROR`、`unmappedSourcePolicy = WARN`
- converter 声明一律为 `@Mapper(config = LoadUpMapStructConfig.class)`，
  不在 `@Mapper` 上重复写 componentModel/policy
- 需要模块内静态映射方法时用 `uses = XxxSupport.class`（如 `AuditMappingSupport`）
- converter 通过构造器注入使用方，禁止字段注入

## API 暴露方式

本地业务接口使用 Spring MVC Controller。JSON Controller 调用统一使用 POST + JSON 请求体；上传可以使用 multipart，下载可以返回流式二进制。`loadup-components-webmvc` 对 API 响应统一封装 `result`、`data` 并处理错误及 Jackson 规则；`loadup-commons-dto` 定义报文类型。资源端令牌校验由 `loadup-components-resource-server` 装配，方法授权由 `loadup-components-authorization` 提供；UPMS 凭证校验和令牌签发由 `loadup-modules-upms-authserver` 对接。

## Vue 3 前端

- 新增或修改 Vue 代码使用 Composition API，优先单文件组件 `<script setup lang="ts">`、`ref`/`reactive`、`computed`、composable，以及适用场景下的类型化 props/emits。
- 遵循项目采用的 Vue 3 和 Element Plus API。不引入 Vue 2 语法或弃用 API，包括 `new Vue`、新页面使用 Options API、filters、`.sync`、`v-on.native`、`this.$set`/`this.$delete`。

---

## 测试规范

- 集成测试类名 `*IT.java`，单元测试类名 `*Test.java`
- 集成测试用 Testify + Testcontainers（`@EnableTestContainers(ContainerType.MYSQL)`）
- 测试模块 `*-test/` 的 parent 指向根 `loadup-parent`，不是模块聚合 pom
- 测试模块必须有 3 个 yml 文件：`application.yml`（激活 test profile）、`application-test.yml`（本地）、`application-ci.yml`（CI）

---

## 技术栈速查

- Java **25** | Spring Boot **4.1.1** | MyBatis-Flex **1.11.8**（版本以 BOM 为准）
- MySQL 8.0+ | Caffeine（本地）| Redis/Redisson（分布式）
- Resilience4j **2.3.0**（容错；版本由 loadup-dependencies BOM 统一，跟随 Spring Cloud 2025.1.x）
- JUnit 5 + `loadup-testify-spring-boot-starter` + Testcontainers
- SpringDoc + Scalar | Spotless (Palantir Java Format) | Maven
- License: **Apache-2.0**（Java 文件头由 license-maven-plugin 自动维护）

---

## 关键参考文件

| 文件                                                  | 用途                    |
|-----------------------------------------------------|-----------------------|
| `loadup-components/data/loadup-components-database/ARCHITECTURE.md` | MyBatis-Flex 集成与审计设计 |
| `loadup-modules/loadup-modules-upms/ARCHITECTURE.md` | 业务模块 COLA 分层参考 |

---

## 文档策略

### 三层文档体系

| 位置 | 受众 | 内容 | 维护者 |
|------|------|------|--------|
| `AGENTS.md`（根目录） | AI 编码助手 | 项目定位、禁止项、构建命令、技术栈 | 开发者 + AI |
| `**/README.md`（模块级） | 集成方开发者 | 模块用途、Maven 坐标、配置方式、示例 | 模块开发者 |
| `**/ARCHITECTURE.md`（模块级） | 深入理解者 | 设计决策、内部结构、扩展点 | 模块开发者 |
| `docs/` | 外部文档站读者 | Hugo + Hextra 文档站，面向非本项目开发者 | 文档维护者 |

### 规则

1. **docs/ 不作为编码依据。** docs/ 是 Hugo + Hextra 文档站源码。AI 写代码时以 `AGENTS.md`、模块 `README.md`、`ARCHITECTURE.md` 和实际代码为准。

2. **docs/ 不参与 Java/Maven 构建。** docs/ 目录是独立的 Hugo 项目，有自己的构建流程（`hugo` CLI）。CI 中不需要为它配置 Maven 插件。

3. **模块 README.md 是集成方入口。** 说明用途、Maven 坐标、配置及接入示例。

4. **ARCHITECTURE.md 记录设计。** 说明职责边界、内部结构、关键决策和扩展点；遵循项目每个模块维护 README 与 ARCHITECTURE 的文档结构。

5. **文档与代码同步。** 修改代码导致接口、配置或行为变化时，同步更新对应的 README.md 或 ARCHITECTURE.md；使用根目录的 `doc-sync-agent.py` 同步到文档站。首页由文档站单独维护，不由同步脚本生成。

6. **根 README.md 是项目门面。** 面向首次接触者，含项目简介、快速开始、BOM 引入方式、模块目录概览，不深入单个模块细节。

## 日志、观测与任务收尾

- 框架日志打印统一使用 `loadup-commons-log` 的 `LogUtil`，生产代码显式传入来源类；第三方要求 SLF4J Logger 时通过 `LogUtil.getLogger` 获取。保留参数化占位符，异常作为最后参数，敏感字段显式脱敏。
- 指标和追踪使用 Spring Boot 管理的 `MeterRegistry`、`ObservationRegistry` 和 Micrometer Tracing；组件注入共享实例，基础 HTTP/JVM 观测由 Boot 提供。自定义指标以 `loadup.<domain>.*` 命名，仅使用低基数标签，配置使用标准 `management.*`。
- 每完成一项任务立即更新根 `ROADMAP.md`：移除已完成的实现项，把未执行的测试或部署验收单独保留为待办；源码或测试已编写不等于运行验证通过。

## 执行链共享数据

- 业务元数据使用 `commons-context` 的不可变 ExecutionContext 与 ScopedValue ContextHolder；入口用 runWith/callWith，下游只读，临时覆盖派生新对象进入嵌套回调。ServiceTemplate 的 init/clean 按入口及资源需求使用，普通 Service 可直接调用。
- 跨线程业务上下文用 Observability 的 LoadUpContextTaskDecorator 或纯 Java wrap；Boot 标准 Micrometer 装饰器负责 Trace，任意 Micrometer snapshot 不会自动捕获 ScopedValue。只绑定轻量不可变值；用户身份由 SecurityContextHolder 管理，traceId/spanId 由 Micrometer/MDC 管理。

## Business Facades and Persistence

- Public business interfaces reside in `client.facade` and use `XxxFacade`. Application services implement the interfaces directly; Controllers and cross-module consumers depend on Facades. Consumer-supplied extensions remain `client.spi.XxxProvider` or `XxxHandler`.
- Facades return client DTOs or JDK values. HTTP envelopes belong to Controllers. Keep domain Gateway ports separate from public Facades.
- Business infrastructure depends on `loadup-components-database`. GatewayImpl uses MyBatis-Flex BaseMapper and QueryWrapper with generated Tables/TableDef constants. Business repositories contain no JdbcTemplate, NamedParameterJdbcTemplate, handwritten SQL, string columns or SQL sort fragments.
- DOs extend BaseDO. Mappers are explicit empty `BaseMapper<XxxDO>` interfaces; APT generates table definitions, not Mappers. Generated sources stay in target and are not committed.
- database owns fixed framework defaults for audit fields, UUID keys and logical deletion. Business modules and consumers must not replace FlexGlobalConfig, framework listeners, or MyBatisFlexCustomizer. Deployment DataSource settings and trusted tenant resolution remain integration responsibilities. Validate conflicting persistence conventions at startup.
- Root `mybatis-flex.config` owns common APT options. Do not override these options in child modules. Each infrastructure generates its own Tables in its DO package's `.table` subpackage.

## Mapping, HTTP Contracts and Utilities

- Every object mapping uses a Spring-managed MapStruct Converter with `@Mapper(config = LoadUpMapStructConfig.class)` and constructor injection. DTO/domain mapping belongs to app.converter, DO/domain mapping to infrastructure.converter, HTTP projections to web converters. Do not use Mappers.getMapper, static conversion factories or manually instantiate generated converters.
- Domain methods implement business validation and transitions, not representation conversion or input-field copying. Express update merge rules in Converter mappings; document null-as-unchanged versus empty-as-clear. Authorize private-field modifications before mapping.
- Include OpenAPI Tag/Operation on public Controllers and Schema descriptions/examples/units/formats on public DTO, Command and Query fields. Validation annotations define actual constraints; documentation must agree. Java descriptions are English. Credential request fields are WRITE_ONLY with synthetic examples. Document HTTP 200 business failures and actual response envelopes. Domain has no OpenAPI annotations.
- Business JSON Controllers explicitly return SuccessResponse or IResponse; use SuccessResponse.ofPage for pagination and SuccessResponse.success for no data. Global exception handling returns FailureResponse. Downloads, SSE and standard protocol endpoints preserve their protocol-specific types. Keep application services free of HTTP envelopes.
- Project DTOs, commands, queries, domain data, DOs and envelopes implement JSON toString through ToStringUtils.reflectionToString. Credentials and keys must be redacted. Output handles records, inheritance, cycles and bounded collections without triggering resource access. Preserve JDK/third-party types and value objects with intentional textual semantics. Log JSON and HTTP JSON have separate disclosure policies.
- Prefer JDK APIs, then Guava for missing general utilities and Vavr for useful Either/Validation/composition. Public contracts use JDK types. Avoid pass-through utility wrappers; retain project-specific money, signature, context and masking semantics. Propagate Vavr Try failures across transactional entry points. Manage versions through BOM and declare dependencies where used.
- Business configuration uses loadup.modules.<module>.*. Optional web exposure is selected by the web dependency; do not add general web.enabled properties. Keep meaningful capture/path configuration.

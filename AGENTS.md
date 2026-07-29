# LoadUp — Codex 项目上下文

> 本文件是 AI 编码助手的唯一真相来源（Single Source of Truth）。
> 项目其他 AI 指令文件均为本文件的引用或衍生。
>
> **docs/ 目录是 Hugo + Lotusdocs 文档站，不参与 Java/Maven 构建。**
> AI 写代码时以本文件和各模块的 `README.md` / `ARCHITECTURE.md` 为准，不应引用 docs/ 中的内容指导编码。
> 使用 `/docs-sync` skill 将模块 README.md 变更同步到文档站。

---

## 项目定位

LoadUp 是一个**被消费的框架/SDK**，通过 `loadup-dependencies` BOM 对外提供能力。
集成方在自有 Spring Boot / Spring Cloud 项目中引入 BOM，按需使用组件和业务模块。

- `loadup-application` 仅为集成测试验证器和本地开发启动器，**不是生产部署单元**
- `modules/` 下的 UPMS/Config/Log 是**框架自带的可复用通用业务能力**，不是测试代码
- Gateway 是**嵌入式组件**（类比 Spring Cloud Gateway），不是独立网关服务

---

## 构建与质量命令

```bash
# 全量构建 + 测试
mvn clean verify

# 跳过测试（仅编译打包）
mvn clean install -DskipTests

# 格式化代码（本地修复）
mvn spotless:apply

# 检查格式化（不修改文件，CI 使用此命令）
mvn spotless:check

# 单模块构建（含依赖）
mvn clean verify -pl modules/loadup-modules-config/loadup-modules-config-app -am

# 单模块测试
mvn test -pl modules/loadup-modules-config/loadup-modules-config-test
```

---

## 项目结构

```
loadup-parent/
├── loadup-dependencies/        # BOM，统一依赖版本
├── commons/                    # 最底层通用基础
│   ├── loadup-commons-api/     # 通用接口、SPI 抽象基类
│   ├── loadup-commons-dto/     # 通用 DTO：Result<T>、PageDTO、BaseDO
│   └── loadup-commons-util/    # 工具类：JsonUtil、StringUtils、DateUtils
├── components/                 # 可复用技术组件（框架级中间件）
│   ├── loadup-components-authorization/   # 方法级授权 @RequirePermission
│   ├── loadup-components-cache/           # 缓存（API + binder-caffeine + binder-redis）
│   ├── loadup-components-captcha/         # 验证码生成
│   ├── loadup-components-configcenter/    # 配置中心（API + binder-local/nacos/apollo）
│   ├── loadup-components-database/        # MyBatis-Flex 配置、多租户、审计、Flyway 迁移
│   ├── loadup-components-dfs/             # 分布式文件存储（API + binder-local/s3/database）
│   ├── loadup-components-extension/       # AspectJ 扩展机制
│   ├── loadup-components-globalunique/    # 全局幂等性控制
│   ├── loadup-components-gotone/          # 统一消息通知（API + email/sms/push/webhook）
│   ├── loadup-components-pipeline/        # 流水线编排引擎
│   ├── loadup-components-retrytask/       # 分布式重试任务框架
│   ├── loadup-components-scheduler/       # 任务调度（API + simplejob/quartz/xxljob/powerjob）
│   ├── loadup-components-signature/       # 数字签名
│   ├── loadup-components-springdoc/       # knife4j / OpenAPI 文档自动配置
│   ├── loadup-components-testcontainers/  # 测试容器封装
│   └── loadup-components-tracer/          # OpenTelemetry 链路追踪
├── middleware/
│   ├── loadup-gateway/         # 嵌入式 API 网关（facade + core + starter + plugins）
│   └── loadup-testify/         # 集成测试框架
├── modules/                    # 通用业务能力（可复用业务模块）
│   ├── loadup-modules-upms/    # 用户权限管理 RBAC3 + OAuth2 三方登录
│   ├── loadup-modules-config/  # 系统参数 + 数据字典 + 热刷新
│   └── loadup-modules-log/     # 操作日志 + 审计日志 + 错误日志
└── loadup-application/         # 集成测试启动器（非生产部署单元）
```

---

## 模块依赖方向（严格单向）

```
loadup-dependencies (BOM)
        ↑
   commons/*
        ↑
  components/*   ← 通过 API/binder 模式解耦横向依赖
        ↑
   modules/*     ← 模块间禁止横向依赖
        ↑
loadup-application

middleware/loadup-gateway  → 可依赖 commons、components
middleware/loadup-testify  → 仅 test scope，深度依赖框架内部类型
```

---

## 组件设计规范

### 单后端选择模式（Mode A）

适用于一次选一种后端。Cache / ConfigCenter / DFS / Scheduler。

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
- 示例：cache、configcenter、dfs、scheduler

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

- 示例：captcha、signature、database、authorization、pipeline

**判断标准**：如果一个组件有多于一种后端实现 → Mode A（单后端）或 Mode B（多后端共存）。反之 → 单一 jar。

---

## 业务模块内部分层（COLA 4.0）

```
loadup-modules-{mod}/
├── {mod}-client/          # DTO、Command、Query（可被其他模块依赖）
├── {mod}-domain/          # 纯 POJO + Gateway 接口 + 枚举（零 Spring 注解）
├── {mod}-infrastructure/  # DO extends BaseDO、Mapper、GatewayImpl、Converter
├── {mod}-app/             # @Service 业务编排、AutoConfiguration
└── {mod}-test/            # 集成测试 + 单元测试（parent = 根 loadup-parent）
```

**domain 层铁律**：无 `@Table`、无 `@Service`、无任何 Spring / ORM 注解。

---

## 硬性禁止项

| #  | 禁止行为                                     | 正确做法                                                                |
|----|------------------------------------------|---------------------------------------------------------------------|
| 1  | Java 文件头写 `/*- #%L ... #L% */` License 块 | CI 的 `license-maven-plugin` 自动插入                                     |
| 2  | 创建 `@RestController` / `@Controller`     | Gateway `bean://serviceName:method` 路由                                   |
| 3  | 集成测试中用 `@MockBean` 替代 DB                 | `@EnableTestContainers(ContainerType.MYSQL)` 启动真实容器                      |
| 4  | `@Autowired` 字段注入                        | 构造器注入：显式 `public XxxService(XxxGateway gw) { this.gw = gw; }`                               |
| 5  | 字符串拼接 SQL                                | MyBatis-Flex `QueryWrapper`                                              |
| 6  | `@Table` 放在 domain 层                     | DO（`XxxDO extends BaseDO`）只放在 `infrastructure.dataobject`                   |
| 7  | 子模块 `<parent>` 指向模块自身 pom                | 所有子模块 `<parent>` 统一指向根 `loadup-parent`                                    |
| 8  | 子模块内写 `<version>` 引用同项目模块                | 版本由 `loadup-dependencies` BOM 统一管理                                       |
| 9  | 表主键 `BIGINT AUTO_INCREMENT`              | 主键 `VARCHAR(64)`，业务层用 `UUID.randomUUID()` 赋值                               |
| 10 | modules 之间横向互相依赖                         | 严格单向：commons → components → modules                                       |
| 11 | Java 文件中写中文注释或 Javadoc                   | 注释/Javadoc 统一使用英文；中文只允许出现在 `*.md` 文档文件中                                     |
| 12 | `BOOLEAN`/`BOOL` 类型                       | 统一使用 `TINYINT`（0/1）                                                    |
| 13 | DO 中重复定义 id/createdAt/updatedAt         | 这些字段在 `BaseDO` 中已定义                                                      |
| 14 | Mapper 中写额外 SQL 方法                       | 用 `QueryWrapper` 在 GatewayImpl 中操作                                       |
| 15 | 新增三方依赖不在 BOM 中声明                         | 版本管理集中在 `loadup-dependencies/pom.xml`                                     |
| 16 | 使用 Lombok（`@Data`/`@Getter`/`@Slf4j`/`@Builder` 等） | 写显式 Java 代码；DTO/Command/Query 优先使用 Java `record`；Logger 用 `LoggerFactory.getLogger` |

---

## 数据库表规范

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
| domain model      | `.domain.model`              |
| domain gateway    | `.domain.gateway`            |
| infra DO          | `.infrastructure.dataobject` |
| infra Mapper      | `.infrastructure.mapper`     |
| infra GatewayImpl | `.infrastructure.repository` |
| infra Converter   | `.infrastructure.converter`  |
| app Service       | `.app.service`               |
| app AutoConfig    | `.app.autoconfigure`         |

---

## API 暴露方式

路由通过 **CSV 文件** 或 **数据库** 管理，无 Controller 层。

```csv
# resources/gateway-config/routes.csv
path,method,target,securityCode,requestTemplate,responseTemplate,enabled,properties
/api/v1/config/list,POST,bean://configItemService:listAll,default,,,true,
/api/v1/config/value,POST,bean://configItemService:getValue,OFF,,,true,
```

| securityCode | 含义               |
|-------------|------------------|
| `OFF`       | 无校验（公开接口）        |
| `default`   | JWT Bearer Token  |
| `signature` | HMAC-SHA256 签名验签 |
| `internal`  | 内部调用白名单          |

---

## 测试规范

- 集成测试类名 `*IT.java`，单元测试类名 `*Test.java`
- 集成测试用 Testify + Testcontainers（`@EnableTestContainers(ContainerType.MYSQL)`）
- 测试模块 `*-test/` 的 parent 指向根 `loadup-parent`，不是模块聚合 pom
- 测试模块必须有 3 个 yml 文件：`application.yml`（激活 test profile）、`application-test.yml`（本地）、`application-ci.yml`（CI）

---

## 技术栈速查

- Java **21** | Spring Boot **4.1.0** | MyBatis-Flex **1.11.7**
- MySQL 8.0+ | Caffeine（本地）| Redis/Redisson（分布式）
- JUnit 5 + `loadup-testify-spring-boot-starter` + Testcontainers
- knife4j **4.5.0** | Spotless (Palantir Java Format) | Maven 3.6+
- License: **GPL-3.0**

---

## 关键参考文件

| 文件                                                  | 用途                    |
|-----------------------------------------------------|-----------------------|
| `components/loadup-components-database/ARCHITECTURE.md` | MyBatis-Flex 集成与审计设计  |
| `modules/loadup-modules-upms/ARCHITECTURE.md`       | 业务模块 COLA 4.0 实现参考    |

---

## 文档策略

### 三层文档体系

| 位置 | 受众 | 内容 | 维护者 |
|------|------|------|--------|
| `AGENTS.md`（根目录） | AI 编码助手 | 项目定位、禁止项、构建命令、技术栈 | 开发者 + AI |
| `**/README.md`（模块级） | 集成方开发者 | 模块用途、Maven 坐标、配置方式、示例 | 模块开发者 |
| `**/ARCHITECTURE.md`（模块级） | 深入理解者 | 设计决策、内部结构、扩展点 | 模块开发者 |
| `docs/` | 外部文档站读者 | Hugo + Lotusdocs 文档站，面向非本项目开发者 | 文档维护者 |

### 规则

1. **docs/ 不作为 AI / 开发参考。** docs/ 是 Hugo + Lotusdocs 文档站源码，内容由 `/docs-sync` skill 从模块 README.md 自动同步生成。AI 写代码时以 `AGENTS.md`、模块 `README.md` 和 `ARCHITECTURE.md` 为准。

2. **docs/ 不参与 Java/Maven 构建。** docs/ 目录是独立的 Hugo 项目，有自己的构建流程（`hugo` CLI）。CI 中不需要为它配置 Maven 插件。

3. **模块 README.md 是集成方入口。** 每个被外部集成的模块应有一个 README.md，控制在 50 行以内，回答三个问题：做什么、怎么引入（Maven 坐标）、怎么配置（关键 properties）。

4. **ARCHITECTURE.md 按需编写。** 只在设计复杂的模块（database、cache、gateway、upms、pipeline 等）中提供，含架构图和扩展点说明。不需要每个模块都写。

5. **文档与代码同步。** 修改代码导致接口/配置/行为变化时，必须同步更新对应的 README.md 或 ARCHITECTURE.md。运行 `/docs-sync` 将变更推送到文档站。

6. **根 README.md 是项目门面。** 面向首次接触者，含项目简介、快速开始、BOM 引入方式、模块目录概览，不深入单个模块细节。

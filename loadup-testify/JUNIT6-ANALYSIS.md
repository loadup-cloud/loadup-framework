# JUnit 6 接入分析

分析日期：2026-10-13。范围为依赖声明、源码与已有报告；本次没有运行 Maven、构建或测试，没有修改依赖。

2026-10-13 接入收尾：保留 Boot BOM 管理的 JUnit 6.0.3；launcher 改用核心 `archunit`，LoadUp BOM 新增该坐标管理并保留原 `archunit-junit5` 管理项供既有消费者使用。AGENTS、Testify README 与相关 Javadoc 已对齐。以下仓库证据与建议描述收尾前状态；运行验收仍待用户执行。

## 结论

当前仓库已通过 Spring Boot 4.1.1 默认采用 JUnit 6.0.3。无需再实施一次 JUnit 5 → 6 版本替换，建议完成文档对齐、ArchUnit 依赖整理及定向运行验收。JUnit 6.1.3 是另一个可选升级，不应与当前接入收尾混在一起。

## 仓库证据

| 项目 | 当前状态 | 判断 |
|---|---|---|
| LoadUp BOM | `loadup-dependencies/pom.xml` 的 parent 为 `spring-boot-dependencies:4.1.1` | 继承 Boot 的 JUnit BOM，仓库无独立 JUnit 版本覆盖 |
| 本地 Boot BOM | `junit-jupiter.version=6.0.3`，导入 `org.junit:junit-bom` | Jupiter 与 Platform 均管理为 6.0.3；`junit.version=4.13.2` 属于旧 JUnit 4，不能用来判断 Jupiter 版本 |
| Java | 根 POM 为 Java 25 | 满足 JUnit 6 的 Java 17+ 要求 |
| 测试插件 | Surefire/Failsafe 3.6.0 | 超过 JUnit 6 要求的最低 3.0.0 |
| 测试源码 | 118 个 Java 文件导入 Jupiter；未检出旧 JUnit 4 Test、Platform 内部 API 或自定义 Jupiter 扩展实现 | 常规注解和 Assertions 无需改包名；静态搜索不能证明所有运行行为兼容 |
| 既有报告 | database 的 MyBatisFlexAutoConfigurationTest 3 项、BaseEntityListenerTest 5 项，失败/错误/跳过均为 0；classpath 包含 Jupiter 与 Platform 6.0.3 | 有局部运行证据，报告不是本次执行，也不代表全仓库验收 |
| Testify | 主源码未直接引用 JUnit，提供 Spring 测试辅助与数据/断言能力 | 无自定义 JUnit 引擎需要迁移 |
| LoadUp Testcontainers | 容器生命周期由 Spring TestExecutionListener 管理 | 无需为 JUnit 6 改写生命周期实现 |
| ArchUnit | BOM 与 launcher 仍声明 `archunit-junit5:1.5.0`；ArchitectureTest 使用普通 Jupiter Test 和 ClassFileImporter，无 ArchTest | 当前规则可使用核心 `archunit`，避免携带不用的引擎；未来采用 ArchTest 时再选择 `archunit-junit6` |

检索未发现 `MethodOrderer.Alphanumeric`、CSV `lineSeparator`、旧 TempDir scope、`interceptDynamicTest` 或 `getOrComputeIfAbsent` 用法。没有实际解析每个模块的 effective POM 或测试依赖树，消费工程自己的 BOM/版本覆盖仍需单独检查。

## 建议收尾范围

1. 保留 Boot 管理的 JUnit 6.0.3，不添加重复版本管理，不把 `org.junit.jupiter` 改成不存在的 junit6 包名。
2. 将 launcher 的 ArchUnit 依赖改为核心 `archunit`，在 LoadUp BOM 管理其版本；若消费契约需要保留现有受管坐标，不随意删除旧条目。
3. 更新根 AGENTS.md、Testify README、starter README 和 TestScenario Javadoc 中的 JUnit 5 表述；接口行为未变化，不需要重写现有测试。
4. 用户定向验证普通单测、参数化测试、SpringBootTest、ArchitectureTest/SourceStandardsTest/ErrorCodeContractTest，以及一个真实 MySQL IT。比较测试发现数量，防止仅构建成功但测试漏跑。
5. 验证时使用 mvnd、clean 和受影响模块选择，分别记录单测与真实容器测试。不要由分析任务触发全 reactor 构建。

若明确需要 6.1.3，应在 LoadUp BOM 覆盖 Boot 父 BOM 的 `junit-jupiter.version` 并确保整套 BOM 一致，检查实际解析版本；不要只单独升级 API 或 engine。该路线需要额外验证 Boot 的测试集成及第三方引擎，当前没有功能需求证明它有必要。

## Testify 专项复核（2026-10-13）

后续修复：移除 `archunit-junit5` BOM 管理项；数据库断言引擎按 JdbcTemplate Bean 存在条件装配，ScenarioAssert 获取可选数据库引擎并在缺失时明确拒绝 verifyDb。补齐消费方 spring-boot-starter-test 依赖和无数据库时排除 Boot 数据源自动配置的文档；新增两个装配回归测试（未运行），当前定向验收预期 19 次执行。以下两项接入问题描述修复前状态。

静态复核五个子模块 POM、主源码的 JUnit 接点、三个测试类及测试资源：未发现 JUnit 6.0.3 特有的不兼容调用。core/data-engine/assert-engine/starter 不直接依赖 JUnit，也没有 TestEngine、Launcher、Jupiter Extension 或被删除的 Platform API。断言失败抛出标准 AssertionError，JSONAssert 被转换为 MatchResult，不依赖旧 JUnit Runner。

测试模块通过 `spring-boot-starter-test:4.1.1` 引入 `junit-jupiter:6.0.3`，后者包含 params 与 engine。UserServiceBatchIT 使用静态无参 `cases()` 返回 Stream<TestifyCase>，与单个 TestifyCase 参数一一对应；没有 CSV、旧参数转换器或资源自动关闭接点。SpringBootTest/MockitoBean 走 Spring Test 集成，未发现自定义生命周期拦截。当前有 12 个断言单测、2 个服务 IT、3 个 YAML 参数化用例；没有找到 Testify 自身的现存测试报告，仍不能宣称运行验收通过。

另有两处既有接入问题，均非 JUnit 6 引入：

1. TestifyAutoConfiguration 第 63–66 行的 dbAssertEngine 只检查缺失 Bean，没有检查 JdbcTemplate Bean 是否存在。消费工程排除 DataSource/JdbcTemplate 自动配置但使用纯响应/异常断言时，该方法仍要求注入 JdbcTemplate，导致上下文启动失败。testScenario 已有相应 Bean 条件。可选择明确要求 Starter 必须配置数据库，或拆出按 JdbcTemplate Bean 装配的数据库能力，避免影响纯断言能力。
2. Starter README 的引入示例只声明 Testify starter，但 starter 不传递 spring-boot-starter-test/JUnit；示例中的 SpringBootTest、Test 与 ParameterizedTest 仍需要消费工程自行声明 Boot 测试 starter（test scope）。仓库自己的测试模块已有该依赖，所以其编译不受影响。接入文档应补齐该前置依赖。

建议用户定向运行 ScenarioAssertTest、UserServiceIT、UserServiceBatchIT，预期合计 17 次执行；运行前后核对 YAML 用例数与报告数。此次未运行构建或测试，未修改实现。

## 官方依据

- [Spring Boot 4.1.1 受管依赖](https://docs.spring.io/spring-boot/appendix/dependency-versions/coordinates.html)：JUnit 6.0.3、Mockito 5.23.0、Testcontainers 2.0.5，与本地 Boot BOM 对照。
- [JUnit 6.0 迁移指南](https://github.com/junit-team/junit-framework/wiki/Upgrading-to-JUnit-6.0)：Java 17+、版本号统一及旧废弃 API 移除。
- [JUnit Maven 运行要求](https://docs.junit.org/6.1.3/running-tests/build-support.html)：Surefire/Failsafe 最低 3.0.0。
- [JUnit 6.0.3 发布说明](https://docs.junit.org/6.0.3/release-notes.html)：迁移时的 API、参数化和嵌套测试行为变化。
- [JUnit 6.1.3 发布说明](https://docs.junit.org/6.1.3/release-notes.html)：截至分析日期的最新正式版本。
- [ArchUnit 安装与引擎选择](https://www.archunit.org/userguide/html/000_Index.html#_installation)：核心 API 与 JUnit 5/6 引擎的区别。

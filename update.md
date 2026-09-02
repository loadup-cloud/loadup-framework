# LoadUp Framework 优化方案

## Context

基于 commit `bbe897e` (main) 的一次只读评审，在 sessionId `fafa7bd4-f40b-4481-86b9-d4012e51feed` 中输出了两份报告（P0/P1/P2 问题 + 架构 A1–A8）。本轮会话对所有结论做了二次代码核实，并发现 4 条**超出原报告的加重事实**：

1. **B1 波及所有 6 个 MapStruct converter**（`User/Role/Department/Permission/LoginLog/UserOAuthBinding`），全部 `@Mapping` 数为 0；domain 侧字段命名自身也不统一。
2. **`UserDO` 没有 `createdBy/updatedBy`**，但 domain `User` 有 —— 是单向幻影字段。
3. **`DataScopeAspect` 方法体第 ~80 行 `String username = "";`** 硬编码空串。仅修切点会把"静默失效"升级为"每次调用都 warn 并放行"，安全姿态更差。
4. **`.github/dependabot.yml` 存在但是坏桩**（`package-ecosystem: ""`，非法值，Dependabot 直接报错不运行）。

用户已确认：JWT 密钥策略 = **严格拒绝默认值**；本轮执行范围 = **P1 治理与文档 + A1/A7/A8 组件契约 + A2/A5/A6 基础层与边界**。方案按风险/收益拆为三阶段。

## Scope of this execution

本轮执行 **Phase 1**（Quick Wins，9 项独立 commit，约 30-50 文件修改）。**Phase 2 / Phase 3 是设计先行项，本轮仅产出契约草案与路线图**，具体实施留给后续会话。

**全局收口**（Phase 1 全部完成后统一执行，避免中间态触发 license/格式违规）：
```
mvn license:update-file-header
mvn spotless:apply
mvn clean verify -P github
```
所有新增/修改 Javadoc 与注释必须英文（AGENTS.md 硬性要求）。

---

## Phase 1 — Quick Wins（本轮执行）

执行顺序：`P1 治理 → 文档漂移 → A7 starter optional → A5-死代码 → B4 DB 口令 → B3 JWT 密钥 → B1 MapStruct → A6 网关校验 → B2 DataScope`。每项 1 个 commit，便于单点回滚。

### 1. P1 治理文件

**文件**
- 新增 `SECURITY.md`、`CONTRIBUTING.md`、`.github/CODEOWNERS`
- 修复 `.github/dependabot.yml`（`package-ecosystem: ""` → `"maven"`；增 `"github-actions"`）
- 新增 Maven Wrapper：`mvnw`、`mvnw.cmd`、`.mvn/wrapper/maven-wrapper.properties`
- `.github/workflows/ci.yml`：`mvn` → `./mvnw`（确认 wrapper 工作后切换）

**要点**
- `SECURITY.md`：支持版本矩阵、GitHub Security Advisory 私密渠道、响应 SLA；显式写 "JWT secret & DB password must be externally provided"（呼应 B3/B4）。
- `CONTRIBUTING.md`：从 AGENTS.md 抽取硬性禁止项（禁 Lombok / `@RestController` / 字段注入 / `BOOLEAN`；主键 `VARCHAR(64)` UUID；注释英文；`<parent>` 指向 `loadup-parent`；版本由 BOM；`mvn spotless:apply`）。
- `CODEOWNERS` 仅覆盖 `loadup-dependencies/` 与根 `pom.xml`（最小起步，避免 PR 审批压力）。
- Wrapper 锁定与 CI 当前 `maven: '3.9.x'` 一致的版本。

**验证**：`./mvnw -v` 输出版本；`gh api repos/:owner/:repo/dependabot/alerts` 不报配置错误。
**风险**：CODEOWNERS 立即改变审批要求 → 仅覆盖 BOM/根 pom，逐步扩面。

### 2. 文档漂移

**文件**：`README.md:1056`、`AGENTS.md:39-42`。

**要点**
- `README.md:1056`：`mvn clean deploy -pl bom` → `mvn clean deploy -pl loadup-dependencies`。
- `AGENTS.md:39-42`：`modules/loadup-modules-config/...` → `loadup-modules/loadup-modules-upms/loadup-modules-upms-app` / `-test`（模块路径前缀与模块本身都已漂移）。
- 顺带核对 README/AGENTS 中所有 `-pl` 示例。

**验证**：每条命令 `mvn validate -pl <路径> -am` 通过。

### 3. A7 gateway-starter optional 化

**文件**
- `loadup-gateway/loadup-gateway-starter/pom.xml`（L46 yaml-plugin、L53 resilience4j-binder-core 加 `<optional>true</optional>`）
- `.../starter/.../GatewayAutoConfiguration.java`（`YamlRouteStore` bean 加 `@ConditionalOnClass(YamlRouteStore.class)`，熔断相关 bean 同理）
- `.../starter/.../RouteFunctionRegistry` 注入点改为 `ObjectProvider<CircuitBreakerFilter>` 或 `@Nullable`（如已是则不动）

**要点**
- 必须同时加 `<optional>` + `@ConditionalOnClass`，缺一即触发 `NoClassDefFoundError`。
- 无 RouteStore 时启动 `warn` 并注册返回 `List.of()` 的空实现，不 fail-fast（SDK 定位下"引入 starter 但用自定义 RouteStore"合法）。

**验证**
```
mvn dependency:tree -pl loadup-gateway/loadup-gateway-starter    # 两者标 (optional)
mvn test -pl loadup-gateway/loadup-gateway-test                 # 全绿
```
**风险**：下游隐式传递依赖 → CHANGELOG 给出"显式添加"片段。回滚 = 去掉 `<optional>`。

### 4. A5-子集 删死代码 + 字段对齐

**文件**
- 删除 `upms-client/src/main/java/.../client/dto/PageDTO.java`（零引用，全仓 8 处 import 均指向 `io.github.loadup.commons.result.PageDTO`）
- 删除 `upms-domain/src/main/java/.../domain/valueobject/DataScope.java`（零引用）
- `upms-app/.../strategy/oauth/GitHubOAuthProvider.java`：SPI 接口重命名为 `OAuthProviderStrategy`，删除 FQN hack；`client.constant.OAuthProvider` 常量类不动（对外契约）
- `upms-app/.../dto/UserDetailDTO.java`：字段名/类型向 `client.dto.UserDetailDTO` 对齐（`username→account`、`createdTime→createdAt`、`Short→Integer`、`List<RoleDTO> roles → List<String> roles`）—— 本轮**只对齐不合并**，合并留 Phase 2

**要点**：`PageDTO`/`DataScope` 在 `-client`/`-domain`（对外），严格说属 API 收缩 → CHANGELOG 记 `removed (dead code, zero references)`。

**验证**
```
grep -rn "io.github.loadup.modules.upms.client.dto.PageDTO\|domain.valueobject.DataScope" .   # 零命中
grep -n "io.github.loadup.modules.upms.app.strategy.oauth.OAuthProvider" .                    # 零命中（已改名）
mvn clean install -DskipTests && mvn test -pl loadup-modules/loadup-modules-upms/loadup-modules-upms-test
```
**风险**：删除公共类对外破坏 → 已确认零引用，回滚 = revert。

### 5. B4 demo 默认 DB 口令

**文件**：`loadup-application/src/main/resources/application.yml:7`（及 profile 变体）。

**要点**：`${DB_PASSWORD:123456}` → `${DB_PASSWORD}`；`${DB_USERNAME:root}` → `${DB_USERNAME}`。文件头部英文注释列出必需环境变量。一键体验需求另建 `application-local.yml` 指向 testcontainers/docker-compose，不含明文口令。

**验证**：`mvn spring-boot:run -pl loadup-application` 无 env 时报 `Could not resolve placeholder 'DB_PASSWORD'`。
**风险**：仅影响 demo launcher。回滚 = revert。

### 6. B3 严格拒绝默认 JWT 密钥（**唯一破坏性变更**）

**文件**
- `UpmsSecurityProperties.java:39`（默认值 → `null`）
- `GatewayProperties.java:80`（默认值 → `null`）
- 新增共享校验器 `loadup-commons-util/.../util/JwtSecretValidator.java`
    - `public static SecretKey requireStrong(String propertyName, String secret)`
    - 三条规则：非空 / UTF-8 字节 ≥ 32 / 不在已知弱值黑名单（含两个历史默认值字面量 + `changeme/secret/password` 前缀匹配）
    - 失败抛 `IllegalStateException`，错误文案含属性全名 + `openssl rand -base64 48` 建议，**不回显密钥**
- `TokenService.secretKey()` / `NimbusResourceServerBinder` 调用 `JwtSecretValidator.requireStrong`
- 必改测试：`upms-test/.../TokenServiceTest.java`、`gateway-test/.../NimbusResourceServerBinderTest.java`、`gateway-test/.../GatewayWebMvcIntegrationTest.java`：硬编码密钥换成测试专用随机常量（如 `"loadup-test-only-hs256-secret-0123456789abcdef"`），各加一条负向用例断言"历史默认值被拒绝"（防回归关键）
- `README.md` 安全章节 + 根 `application.yml.example` 文档同步

**验证**
```
mvn test -pl loadup-modules/loadup-modules-upms/loadup-modules-upms-test
mvn test -pl loadup-gateway/loadup-gateway-test
mvn spring-boot:run -pl loadup-application   # 不设 env → 启动失败
grep -rn "loadup-secret-key-change-in-production\|loadup-gateway-secret-key" --include=*.java --include=*.yml
```
**风险**：依赖默认值的现存部署启动失败 —— 这正是意图。CHANGELOG 标 `BREAKING`。回滚 = revert，**不提供"宽松模式"开关**。

### 7. B1 MapStruct 审计字段静默丢失

**文件**
- `upms-infrastructure/.../converter/{User,Role,Department,Permission,LoginLog,UserOAuthBinding}Converter.java`
- 新增 `upms-infrastructure/.../converter/AuditMappingSupport.java`（`final class`，`Integer toFlag(Boolean)` / `Boolean toBoolean(Integer)`，语义与 `BaseDO.isDeleted()` 一致：`!= 0` 为已删，`null → 0`）
- `upms-domain/.../entity/User.java`（增 `tenantId`；删 `createdBy/updatedBy`）
- 新增测试 `upms-test/.../converter/UserConverterTest.java`（断言 round-trip 后审计字段非空且值相等）

**要点**
- **核心**：每个 converter 声明 `@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, unmappedSourcePolicy = ReportingPolicy.WARN)`，把"静默丢失"升级为"编译失败"。首次编译会逐条报出未映射字段，逐条消掉。
- 显式 `@Mapping`：`createdTime ↔ createdAt`、`updatedTime ↔ updatedAt`、`deleted(Boolean) ↔ deleted(Integer)`，通过 `uses = AuditMappingSupport.class` 复用类型转换器。
- **`tenantId`**：domain `User` 增补字段并纳入映射。**不**在 converter 里注入 `TenantContext`（保持纯函数）。租户填充留给 MyBatis-Flex 租户处理器 / `GatewayImpl` 写入前赋值。
- **`createdBy/updatedBy`**：DB 表标准 5 字段不含它们 → **删除 domain 侧**（避免引入未声明 DDL）。若确需保留须同批提供 Flyway 脚本。
- `@Mapper(componentModel = "spring")` 与 `INSTANCE = Mappers.getMapper(...)` 并存是双装配路径 → `INSTANCE` 标 `@Deprecated`，Javadoc 指向 Spring 注入。

**验证**
```
mvn clean compile -pl loadup-modules/loadup-modules-upms/loadup-modules-upms-infrastructure -am   # 无 ERROR
mvn test -pl loadup-modules/loadup-modules-upms/loadup-modules-upms-test
grep -n "setCreatedAt\|setUpdatedAt\|setTenantId" target/generated-sources/annotations/.../UserConverterImpl.java   # 确认生成代码含审计字段赋值
```
**风险**：`deleted` 语义变化可能让原本"查得到"的记录被过滤 → 先只做 `toEntity` 方向 + 测试，确认后再开 `toDataObject`。回滚 = revert。

### 8. A6 Gateway 启动期路由校验 + 重载支持

**文件**
- `loadup-gateway/plugins/proxy-springbean-plugin/.../SpringBeanProxyProcessor.java`（`findMethod` L125-130 + `proxy` L80-91）
- `loadup-gateway/loadup-gateway-webmvc/.../router/RouteFunctionRegistry.java`（`init` + `refresh`）
- `loadup-gateway/loadup-gateway-facade/.../model/RouteDefinition.java`（`BackendDefinition` 增 `List<String> paramTypes`）
- `loadup-gateway/plugins/repository-yaml-plugin/src/main/resources/gateway-routes-schema.json`（新增 `paramTypes` 字段）
- `loadup-gateway/plugins/repository-yaml-plugin/.../YamlRouteStore.java`（L187-198 解析循环）
- 新增 `loadup-gateway-webmvc/.../router/RouteDefinitionValidator.java`
- 新增测试 `loadup-gateway-test/.../unit/RouteValidationTest.java`、`.../webmvcapp/OverloadedDemoService.java`

**要点**
- **`findMethod` 重写**：用 `AopUtils.getTargetClass(bean)` + `ReflectionUtils.doWithMethods` 收集全部同名 public 方法（含继承链、CGLIB 代理）；候选 == 1 直接用；候选 > 1 且 `paramTypes` 已声明则精确签名匹配；候选 > 1 且未声明则**抛 `RouteException`，不再"取第一个"**。结果缓存到 `ConcurrentHashMap<beanName+methodName+sig, Method>`。
- **`paramTypes`**：`BackendDefinition` 新增 `List<String> paramTypes`（FQN 或简单名），schema 中为 `optional array of string`。可选 → 现有路由零改动。已确认存在真实重载：`RoleGatewayImpl.findAll()` / `findAll(PageQuery)`，`LoginLogGatewayImpl.findByUserId(String)` / `findByUserId(String, PageQuery)`。
- **`RouteDefinitionValidator`**（`-webmvc`，需 `ApplicationContext`），在 `RouteFunctionRegistry.init()` 里于 `refresh()` 之前逐条检查：`protocol` ∈ 已注册 `ProxyProcessor.getSupportedProtocol()` 集合（**动态取，不硬编码枚举**）；`bean` 时 bean 存在且方法可唯一解析；`http/rpc` 时 `url` 非空且可解析 URI；`path + method` 唯一；`id` 唯一。聚合所有错误后**一次性抛出**，按 `routeId` 分行。
- **严格度开关**：`loadup.gateway.route.validation-mode = strict | warn`。建议**本轮默认 `warn`** 并在日志显式提示"下一版将改为 strict"，给存量应用一个版本过渡。
- **`YamlRouteStore` 单条失败**（L190-191 仅 warn 跳过）：`strict` 下汇总抛出；`warn` 下保持现有行为但日志级别升 `error` 且带条数统计。
- **启动期与运行期区分**：`refresh(boolean failFast)`，`init()` 传 `true`，运行期热刷新传 `false`（保留旧快照语义）。

**验证**
```
mvn test -pl loadup-gateway/loadup-gateway-test
mvn spring-boot:run -pl loadup-application   # 现有 gateway-routes.yml 必须正常启动
```
期望：故意写错 `beanName` 的用例启动失败（strict）或 error 日志（warn），列出 routeId 与原因；指向重载方法且无 `paramTypes` 时启动失败，加上 `paramTypes` 后成功；现有路由在 `warn` 下正常启动（回归底线）。

**风险**：`strict` 默认值让存量应用启动失败 → 本轮用 `warn` 默认。回滚 = `validation-mode=off`。

### 9. B2 `@DataScope` 切点包名反转（数据权限失效）

**文件**
- `upms-infrastructure/.../security/datascope/DataScopeAspect.java`（L75 切点 + L89 主体解析 + 加 `@After` 清理）
- 新增 `upms-infrastructure/.../security/datascope/DataScopeAutoConfiguration.java`（开关）
- 新增测试 `upms-test/.../security/DataScopeAspectTest.java`

**要点**
- **切点改为注解绑定形参**：`@Before("@annotation(dataScope)")` + `public void before(JoinPoint jp, DataScope dataScope)`，从根上消除手写 FQN 出错的可能；方法体内 `method.getAnnotation(...)` 与 null 判断可删除。
- **必须同时修 NEW-3**：主体解析改从 `SecurityContextHolder.getContext().getAuthentication()` 取；主体为空/匿名时 **fail-closed** —— `DataScopeContext.denyAll()`（而非 `return` 放行）。关键设计决策：数据权限切面的默认分支必须是"拒绝"。
- **默认关闭**：`loadup.upms.data-scope.enabled = false`。理由：该功能自诞生起从未真正生效，一次性打开会立刻改变**所有**被注解方法的查询结果集。默认关 + 文档说明"1.x 起需显式开启"，是唯一兼顾"修 Bug"和"不炸下游"的方案。
- **`@After` 清理**：当前 `getCurrentContext()/clearContext()` 是 static + ThreadLocal，缺 `@After` 清理路径 → 补 `@After(...) clearContext()`，避免线程池复用串号。

**验证**
```
mvn test -pl loadup-modules/loadup-modules-upms/loadup-modules-upms-test -Dtest=DataScopeAspectTest
```
期望：AOP 代理测试中 ① 带 `@DataScope` 的方法被拦截（计数器 +1）；② 无认证主体时得到 deny-all 上下文；③ `enabled=false` 时切面 bean 不注册。

**风险**：默认关闭 → 风险接近零。若某集成环境已依赖"失效"行为，开关置回 `false` 即可。

---

## Phase 2 — 设计先行（下一轮）

本轮**只输出契约文档草案**，不实施代码。契约落 `docs/contracts/`，作为 Phase 2 实施前置。

### 2.1 A1 统一组件装配契约

`docs/contracts/component-assembly.md`（英文），核心规则 5 条：
1. **标准优先**：若 JSR/Spring 已有稳定抽象（缓存 → `CacheManager`、调度 → `TaskScheduler`、校验 → `jakarta.validation`），**直接用标准接口，禁止另造 LoadUp SPI**。`cache` 保持标准；`scheduler` 保留 SPI 但补齐结构（Quartz/JobRunr 的持久化与 misfire 语义超出 `TaskScheduler`）。
2. **五件套结构强制**：任一采用 SPI 的组件必须 `-api`（SPI 接口 + Properties + 门面 + autoconfigure + `AutoConfiguration.imports`）+ `-binder-<vendor>` ≥1 + `-test`。`scheduler-api` 缺 imports / Properties / binder 开关；`retrytask` 缺 imports → 按此补齐。
3. **命名归一**：模块后缀统一 `-api`（`retrytask-facade` → `retrytask-api`；`gateway-facade` 因是网关内核 SPI 非组件，作为**显式例外**）；门面统一 `XxxTemplate`（操作型），`XxxService` 仅用于应用服务；SPI 提供者统一 `XxxProvider`。
4. **属性键归一**：`loadup.<component>.enabled` + `loadup.<component>.binder-type`（放弃 `type`）。每个 binder 统一 `@ConditionalOnProperty(name="loadup.<c>.binder-type", havingValue="<vendor>")` + `@ConditionalOnMissingBean`；`-api` 侧门面用 `@ConditionalOnSingleCandidate(XxxProvider.class)`。
5. **零 Provider 反模式禁止**：Template 不得由各 binder 各自实现（scheduler 现状）。Template 必须唯一、位于 `-api`，binder 只实现 Provider。

**涉及模块**：`scheduler`（-api + 2 binder）、`retrytask`（-facade → -api、jobrunr binder、gotone notifier）、`loadup-dependencies`（新 artifactId）、`loadup-application`。

**兼容性**：`retrytask-facade` 保留空壳 pom 指向新 `-api` + `<description>` 标 deprecated，一个小版本后删；`type` → `binder-type` 用 `additional-spring-configuration-metadata.json` 的 `deprecation.replacement`，运行期两键都读、旧键 warn；Template 归一属 breaking，旧 binder 侧 Template 类 `@Deprecated(forRemoval=true)` 委托新 Template。

### 2.2 A8 gateway-facade 框架中立化

`docs/contracts/gateway-spi-neutrality.md`，核心规则 4 条：
1. `loadup-gateway-facade` 编译期依赖白名单：仅 JDK + `jakarta.validation-api`（如需）+ SLF4J API。**禁止** `spring-context` / `spring-web` / `spring-webmvc`。
2. facade 只放：SPI 接口、不可变模型、异常体系、常量。
3. `RouteStoreRefreshedEvent extends ApplicationEvent` 与 `GatewayProperties(@ConfigurationProperties)` 移入新模块 `loadup-gateway-facade-spring`（或直接进 `-webmvc`）。
4. `RouteStore` 若需通知刷新，改 SPI 侧 `RouteStoreListener` 回调（框架中立），由 spring 层适配为 `ApplicationEvent`。

**兼容性**：迁移保持 FQN 不变，只换所在 Maven 模块 → 下游 import 零改动；facade 老 pom 保留 `spring-context` 为 `optional` 一个版本，配合 `maven-enforcer-plugin` 的 `bannedDependencies` 守白名单不回退。

### 2.3 A5 剩余 AuthGateway 归位 + UserDetailDTO 合并

`docs/contracts/upms-layering.md`，核心规则 3 条：
1. **Gateway 接口归属 domain**：`AuthGateway` 现居 `client/gateway` → 迁至 `domain/gateway`。
2. **DTO 单一来源**：`app.dto.UserDetailDTO` 删除（Phase 1.4 已完成字段对齐，退化为纯删除 + 改 import）。
3. **domain 不得依赖 client**：迁移 `AuthGateway` 时若其签名引用 client DTO，须改用 domain 实体/值对象。

**兼容性**：`AuthGateway` breaking（包名变更）→ `-client` 保留 `@Deprecated(forRemoval=true) interface AuthGateway extends <domain>.AuthGateway {}`（空继承桥），一个小版本后删。

---

## Phase 3 — 长期演进（不在近期执行）

### 3.1 A3 包根统一

**方向**：收敛到 `io.github.loadup.commons.*`（现存类最多、语义最中性）。**破坏性极大**，影响所有下游 `import`，无法用继承桥（涉及注解、枚举、静态工具）。**弃用周期 2 个大版本**：v(N) 双包并存（旧包 `@Deprecated`，实现委托新包）→ v(N+1) 旧包仅存根 → v(N+2) 删除。**必须提供 OpenRewrite recipe（`ChangePackage` 组合）+ sed 脚本**，无自动化迁移工具不应启动。

### 3.2 A4 结果/追踪模型统一

**方向**：`Result`/`SuccessResponse`/`FailureResponse`/`ResultCode`/`PageDTO` 收敛到 `loadup-commons-api`；`TraceUtils`/`TraceUtil`/`TraceContext`/`MDCUtils` 收敛为单一 `TraceContext` + 单一 MDC key 常量源。**可提前到 Phase 2 尾部的低成本子项**：MDC key 硬编码 `"TraceId"` 与 `LogContext.TRACE_ID` 不一致 → 仅改常量引用，破坏性小。**主项破坏性**：`Result` 泛型形态变化影响所有 Gateway 响应序列化 JSON → 对外 API breaking，需 `wrapResponse` 兼容开关 + 一个版本的双写。**弃用周期 1 个大版本**。

### 3.3 A2 BaseDO 下沉（备选）

**方向**：把 `commons-dto` 的 `BaseDO` 移到 `loadup-components-database`，`commons-dto` 只留纯 POJO。**现状评估：实际引用方仅 components/infrastructure 层，分层边界已干净，收益有限**。**建议不单独立项**，仅在 Phase 3.1 包根统一时顺带完成，摊薄迁移成本；若 Phase 3.1 不做，本项也不做。

---

## Verification

**Phase 1 全部完成后的回归**：
```bash
mvn license:update-file-header
mvn spotless:apply
mvn clean verify -P github                       # 全量构建 + 测试
mvn spotless:check -P github                     # CI 一致的格式校验
mvn dependency:tree -pl loadup-gateway/loadup-gateway-starter
mvn spring-boot:run -pl loadup-application       # 不设 JWT/DB env → 启动失败
```

**预期结果**
- CI 全绿（含 `spotless:check` + `check-file-header` 双校验）
- `loadup-application` 不设 env 时启动失败，错误信息含属性名 + 生成建议
- `grep -rn "loadup-secret-key-change-in-production\|loadup-gateway-secret-key-must-be-long-enough" --include=*.java --include=*.yml` 只在 `JwtSecretValidator` 黑名单常量与负向测试中命中
- `UserConverterImpl.java` 等生成代码含 `setCreatedAt`/`setUpdatedAt`/`setTenantId` 调用
- `DataScopeAspect` AOP 测试中 `enabled=true` 时被正确拦截，`enabled=false` 时不注册
- 现有 `gateway-routes.yml` 在 `validation-mode=warn` 下正常启动

**回滚方式**：Phase 1 每项 1 commit，`git revert <commit>` 即可单点回滚；B3 若触发大规模下游失败，最快路径是 revert B3 commit + 在 CHANGELOG 标注延期。

## Commit plan

每项 1 个 commit，按执行顺序：

1. `chore: add governance files (SECURITY/CONTRIBUTING/CODEOWNERS/dependabot/Maven wrapper)`
2. `docs: fix stale module paths and deploy commands in README/AGENTS`
3. `refactor(gateway): mark yaml-plugin and resilience4j-binder-core optional in starter`
4. `refactor(upms): remove dead PageDTO/DataScope and rename OAuthProvider SPI`
5. `security(application): remove default DB credentials from demo launcher`
6. `security: fail-fast on weak/default JWT secrets` — `BREAKING CHANGE`
7. `fix(upms): align MapStruct audit field mappings across all converters`
8. `feat(gateway): startup route validation and overloaded method resolution`
9. `fix(upms): correct @DataScope pointcut and fail-closed subject handling`

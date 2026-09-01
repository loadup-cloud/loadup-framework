# LoadUp 设计总纲与组件规范

> 本文是 LoadUp 组件设计的唯一总纲，与根目录 `AGENTS.md` 配合使用：
> AGENTS.md 负责"禁止项、构建命令、技术栈"等硬性规则，本文负责"设计理念、组件契约、目标架构、演进方向"。
>
> 版本：v2.0（2026-08）

---

## 1. 定位

LoadUp 的目标是成为类似 RuoYi / Pig 的**被消费脚手架**：集成方通过 BOM 引入，快速完成二次开发。

与 RuoYi / Pig 的本质区别：

| 维度 | RuoYi / Pig 风格脚手架 | LoadUp |
|------|----------------------|--------|
| 技术栈 | 定死（固定 ORM / 缓存 / 权限 / 调度全家桶） | binder 可插拔，依赖级选型 |
| 业务代码 | 绑定具体中间件 API | 只依赖 LoadUp facade（尽量是业界标准 API） |
| 切换中间件 | 改框架 / 改代码 | 换 binder 依赖 + 配置 |
| 扩展方式 | 侵入式修改框架 | 遵循契约实现 SPI / binder |
| 二开体验 | 全家桶，裁剪困难 | BOM + 按需 starter + 业务模块可裁剪 |

**终极目标**：开发者拿到工程后只写业务代码（Service + 接口/路由定义 + 配置），中间件选型在依赖与配置层面完成，不进入业务代码。

---

## 2. 设计理念（十大原则）

### 2.1 底层 OSS + 薄集成

每个组件 = **facade（契约）+ binder（对底层 OSS 的薄适配）**。能力由底层 OSS 提供，LoadUp 只做：

- 统一业务侧 API（尽量用业界标准 API）
- 屏蔽底层 SDK 差异
- 补充框架级横切能力（审计、多租户、幂等、可观测性）

### 2.2 业界标准 API 优先

能用标准接口表达业务语义时，**facade 直接采用标准 API**，不自创平行接口：

| 领域 | 标准 API / 底层 OSS |
|------|-------------------|
| 缓存 | Spring Cache 注解（`@Cacheable` 等）+ `CacheManager` |
| 认证授权 | Spring Security（`@EnableMethodSecurity` / `@PreAuthorize` / `SecurityContextHolder`） |
| 配置中心 | Nacos / Apollo（SDK 差异由 binder 屏蔽） |
| 任务调度 | JobRunr（周期任务，与 retrytask 共用引擎）/ Quartz |
| 文件存储 | S3 协议（MinIO / OSS / COS） |
| 网关 | Spring Cloud Gateway Server MVC |
| 容错（熔断/重试/限流/舱壁/超时） | Resilience4j |
| 链路追踪 | OpenTelemetry |
| 数字签名 | JCA（Java Cryptography Architecture） |
| 测试容器 | Testcontainers |
| 重试/后台任务 | JobRunr（可选 binder） |

自创接口**只允许**出现在标准接口表达不了的语义上：

- Gateway 路由模型（`RouteDefinition` + `securityCode` + filter 声明）
- `RetryTaskFacade` 的 `bizType + bizId` 幂等语义
- `ServiceCode` 驱动的通知路由
- Pipeline 四阶段 DSL（业务编排语义）

### 2.3 能力并集，不做最小公分母

facade 必须是底层 binder **能力的并集**，而不是交集。

反例：cache facade 若没有"异步刷新"，业务方切到 JetCache 后享受不到核心优势，就会绕过 facade 直接用 JetCache API——抽象泄漏。

每个组件必须维护一张**能力矩阵**（facade 能力 × binder 支持情况），作为薄集成的契约文档，放在组件 README 中。

### 2.4 可切换性分三类，不混为一谈

| 类型 | 例子 | 定位 |
|------|------|------|
| 开发/生产切换 | Caffeine ↔ Redis；DFS local ↔ S3；config local ↔ Nacos | facade 的核心价值，常态操作 |
| 架构级切换 | Redis ↔ JetCache；JobRunr ↔ Quartz | 可切换，但属于架构决策而非配置项 |
| API 统一 | Nacos ↔ Apollo | 实际不会换，facade 的价值在屏蔽 SDK 差异 |

**禁止把"可切换"宣传成运行时动态切换**。binder 选型在构建/部署时确定，facade 保证业务代码不变即可。

### 2.5 部署拓扑差异只文档化，不抹平

JobRunr 自带服务端与 Dashboard、Quartz 是嵌入式、Nacos 和 Apollo 是不同的运维体系。facade 统一的是 **API**，不是**运维模型**。每个 binder 的 README 必须标注部署拓扑要求。

### 2.6 薄集成的边界

- facade 只表达"能做什么"，**策略归配置与 binder**
- facade 不得包含业务能力（模板渲染、渠道选择、路由规则等一律下沉）
- facade 模块零框架依赖（仅 `loadup-commons-api`），保证被任何 binder 复用

### 2.7 binder 独立模块 + BOM 版本管理

- 每个 binder 独立 Maven 坐标，按需引入，不默认打包
- 版本统一由 `loadup-dependencies` BOM 管理
- binder 依赖底层 OSS 的版本同样进 BOM（AGENTS.md 禁止项 #15）

### 2.8 真容器测试契约

每个 binder 必须提供集成测试，用 Testcontainers（`@EnableTestContainers`）验证**行为一致性**：

- 同一 facade API，不同 binder 的结果语义一致
- 切换 binder 后业务代码零修改（用测试证明，而不是口头承诺）

### 2.9 不 fork 上游

禁止把上游 OSS 代码复制进仓库（captcha 已去 fork 化，直接依赖 tianai-captcha / nanocaptcha upstream 坐标）。需要增强时优先：

1. 向 upstream 提 PR
2. 用 facade 层补充能力（如验证码存储）
3. 用 binder 适配差异

### 2.10 许可证与商业友好（已定：Apache-2.0）

现状：项目已从 **GPL-3.0** 切换为 **Apache-2.0**（插件标准默认模板 `apache_v2`），Java 文件头由
`license-maven-plugin` 自动维护：`mvn license:update-file-header` 更新后执行
`mvn spotless:apply` 去除模板空行的尾随空格，`verify` 阶段 `check-file-header` + `spotless:check` 双校验。

依据：GPL-3.0 要求衍生作品开源，与"被消费的脚手架 + 商业快速二次开发"目标**直接冲突**；
同类脚手架（RuoYi 为 MIT，Pig 为 Apache-2.0）均采用宽松许可证。

约定：底层依赖尽量选 Apache-2.0 / MIT / LGPL，避免引入 GPL 系依赖。

---

## 3. 约束执行机制（防绕过治理）

治理目标：业务开发者必须使用 facade（尽量是业界标准 API），**禁止绕过 LoadUp 直接对接底层中间件**（如绕过 Cache facade 直接使用 JetCache API）。

治理目标不是"绝对不可能绕过"，而是**让绕过成本 > 合规成本**。三层防线缺一不可：

### 3.1 防线一：依赖隔离（构建期，编译层）

让业务模块在 classpath 上根本拿不到中间件坐标：

- 业务模块（`*-client` / `*-domain` / `*-app`）**只允许声明 facade**（`*-api` / `*-starter`）
- binder 依赖**只允许出现在 binder 模块自身与集成方根工程**（`loadup-application`）
- **starter 不得传递 binder**：starter 只依赖 facade/api；实现拆为 `binder` 模块，由集成方显式选型（如 retrytask 已拆为 facade + binder-jobrunr）

根 pom 引入 `maven-enforcer-plugin` 的 `bannedDependencies`，CI 必跑：

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-enforcer-plugin</artifactId>
  <executions>
    <execution>
      <id>ban-middleware-deps</id>
      <goals><goal>enforce</goal></goals>
      <configuration>
        <rules>
          <bannedDependencies>
            <excludes>
              <exclude>com.alicp.jetcache:*</exclude>
              <exclude>org.springframework.data:spring-data-redis</exclude>
              <exclude>com.alibaba.nacos:nacos-client</exclude>
              <exclude>io.github.loadup-cloud:*binder*</exclude>
            </excludes>
          </bannedDependencies>
        </rules>
      </configuration>
    </execution>
  </executions>
</plugin>
```

binder 模块与集成方工程通过 `<properties><loadup.enforcer.skip>true</loadup.enforcer.skip></properties>` 豁免。

### 3.2 防线二：架构测试（测试期，CI 层）

用 **ArchUnit**（Java 架构测试事实标准，JUnit 5 集成）把 import / 依赖规则变成测试，违反即红：

```java
@AnalyzeClasses(packages = "io.github.loadup.modules")
class ArchitectureTest {

    @ArchTest
    static final ArchRule no_middleware_in_business =
        noClasses()
            .that().resideInAnyPackage("..app..", "..domain..", "..client..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "com.alicp.jetcache..",
                "org.springframework.data.redis..",
                "com.alibaba.nacos..",
                "io.github.loadup.retrytask.jobrunr..");
}
```

落地约定：

- ArchUnit 测试基类放 `loadup-testify`（test scope），各模块继承一个 `ArchitectureTest`
- 中间件坐标黑名单维护在根 pom 与 ArchUnit 规则常量中，**新增 binder 时同步更新**
- 规则与 AGENTS.md 禁止项表保持一致

### 3.3 防线三：能力供给与反馈（动机层）

前两层是"堵"，这一层是"疏"。开发者绕过 facade 的**根因几乎都是 facade 能力不够**（如想要 JetCache 多级缓存、按前缀批量失效，发现 `@Cacheable` 表达不了）。因此：

- 把"被绕过"当作**能力缺口信号**：需要中间件私有能力 → 提 Issue/PR 增强 facade + 能力矩阵，而不是在业务代码里直接调中间件 API
- facade 保持**注解驱动 + 声明式**，让"正确用法"成为唯一舒服的写法（JetCache 场景 = 集成方工程引入 `cache-binder-jetcache` + `loadup.cache.type=jetcache`，业务代码仍是 `@Cacheable`）
- 脚手架模板（`loadup-application`）只示范 facade 用法，作为正确姿势的活文档

### 3.4 规则总表

| 层 | 允许 | 禁止 | 强制手段 |
|---|---|---|---|
| client / domain / app | 只依赖 `*-api` / `*-starter` | import 任何中间件、binder、infra | Enforcer + ArchUnit |
| binder 模块 | 依赖 facade + 底层 OSS | 被业务模块传递依赖 | 依赖方向 + starter 不传递 binder |
| 集成方工程（application） | 显式声明 binder + 配置 | 业务代码直接调 binder API | 代码评审 + 脚手架示范 |

运行期检测（扫描谁直接 new 了中间件客户端）成本高收益低，**不建议做**。依赖层拿不到 + CI 层过不了 + facade 层够用，组合起来已能把绕过率压到趋近于零。

---

## 4. 组件契约规范

### 4.1 模块结构

```
loadup-components-{domain}/
├── {domain}-api/          # facade：接口 + 模型 + 配置 + 自动装配
├── {domain}-engine/       # 可选：需要内部引擎时（纯实现，零存储依赖）
├── {domain}-binder-{impl}/ # 每个底层 OSS 一个 binder
└── {domain}-test/         # 集成测试 + 单元测试（parent = 根 loadup-parent）
```

### 4.2 facade 铁律

1. 业务侧 API 优先采用业界标准接口（见 2.2）
2. 接口命名沿用 `XxxTemplate` / `XxxFacade`，SPI 沿用 `XxxProvider`（AGENTS.md 命名约定）
3. 零 Spring 框架注解污染领域模型；`@Service` 只出现在 app 层
4. 每个 facade 必须附带**能力矩阵**表
5. facade 的自动配置负责"根据 classpath 上唯一的 binder 装配默认实现"（`@ConditionalOnSingleCandidate`）

### 4.3 binder 铁律

1. 每个 binder 独立模块、独立 AutoConfiguration，`@ConditionalOnProperty` / `@ConditionalOnClass` 控制启用
2. binder 只做适配，不包含业务逻辑
3. binder 必须提供集成测试证明行为一致（2.8）
4. binder 不得被业务代码直接引用（业务代码只依赖 facade）
5. binder README 必须标注：部署拓扑、依赖的外部中间件、能力矩阵中支持的行

### 4.4 能力矩阵模板

每个组件 README 增加以下契约表：

```markdown
## 能力矩阵

| 能力 | binder-a | binder-b | binder-c |
|------|----------|----------|----------|
| 能力 1（基础 CRUD） | ✓ | ✓ | ✓ |
| 能力 2（标准 API 特性） | ✓ | ✓ | ✗（用 LoadUp 配置补齐） |
| 能力 3（高级特性） | ✓ | ✗ | ✗ |

> 切换 binder 不修改业务代码的前提：业务只用 ✓ 的能力。
```

### 4.5 命名规范补充

| 类型 | 规则 | 示例 |
|------|------|------|
| binder 模块 | `{domain}-binder-{impl}` | `loadup-components-cache-binder-redis` |
| binder 自动配置 | `{Impl}XxxAutoConfiguration` | `RedisCacheAutoConfiguration` |
| 能力矩阵 | 组件 README 内嵌 | — |
| 部署拓扑说明 | binder README 内嵌 | — |

---

## 5. 组件目标设计

> 优先级：P1 = 近期改造（与"去自研化"直接相关）；P2 = 中期；P3 = 文档/契约补齐；P4 = 演进。

### 5.1 cache — P1

- **✅ 已落地**：自研 `CacheBinding` / `CacheTemplate` / `CacheProvider` 体系已删除，facade 就是 Spring Cache 标准注解（`@Cacheable` / `@CacheEvict` / `@CachePut`）。
- **facade**：Spring Cache + `loadup.cache.*`；LoadUp 增量 = 按 cache name 的 TTL / 空值缓存 / 随机过期（防雪崩），由 api 模块 `LoadupCacheProperties` + `RandomExpiration` 统一表达。
- **binder**：`caffeine`（默认本地）/ `redis`（Spring Data Redis，同步写保证"写后立即可读"）/ `jetcache`（多级 LOCAL/REMOTE/BOTH + 可选防穿透，程序化适配 `Cache` 接口，不引入 `@Cached` 注解体系）。
- **序列化**：统一 JSON + `@class` 类型标记（`CacheJsonCodec`），弃用 JDK 序列化；`NullValue` 有专用序列化器，空值可跨 binder 往返。
- **契约验证**：`AbstractCacheBinderIT` 同一业务服务在三个 binder 上跑同一套用例（读/写/失效/全清/空值/TTL/随机过期），证明切换零代码修改。

### 5.2 authorization — P1

- **现状**：已删除自研 `@RequireRole` / `@RequirePermission` AOP 与 ThreadLocal `UserContext`。
- **落地**：facade = **Spring Security 标准 API**——`@EnableMethodSecurity` + `@PreAuthorize` / `@Secured`；
  `UserContext` 是 `SecurityContextHolder` 的薄适配器（写入 `UsernamePasswordAuthenticationToken`，
  roles → `ROLE_x` 前缀，permissions 原样保存）；`LoadUpUser` 仅承载业务属性。
- **配置**：`loadup.authorization.enabled`（默认 true，关闭后不启用方法安全）、
  `loadup.authorization.default-security-filter-chain`（默认 true，无其他 Security 配置时提供最小过滤链）。
- **边界**：未认证访问受保护方法抛 `AuthenticationCredentialsNotFoundException`
  （继承 `AuthenticationException`）；已认证但权限不足抛 `AccessDeniedException`，由全局异常处理统一映射。

### 5.3 configcenter — P3

- **现状**：Binder/Binding 薄封装 local/nacos/apollo，符合理念。
- **目标**：保持 facade；补能力矩阵；明确与 Spring `Environment` + `@RefreshScope` 的集成边界（`@EnableConfigAutoRefresh` 保留）。
- **动作**：文档化 Nacos ↔ Apollo 的"API 统一"定位与部署拓扑差异。

### 5.4 scheduler — 已完成（JobRunr / Quartz 双 binder）

- **现状**：旧 `@DistributedScheduler` 注解 + SimpleJob / Quartz / XXL-Job / PowerJob 自研 binder
  已全部删除（含 PowerJob / XXL-Job / SimpleJob binder、SchedulerTask 模型与旧自动配置）。
- **落地**：`SchedulerTemplate` + `SchedulerProcessor` facade（任务名分派、按 `taskName` 幂等），
  binder 采用 `binder-jobrunr`（与 retrytask 共用 JobRunr 引擎，官方 starter 管理存储/集群/Dashboard）
  与 `binder-quartz`（Spring Boot starter-quartz，内嵌调度）双实现。
- **语义**：`register` / `delete` / `trigger` / `updateCron` / `getStatus`；cron 更新与删除会清理
  JobRunr 待执行实例，避免旧计划阻塞新计划或删除后残留孤儿执行。
- **集成测试**：JobRunr binder 用 MySQL TestContainer、Quartz binder 用内存 JobStore，同一套
  facade 用例跑两个 binder，证明切换零代码修改。

### 5.5 gateway — P2

- **现状**：自研 MVC 引擎（`HandlerMapping` / `HandlerAdapter` / `DefaultGatewayEngine` / `DefaultFilterChain`）+ HTTP(RestClient) / RPC / bean 三种代理。阻塞、不转发入站 header、限流/熔断为 JVM 本地级。
- **目标 facade**（保留）：`RouteDefinition` + `ProxyProcessor` SPI + `SecurityStrategy` + `GatewayProperties`（已退役 `GatewayFilter` / `FilterChain` 命名 filter 契约）。
- **目标 binder**：`gateway-engine-webmvc` = **Spring Cloud Gateway Server MVC**（`spring-cloud-starter-gateway-server-webmvc`，Spring Cloud 2025.0.x / Gateway 5.x，对应 Boot 4.1）。
  - 路由编译：YAML / DB 路由 → `RouterFunction`，原子快照热刷新
  - `http://` 后端 → `HandlerFunctions.http()` + `uri()` filter
  - `bean://` 后端 → 自定义 `HandlerFunction`（ApplicationContext 按名取 bean 调用）
  - `rpc://` 后端 → 自定义 `HandlerFunction`（Dubbo GenericService）
  - `securityCode` / 限流 / 熔断 / 响应包装 / 追踪 → before / after `HandlerFilterFunction`（复用现有 filter 业务逻辑）
  - 与 `@RestController` 共存：路由统一 `/api/**` 前缀 + `RouterFunctionMapping` order 控制
- **动作**：引入 SCG Server MVC 做最小验证 → 路由编译与热刷新 → filter 适配 → 删除自研引擎 → 集成测试。
- **明确排除**：WebFlux 路线（整个项目为 MVC 模式）。

#### 安全设计（定案）

Gateway 作为标准 **OAuth2 资源服务器**（Servlet 过滤器链），认证与授权分层，`SecurityStrategy`
退化为"路由策略编排"，不再是认证实现：

```
请求 → Spring Security 过滤器链
         BearerTokenAuthenticationFilter + Nimbus JwtDecoder → 标准 SecurityContext
       → gateway SecurityHandlerFilterFunction（securityCode → SecurityStrategy）
         OFF       → 匿名放行 + 清理上下文
         default   → 要求已认证 + 路由级 authorize（SpEL / 权限列表简写，P3）
         signature → 复用 loadup-components-signature 验签（不产生用户身份）
         internal  → IP / 内网头白名单
       → bean 路由（方法级 @PreAuthorize，同一 SecurityContext）
```

- **JWT 统一 Nimbus**：签发与验签全部走 `spring-security-oauth2-jose` 标准 API
  （签发 `NimbusJwtEncoder` + `JwtClaimsSet`，验签 `NimbusJwtDecoder`，HMAC-SHA256）；
  gateway 验签密钥取自 `loadup.gateway.security.secret`，UPMS 签发密钥取自
  `loadup.upms.security.jwt.secret`；自研 `JwtUtils`（jjwt）项目级移除。
- **claims 契约（自包含、无状态）**：`sub`(userId) / `username` / `roles`(数组) / `permissions`(数组)；
  `JwtAuthenticationConverter` 映射 roles → `ROLE_x` + 原始值、permissions → 原始值，principal =
  `LoadUpUser`；权限变更需重新签发（短 TTL + 刷新）。
- **claims 作为 `default` SecurityStrategy**：认证事实由 Spring Security 过滤器链产生，路由策略只做
  强制与判定，不再自己解析 token；`SpringBeanProxyProcessor` 的反射桥接删除。
- **签名与认证分离**：`signature` 只证明请求来源/内容可信，不写入 SecurityContext；手写 HmacSHA256
  替换为复用 `loadup-components-signature`（JCA 薄封装）。
- **资源服务器选择 SPI（已落地）**：`ResourceServerBinder` 扩展点，默认 `nimbus`
  （`loadup.gateway.security.jwk-set-uri` > `issuer-uri` > `secret` 三级选择），后续可加
  Sa-Token 等其他认证后端。
- **路由级授权（已落地）**：`RouteConfig.authorize` 支持完整 SpEL 或逗号分隔权限列表简写
  （编译为 `hasAnyAuthority`），用 Spring Security `WebExpressionAuthorizationManager` 执行；
  401（`SECURITY`）/ 403（`AUTHORIZATION`）统一 JSON。
- **与 authserver 共嵌**：gateway 安全链固定 `@Order(SecurityFilterProperties.DEFAULT_FILTER_ORDER)`，
  SAS 的 `/oauth2/**` 链 order 更低且带 matcher，二者可同进程共存；应用自定义链时可通过
  `loadup.gateway.security.enabled=false` 整体关闭 gateway 默认链。

### 5.6 dfs — P3

- **现状**：Local / DB / S3 binder。
- **目标 facade**：`DfsService`（上传 / 下载 / 删除 / 元数据 / 签名 URL）。
- **目标**：主推 **S3**（MinIO 兼容）；local 仅开发环境；database binder 标记为过渡实现并逐步弃用。
- **动作**：能力矩阵；补 S3 签名 URL 与分片上传；容器测试（LocalStack）。

### 5.7 gotone — 已完成（Mode B：引擎 + 可选存储 + 渠道 binder）

- **落地**：按 Mode B 重构。facade = `NotificationService` + `NotificationChannelProvider`
  SPI；`DefaultNotificationService` 为纯发送引擎（零存储、零 DB），可选存储 SPI
  （`ServiceConfigProvider` / `ChannelConfigProvider` / `RecordHandler`）由 `-store-jdbc`
  （MyBatis-Flex，3 张表，Flyway 自动迁移）实现。
- **渠道 binder**：email（Spring Mail，真实实现）、webhook（JDK HttpClient，真实 HTTP）、
  sms / push（stub，待接厂商 SDK）；resilience4j 按 `gotone-<channel>-<provider>` 实例名
  包装每个 provider（熔断 + 重试 + 降级链）。
- **与 retrytask 复用**：`loadup-components-retrytask-notifier-gotone` 把重试任务永久失败
  转成 gotone 通知告警（配置 `loadup.retrytask.notify.*`），形成失败处理闭环。
- **后续**：补 sms / push 厂商 SDK 真实实现；能力矩阵见模块 README / ARCHITECTURE。

### 5.8 retrytask — 已完成（JobRunr 底座）

- **落地**：以 JobRunr 8.8.2 为底座的 `binder-jobrunr` 已实现并入库；删除自研引擎 / JDBC 存储 / 重试策略 / notifier / 线程池 / 乐观锁 / schema。
- **目标 facade**（保留）：`RetryTaskFacade` + `RetryTaskProcessor`（bizType 注册，处理器抛异常即触发重试）。
- **binder**：`binder-jobrunr`（JobRunr 官方 `spring-boot-4-starter`；获得 dashboard、状态机、集群心跳、死任务找回）。
- **能力**：`bizType + bizId` 幂等（确定性 jobId）、定时执行、`delete` 取消、`reset` 按原参数重跑、状态查询；失败告警通过 `RetryTaskNotifier` SPI + `ApplyStateFilter` 追加（默认日志，可选 gotone 渠道）。

### 5.9 database — P3

- **现状**：MyBatis-Flex + 审计 / ID 生成 / 多租户 / 逻辑删除。
- **目标**：保留（审计、ID、多租户是框架价值）；ORM 选型（MyBatis-Flex vs MyBatis-Plus）列为待决策项。

### 5.10 captcha — P1

- **现状**：已删除 EasyCaptcha fork 源码（原 fork 与 AJ-Captcha 均长期停更，不再采用）。
- **落地**：facade = `CaptchaTemplate`（generate / verify / getBinderType），binder 可插拔：
  `binder-tianai`（tianai-captcha 1.5.5，行为验证码：滑块 / 旋转 / 拼图 / 点选，默认）+
  `binder-nanocaptcha`（nanocaptcha 2.1，传统图像验证码：数字 / 字母 / 中文）。
- **存储**：答案与过期由各引擎侧缓存负责（tianai 本地 `LocalCacheStore`，nanocaptcha 进程内 Map TTL），
  LoadUp 不重复造存储；图像统一返回 base64 data URI。
- **接口暴露**：通过 Gateway `bean://captchaTemplate:generate` 路由，组件不提供 Controller。

### 5.11 signature — P4

- **现状**：JCA 薄封装，符合理念；README 已对齐契约（能力矩阵 + 防重放语义约定）。
- **目标**：保留；网关 HMAC 签名校验对齐业界标准（如 AWS SigV4 风格）或明确定义防重放语义（已约定：`X-App-Id` / `X-Timestamp` / `X-Nonce` / `X-Signature`，时间窗 + nonce 防重放，由 gateway `SignatureSecurityStrategy` 落地）。

### 5.12 common-log / common-tracer / testcontainers — P3

- **现状**：`loadup-commons-log` 统一 Spring Boot console 日志默认格式，约定 `traceId` / `spanId` /
  `requestId` MDC 键；`loadup-commons-tracer` 基于 OpenTelemetry 做薄集成，并将 Span 上下文写入 common-log。
- **目标**：common-log 不绑定具体日志实现、不创建 Span；common-tracer 负责 HTTP / AOP / async 追踪，二者按
  `common-log ← common-tracer` 单向依赖组合。能力矩阵与 README 已落地；testcontainers 保持"共享容器 +
  可切换实际服务"模式。

### 5.13 pipeline / globalunique — P4

- **现状**：pipeline 四阶段 DSL、globalunique 数据库唯一键幂等。
- **目标**：保留（无标准 OSS 直接对应）；pipeline 定位为**轻量进程内编排**，不宣传为工作流引擎；长流程场景再评估 Flowable / Temporal。
- **globalunique 落地**：单一 jar；`GlobalUniqueService` 事务内幂等（INSERT + 唯一键）；表结构含标准字段
  `id / tenant_id / created_at / updated_at / deleted`，MySQL / PostgreSQL / Oracle 三套 Flyway 迁移；
  能力矩阵见模块 README。

### 5.14 upms / config / log 模块 — P2/P3

- **现状**：COLA 4.0 业务模块；UPMS 提供 RBAC3 + JWT + 数据权限。
- **目标**：保留为"框架自带可复用业务能力"；认证后端跟随 authorization 决策（Spring Security 标准实现）；OAuth2 三方登录用 Spring Authorization Server 或厂商 SDK 适配。
- **动作**：UPMS 与 authorization 组件的注解解耦（依赖 facade 而非实现）。

### 5.15 resilience4j — 已完成（标准装配 + 双消费者）

- **现状**：gateway 自研 Caffeine 熔断/令牌桶；gotone 声明了 resilience4j 依赖但零使用；BOM 的
  `resilience4j.version` 声明失效（Spring Cloud 2025.1.x 的 first-declared-wins 覆盖为 2.3.0）。
- **落地**：新增 `loadup-components-resilience4j`（`-api` + `-binder-core`）。facade **直接采用
  Resilience4j 标准 API**（注解 + Registry），不自创平行接口；组件只做装配（registries + aspects +
  Micrometer 指标）。binder 用 `resilience4j-spring6` + 自写 AutoConfiguration，规避官方
  `spring-boot3` starter 对 Boot 4 的未支持风险（issue #2371）。
- **消费者**：gateway 两个手写 filter 替换为 Resilience4j 实现（路由级熔断按上游共享实例、刷新时
  prune；限流 per route+IP、Caffeine 有界缓存防内存膨胀）；gotone 引擎按
  `gotone-<channel>-<provider>` 实例名包装每个 provider（熔断包裹重试循环）。
- **版本**：`resilience4j.version` 对齐 **2.3.0**（与 Spring Cloud 2025.1.x 一致），删除失效声明。
- **后续**：`binder-redis`（分布式熔断/限流状态）为规划扩展点，业务代码零修改。

### 5.16 authserver（授权服务器）— P4

- **定位**：授权服务器独立成组件（Mode A 单后端选择），负责**签发**带 claims 的 JWT；签发与校验解耦
  （gateway 只做校验，不依赖 authserver）。
- **结构**：
  ```
  loadup-components-authserver/
  ├── authserver-api/                  # LoadUpAuthServerProperties + LoadUpJwtTokenCustomizer（标准 OAuth2TokenCustomizer 实现）
  ├── authserver-binder-sas/           # 内嵌 Spring Authorization Server（默认）：RegisteredClientRepository / AuthorizationServerSettings / JWKSource / claims customizer
  ├── authserver-binder-keycloak/      # 外部 IdP issuer-only 对接：issuer / jwk-set-uri → NimbusJwtDecoder
  └── authserver-test/
  ```
- **binder 语义**：`loadup.components.authserver.binder-type: sas | keycloak`（sas 默认）。
  SAS 是内嵌授权服务器（yml 注册 `clients[]`，启动即暴露标准 OAuth2 端点）；Keycloak 只作为
  issuer 对接（配置层，不做 admin API / 客户端管理）；两者共用同一套 claims 契约。
- **claims 定制（已落地）**：`LoadUpJwtTokenCustomizer`（标准 `OAuth2TokenCustomizer<JwtEncodingContext>`）
  把 principal 的 roles（`ROLE_` 前缀剥离）与 permissions 写入 JWT；`/oauth2/token` 端到端
  集成测试通过（client_credentials 签发 + JWK 验签）。
- **依赖方向**：UPMS（认证业务）→ authserver；gateway → 只依赖资源服务器标准装配。
- **UPMS 现状（已标准化）**：登录/刷新通过 UPMS app 层 `TokenService` 用标准 Nimbus
  `JwtEncoder`/`JwtDecoder` 签发（HS256，claims 契约 sub/username/roles/permissions 自包含），
  jjwt 与 `JwtUtils` 已全量移除；接入 SAS 签发（OAuth2TokenGenerator）作为后续演进项，
  接口对集成方不变。
- **切换影响**：SAS ↔ Keycloak 是"内嵌 vs 外部 IdP"的部署决策，业务侧只感知标准 JWT。

---

## 6. 脚手架体验设计

### 6.1 引入方式

```xml
<!-- BOM：统一版本 -->
<dependencyManagement>
  <dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-dependencies</artifactId>
    <version>${loadup.version}</version>
    <type>pom</type>
    <scope>import</scope>
  </dependency>
</dependencyManagement>

<!-- 业务能力：按需选择 -->
<dependency>...loadup-modules-upms-app...</dependency>
<dependency>...loadup-modules-config-app...</dependency>

<!-- 中间件 binder：自由选型，不影响业务代码 -->
<dependency>...loadup-components-cache-binder-redis...</dependency>
<dependency>...loadup-components-scheduler-binder-jobrunr...</dependency>
<dependency>...loadup-components-configcenter-binder-nacos...</dependency>
```

### 6.2 选型手册

每个组件 README 的能力矩阵 + 部署拓扑说明组成"选型手册"，回答三类问题：

1. 我要本地开发怎么配？（默认 binder：caffeine / local / quartz-memory）
2. 我上生产怎么切？（redis / nacos / xxl-job / s3，业务代码零修改）
3. 我要更高级的能力怎么办？（换 binder，如 jetcache / jobrunr，或提 PR 增强 facade）

### 6.3 模板工程

`loadup-application` 重构为**脚手架模板工程**：

- 一个可运行的"最小业务 + 全部默认 binder"示例
- 每个 binder 一个 profile / 配置片段示例
- 新开发者按手册 30 分钟内跑通

---

## 7. 待决策项

| # | 决策项 | 建议 | 影响 |
|---|--------|------|------|
| 1 | 许可证：GPL-3.0 → Apache-2.0 | **Apache-2.0**（已定，license-maven-plugin 自动维护） | 商业二开可行性（已解决） |
| 2 | Cache facade：Spring Cache vs 自研注解 | **Spring Cache** | cache 组件 P1 改造方向 |
| 3 | Authorization 后端：Sa-Token vs Spring Security | **Spring Security**（已定，标准 API 为 facade） | authorization 已重构落地 |
| 4 | RetryTask：自研引擎 vs JobRunr 底座 | **JobRunr**（已落地：binder-jobrunr） | retrytask 路线（已定） |
| 7 | Scheduler：自研多 binder vs JobRunr/Quartz 底座 | **JobRunr（与 retrytask 共用引擎）+ Quartz**（已落地：双 binder） | scheduler 路线（已定） |
| 8 | 容错：自研实现 vs Resilience4j | **Resilience4j**（已落地：组件 + gateway/gotone 双消费者，版本 2.3.0 对齐 Spring Cloud） | 容错路线（已定） |
| 5 | Gateway 引擎替换时机 | 先最小验证 SCG Server MVC | gateway P2 排期 |
| 6 | ORM：MyBatis-Flex vs MyBatis-Plus | 保持 MyBatis-Flex（已投入） | database 组件 |
| 9 | Gateway 认证：自研 JWT vs OAuth2 资源服务器 | **OAuth2 资源服务器 + Nimbus**（已定） | gateway 安全 P1 已实施 |
| 10 | 授权服务器：内嵌 SAS vs 外部 Keycloak | **authserver 组件（Mode A）**：binder-sas 内嵌（默认）/ binder-keycloak issuer-only | authserver P4 已实施（含 /oauth2/token 端到端测试） |
| 11 | JWT claims 契约 | **自包含**：sub/username/roles/permissions 写进 JWT，无状态校验 | claims 契约（已定） |
| 12 | 路由级授权格式 | **Spring Security SpEL 标准** + 逗号分隔权限列表简写（编译为 hasAnyAuthority） | gateway 安全 P3 已实施 |
| 13 | 资源服务器后端选择 | **ResourceServerBinder SPI**（默认 nimbus，预留 Sa-Token 等） | gateway 安全 P5 已实施 |

# LoadUp 架构收敛路线图

> 2026-09 架构调整：Gateway 组件已移除；业务入口使用 Controller。下方 P0–P6 只保留历史背景，当前边界以模块 README 和 DESIGN.md 为准。

## 当前安全模块收敛计划

- [x] UPMS 收敛为凭证校验、登录策略与 RBAC；移除独立 JWT 签发、刷新和无消费方的认证查询桥接。
- [x] SAS 成为内嵌部署的唯一令牌签发方；UPMS 通过可选 `loadup-modules-upms-authserver` 提供用户主体。
- [x] 由通用 Resource Server 校验 issuer、audience 与访问令牌用途；Authorization 仅保留方法安全。
- [x] 应用示例、BOM 和模块 README 迁移到 Controller 与独立安全组件。
- [ ] 增加 UPMS + SAS 授权码与刷新令牌的完整集成验证，覆盖持久化授权数据、多实例密钥与客户端配置来源。
- [ ] 刷新令牌签发新访问令牌时重新读取 UPMS 用户状态与 RBAC，确保禁用账号和撤销权限生效。


> 配套文档：[DESIGN.md](./DESIGN.md)（现行设计总纲与组件规范）。本文记录下一阶段的目标架构、决策顺序、交付计划与验收标准；下方保留 P0–P6 历史路线和完成记录。
> 版本：v3.0（2026-09）

---

## 当前判断与目标

LoadUp 已形成 BOM、标准 API 优先、facade/binder 分离、业务模块按需引入的基本结构。下一阶段的主要风险是能力范围扩张快于契约稳定：组件数量多，跨 binder 的“可切换”承诺不够精确，身份签发职责分散，出站 HTTP 调用需要明确配置边界。先收敛这些边界，再扩展新 binder 和工具链。

**目标定位**：LoadUp 是被集成方消费的框架/SDK，不是必须整体部署的平台。`loadup-application` 只用来验证组合和展示集成方式，不作为生产单元。保持 `commons → components → modules → 集成方` 的单向依赖。

### 目标架构与取舍

| 决策面 | 目标方案 | 不再默认承诺 |
|--------|----------|--------------|
| 产品边界 | BOM 管版本；commons 保持最小基础类型；技术组件、UPMS/Config/Log 均可单独选择。建立“稳定、试验、占位、弃用”目录，新增能力须说明真实消费场景及维护者 | 引入一个 starter 即获得全家桶；为每个组件预建多 binder |
| 能力契约 | 每个组件区分跨 binder 的**基础契约**、仅部分 binder 支持的**扩展能力**及部署差异。只有基础契约承诺换 binder 后业务代码不变；扩展能力通过显式接口或配置选用 | facade 能力并集等于所有 binder 无条件可切换 |
| 身份体系 | UPMS 负责用户、凭证和 RBAC；AuthServer 或外部 IdP 负责签发；通用 Resource Server 负责验证；Authorization 负责方法授权。旧 UPMS 签发路径已移除 | UPMS、authserver、gateway 各自定义令牌语义 |
| 出站 HTTP 调用 | 规划独立客户端组件，配置外部服务地址、认证、超时和操作；不承载入站路由 | 用配置模拟任意业务流程 |
| 模块粒度 | 单实现、低变化能力可用单 jar；有真实替换需求时再拆 API/binder；引擎、存储、starter 只在职责确实独立时拆分 | 所有组件套用同一多层目录模板 |
| 发布契约 | 对公开 Java API、配置键、事件、数据迁移分别定义兼容规则；能力状态与文档、测试、发布物一致 | 仅凭编译通过或 README 中的功能描述判断可发布 |

**决策顺序**：先确定产品边界和能力契约，再确定身份与出站 HTTP 的跨模块契约，最后调整 Maven 模块和实现。若目标方案与 [DESIGN.md](./DESIGN.md) 的“能力并集”或现有 starter 约定冲突，以 R0 的 ADR 决策为准，并在 R1 同步修订 DESIGN.md、AGENTS.md 与模块 README；本文本身不替代现行编码规范。

## 下一阶段计划（R0–R3）

时间按连续工作周估算，阶段入口以前一阶段验收通过为准；不把历史 P0–P6 的周次接续为实际日历承诺。每项未完成任务保持 `[ ]`，只有交付物和验收证据齐备才改为 `[x]`。

| 阶段 | 建议周期 | 核心交付 | 阶段门槛 |
|------|----------|----------|----------|
| R0 边界与决策 | 第 1–2 周 | 模块/依赖地图、能力状态目录、架构决策记录 | 对外承诺和跨模块责任有唯一版本 |
| R1 契约收敛 | 第 3–5 周 | 基础/扩展契约、身份与网关契约、兼容规则 | 代表性组件能按统一模板描述并验证 |
| R2 代表性闭环 | 第 6–10 周 | 一个技术组件和一条 Controller 身份授权消费链 | 集成方按文档独立组合，关键契约有验证证据 |
| R3 发布准备 | 第 11–12 周 | 版本基线、迁移说明、准入检查 | 对外发布范围明确，未成熟能力不会被误认为稳定 |

### R0：边界与决策

- [ ] 盘点所有发布坐标、传递依赖、AutoConfiguration、外部服务要求和真实消费者；把每个能力标为稳定、试验、占位或弃用。记录“单独引入是否可用”和“移除后谁受影响”。
- [ ] 为 cache、scheduler、DFS、gotone、UPMS 各写一页契约清单：业务入口、可替换部分、扩展能力、部署拓扑和已知限制。优先识别“文档承诺强于实现”的地方。
- [ ] 形成 ADR：核心与可选模块的边界；基础契约与扩展能力；唯一令牌签发权威及迁移路径；出站 HTTP 组件边界；何时允许新 SPI/binder。每项记录选项、选择理由、代价和复审触发条件。
- [ ] 对照 ADR 制作依赖方向图和可交付坐标清单，列出需调整的 starter 传递依赖。暂缓没有明确消费场景的新 binder、代码生成器及部署形态扩张。

**验收**：集成方能从一份目录判断“引入哪些坐标、会启动什么、依赖什么外部服务、该能力是否稳定”；跨模块职责无重复所有者；新增模块和 SPI 有书面准入标准。

### R1：契约收敛

- [ ] 修订 DESIGN.md：把“能力并集”表述为可发现的能力集合，同时明确只有基础契约保证跨 binder 一致；扩展能力须显式声明支持范围、失败语义和切换影响。
- [ ] 统一组件 README 契约模板：基础行为、扩展能力矩阵、配置键、外部依赖、部署拓扑、替换步骤及不兼容差异。优先完成 R0 选定的代表组件，再推广到其他稳定组件。
- [ ] 定义身份契约：签发权威、access/refresh 用途、issuer/audience、密钥轮换、权限变更后的生效语义和旧令牌迁移窗口；写清 UPMS、authserver、authorization 的责任边界。
- [ ] 定义出站 HTTP 契约：命名服务与操作配置、认证引用、超时、幂等重试和错误语义；明确配置热更新的边界。
- [ ] 制定兼容政策：公开 API、配置键、数据库迁移和事件分别如何演进、弃用、验证；稳定能力的破坏性变化必须有迁移说明。

**验收**：至少一个 Mode A 组件、一个 Mode B 组件按新模板描述；“更换 binder 不改业务代码”的适用范围可由契约和同一组行为测试判定；身份和出站 HTTP 的所有权经 ADR 固化。

### R2：代表性闭环

- [ ] 选择 cache 的本地/生产 binder 路径作为技术组件样板，验证最小依赖、配置切换、基础契约一致性与扩展能力的显式选择。若 R0 发现其他组件更具代表性，先在 ADR 中说明替换理由。
- [ ] 选择“UPMS 用户与权限数据 → 唯一签发路径 → Resource Server 验证 → Controller 方法授权”作为跨模块样板，验证集成方可以分别选择身份源与 Web 适配模块；把过渡期旧签发路径的退出条件写入迁移方案。
- [ ] 审核样板的 Maven 传递依赖与自动装配：不让可选 binder、外部服务通过通用 starter 意外进入应用；用最小消费工程验证组合行为。
- [ ] 将样板中的契约检查、架构约束与文档一致性检查沉淀为可复用发布门槛，再按风险顺序推广到其余稳定能力。

**验收**：两个样板均有可复现的集成说明和验证证据；基础能力跨 binder 的行为一致；身份令牌的签发、验证、授权语义一致；单独引入不触发无关组件的自动装配。此阶段不以“所有组件全部重构”为完成标准。

### R3：发布准备

- [ ] 发布坐标逐一确认状态；占位能力不能按稳定能力宣传，试验能力显式告知兼容限制。根 README、模块 README、BOM 与示例应用使用同一套坐标和术语。
- [ ] 提供一份最小接入示例和一份可选组合示例，验证新开发者可在 30 分钟内完成基础接入；记录实际卡点并修正文档或默认配置。
- [ ] 为 v1.0 候选版建立发布清单：稳定范围、兼容承诺、升级说明、已知限制及后续路线。只有稳定范围满足 R1/R2 的门槛才讨论发布；高级 binder 和代码生成器继续按真实需求排期。

**验收**：消费者可以只按公开文档完成接入与升级；发布清单中的每项稳定承诺有对应验证证据；未完成项有明确状态和后续负责人。

### 关键风险与处理原则

| 风险 | 处理原则 |
|------|----------|
| 收敛契约影响已有集成方 | R0 先盘点公开用法；R1 给出兼容窗口和迁移说明；R2 用消费工程验证旧用法或明确弃用路径 |
| 单一签发权威需要改动 UPMS 与 authserver | 先固定令牌和权限语义，再迁移签发实现；旧路径退出以真实消费者迁移完成为条件 |
| 扩展能力从通用 facade 拆出后增加选择成本 | 保留统一能力发现和文档入口；只有基础契约承担零代码切换承诺 |
| 12 周估算受依赖与外部服务影响 | 阶段以验收证据推进；R2 只选代表性闭环，其余组件按稳定性和使用量另排期 |

## Gateway 移除与出站 HTTP 组件规划

- [x] 从聚合 POM、BOM 和应用依赖中移除 Gateway；业务入口使用 Spring MVC Controller。
- [x] UPMS 预置 HTTP 接口进入可选 `loadup-modules-upms-web`，核心服务保持独立。
- [ ] 实现命名外部服务和操作配置，支持地址、方法、路径、认证引用、超时与默认请求头。
- [ ] 以 Spring `RestClient` 和 JDK `HttpClient` 构建同步出站客户端；根据真实负载评估 Apache HttpClient/OkHttp 是否必要。
- [ ] 验证请求变量编码、敏感头脱敏、超时、4xx/5xx 错误映射和幂等操作的重试限制。

**验收**：不引入新 HTTP 组件时 Controller 正常工作；引入后可通过修改外部服务配置切换目标地址，无需改写业务调用代码。当前只完成 Gateway 移除，HTTP 组件尚未实现。

## 历史路线（P0–P6，保留原完成记录）

以下是原“脚手架化、去自研化”计划的记录。原计划中的目标和勾选状态不自动代表新的稳定性评级；新工作的优先级与验收以 R0–R3 为准。

### 原计划总览

路线目标：把 LoadUp 从"自研组件集合"重构为"**底层 OSS + 薄集成**的可配置脚手架"，最终达到：

- 业务代码只依赖 facade（尽量是业界标准 API）
- 中间件选型 = 换 binder 依赖 + 配置，业务代码零修改
- 新开发者 30 分钟内跑通一个可切换中间件的示例

原计划首先定义了五个阶段，随后增加了 Gateway 安全与 UPMS 签发两个阶段：

| 阶段 | 主题 | 周期 | 关键交付 |
|------|------|------|----------|
| P0 | 契约与决策 | 第 1-2 周 | 设计规范落地 AGENTS.md、能力矩阵模板、决策项拍板 |
| P1 | 去自研化 | 第 3-8 周 | captcha / cache / authorization / retrytask / gotone 改造 |
| P2 | Gateway 引擎替换 | 第 9-16 周 | 基于 SCG Server MVC 的 loadup-gateway |
| P3 | 契约落地与脚手架化 | 第 17-24 周 | 能力矩阵全覆盖、binder 容器测试、模板工程 |
| P4 | 演进 | 第 25-40 周 | 可选 binder（jetcache/jobrunr）、指标、代码生成器、v1.0 |

---

### Phase 0：契约与决策（第 1-2 周）

**目标**：把设计理念固化为项目规范，P1 启动前所有决策项有结论。

任务：

- [ ] 将 [DESIGN.md](./DESIGN.md) 第 2、3、4 节要点并入 `AGENTS.md`（设计原则 + 约束执行机制 + facade/binder 铁律 + 能力矩阵要求）
- [x] 制定能力矩阵模板，并在 3 个代表组件（cache / scheduler / dfs）README 落地示范（cache / scheduler 已完成，dfs 待补）
- [ ] 决策项拍板（DESIGN.md 第 7 节）：ORM（Cache facade 已定 Spring Cache，Authorization 已定 Spring Security，许可证已定 Apache-2.0，RetryTask 已定 JobRunr）
- [ ] CI 增加文档一致性检查（能力矩阵存在性、README 许可证标识一致性）
- [ ] 根 pom 引入 `maven-enforcer-plugin` `bannedDependencies`（业务模块禁止依赖 binder / 中间件坐标），binder 模块与集成方工程豁免
- [ ] `loadup-testify` 增加 ArchUnit 测试基类，在代表模块落地示例并接入 CI

**验收**：决策记录归档；AGENTS.md 包含组件契约章节；3 个组件 README 有契约表；Enforcer 与 ArchUnit 示例在 CI 生效（故意引入违规依赖/import 会被阻断）。

**风险**：许可证决策可能涉及法务，需所有者尽早介入。

---

### Phase 1：去自研化（第 3-8 周）

**目标**：把"自己在实现成熟标准"的组件收敛到标准之上，低风险高收益。

任务：

- [x] **captcha 去 fork**：删除 EasyCaptcha fork 代码；facade = `CaptchaTemplate`（generate / verify）；`binder-tianai`（行为验证码，默认）+ `binder-nanocaptcha`（传统图像）落地，容器测试验证引擎切换零代码修改
- [x] **cache 迁移 Spring Cache**：删除自研 `CacheBinding` / `CacheTemplate` / `CacheProvider` 体系；facade = Spring Cache 注解 + `loadup.cache.*` 增量配置（按 cache name 的 TTL / 空值 / 随机过期）；caffeine / redis / jetcache 三 binder 重写；容器测试证明切换零代码修改
- [x] **authorization 迁移 Spring Security**：删除自研 `@RequireRole` / `@RequirePermission` AOP；facade = Spring Security 标准 API（`@EnableMethodSecurity` + `@PreAuthorize`）；`UserContext` 委托 `SecurityContextHolder`；补方法安全测试
- [x] **retrytask 迁移 JobRunr**：删除自研引擎/JDBC 存储；facade 保留，`binder-jobrunr` 落地（幂等、定时、取消、失败重跑、状态查询、失败告警）；集成测试验证业务代码零修改
- [x] **scheduler 去自研化**：删除 `@DistributedScheduler` / SimpleJob / Quartz / XXL-Job / PowerJob 旧 binder；facade = `SchedulerTemplate` + `SchedulerProcessor`；`binder-jobrunr`（与 retrytask 共用引擎）+ `binder-quartz` 落地；JobRunr/Quartz 双 binder 集成测试验证切换零代码修改
- [x] **resilience4j 落地**：新增 `loadup-components-resilience4j`（api + binder-core，标准 Resilience4j API 为 facade）；gateway 手写熔断/限流 filter 替换为 Resilience4j 实现；gotone 引擎按 provider 接入熔断 + 重试；BOM 版本对齐 Spring Cloud 管理的 2.3.0
- [x] **gotone 去自研化重构（Mode B）**：`NotificationService` facade + `NotificationChannelProvider`
  SPI；纯发送引擎（零存储）+ 可选 `store-jdbc`；email（Spring Mail）/ webhook（真实 HTTP）
  落地，sms / push 为桩；resilience4j 按 provider 接入熔断 + 重试
- [x] **retrytask 失败告警复用 gotone**：`RetryTaskNotifier` SPI + `notifier-gotone` 模块，
  永久失败自动走 serviceCode 渠道告警

**验收**：

- captcha / cache / authorization 无自研平行实现
- cache 与 retrytask 提供"切换 binder 业务代码零修改"的集成测试用例
- gotone 至少 2 个渠道可用并有测试

**风险**：UPMS 与 authorization 注解深度耦合，改造时需同步回归 UPMS。

---

### Phase 2：Gateway 移除（当前）

- [x] 删除自研路由引擎、`GatewayExpose` 和仅传递 SCG 依赖的 starter。
- [x] 从聚合 POM 与 BOM 移除 Gateway，UPMS HTTP 入口迁到可选 Web 适配模块。
- [ ] 出站 HTTP 客户端按上方计划独立实现，不复用入站路由模型。

---

### Phase 3：契约落地与脚手架化（第 17-24 周）

**目标**：全部组件满足 DESIGN.md 契约，脚手架体验成型。

任务：

- [ ] 全部组件 README 补齐能力矩阵 + 部署拓扑说明
- [ ] 全部 binder 补齐真容器集成测试（Testcontainers），验证行为一致性
- [ ] 全部模块 `ArchitectureTest` 覆盖（防绕过规则进 CI），中间件坐标黑名单随新增 binder 同步维护
- [ ] `loadup-application` 重构为脚手架模板工程：最小业务 + 默认 binder 示例 + 每个 binder 的 profile/配置片段
- [ ] 编写"选型手册"（本地开发怎么配 / 上生产怎么切 / 高级能力怎么换）
- [ ] 运行 `/docs-sync` 同步文档站
- [x] 修复 gotone / retrytask 组件 README 与代码脱节问题（其余组件继续排查）
- [x] tracer / signature 契约落地：README 能力矩阵 + 代码规范对齐（英文注释、显式 Bean 装配）
- [x] globalunique 重构：切换 `GlobalUniqueTemplate` + MyBatis-Flex，复用 database 的 ID / 审计 /
  逻辑删除 / 多租户；唯一维度统一为 `tenant_id + biz_type + unique_key`，删除 JDBC 方言和动态表名代码，
  由 MySQL Testcontainers 验证并发、事务回滚与租户隔离

**验收**：

- 每个组件 README 有契约表，binder 有容器测试
- 新开发者按手册 30 分钟内跑通"本地 Caffeine → 生产 Redis"的切换示例

---

### Phase 4：演进（第 25-40 周）

**目标**：按需扩展 binder 与工具链，发布 v1.0。

任务：

- [ ] 可选 binder 落地：cache-binder-jetcache（多级 + 异步刷新）、configcenter-binder-apollo 增强（实时推送写回）
- [ ] Micrometer 指标统一暴露（cache 命中率、gateway 路由、retry 次数等）
- [ ] 代码生成器（可选）：基于模板工程生成业务模块骨架
- [ ] 评估独立网关部署单元（仅当真实网关场景出现，MVC 组件不受影响）
- [ ] 社区反馈迭代，发布 v1.0

---

### Phase 5：身份与入口治理职责收敛

- [x] UPMS 负责凭证校验与 RBAC 数据；AuthServer 负责 OAuth2 令牌签发。
- [x] Resource Server 独立负责 Bearer 验签和身份上下文，Authorization 独立负责方法级授权。
- [x] Gateway 已移除；Controller 使用应用的 Spring Security 配置。
- [ ] 针对真实场景补充请求签名、限流和熔断的独立集成方案，不要求使用 Gateway。

**验收**：普通 Controller 无需 Gateway 即可组合认证与方法授权。

### Phase 6：UPMS 签发标准化（Nimbus）

**目标**：UPMS 登录/刷新从 jjwt 自研签发迁移到标准 Nimbus JOSE API，与 gateway 验签、
authserver claims 契约完全对齐，删除 jjwt 全部残留。

任务：

- [x] **P1 TokenService**：UPMS app 层新增 `TokenService`（`NimbusJwtEncoder` +
  `NimbusJwtDecoder`，HS256，密钥 `loadup.upms.security.jwt.secret`），
  `issueAccessToken` / `issueRefreshToken` / `parseRefreshToken`（签名 + 过期校验）
- [x] **P2 认证服务迁移**：`AuthenticationServiceImpl` 登录/刷新全部走 `TokenService`；
  登录同时签发 access + refresh token（补齐原 TODO）；claims 契约
  （sub/username/roles/permissions）保持不变
- [x] **P3 清理**：删除 `JwtUtils`（commons-util）与 jjwt 依赖（BOM / commons-util /
  upms-infrastructure）
- [x] **P4 测试**：`TokenServiceTest` 覆盖签发契约、刷新校验、过期/篡改拒绝、弱密钥拒绝

**验收**：UPMS 签发/校验 100% 基于 `spring-security-oauth2-jose`，项目内无 jjwt 引用；
TokenService 单测通过；`mvn clean install -DskipTests` 全量编译通过。

---

### 原计划风险总表

| 风险 | 等级 | 缓解 |
|------|------|------|
| GPL-3.0 许可证阻碍商业采用 | 高 | P0 决策项 #1（已解决：切换 Apache-2.0，license-maven-plugin 自动维护） |
| SCG Server MVC 较新，能力子集 | 中 | P2 前先最小验证 + 能力矩阵对齐 |
| UPMS 与 authorization 深度耦合 | 中 | P1 同步回归；facade 解耦 |
| 文档与代码脱节（历史问题） | 中 | 契约表 + CI 检查 |
| binder 行为不一致导致切换踩坑 | 中 | 2.8 真容器测试契约 |

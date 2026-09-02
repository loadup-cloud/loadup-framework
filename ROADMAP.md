# LoadUp 脚手架化路线图

> 配套文档：[DESIGN.md](./DESIGN.md)（设计总纲与组件规范）。本文只含分期计划、验收标准与风险。
> 版本：v2.0（2026-08）

---

## 总览

路线目标：把 LoadUp 从"自研组件集合"重构为"**底层 OSS + 薄集成**的可配置脚手架"，最终达到：

- 业务代码只依赖 facade（尽量是业界标准 API）
- 中间件选型 = 换 binder 依赖 + 配置，业务代码零修改
- 新开发者 30 分钟内跑通一个可切换中间件的示例

五个阶段：

| 阶段 | 主题 | 周期 | 关键交付 |
|------|------|------|----------|
| P0 | 契约与决策 | 第 1-2 周 | 设计规范落地 AGENTS.md、能力矩阵模板、决策项拍板 |
| P1 | 去自研化 | 第 3-8 周 | captcha / cache / authorization / retrytask / gotone 改造 |
| P2 | Gateway 引擎替换 | 第 9-16 周 | 基于 SCG Server MVC 的 loadup-gateway |
| P3 | 契约落地与脚手架化 | 第 17-24 周 | 能力矩阵全覆盖、binder 容器测试、模板工程 |
| P4 | 演进 | 第 25-40 周 | 可选 binder（jetcache/jobrunr）、指标、代码生成器、v1.0 |

---

## Phase 0：契约与决策（第 1-2 周）

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

## Phase 1：去自研化（第 3-8 周）

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

## Phase 2：Gateway 引擎替换（第 9-16 周）

**目标**：loadup-gateway 的自研引擎退役，改为 Spring Cloud Gateway Server MVC 之上的薄适配。**明确排除 WebFlux 路线**（项目整体为 MVC 模式）。

任务：

- [x] **最小验证**：在 `loadup-application` 引入 `spring-cloud-starter-gateway-server-webmvc`（Spring Cloud 2025.0.x / Gateway 5.x），用一条 YAML 路由跑通 `bean://` 调用，验证 Boot 4.1 兼容性与 RouterFunction 动态注册
- [x] **路由编译**：YAML / DB 路由 → `RouterFunction` 原子快照；`YamlRouteStore` / `DatabaseRouteStore` 刷新时热替换
- [x] **后端 HandlerFunction**：`http://` → HTTP proxy 插件；`bean://` → 容器按名取 bean 调用；`rpc://` → Dubbo GenericService
- [x] **filter 适配**：`securityCode`（JWT/HMAC/internal/OFF）、限流、熔断（Resilience4j 底座）、响应包装、追踪 → SCG MVC HandlerFilterFunction 固定管线
- [x] **共存与迁移**：路由统一 `/api/**` 前缀，与现有 `@RestController` 共存；存量 routes 配置已迁移（移除命名 filters，改用固定管线 + securityCode）
- [ ] **测试**：MockMvc 集成测试已覆盖路由匹配 / bean 调用 / 响应包装 / 404；Testify + Testcontainers 完整集成测试待补
- [x] **清理**：删除 `loadup-gateway-core` 模块（`DefaultGatewayEngine` / `DefaultFilterChain` / 自研 `HandlerMapping` / `HandlerAdapter` 及旧引擎单测），并清理 facade 中仅旧引擎使用的 `GatewayFilter` / `FilterChain` / `GatewayPlugin` / `PathPattern`

**验收**：

- 存量 YAML/DB 路由全部在 SCG Server MVC 上跑通，行为一致
- 热刷新 ≤ 5 秒（保持现状能力）
- 自研引擎代码删除，`loadup-gateway-core` 模块整体移除，facade 只保留 `RouteStore` / `ProxyProcessor` / `SecurityStrategy` / 模型与异常契约

**风险**：SCG Server MVC 较新，部分 WebFlux 版 filter 无 MVC 对应实现——先做能力矩阵对齐，缺失的用自研 filter（限流/熔断本来就是自研）补齐。

---

## Phase 3：契约落地与脚手架化（第 17-24 周）

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

## Phase 4：演进（第 25-40 周）

**目标**：按需扩展 binder 与工具链，发布 v1.0。

任务：

- [ ] 可选 binder 落地：cache-binder-jetcache（多级 + 异步刷新）、configcenter-binder-apollo 增强（实时推送写回）
- [ ] Micrometer 指标统一暴露（cache 命中率、gateway 路由、retry 次数等）
- [ ] 代码生成器（可选）：基于模板工程生成业务模块骨架
- [ ] 评估独立网关部署单元（仅当真实网关场景出现，MVC 组件不受影响）
- [ ] 社区反馈迭代，发布 v1.0

---

## Phase 5：Gateway 安全标准化（OAuth2 资源服务器 + authserver）

**目标**：gateway 从自研 JWT 校验升级为标准 Spring Security OAuth2 资源服务器（Nimbus），
认证/授权分层；授权服务器独立成组件（SAS 内嵌 / Keycloak issuer-only）。

任务：

- [x] **方案定案**：Nimbus 统一 JWT、claims 自包含（sub/username/roles/permissions）、
  claims 作为 gateway `default` SecurityStrategy、签名复用 signature 组件（决策已记录于 DESIGN.md）
- [x] **P1 资源服务器化**：gateway-webmvc 引入 `spring-boot-starter-security` +
  `spring-security-oauth2-resource-server`（+ `-jose` 显式声明）；`GatewaySecurityAutoConfiguration`
  （无状态链 + `NimbusJwtDecoder` HMAC + `LoadUpJwtAuthenticationConverter`，claims →
  `LoadUpUser` principal + `ROLE_x`/permission authorities）；`DefaultSecurityStrategy` 改为基于
  SecurityContext 强制认证；删除 `SpringBeanProxyProcessor` 反射桥接；authorization 默认链
  `@ConditionalOnMissingBean(SecurityFilterChain)`；修复 SCG body 缓存被消费后二次读取为空的问题；
  集成测试覆盖无 token 401 / 有效 token bean 路由 / 方法级 `@PreAuthorize` 权限放行
- [x] **P2 签名整合**：`SignatureSecurityStrategy` 改调 `loadup-components-signature`
  （`DigestService.hmac`，协议不变），删除手写 HmacSHA256，签名与认证职责分离
- [x] **P3 路由级授权**：`RouteConfig.authorize`（YAML/DB 两 store 均支持）——
  完整 SpEL（`hasRole('ADMIN')`）或逗号权限列表简写 → `hasAnyAuthority`，用 Spring Security
  标准 `WebExpressionAuthorizationManager` 执行；`forbidden` → 403（`AUTHORIZATION`），
  `unauthorized` → 401（`SECURITY`），统一 JSON
- [x] **P4 authserver 组件**：`loadup-components-authserver`（api + binder-sas + binder-keycloak + test）；
  binder-sas 内嵌 Spring Authorization Server（`/oauth2/token` 实测通过），yml 注册客户端 +
  `LoadUpJwtTokenCustomizer` 写入 roles/permissions/username；UPMS 登录/刷新已按 claims 契约
  签发（roles + permissions 自包含）；BOM 注册全部坐标
- [x] **P5 Keycloak issuer-only + SPI**：binder-keycloak 只装配 `issuer` / `jwk-set-uri` 的
  `NimbusJwtDecoder`；gateway 新增 `ResourceServerBinder` SPI（默认 nimbus，预留 Sa-Token），
  `loadup.gateway.security.issuer-uri / jwk-set-uri / secret` 三级选择

**验收**：

- 带有效 JWT 的 bean 路由正常调用，SecurityContext 含 roles + permissions，方法级 `@PreAuthorize`
  的 `hasAuthority` 生效
- 无 token / 过期 token / 坏 token 在 protected 路由返回 401；OFF 路由匿名放行
- 路由级 `authorize`：无 token → 401，权限不足 → 403，权限满足 → 200（SpEL 与简写双写法覆盖）
- signature / internal 策略行为与改造前一致
- `SasAuthServerIntegrationTest`：`/oauth2/token`（client_credentials）签发 JWT 且可用
  authserver JWK 验签通过，issuer/claims 契约正确
- `JwtUtils`（jjwt）项目级移除，UPMS 签发统一 Nimbus 标准 API（TokenService）

**验收**：v1.0 发布，含完整契约文档、模板工程、至少一个"高级 binder"示例。

---

## Phase 6：UPMS 签发标准化（Nimbus）

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

## 风险总表

| 风险 | 等级 | 缓解 |
|------|------|------|
| GPL-3.0 许可证阻碍商业采用 | 高 | P0 决策项 #1（已解决：切换 Apache-2.0，license-maven-plugin 自动维护） |
| SCG Server MVC 较新，能力子集 | 中 | P2 前先最小验证 + 能力矩阵对齐 |
| UPMS 与 authorization 深度耦合 | 中 | P1 同步回归；facade 解耦 |
| 文档与代码脱节（历史问题） | 中 | 契约表 + CI 检查 |
| binder 行为不一致导致切换踩坑 | 中 | 2.8 真容器测试契约 |

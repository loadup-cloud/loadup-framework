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
- [ ] 制定能力矩阵模板，并在 3 个代表组件（cache / scheduler / dfs）README 落地示范
- [ ] 决策项拍板（DESIGN.md 第 7 节）：Cache facade、Authorization 后端、ORM（许可证已定 Apache-2.0，RetryTask 已定 JobRunr）
- [ ] CI 增加文档一致性检查（能力矩阵存在性、README 许可证标识一致性）
- [ ] 根 pom 引入 `maven-enforcer-plugin` `bannedDependencies`（业务模块禁止依赖 binder / 中间件坐标），binder 模块与集成方工程豁免
- [ ] `loadup-testify` 增加 ArchUnit 测试基类，在代表模块落地示例并接入 CI

**验收**：决策记录归档；AGENTS.md 包含组件契约章节；3 个组件 README 有契约表；Enforcer 与 ArchUnit 示例在 CI 生效（故意引入违规依赖/import 会被阻断）。

**风险**：许可证决策可能涉及法务，需所有者尽早介入。

---

## Phase 1：去自研化（第 3-8 周）

**目标**：把"自己在实现成熟标准"的组件收敛到标准之上，低风险高收益。

任务：

- [ ] **captcha 去 fork**：删除 fork 代码，依赖 `com.pig4cloud.plugin:easy-captcha`；LoadUp 只保留验证码存储（Redis/本地）与接口封装
- [x] **cache 迁移 Spring Cache**：删除自研 `CacheBinding` / `CacheTemplate` / `CacheProvider` 体系；facade = Spring Cache 注解 + `loadup.cache.*` 增量配置（按 cache name 的 TTL / 空值 / 随机过期）；caffeine / redis / jetcache 三 binder 重写；容器测试证明切换零代码修改
- [ ] **authorization 委托底层**：抽象 `PermissionChecker` SPI；默认适配 Sa-Token；`UserContext` 改 `TransmittableThreadLocal`；补异步上下文测试
- [x] **retrytask 迁移 JobRunr**：删除自研引擎/JDBC 存储；facade 保留，`binder-jobrunr` 落地（幂等、定时、取消、失败重跑、状态查询、失败告警）；集成测试验证业务代码零修改
- [ ] **gotone 补渠道**：实现 email（Spring Mail）/ sms / push / webhook channel，桩 + 容器测试

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
- [x] **filter 适配**：`securityCode`（JWT/HMAC/internal/OFF）、限流、熔断、响应包装、追踪 → SCG MVC HandlerFilterFunction 固定管线
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
- [ ] 修复各组件 README 与代码脱节问题（gotone、scheduler 许可证标识等）

**验收**：

- 每个组件 README 有契约表，binder 有容器测试
- 新开发者按手册 30 分钟内跑通"本地 Caffeine → 生产 Redis"的切换示例

---

## Phase 4：演进（第 25-40 周）

**目标**：按需扩展 binder 与工具链，发布 v1.0。

任务：

- [ ] 可选 binder 落地：cache-binder-jetcache（多级 + 异步刷新）、authorization-binder-spring-security
- [ ] Micrometer 指标统一暴露（cache 命中率、gateway 路由、retry 次数等）
- [ ] 代码生成器（可选）：基于模板工程生成业务模块骨架
- [ ] 评估独立网关部署单元（仅当真实网关场景出现，MVC 组件不受影响）
- [ ] 社区反馈迭代，发布 v1.0

**验收**：v1.0 发布，含完整契约文档、模板工程、至少一个"高级 binder"示例。

---

## 风险总表

| 风险 | 等级 | 缓解 |
|------|------|------|
| GPL-3.0 许可证阻碍商业采用 | 高 | P0 决策项 #1（已解决：切换 Apache-2.0，license-maven-plugin 自动维护） |
| SCG Server MVC 较新，能力子集 | 中 | P2 前先最小验证 + 能力矩阵对齐 |
| UPMS 与 authorization 深度耦合 | 中 | P1 同步回归；facade 解耦 |
| 文档与代码脱节（历史问题） | 中 | 契约表 + CI 检查 |
| binder 行为不一致导致切换踩坑 | 中 | 2.8 真容器测试契约 |

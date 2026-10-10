# LoadUp Roadmap

本文只记录尚未完成的工作。每完成一项任务立即更新：移除已完成的实现项，未执行的运行验证单独保留。已实现的能力、接入方式和设计边界以各 Maven 模块的 `README.md`、`ARCHITECTURE.md` 为准。LoadUp 是通过 BOM 按需引入的框架；`loadup-application` 用于本地集成验证。

## 当前建设目标与实施顺序

面向支付宝、微信支付、银联的商户聚合收单应用，优先补齐通用技术能力。收单业务放在独立消费工程中；支付状态机、渠道协议、退款、商户通知任务、费用与对账规则由业务工程负责。当前范围不包含用户余额钱包、充值、内部转账及提现。

首批交付顺序为 **Outbox → HTTP → 两者组合验证**；随后完善开放接口安全、外部协议隔离和生产运行能力。下面列出的验收条件为后续实现的完成标准，不代表已经执行验证。

## P1 · 统一模块开发契约实施

2026-10-11 MyBatis-Flex APT 配置已移入 database processor 的 Java 固定配置，删除根配置文件和手写空 Mapper，统一生成 XxxDOMapper、Tables 与 TableDef；11 个 DO 所属模块及相关依赖定向 clean package 成功，生成器编译测试及六个业务模块自动装配测试共 20 项通过，含下游主源码与测试源码编译。真实数据库运行验收仍保留为待办。

2026-10-10 已将 JsonUtil 与 MultiDateDeserializer 从 commons-util 归并到 commons-json，更新 WebMVC/Nacos 引用并移除 util 的直接 Jackson 依赖。相关 11 个模块的 Maven clean package 成功，含测试源码编译；测试未执行。

多格式日期输入已补齐 Date、LocalDate、LocalDateTime：严格完整解析、常用分隔符/紧凑格式/小数秒、Date 时区与毫秒时间戳，以及显式字段 JsonFormat。保持固定输出格式，拒绝 Local 类型的有损转换；2026-10-10 定向 Maven clean test 通过全部 30 个日期测试，包含 WebMVC 在内的 9 个相关模块编译成功。真实 HTTP 接入验收仍待执行。

database 固定默认配置、Tables APT 和五个业务仓储的 MyBatis-Flex 迁移已编写；新增迁移补齐标准字段，任务执行与文件清理恢复可信租户上下文。商户隐私合并与 UPMS DTO 映射已移入 Spring MapStruct Converter；Facade、显式响应、OpenAPI 和独立 commons-json 均已提供。工具选择规范已写入 AGENTS，未为无收益的替换引入额外抽象。2026-10-10 全量 141 个模块执行 `mvn clean package -DskipTests -Dskip.spotless=true -Dskip.spotbugs=true` 成功，含主源码、测试源码及 APT 生成代码编译；未执行测试、格式或静态分析门禁。

- [ ] 定向执行 commons-json、database guard、MapStruct 与 Facade 自动装配测试；在真实 MySQL/WebMVC 环境验证迁移、锁、幂等、OpenAPI 与响应报文，并执行发布所需格式与静态分析门禁。

## P1 · COLA 统一分层与业务配置验收

审计、字典、文件、通知和导入导出已拆分为业务聚合目录内的 client/domain/infrastructure/app/web/test；协议、领域 Gateway、持久化装配、MapStruct、BOM 和接入引用已调整。业务配置统一为 `loadup.modules.<module>.*`，启停使用 `enabled`，既有 UPMS/Contract/Merchant 配置同步迁移。

- [ ] 在真实 MySQL 与 WebMVC 环境验证迁移组合、Gateway 覆盖、模块整体开关与按依赖装配 Web、请求/响应投影和既有 Router.http；检查消费工程/配置中心已迁移到新的 modules 前缀。

## P1 · 金额基础能力验收

Money、CurrencyEnum（JDK25目录233项）、MoneyUtil 和 MoneyFormatter 已提供，接入见 [commons-util README](loadup-commons/loadup-commons-util/README.md)。

- [ ] 定向执行 MoneyTest：币种精度、越界、异币种、舍入、格式化、目录完整性及 Jackson3 往返；测试源码已通过全量编译，尚未运行。
- [ ] 消费工程明确渠道可用币种、计费舍入、金额上限与跨端整数表示；JDK 升级时核对币种目录和历史精度变化。

## P1 · 商户信息管理后续验收

[Merchant 模块](loadup-modules/loadup-modules-merchant/README.md) 已提供 COLA 资料管理、MySQL/Flyway、租户隔离、乐观版本、启停、Web API、脱敏输出和管理页面，merchant-contract 适配将持久基本事实供合约使用。单测/真实 MySQL 组合 IT 源码已写，尚未运行。

- [ ] 用户定向执行 MerchantBasicInfoTest、MerchantFactsTest 和 MerchantContractIT；验证 Boot/MapStruct 装配、资料唯一/隔离/更新、可信资料签约、停用和副本路由语义。
- [ ] 执行前端类型/构建/交互与真实 WebMVC 脱敏验收，补执行既有数据库的商户权限种子并重新登录，运行 Router.http。
- [ ] 消费工程明确行业/地区目录、资质审核与商户数据访问范围；基本资料不代表 KYC 或渠道入网。按实际业务接入可靠审计、历史资料、银行账户/渠道商户号与门店。

## P1 · 产品目录、销售方案与商户合约

详细设计见 [Contract ARCHITECTURE](loadup-modules/loadup-modules-contract/ARCHITECTURE.md)。领域、client、租户 Gateway、MySQL/Flyway、app 自动装配、幂等签约、生命周期、管理 API、权限/OpenAPI 和前端页面已提供。页面已接入目录保存/发布/下架、服务端签约预览/签约/查询/状态接口；目录发布按钮的未保存判断已修正为忽略对象字段顺序，销售方案保存前会提示至少需要一个默认选中的产品项；业务 Controller 的 `/api` 路由前缀已统一归属 `loadup-components-webmvc`，本地 UPMS 种子已补七类权限，未启用多租户时由 database 组件为请求与新数据统一使用默认租户 ID；Java 主源码及测试源码已通过全量编译，尚未运行测试或前端构建。

- [ ] 定向执行 ContractFlowTest、ConditionEvaluatorTest、ContractCodecTest、ContractAutoConfigurationTest 和 ContractPersistenceIT；验证 Jackson3/MapStruct/Boot 装配（REGISTER_BEAN 条件与组件扫描冲突已通过显式 Import 修复，待运行回归）、真实 MySQL 并发、租户隔离、回滚和迁移组合，记录结果。
- [ ] 多租户消费工程接入可信租户解析，消费工程验收 Merchant 默认事实适配、主库路由及商户/合约方法权限；已有本地数据库补执行 Contract 授权语句，重新登录并执行 Router.http 与鉴权/响应回归。
- [ ] 已有单租户数据若使用旧的 `__loadup_global__` GlobalUnique 范围，切换统一默认租户 ID 前核对并迁移历史声明，避免重放查询遗漏。
- [ ] 实现受限 JSON Schema 转换及更完整的动态表单；Contract 的 7 个 Vue 模板已通过定向解析，用户本地仍需执行前端类型检查、构建与页面交互联调。
- [ ] 实现内容摘要绑定审批、修订生效安排与历史，事务锁定稳定合约/范围行防止区间重叠，不修改旧条款。
- [ ] 同事务接入合约可靠审计和 Outbox，补充不可变快照缓存、低基数指标与受控 explain。
- [ ] 补充并执行真实 MySQL 多进程故障恢复、重复/乱序事件与历史支付条款绑定验收。

## P0 · Outbox 首版后续验证与运行完善

首版代码、自动装配、BOM、迁移、README 和 ARCHITECTURE 已提供：同事务发布、MySQL 多实例领取、租约恢复、有限退避重试、失败查询、人工重放、Inbox 去重及观测。接入契约以 [Outbox README](loadup-components/reliability/loadup-components-outbox/README.md) 为准。

- [ ] 在真实消费工程执行 `OutboxIT` 并记录结果；现有用例已覆盖事务回滚、并发争抢、旧领取者确认、租约恢复、Inbox 回滚和失败重放，尚未执行。
- [ ] 补充并执行多进程退出/重启故障演练，验证真实业务提交后的事件恢复；验证多节点时钟、长处理器超过租约以及实际下游幂等行为。
- [ ] 设置事件积压、最老事件年龄、最终失败和租约丢失告警；验证高并发领取索引和数据库采集成本。
- [ ] 根据业务保留要求实现成功事件归档/清理及 Inbox 保留机制，防止过早删除去重记录后旧事件重放产生重复结果。

## P0 · HTTP 首版后续验证与集成

首版采用 Spring RestClient + Apache HttpClient 5，提供命名客户端/操作、URI 编码、JSON/原始报文、受限下载、凭证引用、SSL Bundle、连接池、代理、地址限制及显式原子配置刷新。自动重试和重定向关闭；业务通过幂等契约及现有任务能力选择重试。接入见 [HTTP README](loadup-components/integration/loadup-components-http/README.md)。

- [ ] 在消费工程执行 HTTP 单测和自动装配测试并记录结果；现有用例尚未执行，不能视为运行验证。
- [ ] 补充并执行 TLS 客户端证书、可信代理、响应断连、分块响应大小限制、长下载与并发刷新、凭证轮换及 Trace 传播验证。
- [ ] 按实际配置来源接入 ConfigCenter 监听和完整快照解析；首版仅提供显式 `refresh`，验证更新失败保留旧版本及旧连接池释放。
- [ ] 验证客户端或渠道 SDK 没有未声明的自动重试；验证出口网络与代理策略，避免访问非授权目标。

### 首批组合验收

- [ ] 执行现有“业务事务发布 Outbox → HTTP 通知”MySQL 集成用例，包含对端受理后模拟响应丢失、稳定幂等标识及重复投递；测试已编写但尚未运行。
- [ ] 使用真实消费工程验证业务写入与事件同事务、外部调用期间无数据库长事务、人工重放权限和通知协议确认。
- [ ] 按模块 README 提供的定向命令由用户本地执行，遵循构建纪律，不默认启动全 reactor 构建或测试。

## P0 · KMS 首版后续验证与部署集成

基于 OpenBao Transit 的独立组件已提供运行密码操作、独立管理身份、版本引用、RSA/AES、公钥读取及受控包装导入；接入见 [KMS README](loadup-components/security/loadup-components-kms/README.md)。首版协议夹具和自动装配测试已编写，尚未执行。

- [ ] 在消费工程执行 KMS 定向测试，并针对固定 OpenBao 版本执行真实服务验证：生成/导入 RSA 与 AES、签名互操作、轮换、历史验签解密、最低版本限制、公钥导入、BYOK 包装及 ACL；夹具不能代替服务验证。
- [ ] 接入生产身份登录与续租 Provider，验证令牌过期、权限撤销、TLS/mTLS、封印、重启和超时；管理操作结果未知时查状态，禁止盲目重试。
- [ ] 配置 OpenBao 持久化、HA、解封、审计设备、备份恢复和告警；关闭 upsert，运行身份不授予创建/导出/备份权限。
- [ ] 在消费工程建立商户/渠道到密钥版本的可信映射和公钥切换流程，审计管理操作；按需求评估导入新版本、重包装和证书生命周期。

## P0 · 开放接口与外部协议接入

- [ ] 在 Spring Security 上扩展商户应用身份认证：应用启停、权限范围、凭证版本及轮换；保留后台 JWT 认证的独立职责。
- [ ] 验证 Signature 的 LoadUp request v1 首版协议：已实现 method、原始 path/query、Content-Type、payload 摘要、timestamp、nonce、应用/接收方和版本/算法绑定，以及 KMS 与 Redis 原子防重放；运行现有定向测试并验证真实 OpenBao/Redis、多实例 TTL、代理路径和跨语言固定向量。
- [ ] 在消费工程为指定开放接口接入 Spring Security 验签认证、可信商户/应用绑定、受限可重复读取原始 body 和错误报文；nonce 防重放与业务幂等分别处理，不对后台 JWT Controller 全局强制签名。
- [ ] 商户与租户范围由已认证身份及授权关系确定，禁止直接信任客户端租户头；后台执行显式恢复并清理租户上下文。
- [ ] WebMVC 支持显式外部协议接口，成功包装、异常处理、ErrorController 和安全拒绝响应共同遵守协议；渠道回调可读取原始报文并返回渠道要求的状态码及报文。
- [ ] 定义 Secret/证书引用、版本、有效期和脱敏契约，接入现有秘密管理设施；不自研密钥管理平台。
- [ ] 为开放接口明确应用及接口维度限流；区分单实例限流与全局配额，仅在业务要求全局配额时实现跨实例控制。

## P0 · 契约与发布边界

- [ ] 盘点全部发布坐标、传递依赖、自动配置、外部服务要求和真实消费者；为每项能力标注稳定、试验、占位或弃用状态。
- [ ] 在组件 README 中明确基础契约、可选扩展、binder 支持矩阵、外部依赖和失败语义；优先检查 Cache、DFS、Scheduler、Gotone 与 ConfigCenter。
- [ ] 明确公开 Java API、配置键、数据库迁移和事件的演进规则，并为破坏性变化提供迁移说明。
- [ ] 用最小消费工程检查 BOM 引入、可选 binder 依赖和自动配置行为，避免无关后端进入应用。

## P1 · 身份与授权闭环

- [ ] 完成 UPMS + SAS 授权码及刷新令牌集成验证，覆盖授权数据持久化、多实例签名密钥和客户端配置来源。
- [ ] 刷新令牌换取新访问令牌时重新校验 UPMS 用户状态与 RBAC，使禁用账号和已撤销权限及时生效。
- [ ] 定义并验证 issuer、audience、密钥轮换、access/refresh 用途及权限快照的部署契约。

## P1 · 通用后台业务模块

- [ ] 审计中心后续完善：覆盖认证过滤器内的拒绝事件，补充可靠写入与保留/归档策略，并验证多租户查询隔离及高并发写入。首版提供可配置路径的操作采集、持久化和管理员查询。
- [ ] 实现系统参数管理，区分面向业务的参数数据与 ConfigCenter 应用配置。数据字典已作为独立模块提供类型、条目管理与启用项查询。
- [ ] 文件资源后续完善：按业务需求加入病毒扫描/内容审核、保留期限与孤儿对象巡检，验证多节点部署所用共享存储 binder。
- [ ] 站内通知中心后续完善：依据业务量验证批量投递、过期归档与清理策略；首版已提供持久化收件箱和 Gotone `IN_APP` 渠道。
- [ ] 账号安全后续完善：评估会话/令牌撤销、密码变更后已签发 JWT 的失效机制及 MFA；首版已提供自助改密、安全概览和登录记录。
- [ ] 导入导出任务后续完善：按实际业务量验证处理器幂等、结果文件清理、运行中作业恢复与超大文件分片；首版提供持久任务状态、进度、手动重试和文件结果。
- [ ] 仅在确定多租户 SaaS 或审批场景后规划租户管理与工作流。

## P1 · 事务、任务与数据可靠性

- [ ] 明确 GlobalUnique 的唯一声明边界；业务使用数据库唯一约束及订单状态实现请求摘要校验、历史结果重放和渠道执行幂等，不将支付状态机塞入通用组件。
- [ ] 验证 RetryTask 重复执行、运行中退出、取消、执行超时、终态再注册及重试耗尽行为；明确任务状态与业务状态的关系，记录人工重试及参数版本契约。
- [ ] 验证数据库事务、唯一冲突、行锁、乐观锁与条件更新使用方式，明确交易历史不可覆盖删除的表级例外及多模块 Flyway 迁移组合规则。
- [ ] 为敏感管理操作定义可靠审计策略，覆盖凭证变更、审批和人工修正；区分可降级审计与必须持久记录的操作，补充保留、归档和访问控制。

## P3 · 质量与可观测性

- [ ] 为代表性 binder 建立同一契约下的 Testcontainers 行为测试，先覆盖 Cache 本地/Redis 路径，再按风险扩展。
- [ ] 增加依赖方向与层间约束检查，阻止业务模块绕过 API 直接依赖实现模块。
- [ ] 对普通 Controller 的认证、方法授权、响应报文及 `traceId` 响应头建立端到端回归用例。
- [ ] 使用共享 Micrometer 实例补充缓存、任务重试和通知渠道的指标；名称及低基数维度遵循 Observability README 契约。
- [ ] 补齐出站 HTTP、Outbox 与后台任务观测；敏感数据不进入日志与标签，跨线程及后台任务传播采用明确契约。
- [ ] 为 Testify 提供外部 HTTP 模拟及故障注入用法，覆盖重复、乱序、超时、断连和响应丢失；不重复建设通用网络模拟引擎。
- [ ] 自动检查每个 Maven 模块恰有 `README.md` 和 `ARCHITECTURE.md`，并校验文档中的相对链接。

## P4 · 发布准备

- [ ] 提供最小接入和可选组合示例，让新开发者仅按模块文档完成配置与验证。
- [ ] 按能力成熟度确定 v1.0 发布范围，记录兼容承诺、升级步骤和已知限制。
- [ ] 提供固定发布版本的最小消费验证、依赖安全检查、数据库升级与备份恢复演练步骤；生产验收记录实际容量与故障恢复证据。
- [ ] 仅在真实场景提出需求后评估新的 binder、OAuth Provider、代码生成器或独立网关部署单元。
- [ ] 如确需跨实例共享容错状态，先验证 Resilience4j 分布式状态语义，再决定是否实现 Redis binder。

## 数据脱敏验收

- [ ] 在消费工程执行本轮新增的单元/MVC 测试，验证真实 Boot 装配、全局 Mapper/出站 HTTP/缓存隔离，以及真实 MySQL 审计提交失败时明文不可返回。
- [ ] 按真实业务逐一盘点输出 DTO、日志与导出中的敏感字段，补充脱敏注解或显式处理；禁止以全局正则替换代替字段契约。

## 统一日志与观测后续验收

- [ ] 由用户执行相关定向测试与真实消费工程验收，检查指标导出、Trace/MDC、文本与 JSON 日志格式。

## 组件目录迁移后续验收

- [ ] 本地重新导入 Maven，并按需刷新 IDE/CodeGraph 索引；由用户执行选定消费模块的定向构建与运行验证。组件目录及坐标契约见 components README。

## 执行链上下文后续验收

- [ ] 用户执行 JDK 25 ScopedValue commons-context/ServiceTemplate、TenantUtil、Database/WebMVC Filter 与 Boot/Micrometer 装饰器组合的定向测试，验证 init/clean 异常语义、真实请求、线程池/虚拟线程和异步分派无上下文串数据。
- [ ] 在实际消费工程梳理非 HTTP 执行入口，确保后台任务、消息消费与自定义线程池使用回调作用域/业务装饰器，并显式保存持久任务需要的元数据；验证自定义 Executor 装饰器组合及所绑定值的不可变性。

## 分布式锁后续验收

- [ ] 用户执行 LockTemplate 单测与真实 Redis IT，验证 Boot 装配、平台/虚拟线程、独立客户端竞争、重入及 watchdog 续期；源码已提供，未执行编译/测试。
- [ ] 在消费工程验证原生 Redisson TLS/认证、Redis 拓扑与故障转移、断网、长暂停及固定租期失效；确认旧执行者写入由数据库状态条件/唯一约束或资源 fencing 拒绝。
- [ ] 验证锁先于真实事务获取、提交/回滚后才释放，排查事务代理自调用、异步/流式回调越界；按实际负载配置客户端超时、并发限制及告警。

## P1 · 入参命名与公共分页验收

Command/Query 命名、ID 查询复用、字典 Command 合并、七类重复分页 DTO 移除、公共 PageResponse 与相关前端调用已实现；规则已集中写入 AGENTS 的命名章节，并修正旧分页响应约定。2026-10-11 相关 60 个模块 Maven clean package 成功（测试源码编译）；最终包含全部八个 Web 适配模块的 54 模块 clean test 成功，ApiMaskingJsonTest 的 4 项测试通过，验证公共分页响应、展示 DTO 映射、元数据及脱敏；其他测试未执行。

前端 `pnpm --filter @vea/admin build` 成功；同时修复商户查询事件的多行表达式语法问题。整体 vue-tsc 类型检查仍受以下表格插槽问题阻塞。

- [ ] 实际 WebMVC 请求验证分页 OpenAPI、响应 envelope、租户与权限；运行 Router.http 验证字典扁平 Command，联调商户/合约分页页面。
- [ ] 修复前端 vue-tsc 报出的多页面表格插槽 DefaultRow 类型错误并重新执行类型检查；本次分页和 API 字段调整未报错，整体检查未通过。

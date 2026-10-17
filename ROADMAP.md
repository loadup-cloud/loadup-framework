# LoadUp Roadmap

本文只记录尚未完成的工作。每完成一项任务立即更新：移除已完成的实现项，未执行的运行验证单独保留。已实现的能力、接入方式和设计边界以各 Maven 模块的 `README.md`、`ARCHITECTURE.md` 为准。LoadUp 是通过 BOM 按需引入的框架；`loadup-application` 用于本地集成验证。

## 商户与合约 Failsafe 后续验收

2026-10-17 已清理商户、合约测试 test/ci 四份配置中的 mybatis-flex.global-config 遗留覆盖，保留 database 配置保护并补充诊断选项不可覆盖的回归。全仓源码配置检查未发现其他同类覆盖。两个测试模块显式引入可选 Flyway starter/MySQL 扩展，启用多租户，取消固定 test profile 并关闭容器复用，确保从空库执行业务及 Sequence 迁移。真实数据库回归进一步发现并修复合约并发签约的 MyBatis 会话缓存问题：获取方案锁后重查幂等请求，重复键处理移到事务回滚后；并发回归增加同步起跑、超时、幂等重放与不同请求冲突验证。

定向 mvnd clean verify --fail-at-end 成功：5 项 database 配置保护单测、2 项 MerchantContractIT、5 项 ContractPersistenceIT 均无失败/错误/跳过；ci profile、非复用真实 MySQL，相关 Spotless/License/Enforcer 校验通过。IDEA 对生产修改、配置保护测试、POM/YAML 未报错，IT 构造器仍提示自动配置 Bean 不可解析，实际 Boot 启动与注入已通过。未运行完整 reactor、其他单测或 SpotBugs，未另跑 test profile；文档已同步。

- [ ] 提交后复跑远程 CI 的商户与合约 Failsafe 流程；在消费工程验收迁移组合及并发签约容量。此次本地通过不等于远程或生产验收完成。

## CI 插件依赖解析后续验收

2026-10-17 针对 CI 的 Spotless 插件依赖批量 not found，已核对 xz 1.9、OkHttp 4.12.0、JGit 7.7.1、OSGi 3.24.200、Durian Core 1.2.0 的 Central 制品 URL，均返回 HTTP 200；这些代表性版本存在。首次下载失败的 HTTP 原因尚未从用户提供的末尾日志确认。根据 Maven 3 的 not-found 缓存机制，CI 在 setup-java 恢复缓存后清理 *.lastUpdated 失败标记，所有 Maven 命令增加 -U 重新检查缺失依赖，保留正常 jar/pom 缓存，不更换 Spotless 版本或仓库。

本地 mvnd -U -N spotless:check 成功，actionlint、IDEA 修改文件检查及 git diff 空白检查通过；未运行全 reactor，也未触发远程构建。根 README 已更新缓存行为。

- [ ] 提交后复跑出现 Spotless 解析失败的远程任务，确认依赖重新下载；若仍失败，保留首次下载的 HTTP 状态和完整解析异常，继续核对 CI 出口或 Central 的临时响应。不能仅凭本地成功声称远程问题已验证解决。

## UPMS Failsafe 后续验收

2026-10-17 已复现并修复 UserServiceIT 启动失败：测试 profile 覆盖 mybatis-flex.global-config，被 database 的持久化配置保护拒绝。保留保护并清理两个 profile 的覆盖；测试启动器改用标准 Boot 自动装配，不直接扫描 database 自动配置或重复扫描 Mapper。SQL 初始化收敛至公共 application.yml，复用 database 的 Sequence 建表脚本，并在测试 schema 中清理旧分配表。README 修正为 mvnd clean verify，明确 test 阶段不执行 Failsafe IT。

本地 test profile 与 ci profile（testcontainers.reuse.enable=false）分别通过定向 mvnd clean verify；同一个 UserServiceIT 在两种配置下各执行 1 次，均无失败/错误/跳过，连接真实 MySQL。CI 配置验证同时通过相关模块的 Spotless、License、Enforcer，IDEA 修改文件检查、git diff 空白检查通过；本轮跳过单元测试和 SpotBugs，没有执行完整 reactor。模块文档与文档站已同步。

- [ ] 提交后在远程 CI 验收同一 Failsafe 流程；其他业务模块和完整测试集不在此次验证结论内。

## 前端依赖安全与部署清理后续验收

2026-10-16 已读取 GitHub 的 24 条 open Dependabot 告警（12 个包，全部来自 frontend/pnpm-lock.yaml），并处理对应依赖：移除 Wrangler/Cloudflare 部署依赖及 miniflare/workerd/sharp/undici 链路；保留文档源码、本地预览和构建命令。删除本地文档中的模板 Cloudflare 发布说明。Vue 全链统一 3.5.42；source-map-js 1.2.2、postcss-selector-parser 7.1.6、probe-image-size 7.4.0、brace-expansion 5.0.12、js-yaml 4.3.2、fast-uri 3.1.8 及 colord 2.9.4 通过受限 overrides 管理；VitePress 内部 Vite 升至 6.4.3，移除旧 Vite/esbuild。MockJS 无修复版本，已替换旧插件为 vite-plugin-mock-dev-server 2.5.0，并适配现有静态演示数据，日志设为 warn 避免输出登录请求体。Dependabot 已增加 frontend npm 工作区维护。

frozen-lockfile 安装、管理端生产构建、本地 VitePress 文档构建、请求与 hooks 回归、IDEA 修改文件检查、Prettier 和 git diff 空白检查通过；真实 Vite 开发服务器的 analysis/login/role 三个 Mock POST 接口检查通过。整体 vue-tsc 仍报既有业务页面 DefaultRow 泛型错误，本次修改的 Mock 文件已无类型错误。官方 npm audit 现在剩 1 项新增发现的 braces 告警；原 24 条 GitHub 告警对应的漏洞未在当前审计中出现。未忽略/关闭告警，未提交推送或触发远程扫描，不能据此声称 GitHub 告警已经关闭。

- [ ] 跟进 braces@3.0.3 的 GHSA-vfj7-8cjw-p6xm：micromatch 经 stylelint/fast-glob 引入，当前无修复版本；后续升级上游或评估替换工具链，不以 audit 忽略项绕过。
- [ ] 提交并推送后等待 Dependabot 重新扫描，确认原 24 条告警自动关闭；按需检查新增的 braces 告警。管理端历史 DefaultRow 类型错误仍需单独修复。

## LZ4 升级后续验收

2026-10-16 BOM 新增 at.yawk.lz4:lz4-java 统一版本 1.11.4，覆盖 Kafka 4.2.1 传递依赖的 1.10.1；Redisson 可选 LZ4 依赖仍由消费方按需引入。定向 mvnd dependency:tree 已确认 Testcontainers 的 spring-kafka → kafka-clients → lz4-java 解析为 1.11.4，IDEA BOM 检查与 git diff 空白检查通过，README 和文档站已同步；未编译或执行运行时测试。

- [ ] 消费工程使用 Kafka LZ4 压缩或 Redisson LZ4 codec 时，回归实际压缩/解压、native library 加载和已有数据兼容性，并通过远程依赖扫描验收。

## SpringDoc 与 Jackson 版本后续验收

2026-10-16 已确认 SpringDoc 3.1.1 / Swagger Core 2.2.55 与 Boot 4 的 Jackson 3 共存，Swagger 内部仍依赖 Jackson 2。BOM 新增 jackson-2-bom.version=2.21.7，同步升级旧链路的 core/databind/YAML/JSR310；业务 Jackson 3.2.3 与共享 annotations 2.22 保持不变。包含 BOM 的 4 模块 `mvnd clean verify dependency:tree` 成功：18 项单测、3 项真实 HTTP IT 全部通过，覆盖 OpenAPI 元数据/Schema/安全描述、业务 JSON 收发与未知分组 404；依赖树确认两代 Jackson 版本。此次跳过 Spotless/SpotBugs，IDEA BOM 检查与 git diff 空白检查通过；README/ARCHITECTURE 和文档站已同步，未运行完整 reactor。

- [ ] 在消费工程回归自定义 Jackson 3 Module/序列化器与 OpenAPI Schema 的一致性；Swagger 内部 Jackson 2 不会自动继承业务 mapper 配置。跟进上游原生 Jackson 3 迁移，届时再评估移除旧链路。

## Tomcat 升级后续验收

2026-10-16 BOM 将 Tomcat 从 Boot 默认 11.0.24 统一固定到 11.0.27，满足 ≥11.0.25；通过继承的 tomcat.version 同步覆盖嵌入式 core/EL/WebSocket/Jasper 和其他 Tomcat 受管模块，未使用无上界版本区间。mvnd help:effective-pom 已确认全部受管 Tomcat 坐标解析为 11.0.27，IDEA 检查与 git diff 空白检查通过，README 和文档站已同步；未运行编译或容器启动测试。

- [ ] 在消费应用或远程 CI 验收 Tomcat 11.0.27 的启动、HTTP/API 错误响应和 WebSocket（若使用），核对消费工程未通过其他 BOM 或直接依赖覆盖版本。

## 依赖升级后续验收

2026-10-16 已更新 actions/cache 6.1.0（固定 SHA）、JobRunr 9.0.0、Groovy / Groovy JSON 6.0.0、Axios 1.20.0 及 Central Publishing Plugin 0.11.0。setup-java 6、checkout 7、commons-text 1.15.0 原已到位；UPMS 移除 Awaitility 4.2.0 的局部版本，使用 Boot BOM 的 4.3.0。前端锁文件只保留 Axios 与其依赖变更。包含 BOM 的 14 模块定向 `mvnd clean test` 成功，JobRunr 9 的 scheduler/retrytask 共 8 项测试通过；请求客户端回归及类型检查、actionlint、根 POM release profile 的 validate（含插件加载、Spotless、Enforcer）、git diff 检查通过。接入文档和文档站已同步；未合并/关闭 Dependabot PR，未执行发布。

- [ ] 在真实数据库上验收 JobRunr 8.x → 9 的迁移、存量任务序列化与 scheduler/retrytask 共用引擎；本轮仅运行定向单元测试。
- [ ] 验收远程 CI 的 cache 6.1.0 与 Central 发布；消费工程若使用 Groovy 自定义脚本，按 Groovy 6 变更说明回归实际脚本，仓库当前没有直接使用 Groovy 的代码。

## GitHub CI 后续验收

2026-10-16 已重整 GitHub Actions：统一 JDK 25/mvnd、修复重复 env、单次 clean install 完成单测/真实 IT/质量门禁，同次产物执行逐组件 jar 行/分支双 90% 覆盖率门禁；增加 PR Dependency Review、修复 OWASP 聚合扫描与缓存、失败报告归档、最小凭证范围及显式发布开关。Testcontainers 复用开关改为可被 CI 覆盖，保留本地默认；删除失效配置与全局 Docker prune。actionlint 1.7.12 工作流检查、14 项 Python 工具回归、根 POM 的 `mvnd -N spotless:check` 和 git diff 空白检查通过，实际下载的 mvnd Linux 安装包 SHA-256 与固定值一致。IDEA 检查仍报告原有 surefire-report-plugin 3.6.0 无法解析，本次未修改其版本；没有运行完整 reactor 或触发远程工作流。

- [ ] 推送后验收一次 PR 和主分支 CI，确认 GitHub Dependency Graph/Dependency Review 可用、真实容器测试、逐 jar 覆盖率与 OWASP 报告全部通过；按需配置 NVD_API_KEY，并将新的两个 PR 检查加入分支保护。
- [ ] 需要发布时配置 PUBLISH_PACKAGES、PACKAGES_USERNAME、PACKAGES_TOKEN，验收对 loadup-cloud/loadup-packages 的跨仓库权限与 deployAtEnd；当前未执行发布。配置步骤见根 README 的 GitHub CI 章节。

## P1 · Database 业务 ID 与 Sequence

2026-10-12 已实现最大 64 位业务 ID、格式化/解析、作用域数据库号段、原子批量发号及异步双缓冲。使用 MyBatis-Flex 生成 Mapper/Tables，数据库独立事务提交后发号；首次初始化/死锁有界退避重试、八位上限、预取队列及作用域容量保护已提供，Micrometer 使用共享注册表。默认主键策略已改为 BusinessIdGenerator，通过 IdGenerator 同时接入 Flex 和插入监听器；仅序列表元数据显式使用 UUID 防递归。移除 DatabaseIdGenerator 后，mvnd clean test 定向验证 41 项通过，包含 5 项真实 MySQL IT；覆盖默认策略、配置绑定、自定义策略回退、延迟解析、普通 DO 自动业务 ID、显式 ID 保留及序列表防递归。License 与 Spotless 校验通过，文档站同步完成；未执行全 reactor 或生产容量压测。

- [ ] 在消费工程注册事件码、站点和三位逻辑分表策略，应用序列迁移并接入业务幂等重用，验证现有 Flyway locations 组合。默认自动主键现在依赖 Sequence；核对历史显式 UUID 赋值和通用事件 00000000 的容量。
- [ ] 按实际热点作用域及多实例/虚拟线程负载压测吞吐、P95/P99、数据库补号次数及预取浪费，调整号段、队列、缓存和连接池容量。
- [ ] 在消费环境演练数据库故障转移/备份恢复，保证已提交分配最大值不回退；八位每日容量及恢复策略单独验收。

## P1 · AsyncResult 异步结果等待

2026-10-16 已实现 API、Local/Redis binder、同步/异步等待、超时/容量/事务保护、上下文传播和重复/冲突分类。独立限制等待数量与仍执行的发送回调，避免等待超时后阻塞回调无限累积。BOM 与 components 聚合已接入，应用仅 test scope 引入 API 供架构和错误码检查。定向 `mvnd clean ... verify pmd:check checkstyle:check` 成功：21 项单测、4 项真实 Redis/Kafka/MySQL IT 全部通过，无失败/错误/跳过；SpotBugs、PMD、Checkstyle、License、Spotless 与 Enforcer 通过。测试模块 JaCoCo report-aggregate 不排除手写类型，API 行/分支 96.85%/93.55%，Local 98.04%/96.15%，Redis 90.70%/91.67%，三个运行时 jar 均达到双 90%。另一次定向 `mvnd clean ... test` 的 23 项架构/源码/错误码门禁通过；该次跳过 SpotBugs，以上静态分析结论来自前一次 verify。IDEA Java 批量检查无错误。接入文档、设计文档及文档站已同步；此次没有重新运行其他组件的全量测试。
- [ ] 按消费应用流量压测等待容量、Redis 查询与通知开销；在 K8s 演练实例退出、连接中断及 Redis 故障转移，并验收业务订单查询兜底。

## 当前建设目标与实施顺序

2026-10-16 Maven 质量治理已完成：统一 Checkstyle 的 Java 25 引擎与项目结构规则、PMD 生成源码范围，取消 SpotBugs 对 commons 的整包排除；修复数字转换递归、场景缓存键、JWT 空值、随机数边界、null 占位符与正则校验等问题，清理冗余代码。全仓 143 模块执行 `mvnd clean ... package ...:check`，141 模块成功，两处测试失败分别为 Testify 装配用例缺少 DataSource、源码门禁误判嵌套容器夹具；修复后对 76 模块及依赖定向补验成功，37 项测试无失败/错误/跳过。首轮 228 项定向回归中的其余 226 项通过，最终两处失败均已复验修复。SpotBugs、PMD、Checkstyle、License、Spotless 和 Enforcer 门禁通过；最终保存 103 份 SpotBugs、119 份 PMD、143 份 Checkstyle 报告，报告项均为零。IDEA Java 批量检查及文档同步检查通过；Outbox SQL 检查提示缺少 IDE 数据库元数据，未将其记为数据库验证通过。真实数据库/外部系统、全量 IT 和覆盖率没有重新验收。

2026-10-16 已补齐并统一全部 143 个 POM 的英文 description，简述实际模块职责，区分 API/binder、COLA 各层与测试模块，修正 AuthServer、Testify 及通知占位传输的能力描述。修改范围仅为项目 description，保留已统一的 name 与其余 XML 元数据；XML 解析、代表性 POM 的 IDEA 检查及差异检查通过，文档同步检查无差异，未运行 Maven 构建。

2026-10-16 POM 显示名已统一：143 个 POM 保持唯一英文 name，调整其中 133 个，采用 LoadUp + 模块族 + 能力 + 职责/实现的顺序，规范缩写及三方名称。仅修改顶层 name；artifactId、坐标、依赖、模块路径及其余 XML 元数据保持原样，命名规则已写入 AGENTS。XML 解析和差异检查通过，未运行 Maven 构建；License 插件默认 canUpdateDescription=false，现有 Java 文件头描述无需随显示名重写。

2026-10-16 文档同步已按根 POM 的 modules 声明递归定位，分类目录不增加模块层级，旧路径或游离 POM 不参与同步；删除过期生成页面时清理其空父目录。原 components 根目录下 8 个重复目录实际仅含 14 个 CheckStyle-IDEA `.iml`，已按当前模块位置迁移并清理，原件保留临时备份，IDEA 已执行 Maven 同步。8 项 Python 回归通过，142 个模块/284 份源文档同步检查无差异，Hugo 构建通过；本次未运行 Java/Maven 构建。

2026-10-16 已实现注解驱动的 Service digest log：commons-log 提供 `@ServiceDigest`，Observability 对标记类或 public 方法注册统一 Advisor；Guava Stopwatch 按调用计时，LogUtil 输出固定摘要，不采集入参、返回值或异常文字。未标记 Service 不自动拦截。IDEA 批量检查无错误；一次定向 `mvnd clean spotless:apply verify -pl :loadup-components-observability -am -Dskip.spotbugs=true` 成功，Observability 14 项单测、7 项 IT 全部通过，无失败/错误/跳过，模块行/分支覆盖率均 100%；License/Spotless 校验通过，SpotBugs 未执行。集成测试验证 Boot 自动发现、Micrometer MDC 和事务提交失败的代理顺序，未以测试事务管理器替代真实数据库验收。此次仅定向验证，以下 48 个 jar 的整批报告仍指此前完整运行。

### 组件测试与覆盖率（2026-10-16 验收）

已完成本地组件测试及双 90% 覆盖率目标。一次完整的 `mvnd clean spotless:apply verify -pl <全部组件 jar 及兄弟测试模块> -am --fail-at-end -Dskip.spotbugs=true` 成功：653 项组件单测、148 项组件 IT，失败、错误、跳过均为 0；同时执行的 commons 依赖有 74 项单测通过。License 与 Spotless 校验通过，`git diff --check` 通过，SpotBugs 未执行。

随后对同一次完整运行执行 `tools/component-coverage.py` 正式门禁（未使用 `--report-only`），48/48 个运行时 jar 的行、分支均 ≥90%，返回码 0。报告逐 jar 计算，手写 DTO/配置/错误类型全部计入，仅排除 APT/MapStruct 生成代码；数据缺失或陈旧字节码不会通过。报告与重现命令见 components README。此结论仅覆盖本地执行环境，不代表 CI 或供应商生产环境验收。

收尾补齐 AuthServer 的实际 RSA 签名验签、claim 与配置边界，NanoCaptcha 的过期/单次消费/字符集，Tianai 的轨迹 JSON 及失败边界，Testcontainers 七种类型的配置注入/生命周期和真实 Elasticsearch 读写。修复配置模式遗漏 Redis/LocalStack、LocalStack 忽略 reusable、NanoCaptcha 空 ID 异常；移除验证码解析异常和 LocalStack 密钥日志。此前批次的调度分页删除、时区保留、缓存异常转换、Flyway 回调以及 Gotone 记录状态/表字段回归均在此次整批运行中通过。

- [ ] 将逐 jar 覆盖率门禁接入 CI/发布流程，并保存 summary、HTML 及单测/IT 报告；本地门禁已通过，远端流水线尚未验收。
- [ ] 在已有 Gotone 消费数据库验收 `V2__add_notification_record_trace_id.sql` 升级；本次真实 MySQL 验证覆盖从空库应用 V1/V2，未覆盖存量升级。
- [ ] 为 Gotone FCM 及 Aliyun/Huawei/Yunpian SMS 接入真实传输并验收；当前占位路径显式失败，边界测试达标不代表具备实际发送能力。Webhook 后续补充厂商响应体业务错误码判定，当前只按 HTTP 状态判断。
- [ ] 在真实 Nacos/Apollo、邮件、推送和短信供应商环境单独验收连接、权限及协议错误；SDK 模拟和本地协议服务器测试不替代外部系统验收。

- [ ] JUnit 6.0.3 定向运行验收：版本由 Boot 4.1.1 BOM 统一管理，Testify/AGENTS 文档已对齐，launcher 架构测试已改用 ArchUnit 核心库；见 [分析记录](loadup-testify/JUNIT6-ANALYSIS.md)。用户验收普通/参数化/Spring 测试、架构与源码/错误码门禁及真实 MySQL IT，核对测试发现数量；本次未执行构建或测试。
- [ ] Testify 真实数据库回归：UserServiceIT 已在本次 UPMS Failsafe 修复中通过真实 MySQL 回归；继续验证 Testify 的批量场景用例。TestifyAutoConfigurationTest 与 ScenarioAssertTest 已在本次质量治理中完成 14 项测试，Bean 装配、非数据库断言及缺少数据库引擎的错误契约通过；未以装配测试替代真实数据库验收。

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

## 代码规范一致性检查验收

2026-10-11 已实现敏感日志清理、共享密码策略、UTC Clock 与审计填充分工、合约 Facade、OAuth MapStruct 转换、分页默认值/上限/排序白名单、稳定业务错误码和全局 API 异常响应；统一 DTO/domain/autoconfigure 命名及 Java 注释，更新 AGENTS 和 License 默认配置。52 个受影响模块 clean package 成功。密码、验证码 Clock/日志、JSON 日志及 MVC 响应定向测试已通过。合约/审计/字典/文件/通知/导入导出自动配置、密码写入入口、分页排序、OAuth 绑定映射及令牌 Clock 回归均已执行通过。最终 69 个相关模块 clean test 成功，16 项 ArchUnit 规则和 4 项源码规范检查通过；License/Spotless 全仓校验通过，文档站已同步。子模块统一显式引用当前根 POM，避免本地旧父 POM 配置污染；未运行完整 reactor 测试或真实数据库 IT。

- [ ] 在真实消费工程验证数据库审计填充、租户隔离、密码重置与权限失败 HTTP 响应；单元测试不能代替真实环境验收。

## 本地时区默认值与 Maven Daemon

2026-10-12 按最新约定将业务及数据库 Clock 默认值改为系统本地时区，LocalDateTime 转换使用 Clock 自身时区；JSON 默认时区、DFS 元数据和 Outbox 数据库时间同步调整，OpenAPI 与接入文档同步更新。AGENTS 规定构建使用 mvnd，保留 clean 与定向模块选择。IDEA 检查已修复新增测试源码的 API/构造器调用问题；静态检查未发现生产代码继续强制 UTC 默认值。数据库审计和 JSON 本地时区回归已补充，但本轮未运行构建或测试：此前 PATH 未找到 mvnd；本轮已确认 mvnd 可用，并在错误码改造验证中执行通过 31 项日期解析和 5 项数据库监听器测试。Outbox 的 IDE SQL 检查因未关联数据源提示表不可解析，真实数据库行为仍待 IT 验证。

- [ ] 在消费环境运行本地时区回归及数据库 IT，核对数据库会话/JVM 时区、Outbox 租约和已有时间数据的解释。

## ResultCode 与错误码统一

2026-10-12 新增 JDK-only commons-error，统一错误枚举和 CommonException；八个业务模块错误位于 client.error，删除裸业务异常及字符串失败工厂。KMS/HTTP/DFS/Signature/Lock 等分类异常接入 ResultCode；响应只使用固定枚举文案，诊断保留 cause。已通过主要消费模块 mvnd clean package；最终各组定向回归累计 148 个用例通过（包含 23 项架构/源码/枚举门禁），覆盖错误原因不外泄、报文可读、HTTP 200 result/data、业务模型以及 HTTP/KMS/Signature/Lock。License 与 Spotless 校验通过，文档站已同步；未执行真实数据库和外部渠道 IT。

- [ ] 验证真实消费工程的错误响应、数据库冲突及外部渠道失败，核对前端对新增模块错误码的处理。

## 分页契约去重后续验收

2026-10-13 已删除七个领域 Page 包装和两个纯分页业务 Query，领域统一 PageResult，纯查询复用 PageQuery，带筛选的 Query 复用公共边界校验。Facade/PageResponse 的 HTTP 字段保持不变；SuccessResponse 移除重复分页能力。mvnd clean test 相关模块及依赖编译通过，31 项定向测试通过，覆盖公共分页 JSON、默认/非法入参、元数据保留、脱敏分页、UPMS 筛选排序与七类业务装配；License/Spotless 校验及文档站同步完成。未执行真实数据库分页 IT 或前端联调。

- [ ] 在实际消费工程联调分页筛选、排序与前端表格，确认 OpenAPI 的公共 PageQuery 字段及空数组响应；更新外部代码对已删除 Page 类/纯分页 Query/ofPage 工厂的引用。

## 逻辑删除契约后续验收

2026-10-13 BaseDO.deleted 已显式添加逻辑删除注解，database 使用 IntegerLogicDeleteProcessor；Java Integer、数据库 TINYINT、正常/删除值 0/1 保持一致，无需表迁移。mvnd clean test 定向 8 项回归通过，包含关闭全局列名时继承逻辑删除元数据的验证；格式化与 License 校验通过，文档站已同步。实际删除 SQL 与数据库行为未在本轮执行。

- [ ] 在实际消费工程验证 BaseMapper 删除转换为 UPDATE deleted=1，普通查询及更新过滤删除记录，并核对显式物理删除调用的业务边界。

## Testify 文档后续验收

2026-10-14 已重写 Testify 聚合及五个子模块的 README/ARCHITECTURE，共12份文档。基于实际源码补齐 Maven/条件装配、数据库隔离、两套轮询规则、参数化 YAML、变量/函数、操作符、根因异常、diff 和排错；修正反引号表名、自动函数收集等错误描述。此次仅文档修改，未执行 Maven、Docker 或示例测试；文档站同步完成。

- [ ] 在最小消费工程按 Testify 手册验证有/无数据库接入及案例执行，并将运行结果与环境单独记录。

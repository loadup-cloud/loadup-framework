# LoadUp Contract 详细设计

## 1. 目标、范围与实施状态

提供产品目录、条件规则、产品组合、销售方案、商户合约，以及确定性的运行时配置解析。产品定义已实现的能力，页面组织这些能力的销售条款；新增渠道或支付行为仍需对应业务实现。

本设计面向被消费的 SDK，数据库和 HTTP 都不是领域核心的依赖。商户收单应用在自有工程中消费合约能力。框架不承担渠道调用、扣费、对账、余额账户和电子签章。

**已提供领域、client、MySQL infrastructure、app、可选 web 和前端 API 源码；未编译或执行测试。** 当前交付范围为有效草稿管理、固定版本发布、初次签约、状态管理和权威解析。审批、修订安排、页面后端联调和分布式事件仍待交付。

## 2. 统一术语

| 术语 | 含义 |
|---|---|
| 产品 Product | 可销售业务能力；稳定编码关联业务已实现的 capabilityCode |
| 产品版本 ProductVersion | 参数定义、默认值、使用条件和依赖的不可变版本 |
| 配置项 ParameterDefinition | 有类型、有约束且声明覆盖层级的参数 |
| 条件 Condition | 对可信事实和合约参数做纯判断的结构化规则 |
| 组合 BundleVersion | 固定产品版本清单及组合默认值，不嵌套组合 |
| 销售方案 SalesPlan | 发布后可被商户签约的销售对象 |
| 方案版本 SalesPlanVersion | 展开组合后的固定产品清单、销售值、协商权限和签约条件 |
| 合约 MerchantContract | 租户内商户与销售方案形成的业务约定及管理状态 |
| 合约修订 MerchantContractRevision | 选中产品、商户值和最终条款的不可变快照 |
| 生效安排 | 将不可变修订映射到半开时间区间的运行安排 |
| 租户 | 数据隔离主体；不等同于商户，一个租户可以管理多个商户 |

## 3. 组合关系和不变量

```mermaid
flowchart LR
  P[产品版本] --> B[组合版本]
  B --> S[销售方案版本]
  S --> C[商户合约修订]
  C --> R[运行时判定与配置]
```

- 产品编码、组合编码、方案编码在租户内唯一；停用后不复用历史编码。
- 所有引用指向确切版本，不允许 latest。
- 已发布内容不可修改，修改创建新版本；主档停用阻止新增销售，默认不影响存量合约。
- 组合首版只包含产品。方案可以引用多个组合，通过组合别名限定 itemKey，例如 basic.wechat。
- 同一能力可有多个产品项，运行查询必须明确 itemKey，不能随机选择。
- 产品的必选、依赖、互斥关系在签约选择完成后统一检查。
- 商户自定义值只能覆盖明确授权字段，并满足产品硬约束和方案协商范围。
- 历史合约保存最终配置及来源，不能重新读取上游最新配置解释历史交易。

## 4. 配置模型与 Schema

目标持久化格式采用 JSON Schema 2020-12 受限子集，加 UI/覆盖权限元数据。Schema 用于类型、必填、枚举、范围；UI 负责控件、布局、提示。发布时禁止远程引用，限制字段数、规则规模、字符串长度与报文大小。

**阶段一**采用 Java 强类型 ParameterDefinition/TypedValue，支持 STRING、INTEGER、DECIMAL、BOOLEAN；字段以稳定路径存储，例如 fee.rate。暂不提供任意 JSON Schema 导入、数组、嵌套对象或引用资源校验，后续增加时必须进行受限 Schema 到领域定义的转换，不能维护两套校验行为。

- 金额：币种最小单位整数，币种显式声明；费率：十进制字符串与 BigDecimal，不使用浮点数。
- 数值范围、允许值和覆盖权限由产品定义；商户协商策略不得扩大产品范围。
- 未填写继承上层值；null 不代表删除。阶段一不支持可空配置值。
- 默认值由 ConfigurationResolver 显式填充，不能假设 Schema 验证器填充默认值。
- 影响约束的字典枚举发布时冻结。渠道凭证只存可信引用，不在条款内存私钥。

| 层级 | 配置值 | 权限 |
|---|---|---|
| PRODUCT | 产品默认值 | 定义类型、硬约束和允许覆盖层级 |
| BUNDLE | 组合默认值 | 字段必须允许 BUNDLE 覆盖 |
| SALES_PLAN | 销售条款 | 字段必须允许 SALES_PLAN 覆盖 |
| MERCHANT | 商户自定义 | 字段允许 MERCHANT，且存在方案协商策略 |

按上述顺序合并，未知字段和越权字段拒绝，不做静默截断。约束始终叠加而非覆盖。数组和对象后续采用显式字段路径修改、数组整项替换，不默认进行递归任意 merge。

示例：产品费率0.003～0.01，方案协商范围0.0038～0.006，商户填写0.0045有效；填写0.002或方案协商下限0.002均拒绝。

来源按字段保存 layer、sourceCode、sourceVersion。方案可保留必填但缺省值的字段，最终签约时必须补齐；商户无填写权限且最终缺省的必填字段应在发布校验阻止销售。

## 5. 条件设计

### 5.1 结构

使用规则树 All/Any/Not/Comparison/Exists，不执行 Java、SpEL、SQL、脚本或网络请求。比较支持 EQ、NE、GT、GE、LT、LE、IN、NOT_IN、BETWEEN。单值比较可以引用最终配置值；区间和集合使用固定类型字面量。

规则可以独立管理和发布，产品/组合/方案发布时引用并冻结确切规则版本。当前 API 将独立条件以 CONDITION 目录版本管理，引用固定 id；app 将规则展开为不可变领域树。管理界面仍为草稿原型。

### 5.2 语义

内部结果为 MATCH、NO_MATCH、INDETERMINATE。缺少必要事实、类型不匹配或非法运算产生 INDETERMINATE，NOT 不能将其转为放行。生产放行必须得到 MATCH。

All 遇到 NO_MATCH 返回 NO_MATCH，否则存在未知返回 INDETERMINATE；Any 遇到 MATCH 返回 MATCH，否则存在未知返回 INDETERMINATE。Exists 显式判断缺失，不进行隐式类型转换。

使用条件为产品 AND 组合 AND 销售方案附加条件。签约资格单独在签约阶段计算，不因后续商户事实变化而重写历史资格判断；持续资格要求必须同时配置为使用条件。

规则树深度最多8，节点最多128，每个集合最多100个值。发布阶段检查比较参数个数、类型和区间顺序。后续 API 必须根据阶段和已登记事实字段限制可配置规则。

### 5.3 可信事实

商户行业、资质来自可信商户服务，交易金额和币种来自业务校验后的请求，时间由服务端提供。领域方法接受已验证事实；HTTP 适配不得把用户任意 fact map 当成可信商户资料。

累计限额/交易次数需要计数与并发控制，不由纯条件判断完成。合约定义上限，支付/风控应用执行额度控制。

## 6. 发布和签约

发布生命周期目标：DRAFT → IN_REVIEW → PUBLISHED → RETIRED，审核拒绝为 REJECTED。当前持久化生命周期为 DRAFT → PUBLISHED → RETIRED，提供有效草稿 CRUD 与直接发布，审批后续提供。

SalesPlanCompiler：展开带别名的组合 → 合并产品/组合/方案值 → 校验协商策略 → 检查未知产品项 → 生成不可变方案。禁止发布时按输入顺序覆盖冲突项。

签约过程：校验销售窗口 → 用可信商户事实检查资格 → 检查必选/依赖/互斥 → 检查商户参数 → 填齐必填字段 → 冻结最终配置/使用条件/来源 → 形成修订和 SHA-256 条款摘要。

前端预览与签约复用同一领域解析。最终提交重新检查草稿 rowVersion、方案可售状态与资格，不能接受前端提供的解析结果。签约幂等键与请求摘要持久化，重复同请求返回原结果，不同请求复用键拒绝。

摘要用于完整性比较和历史定位，不替代电子签名或法律文书。采用带长度前缀的 UTF-8 编码、确定性集合顺序、规范化数值；包括租户/商户/范围、修订身份、有效区间、来源、最终值及使用规则。算法版本显式固定为 contract-terms-v1。

## 7. 合约版本与时间

每个合约修订保存明确 effectiveFrom/effectiveTo，区间为 [from,to)，to 可为空。第一阶段 MerchantContract 是持久化层构建的只读运行视图，验证所有修订的身份一致、版本不重复且时间不重叠；不提供会偷偷覆盖历史区间的 amend 方法。

后续变更事务需将不可变条款与可审计的生效安排分离：原条款不修改，安排通过受控变更关闭原运行区间并启用新修订，保留安排历史。不能把新修订直接追加到无限期旧区间。

合约运行管理状态 NORMAL/SUSPENDED/TERMINATED 独立于时间。当前 ACTIVE/EXPIRED 由查询时间推导，不依赖定时任务更新。暂停可恢复，终止不可恢复；后续状态命令检查 actor 权限和 generation。

支付订单绑定 contractId、revision、snapshotHash 与实际计费参数。退款与对账使用原订单条款。运行时间由可信业务入口确定，普通外部调用不得任意倒填时间绕过暂停。

## 8. 运行时接口

```java
ContractDecision resolve(MerchantContract contract, String itemKey,
                         Instant businessTime, Map<String, TypedValue> trustedFacts);
```

app 的 ContractResolveService 从 TenantUtil 的 ScopedValue 作用域取得租户，加载可信商户事实，再读取主库 merchantId/scopeKey 绑定与状态，校验持久快照摘要后调用同一领域接口。时间来自共享 Clock，客户端不能传入判断时间。

返回 allowed、reason、contractId、revision、snapshotHash、configuration。拒绝原因包括暂停、终止、未生效/已到期、未签约产品、条件不匹配、必要事实未知。拒绝结果不返回可用配置。

业务调用方不自行实现继承或条件判断；渠道选择、扣费和订单状态仍由支付业务负责。租户/商户/范围不能仅通过前端参数认定合法。

## 9. 数据库与事务设计

当前以三个表交付，避免为相同版本 CRUD 创建重复仓储：

| 表 | 核心内容 |
|---|---|
| contract_catalog_version | kind 区分产品/条件/组合/方案，租户+kind+code+version 唯一，JSON definition 与 rowVersion |
| merchant_contract | 租户+商户+scope 唯一，状态/generation、方案 id、持久 requestKey/requestDigest、创建者 |
| merchant_contract_revision | 租户+合约+revision 唯一，冻结条款 JSON 与快照摘要 |

每表含 id VARCHAR(64)、tenant_id、created_at、updated_at、deleted TINYINT，DO 继承 BaseDO。使用 QueryWrapper，Mapper 无额外 SQL。所有读取、锁定、分页和更新均显式限制租户和 deleted=0；JSON 引用由 app 在同一租户解析。版本身份不变，已发布内容不能原地编辑。

Flyway 为 V20261009000001__create_contract_catalog.sql，避免重复通用 V1 编号。单表目录是当前设计选择，后续只有需要独立主档、跨项统计或关系约束时再拆表。审批、生效安排、独立绑定历史与审计记录尚未创建空表。

发布事务锁定目录及引用版本，验证全部引用已发布。已下架引用仍允许解释已发布上层版本；下架已有计划本身阻止新签约，不回写存量快照。签约在商户外部查询完成后开启 READ_COMMITTED 事务，锁定方案状态，计算冻结条款，原子写入 header/revision。租户+requestKey 唯一约束处理重复请求，摘要绑定商户/范围/方案/选择/覆盖值/显式生效时间；相同内容重放已提交合约，不同内容拒绝。锁定方案会使同方案签约短暂串行，生产需按实际容量验证；外部调用不能进入持锁区间。

状态更新以 generation 和非终止状态做条件写入；暂停可恢复，终止不可恢复。v1 仅 revision=1，同商户范围始终只能绑定一个合约。修订需要独立可审计安排与历史，不可修改旧快照的无限区间。当前未发布 Outbox 或可靠审计事件；后续接入时与业务事务共同提交。

运行调用应访问主库，持久快照读取后比较 SHA-256 摘要及 tenant/merchant/scope/contract/revision 身份；摘要用于一致性检查，无法防御有权同时更改快照与摘要的攻击者。

## 10. 缓存、事件、观测

不可变快照可缓存，键包含 tenantId/contractId/revision/hash。暂停、终止和绑定变更首版查主库，不能靠异步缓存广播保证立即生效。后续提高吞吐必须声明撤销时效和一致性契约。

事件：SalesPlanPublished、MerchantContractSigned、MerchantContractRevised、MerchantContractSuspended、MerchantContractTerminated。携带 eventId、tenantId、实体id、generation/revision、摘要和发生时间，不携带私钥或敏感原文。消费者通过 Inbox/版本比较实现去重并忽略过期事件。

app 注入共享 MeterRegistry/ObservationRegistry，指标采用 loadup.contract.*，标签只使用操作、结果、拒绝原因，不使用 merchantId/contractId。日志使用 LogUtil；操作日志记录操作者、原因及差异摘要。敏感条款查询与 explain 单独授权。

## 11. 模块分层与 API

目标模块：loadup-modules-contract 聚合 contract-client/domain/infrastructure/app/test；可选 loadup-modules-contract-web。实际 artifact 均使用 loadup-modules-contract-* 前缀。所有 parent 指根工程；版本由 BOM 管理。

已实现 client、domain、infrastructure、app、web、test。domain 无框架依赖；client 为 record DTO 与 MerchantFactsProvider SPI；infrastructure 为 BaseDO/Mapper/Gateway；app 管理事务与业务编排，MapStruct 使用 LoadUpMapStructConfig Spring 模式和构造器注入；web 仅 Controller/权限/OpenAPI 适配。app 从共享 JsonMapper 派生内部存储 mapper，仅内部 Condition mixin 描述多态，禁止修改全局 mapper。

外部路径统一 /api/contract/...，Controller 省略框架配置的 /api 前缀，所有 JSON 操作 POST。当前目录采用 catalog/save/page/detail/publish/retire，kind 区分产品/条件/组合/方案；合约采用 merchant-contracts/preview/sign/page/detail/status；runtime/resolve 仅授权可信调用方。审批、修订/history/explain 是后续能力。完整权限和报文见 README。

HTTP 复用全局 result/data 报文、Jackson、Trace header 和 SpringDoc。客户端不得提供 tenantId、审批人或最终快照覆盖服务端身份与计算。

## 12. 管理页面

Vue3 Composition API + Element Plus。产品页编辑参数/默认值/权限/条件；条件页使用字段选择、类型化操作符和分组；组合页选择产品与默认值；方案向导选择组合、销售值、可协商范围、签约条件、模拟与发布；签约向导选择商户、方案固定版本、可选项、自定义值、预览和确认。

页面展示继承值/自定义值、锁定原因、最终值来源。条件模拟提供逐项结果，但未知事实必须标识未知。合约详情显示当前与未来修订、时间安排、差异、审批和审计。

## 13. 分阶段交付与验收

1. 领域核心：已提供不可变类型、条件判断、覆盖校验、方案编译、签约快照、时间视图与测试源码，未执行。
2. 持久化与编排：client DTO、domain Gateway、DO/Mapper/MapStruct、迁移、事务、幂等、可信商户事实 SPI 和 AutoConfiguration 已提供。
3. 管理 API/前端调用：有效草稿、独立条件、预览、权限、OpenAPI 和 TypeScript 调用已提供；受限 Schema 转换、页面持久化联调待实现。
4. 合约生命周期：暂停/恢复/终止已提供；审批、可审计修订安排、历史对比和 Outbox 待实现。
5. 生产接入：主库门禁、不可变快照缓存、共享观测、支付订单条款绑定与故障演练。

验收：上游修改不改变已签约配置；越权/超范围拒绝；未知事实不放行；必选/依赖/互斥正确；未知参数拒绝；快照独立且摘要稳定；边界时间正确；暂停不因时间倒填放行；重复签约返回同一结果；并发生效区间不重叠；事件重复/乱序不回退状态。

测试源码不等于测试通过。领域单测由用户定向执行，后续数据库集成使用 Testify + MySQL Testcontainers，不用 MockBean 替代数据库。

## 14. 技术依据

- [JSON Schema Validation 2020-12](https://json-schema.org/draft/2020-12/json-schema-validation)：仅采用受限验证词汇，不将任意 Schema/脚本作为业务执行程序。
- 项目 AGENTS.md：COLA 分层、BOM、租户隔离、API、日志、观测与构建纪律。

## 自动装配条件与注册阶段

ContractAutoConfiguration 保留 `@ConditionalOnSingleCandidate(DataSource.class)` 与启用开关，使用 `@Import` 显式注册服务、仓储、支持类及 MapStruct 生成的 Spring 转换器。Mapper 接口继续由 `@MapperScan` 注册，不在自动配置上使用 `@ComponentScan`。Spring 7 禁止把解析阶段的组件扫描与 REGISTER_BEAN 阶段的 OnBeanCondition 混用；内嵌扫描配置也会继承该限制。

转换器仍由 MapStruct 按共享配置生成并由 Spring 构造器注入，不手工实例化。ContractAutoConfigurationTest 覆盖单 DataSource 启用、缺失 DataSource、显式禁用和多 DataSource 无主候选场景；测试源码已提供，未运行。

## 公共边界与映射约束

Facade 是 client 的业务契约，应用服务直接实现；Controller 和跨模块消费者依赖 Facade。domain 保留业务状态、规则与 Gateway，表示层字段转换交给 Spring 管理的 MapStruct。共享配置固定 Spring 模式、构造器注入和目标字段严格校验。

仓储依赖 database 的固定 UUID、审计时间、逻辑删除规则，由 database processor 自动生成带 @Mapper 的 XxxDOMapper（继承 BaseMapper），并通过模块生成的 Tables 表达查询。字典删除与文件引用解绑明确使用物理删除；文件状态、通知归档和任务生命周期是业务状态，独立于 BaseDO 的 deleted。

所有数据对象的诊断文本使用 commons-json；诊断序列化与真实 API JSON 分离，避免因 HTTP 脱敏设置改变日志中的凭证披露规则。

## Client 入参与分页边界

client.command 表达业务写动作，client.query 表达只读条件；Controller 和 Facade 共用入参契约。模块专用 PageDTO 已移除，app 将领域分页对象映射为 commons-dto 的 PageDTO；Web 输出 PageResponse，避免分页结构随模块变化。领域 Gateway 的分页返回值不携带 HTTP envelope。

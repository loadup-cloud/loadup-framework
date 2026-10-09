# LoadUp Contract

产品、组合、销售方案与商户合约的动态配置模块。已提供第一阶段纯 Java 领域核心：有约束的参数继承、条件判断、方案编译、签约快照及运行时判定。

**目前没有数据库存储、Spring 自动装配、管理 API 或管理页面；测试源码已编写，未执行。** 完整设计及后续阶段见 [ARCHITECTURE.md](ARCHITECTURE.md)，未完成工作见根 [ROADMAP](../../ROADMAP.md)。

## 能力矩阵

| 能力 | 当前契约 |
|---|---|
| 配置项 | STRING / INTEGER / DECIMAL / BOOLEAN，必填、默认值、范围、允许值 |
| 分层继承 | 产品 → 组合 → 销售方案 → 商户；逐字段声明允许覆盖的层级 |
| 商户自定义 | 显式协商权限，拒绝扩大产品约束、未知字段和越权修改 |
| 条件判断 | 有界 AND / OR / NOT、比较、集合、区间、EXISTS；未知事实拒绝放行 |
| 方案编译 | 固定产品/组合版本，组合别名限定产品项，默认选择校验 |
| 产品选择 | 必选、依赖、互斥和明确 itemKey |
| 合约快照 | 不可变最终配置、逐字段来源、固定版本、SHA-256 条款摘要 |
| 时间判断 | 半开生效区间、禁止重叠、暂停/终止优先于时间 |
| 动态管理 | 待实现数据库、管理 API、页面；当前通过程序化模型构建 |
| JSON Schema | 待实现受限 Schema 转换，目前为 Java 强类型参数定义 |
| 审批/事件/缓存 | 待实现，不将内存模型作为持久化方案 |

## Maven 接入

先引入项目 BOM，再添加领域 jar，不依赖 Spring：

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-contract-domain</artifactId>
</dependency>
```

聚合 artifact `loadup-modules-contract` 是 POM，不作为运行依赖。`loadup-modules-contract-test` 只供仓库验证使用。

## 程序化示例

以下类型位于 `io.github.loadup.modules.contract.domain.model`，示例还使用 java.util、java.time 和 java.math 的标准类型。

```java
Instant now = Instant.parse("2026-10-01T00:00:00Z");
ParameterDefinition fee = new ParameterDefinition(
        "fee.rate", ValueType.DECIMAL, true, TypedValue.decimal("0.006"),
        new BigDecimal("0.003"), new BigDecimal("0.01"), Set.of(),
        Set.of(ConfigurationLayer.BUNDLE, ConfigurationLayer.SALES_PLAN,
                ConfigurationLayer.MERCHANT));
ProductVersion product = new ProductVersion(
        "WECHAT_SCAN", 1, "payment.collect", Map.of("fee.rate", fee),
        Condition.always(), Set.of(), Set.of());
BundleVersion bundle = new BundleVersion("BASIC", 1, List.of(
        new BundleVersion.Item("wechat", product, true, true, Map.of(), Condition.always())));
SalesPlanVersion plan = new SalesPlanCompiler().publish(new SalesPlanDraft(
        "RETAIL_STANDARD", 1,
        List.of(new SalesPlanDraft.BundleSelection("basic", bundle)),
        Map.of(),
        Map.of("basic.wechat", Map.of("fee.rate", new OverridePolicy(
                new BigDecimal("0.0038"), new BigDecimal("0.006"), Set.of()))),
        Condition.always(), Condition.always(), now, null));
MerchantContractRevision revision = new ContractSigner().sign(
        "tenant1", "merchant1", "store1", "contract1", 1,
        plan, plan.defaultSelection(),
        Map.of("basic.wechat", Map.of("fee.rate", TypedValue.decimal("0.0045"))),
        Map.of(), now, now, null);
MerchantContract contract = new MerchantContract(
        "tenant1", "merchant1", "store1", "contract1", ContractStatus.NORMAL, 1,
        List.of(revision));
ContractDecision decision = new ContractRuntimeResolver().resolve(
        contract, "basic.wechat", now, Map.of());
// decision.allowed() == true
// decision.configuration().values().get("fee.rate").value() == "0.0045"
```

真实业务必须从可信身份/商户数据取得 tenant、merchant、scope 与事实，使用服务端业务时间。当前 API 是纯领域函数，不负责认证、存储或商户事实查询。

## 规则示例

用最终签约参数限制单笔金额：

```java
Condition limit = new Condition.Compare(
        "transaction.amountMinor", ComparisonOperator.LE, List.of(), "limits.singleAmountMinor");
```

参数引用必须存在于对应产品定义中。缺少交易金额或配置值返回 INDETERMINATE，不能放行。签约资格不能引用尚未形成的单项交易配置。

## 重要约束

- 值必须显式指定类型，费率用十进制字符串，不传 double。
- 配置 map 用稳定字段路径，尚不支持嵌套对象、数组与 null 删除语义。
- 组合不嵌套；方案项通过 alias.itemKey 限定。
- 方案默认选中集合必须非空且满足依赖；签约可以改变可选项，但不能取消必选项。
- 协商策略限制显式商户覆盖；未填写的商户字段继续继承销售值，不自动截断或修改。
- 时间区间不重叠；首版签约不允许追溯到 signedAt 之前生效。
- 合约修订摘要是条款完整性标识，不是电子签名。
- 运行查询返回拒绝时不返回可执行配置。合约暂停/终止后不能通过查询历史时间绕过。
- 领域对象的租户检查只保证一个合约视图内部身份一致，生产仓储必须在每次查询和引用中执行租户隔离。

## 定向验证

按项目约定，本次未执行以下命令。用户可在本地只验证本模块及依赖：

```bash
mvn clean test -pl loadup-modules/loadup-modules-contract/loadup-modules-contract-test -am \
  -Dtest=ContractFlowTest,ConditionEvaluatorTest \
  -Dsurefire.failIfNoSpecifiedTests=false -Dskip.spotless=true -Dskip.spotbugs=true
```

这不是生产验收；真实数据库、并发签约和事件恢复验证将在持久化阶段补充。

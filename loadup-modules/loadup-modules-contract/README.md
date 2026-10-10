# LoadUp Contract

可复用的产品目录、条件规则、产品组合、销售方案与商户合约模块。通过管理 API 动态发布条款、签约并解析最终配置，无需为配置变更重新部署。

已提供 COLA client/domain/infrastructure/app 和可选 web 适配、MySQL 迁移、Spring 自动装配及前端页面。**源码已完成，未执行编译、测试或端到端联调。** 浏览器中的 Contract 页面已接入目录管理和商户合约接口；未保存的表单内容仍留在页面内存中。详细设计见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 能力矩阵

| 能力 | 当前契约 |
|---|---|
| 产品参数 | STRING/INTEGER/DECIMAL/BOOLEAN，必填、默认值、范围、枚举和覆盖层级 |
| 条件管理 | 独立版本，ALL/ANY 与可否定的 EXISTS/COMPARE；未知事实不放行 |
| 配置继承 | 产品 → 组合 → 方案 → 商户；逐字段权限与值来源 |
| 目录管理 | 按 kind 统一草稿/发布/下架，固定版本引用与 rowVersion |
| 签约 | 服务端预览、可信商户资料、持久幂等键与请求摘要、范围唯一约束 |
| 合约 | 初次签约 revision=1，冻结值/来源/条件和条款摘要 |
| 生命周期 | 暂停、恢复、终止与 generation 条件更新；终止不可恢复 |
| 运行时 | 共享 Clock、主库状态读取、半开时间窗口、拒绝不返回配置 |
| HTTP/前端 | POST JSON、权限、全局 result/data、SpringDoc；类型化 API 与草稿转换 |
| 后续 | 审批、合约修订与历史安排、受限 JSON Schema、页面联调、Outbox/可靠审计/缓存/指标 |

## Maven 接入

先引入项目 BOM。仅本地编排引入 `loadup-modules-contract-app`；需要管理 HTTP API 引入：

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-contract-web</artifactId>
</dependency>
```

API 类型由 `loadup-modules-contract-client` 提供；纯 Java 解析可单独使用 `loadup-modules-contract-domain`。聚合坐标 `loadup-modules-contract` 为 POM。数据库使用 database 组件的 MyBatis-Flex 与 Flyway；配置 DataSource、共享 Jackson3 JsonMapper、事务管理器及生产数据库迁移。web 自动引入全局 WebMVC 约定，认证需由消费工程接入 resource-server，方法授权由 authorization 提供。

```yaml
loadup:
  contract:
    enabled: true
    web:
      enabled: true
  flyway:
    enabled: true
    locations: classpath:db/migration
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
```

自动配置要求唯一或主 DataSource；多数据源工程须为合约明确主库和事务管理器。运行查询不得走可能滞后的读副本。复用 database 组件提供的共享 `Clock`，消费方自定义 Clock 时也需维持单候选。

## 身份与商户事实

入口必须绑定可信租户：单租户应用由 database 组件使用配置的 `default-tenant-id`；多租户应用由消费方验证身份及租户访问权后建立上下文，不能直接相信请求头中的 tenantId。非 HTTP 调用可用 `TenantUtil.runWithTenant(tenantId, action)` / `callWithTenant` 管理 ScopedValue 作用域。接口正文没有 tenantId、actor 或商户事实字段。actor 来自 Authentication。

消费工程提供一个 `MerchantFactsProvider` Bean，通过可信商户仓储/服务查询 **tenantId + merchantId**，返回 `MerchantProfileDTO(merchantId, active, facts)`；事实键仅限 `merchant.*`，值为 `ValueDTO(type, value)`。商户不存在、停用、查询失败或未配置 Provider 均拒绝签约/运行。消费方仍需校验操作者能否管理该商户；后台权限只授予租户范围管理人员。

```java
@Bean
MerchantFactsProvider merchantFactsProvider(MerchantRegistry registry) {
    return (tenant, merchantId) -> {
        var merchant = registry.requireInTenant(tenant, merchantId);
        return new MerchantProfileDTO(merchantId, merchant.active(), Map.of(
                "merchant.industry", new ValueDTO("STRING", merchant.industry())));
    };
}
```

示例中的 MerchantRegistry 是消费方自身的可信资料接口，并非本模块提供的类。未提供默认伪造商户数据。

## 管理 API

| POST 路径（前缀 `/api/contract`） | 权限 | 行为 |
|---|---|---|
| `/catalog/save` | `contract:catalog:write` | 新建或更新有效草稿，更新携带 expectedRowVersion |
| `/catalog/page`、`/catalog/detail` | `contract:catalog:read` | 按 kind 查询版本；分页最大100条 |
| `/catalog/publish`、`/catalog/retire` | `contract:catalog:publish` | 发布不可变版本或下架，携带 expectedRowVersion |
| `/merchant-contracts/preview`、`/merchant-contracts/sign` | `contract:merchant:sign` | 可信商户资格校验、预览或幂等签约 |
| `/merchant-contracts/page`、`/merchant-contracts/detail` | `contract:merchant:read` | 查询持久合约和冻结配置 |
| `/merchant-contracts/status` | `contract:merchant:manage` | NORMAL/SUSPENDED/TERMINATED，携带 expectedGeneration |
| `/runtime/resolve` | `contract:runtime:resolve` | 权威状态门禁、时间和使用条件判断 |

所有 JSON 请求为 POST，外部 `/api` 前缀由 `loadup-components-webmvc` 统一添加。响应为 HTTP200 的 result/data；使用 `result.status == "S"` 判断成功。错误码见 client 的 ContractError。接口不自动授予权限，消费工程须注册上述七类权限；本地 `loadup-modules-upms/schema.sql` 为超级管理员提供示例授权，已有数据库需要执行其中新增的 Contract 权限语句并重新登录。

`catalog/save` 使用 `{id?, expectedRowVersion?, kind, code, version, definition}`，kind 为 PRODUCT/CONDITION/BUNDLE/SALES_PLAN。definition 结构见 client 的 CatalogDefinitions 和前端 types.ts。服务端按 kind 反序列化并拒绝未知字段；草稿须结构有效，引用的版本必须已经发布。实际保存上限128KiB；不是任意未完成表单的暂存接口。

依次保存并发布条件（可选）、产品、组合、销售方案；每个发布命令用保存响应中的 id 和 rowVersion，引用使用服务端 id，不使用本地草稿 UUID。修改已发布内容需复制 definition 创建新的 version。

```json
{
  "merchantId": "merchant1", "scopeKey": "store1", "planVersionId": "<published-plan-id>",
  "requestKey": "<one-uuid-per-signing-action>", "selectedItems": ["basic.wechat"],
  "values": {"basic.wechat": {"fee.rate": "0.0045"}},
  "effectiveFrom": null, "effectiveTo": null
}
```

sign 和 preview 使用同一结构。预览不会占用幂等键，预览 id/时间不是最终签约结果。最终签约重新检查方案可售状态及可信商户资格。网络重试复用 requestKey 与原始请求；同键不同内容返回冲突。同商户同 scope 仅能首次签约一个合约，终止后也不能以新键替换；修订流程尚未提供。effectiveFrom 为空时采用实际签约时间，显式时间不可早于签约时间。

runtime/resolve 接受 merchantId、scopeKey、itemKey 和 transactionFacts。交易事实仅限 `transaction.*`，必须由受信任支付业务验证；此接口不直接授权商户终端或收银台。金额使用最小单位整数字符串，费率使用十进制字符串。合约暂停/终止、未生效/到期或条件拒绝时不返回可执行配置。

完整顺序调用示例位于 [Router.http](../../loadup-application/src/main/resources/Router.http)。必须先配置可信租户入口、MerchantFactsProvider 和方法权限；启动器只是本地验证工程。

## 前端 API

`frontend/apps/admin/src/api/contract/index.ts` 提供目录、签约、生命周期、查询和运行时调用，复用统一 request 客户端，返回 result/data 信封。

```ts
import { saveCatalog, publishCatalog, productDefinition } from '@/api/contract'
const { data } = await saveCatalog({
  kind: 'PRODUCT', code: draft.productCode, version: draft.version,
  definition: productDefinition(draft)
})
await publishCatalog<'PRODUCT'>(data.id, data.rowVersion)
```

草稿转换提供 productDefinition、conditionDefinition、bundleDefinition、salesPlanDefinition 和 signingCommand，移除 UI 元数据，空选项转 null。时间必须携带 UTC 偏移（如 Z）；页面需先将本地时间显式转换。一次签约生成一次 newSigningRequestKey，自动重试不可重新生成。现有页面尚未接入这些方法。

## 验证状态

提供 ContractFlowTest、ConditionEvaluatorTest、ContractCodecTest 和 ContractPersistenceIT 源码，尚未运行。IT 使用真实 MySQL Testcontainers，涵盖租户引用隔离、发布不可变、乐观版本、持久重放、并发重复签约、暂停/终止门禁和回滚。按项目约定，由用户定向构建/运行，必须带 clean；集成验证需要 Docker。

后续生产验收包括真实 Boot/WebMVC/授权接入、MySQL 多实例与故障恢复、迁移组合、备份恢复及支付订单保存 contractId/revision/snapshotHash 和实际计费值。条款摘要不是电子签名，也不是对数据库管理员篡改的密码学认证。

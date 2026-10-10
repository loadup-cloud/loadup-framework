# LoadUp Merchant

可复用的商户基本信息管理模块，为后台管理与合约签约/运行资格查询提供持久资料源。通过 COLA client/domain/infrastructure/app 分层和可选 web、contract 适配接入，商户核心不依赖合约。

## 能力矩阵

| 能力 | 当前契约 |
|---|---|
| 基本资料 | 编码、名称/简称、类型、行业、国家/省市、地址、登记号码、联系人、电话和邮箱 |
| 管理 | 创建、更新、分页、详情、启用/停用，乐观 rowVersion；编码创建后不变 |
| 隔离 | 所有 Gateway 查询/更新限制 tenant_id 与 deleted=0；租户内编码唯一 |
| 对外报文 | POST JSON、全局 result/data、WebMVC `/api`、Trace header、SpringDoc |
| 隐私 | 地址/登记号/联系方式在 WebMVC JSON 输出中脱敏；内部资料与存储不被输出掩码污染 |
| 合约接入 | 可选 merchant-contract 将持久资料转换为 MerchantFactsProvider；自定义 Provider 优先 |
| 前端 | 商户管理页面、类型化 API、签约页面选择启用商户 |
| 边界 | 基本信息登记不等于资质认证；暂无入网审核、渠道商户号、银行结算账户、门店、KYC 或文件流程 |
| 验证 | 测试源码已提供，未编译、运行或完成端到端联调 |

## Maven 与配置

引入 LoadUp BOM 后按需消费：

```xml
<!-- 本地业务服务；需要 HTTP 管理时改用 loadup-modules-merchant-web -->
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-merchant-app</artifactId>
</dependency>
<!-- 与合约同时使用时增加事实适配 -->
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-modules-merchant-contract</artifactId>
</dependency>
```

商户 Java API 为 MerchantService；跨模块只需依赖 merchant-client 的 MerchantLookup。contract 适配仅依赖两个 client jar，使用 MerchantLookup，不依赖商户内部仓储，也不强制引入合约 app。单独部署商户服务时不引入适配 jar。

```yaml
loadup:
  merchant:
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

database 组件提供共享 Clock、事务管理与 MyBatis-Flex。配置唯一/主 DataSource；合约资料读取需走主库，不能因 readOnly 标志路由到滞后的副本。Flyway 提供 `V20261010000001__create_merchant_profile.sql`。开关只控制 Bean 装配，数据库迁移须由消费工程统一规划。

租户通过可信身份入口绑定 TenantUtil/ScopedValue，JSON 不接收 tenantId/actor；本地单租户启动器沿用 database 组件的默认租户入口。多租户环境不能直接信任客户端租户头。后台权限授予租户范围管理者，商户自助访问还需要消费方增加身份归属检查。

## 管理 API

| POST 路径 | 权限 | 请求 |
|---|---|---|
| `/api/merchants/create` | merchant:write | MerchantCreateCommand |
| `/api/merchants/update` | merchant:write | 基本资料、id、expectedRowVersion |
| `/api/merchants/page` | merchant:read | merchantCode 精确匹配、name 模糊匹配、status、page/size |
| `/api/merchants/detail` | merchant:read | id |
| `/api/merchants/status` | merchant:manage | id、expectedRowVersion、ACTIVE/INACTIVE |

```json
{
  "merchantCode": "M001", "name": "示例零售商户", "shortName": "零售",
  "type": "ENTERPRISE", "industry": "RETAIL", "country": "CN", "province": "GD", "city": "SZ",
  "address": "示例地址", "registrationNo": "REG001", "contactName": "联系人",
  "contactPhone": "13800138000", "contactEmail": "merchant@example.com"
}
```

类型为 ENTERPRISE/SELF_EMPLOYED/INDIVIDUAL。编码与行业为64位以内稳定编码，国家为两位大写代码；当前仅校验格式，消费方需维护自己的行业/地区目录。phone 支持可选 + 与8～15位数字。创建默认 ACTIVE，表示允许参与业务，不表示资料审核通过。

更新必须使用最新 rowVersion，且 merchantCode 不变。**address、registrationNo、contactName、contactPhone、contactEmail 为 null/未提供时保留原值，空字符串明确清空，非空替换。** 前端不把详情中的掩码重新写回；原敏感字段仅作脱敏展示，编辑默认空白。状态变化也递增 rowVersion，冲突后重新查询再确认。

没有删除接口：商户停用后保留历史资料及合约引用。停用不是终止合约，重新启用后仍须重新通过当前运行资格判断。

HTTP 使用200 + result/data，`result.status == "S"` 为成功。MERCHANT_NOT_FOUND/MERCHANT_CONFLICT 复用全局错误报文；其余入参错误由全局 WebMVC 处理。原始敏感数据仍存于数据库，输出脱敏不是加密；生产需控制数据库权限、备份与保留期限。

## 合约使用商户资料

引入 app + merchant-contract + contract-app/web 后，自动装配默认 MerchantFactsProvider；消费方已有自定义 Provider 时适配器退让。字段为 STRING：

| 合约事实 | 来源 |
|---|---|
| merchant.code | 稳定业务编码 |
| merchant.type | 商户类型 |
| merchant.industry | 已管理的行业代码 |
| merchant.country / merchant.province / merchant.city | 地区，缺失或空白不输出 |

合约请求中的 **merchantId 是商户资料的 id，不是 merchantCode**。签约页面从已启用商户列表选择。不存在或 INACTIVE 商户返回未通过资格校验；适配器再次检查返回的 tenantId/id，且不输出联系人、地址、登记号或伪造 `merchant.verified` 等资质事实。

资料变更不会重写已签约条款快照。签约资格取资料查询时的当前值；持续资格必须定义为产品/方案使用条件，运行时每次重新读取。停用后后续新的预览/签约/运行事实读取拒绝；已经提交的签约幂等重放仍返回历史结果，不重新签约。在途并发操作的截止点由消费工程业务门禁决定，本模块不跨商户、合约事务做全局锁。

## 页面、示例与验证

前端入口 `/merchants/list`，API 位于 `frontend/apps/admin/src/api/merchant/index.ts`。合约签约页支持按名称搜索启用商户。页面按钮不替代服务端权限检查。

[Router.http](../../loadup-application/src/main/resources/Router.http) 提供先创建商户、再签约、启停与更新示例；本地 UPMS schema.sql 补三类商户权限。已有数据库需手工执行新增授权语句并重新登录，消费工程自行管理权限。

MerchantBasicInfoTest、MerchantFactsTest 和 MerchantContractIT 源码覆盖私有字段更新语义、事实白名单/未知值、身份错配、自定义 Provider 退让、MySQL 编码唯一/版本冲突/租户隔离、真实商户签约与停用门禁。集成测试使用 Testify + MySQL Testcontainers，不用 MockBean 代替数据库；未执行构建或测试。设计见 [ARCHITECTURE.md](ARCHITECTURE.md)。

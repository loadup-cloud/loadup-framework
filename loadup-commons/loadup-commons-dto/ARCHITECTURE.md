# Loadup Common DTO 架构

## 职责与边界

提供通用响应、分页、金额值、事件信封和通用校验，不依赖 ORM、MapStruct 或 Spring 运行库。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `com.fasterxml.jackson.core:jackson-annotations`
- `io.swagger.core.v3:swagger-annotations`

## 实现入口

主要源码入口：

- [`DTO`](src/main/java/io/github/loadup/commons/dto/DTO.java)
- [`CommonConstants`](src/main/java/io/github/loadup/commons/constant/CommonConstants.java)
- [`BaseEntity`](src/main/java/io/github/loadup/commons/domain/BaseEntity.java)

## 分层与调用路径

```text
业务模型 → Money / PageResult / Result / PageDTO → 上层组件
```

## 扩展契约

- [`DTO`](src/main/java/io/github/loadup/commons/dto/DTO.java)：由实现方或调用方按接口定义对接。
- [`ResultCode`](../loadup-commons-error/src/main/java/io/github/loadup/commons/result/ResultCode.java)：由实现方或调用方按接口定义对接。
- [`IEnum`](src/main/java/io/github/loadup/commons/enums/IEnum.java)：由实现方或调用方按接口定义对接。
- [`CommonConstants`](src/main/java/io/github/loadup/commons/constant/CommonConstants.java)：由实现方或调用方按接口定义对接。
- [`AssertionCallback`](src/main/java/io/github/loadup/commons/error/AssertionCallback.java)：由实现方或调用方按接口定义对接。

集成方式与配置示例见 [README.md](README.md)。

## Money 与 CurrencyEnum

Money 作为基础值对象位于 dto，只有 JDK 类型及既有 Jackson annotations 依赖。三个字段 final，currencyValue 在构造时派生，不提供
setter。支持负值表达差额/冲正，不在通用值对象中施加支付金额必须为正的业务限制。

Jackson properties creator 使用 Long 检查缺失/空金额；对外只输出 cent 和 currencyValue，Currency
与精度为派生内部属性。没有默认币种、汇率换算或隐式浮点入口。

CurrencyEnum 固定字母代码、三位数字代码和显示符号，来自 JDK 25.0.4.1 的233项目录，包含历史代码；toCurrency 和精度读取委托
JDK。币种目录是版本快照，跨实例应采用一致 JDK 币种数据；历史币种精度变化需要业务迁移评估。市场有效性、渠道支持和现金舍入规则由业务定义。

MoneyFormatter/MoneyUtil 在 util，单向依赖此模块。数学运算、Jackson 往返与枚举完整性测试集中在 util，尚未编译或执行。

## 分页职责

PageDTO 是 Facade 的通用分页数据，不携带成功状态。PageResponse 是 HTTP 成功分页 envelope，实现 sealed IResponse，与
SuccessResponse/FailureResponse 并列，字段顺序为 result、data、pageInfo。领域层保留自己的分页对象，app 的 Spring MapStruct
Converter 映射记录和页码元数据；Web 层只包装响应或通过 PageDTO.map 做展示投影。

该划分减少七个业务模块的重复分页 DTO；分页 HTTP data 是列表，不再次嵌套 items/total。商户和合约前端已改用顶层
pageInfo.totalCount。字典命令合并原有 Request 包装，直接携带 id/typeCode；Router.http 和前端请求同步改为扁平字段。

## 错误契约

ResultCode 与 CommonException 由 [commons-error](../loadup-commons-error/README.md) 提供。Result 是不可变响应元数据，失败工厂只接收枚举
ResultCode。FailureResponse.of (code) 或 of (code, traceId) 不接收字符串错误码/文案；读取外部报文仍支持 Jackson creator。

## 分页契约

纯分页输入使用 `io.github.loadup.commons.dto.PageQuery`。HTTP 字段为 `page` 与 `size`，缺失或为 null 时分别取 1 与 20。显式非法值会被拒绝，每页上限为 100。业务查询保留自身筛选字段及平面分页字段，通过 `PageQuery.normalized` 复用规则并保留所属模块错误码。可变的 UPMS 查询在服务边界通过 `pagination()` 校验。各业务维护自己的排序白名单。

领域 Gateway 返回 `io.github.loadup.commons.domain.PageResult<T>`。应用层通过 Spring 注入的 MapStruct converter 和 `PageResult.map` 转换元素，使用 `PageDTO.of(result)` 保留数量、页码和每页长度。Facade 返回 `PageDTO`，Controller 返回 `PageResponse`。

分页 JSON 形如 `{result, data: [...], pageInfo: {totalCount, pageIndex, pageSize, ...}}`，`data` 为当前页数组。`SuccessResponse` 用于普通成功响应。空 `PageDTO` 使用空数组与 0/1/20 作为元数据默认值。原有业务分页包装、`DictionaryTypePageQuery`、`AccountLoginPageQuery` 或 `SuccessResponse.ofPage` 的 Java 调用方需切换到共享契约。HTTP URL 和载荷字段保持原有契约。

## 职责迁移

持久化基类使用 `io.github.loadup.components.database.dataobject.BaseDO`，来自 `loadup-components-database`。
共享映射配置使用 `io.github.loadup.components.runtime.mapping.LoadUpMapStructConfig`，来自 `loadup-components-runtime`。
渠道基础词汇和 `ChannelAmounts.cnyAmount` 来自 `loadup-pay-payment-channel-contracts`；商业合约引用来自 Pay
的 `pay-contracts-commercial`。消费方按实际源码直接声明依赖，不通过 DTO 隐式引入这些能力。

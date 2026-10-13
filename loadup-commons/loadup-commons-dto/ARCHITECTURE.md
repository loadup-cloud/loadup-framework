# Loadup Common DTO 架构

## 职责与边界

提供通用响应、分页、数据对象基类和 MapStruct 共享配置。

## Maven 依赖边界

直接依赖的外部坐标（不含测试与 provided scope）：

- `com.fasterxml.jackson.core:jackson-annotations`
- `org.mapstruct:mapstruct`
- `com.mybatis-flex:mybatis-flex-annotation`
- `io.swagger.core.v3:swagger-annotations-jakarta`

## 实现入口

主要源码入口：

- [`BaseDO`](src/main/java/io/github/loadup/commons/dataobject/BaseDO.java)
- [`DTO`](src/main/java/io/github/loadup/commons/dto/DTO.java)
- [`CommonConstants`](src/main/java/io/github/loadup/commons/constant/CommonConstants.java)
- [`BaseEntity`](src/main/java/io/github/loadup/commons/domain/BaseEntity.java)

## 分层与调用路径

```text
业务模型 → Result/PageDTO/BaseDO 与对象映射约定 → 上层组件
```

## 扩展契约

- [`DTO`](src/main/java/io/github/loadup/commons/dto/DTO.java)：由实现方或调用方按接口定义对接。
- [`LoadUpMapStructConfig`](src/main/java/io/github/loadup/commons/mapping/LoadUpMapStructConfig.java)：由实现方或调用方按接口定义对接。
- [`ResultCode`](../loadup-commons-error/src/main/java/io/github/loadup/commons/result/ResultCode.java)：由实现方或调用方按接口定义对接。
- [`IEnum`](src/main/java/io/github/loadup/commons/enums/IEnum.java)：由实现方或调用方按接口定义对接。
- [`CommonConstants`](src/main/java/io/github/loadup/commons/constant/CommonConstants.java)：由实现方或调用方按接口定义对接。
- [`AssertionCallback`](src/main/java/io/github/loadup/commons/error/AssertionCallback.java)：由实现方或调用方按接口定义对接。

集成方式与配置示例见 [README.md](README.md)。

## Money 与 CurrencyEnum

Money 作为基础值对象位于 dto，只有 JDK 类型及既有 Jackson annotations 依赖。三个字段 final，currencyValue 在构造时派生，不提供 setter。支持负值表达差额/冲正，不在通用值对象中施加支付金额必须为正的业务限制。

Jackson properties creator 使用 Long 检查缺失/空金额；对外只输出 cent 和 currencyValue，Currency 与精度为派生内部属性。没有默认币种、汇率换算或隐式浮点入口。

CurrencyEnum 固定字母代码、三位数字代码和显示符号，来自 JDK 25.0.4.1 的233项目录，包含历史代码；toCurrency 和精度读取委托 JDK。币种目录是版本快照，跨实例应采用一致 JDK 币种数据；历史币种精度变化需要业务迁移评估。市场有效性、渠道支持和现金舍入规则由业务定义。

MoneyFormatter/MoneyUtil 在 util，单向依赖此模块。数学运算、Jackson 往返与枚举完整性测试集中在 util，尚未编译或执行。

## 分页职责

PageDTO 是 Facade 的通用分页数据，不携带成功状态。PageResponse 是 HTTP 成功分页 envelope，实现 sealed IResponse，与 SuccessResponse/FailureResponse 并列，字段顺序为 result、data、pageInfo。领域层保留自己的分页对象，app 的 Spring MapStruct Converter 映射记录和页码元数据；Web 层只包装响应或通过 PageDTO.map 做展示投影。

该划分减少七个业务模块的重复分页 DTO；分页 HTTP data 是列表，不再次嵌套 items/total。商户和合约前端已改用顶层 pageInfo.totalCount。字典命令合并原有 Request 包装，直接携带 id/typeCode；Router.http 和前端请求同步改为扁平字段。

## 错误契约

ResultCode 与 CommonException 由 [commons-error](../loadup-commons-error/README.md) 提供。Result 是不可变响应元数据，失败工厂只接收枚举 ResultCode。FailureResponse.of(code) 或 of(code, traceId) 不接收字符串错误码/文案；读取外部报文仍支持 Jackson creator。

## Unified pagination contract

Pure pagination inputs reuse `io.github.loadup.commons.dto.PageQuery`. HTTP fields remain `page` and `size`, with omitted/null values defaulting to 1 and 20; explicit invalid bounds are rejected and size is capped at 100. Business queries retain their filter fields and flat pagination fields but delegate policy to `PageQuery.normalized`, preserving their declared module error code. Mutable UPMS queries expose `pagination()` for validation at the service boundary. Sort whitelists remain business-specific.

Domain Gateway results use `io.github.loadup.commons.domain.PageResult<T>`, shared with UPMS. Seven duplicate business Page wrappers have been removed. Application mapping uses the injected Spring MapStruct item converter and `PageResult.map`; `PageDTO.of(result)` adapts the unchanged total/page/size metadata. Facades return PageDTO, and Controllers return PageResponse. No HTTP envelope enters a Gateway.

The response remains `{result, data: [...], pageInfo: {totalCount, pageIndex, pageSize, ...}}`; data is the current-page array. SuccessResponse serves ordinary success data only; its duplicate pagination field and ofPage factories have been removed. Empty PageDTO defaults to an empty array and metadata 0/1/20. Existing consumer code referencing removed business Page wrappers, DictionaryTypePageQuery, AccountLoginPageQuery or SuccessResponse.ofPage must use the shared contracts. No URL or frontend payload-field migration is required.

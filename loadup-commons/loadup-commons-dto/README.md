# Loadup Common DTO

提供通用响应、分页、数据对象基类和 MapStruct 共享配置。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-commons-dto</artifactId>
</dependency>
```

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 接入步骤


## 对外契约

- [`DTO`](src/main/java/io/github/loadup/commons/dto/DTO.java)
- [`LoadUpMapStructConfig`](src/main/java/io/github/loadup/commons/mapping/LoadUpMapStructConfig.java)
- [`ResultCode`](src/main/java/io/github/loadup/commons/result/ResultCode.java)
- [`IEnum`](src/main/java/io/github/loadup/commons/enums/IEnum.java)
- [`CommonConstants`](src/main/java/io/github/loadup/commons/constant/CommonConstants.java)
- [`AssertionCallback`](src/main/java/io/github/loadup/commons/error/AssertionCallback.java)

## 金额与币种

`io.github.loadup.commons.money.Money` 是不可变金额对象，字段为 `long cent`、`Currency currency`、`String currencyValue`。cent 表示该币种的最小十进制单位，currencyValue 由 Currency 自动生成 ISO 字母代码，不能独立修改。

```java
Money amount = Money.ofMinor(1234, CurrencyEnum.CNY);
// getCent() == 1234; getCurrencyValue() == "CNY"; toMajor() == 12.34
```

`Money` 在 DTO 模块，计算及格式化在 util 模块，保持 dto 不依赖 util。

| 能力 | 契约 |
|---|---|
| Money | 不可变、有符号 long，按币种精度转换 BigDecimal，仅同币种可比较 |
| CurrencyEnum | JDK 25.0.4.1 ISO 4217 目录快照，共233项，包含历史及特殊代码 |
| 币种代码 | getCode 字母代码；getNumericCode 三位字符串，保留前导零 |
| 显示符号 | 保留指定 CNY/USD/HKD/TWD/EUR/GBP/JPY/BRL 符号；其他采用 JDK Locale.US 符号，缺失时为代码 |
| JSON | `{"cent":1234,"currencyValue":"CNY"}`，两个必填字段，Currency 与精度不重复输出 |

JPY 使用0位，CNY/USD使用2位，KWD使用3位，CLF使用4位，不统一除以100。[JDK Currency 精度定义](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/Currency.html#getDefaultFractionDigits())。

特殊代码如 XAU/XXX 在枚举中保留，但没有定义最小单位，Money 拒绝使用。枚举收录不表示渠道支持或当前可交易；业务应用自行限定可用币种。升级 JDK 后应核对目录变化，历史/特殊代码数字编码可能重复，因此不提供默认按数字代码反查。

JSON cent 为整数；超出 JavaScript 安全整数范围时，前端应采用字符串 DTO/大整数处理，不能依赖普通 number 保留精度。Jackson 3 往返测试源码位于 util 模块，尚未运行。

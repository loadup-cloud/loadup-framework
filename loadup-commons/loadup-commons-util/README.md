# Loadup Common Utils

提供日期、字符串、租户上下文及金额计算等通用工具。JSON 转换和诊断输出统一由 [commons-json](../loadup-commons-json/README.md) 提供。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-commons-util</artifactId>
</dependency>
```

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 接入步骤

## 租户上下文

`TenantUtil` 复用 [commons-context](../loadup-commons-context/README.md) 的只读 TENANT_ID，基于 JDK 25 ScopedValue。使用 `runWithTenant` / `callWithTenant` 临时绑定租户并继承其他业务键，返回或抛异常后自动恢复。移除 setTenantId/clear；任务入口使用 ContextHolder.runWith/callWith 绑定不可变 ExecutionContext。

## 金额计算与格式化

类型：`io.github.loadup.commons.money.Money`、`io.github.loadup.commons.enums.CurrencyEnum`；工具：`io.github.loadup.commons.util.money.MoneyUtil` / `MoneyFormatter`。

```java
Money amount = MoneyUtil.fromMajor(new BigDecimal("12.34"), CurrencyEnum.CNY);
Money fee = MoneyUtil.multiply(amount, new BigDecimal("0.006"), RoundingMode.HALF_UP);
Money net = MoneyUtil.subtract(amount, fee);
String text = MoneyFormatter.format(net); // CNY 12.27
String symbol = MoneyFormatter.formatSymbol(net); // ￥12.27
String localized = MoneyFormatter.format(amount, Locale.US);
```

| 操作 | 契约 |
|---|---|
| fromMajor | 按币种小数位换算；默认 UNNECESSARY，不允许静默舍入；可显式传 RoundingMode |
| add / subtract / negate / abs | 整数精确运算，溢出抛 ArithmeticException |
| multiply(long) | 整数倍精确计算 |
| multiply(BigDecimal) / divide | 必须显式提供舍入方式，仅最终最小单位舍入，除零和溢出拒绝 |
| sum | 显式币种，空集合返回零；BigInteger 累加，仅最终结果检查 long 范围 |
| min / max | 同币种比较，异币种拒绝 |
| format / formatAmount | 稳定代码+金额或纯金额文本，不受机器 Locale 影响，不丢 long 精度 |
| formatSymbol | 按 CurrencyEnum 指定符号显示 |
| format(Money, Locale) | 显式 Locale，独立 NumberFormat 实例，设置币种实际小数位 |

不提供 double 金额入口、隐式币种转换或默认手续费舍入。费率0.6%传 `new BigDecimal("0.006")`。不得将展示符号作为币种身份。

已提供 `MoneyTest` 12个场景源码，已通过 Maven 编译，尚未运行。用户可本地执行：

```bash
mvn clean test -pl loadup-commons/loadup-commons-util -am -Dtest=MoneyTest \
  -Dsurefire.failIfNoSpecifiedTests=false -Dskip.spotless=true -Dskip.spotbugs=true
```

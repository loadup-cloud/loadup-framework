# Loadup Common Utils 架构

## 职责与边界

提供 JSON、日期、字符串等通用工具。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-commons-dto`
- `loadup-commons-log`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.apache.commons:commons-lang3`
- `org.apache.commons:commons-collections4`
- `com.google.guava:guava`
- `org.apache.commons:commons-text`
- `org.apache.commons:commons-pool2`
- `commons-io:commons-io`
- `org.springframework.security:spring-security-crypto`
- `org.springframework:spring-context`
- `tools.jackson.core:jackson-databind`
- `tools.jackson.core:jackson-core`
- `com.fasterxml.jackson.core:jackson-annotations`
- `org.springframework.boot:spring-boot-starter-validation`
- 另有 4 项，详见 `pom.xml`。

## 实现入口

主要源码入口：

- [`JsonUtil`](src/main/java/io/github/loadup/commons/util/JsonUtil.java)
- [`StringUtils`](src/main/java/io/github/loadup/commons/util/StringUtils.java)
- [`AnnotationUtils`](src/main/java/io/github/loadup/commons/util/AnnotationUtils.java)
- [`AssertUtil`](src/main/java/io/github/loadup/commons/util/AssertUtil.java)

## 分层与调用路径

```text
业务与组件调用 → JsonUtil / 日期字符串工具 → 标准 Java 与 Jackson 能力
```

集成方式与配置示例见 [README.md](README.md)。

## 金额工具

MoneyUtil/MoneyFormatter 单向依赖 dto 的不可变 Money，计算使用 Math.*Exact、BigInteger 和 BigDecimal。默认主单位转换要求精确，比例运算由调用方指定舍入方式，且只在最终最小单位舍入。工具不包含汇率服务或支付费率策略。

格式化不共享非线程安全 NumberFormat，每次根据显式 Locale 创建实例；稳定文本用 BigDecimal.toPlainString，不转 double。设置 Currency 后同时设置 min/maxFractionDigits，避免 Locale 的默认币种精度污染 JPY/KWD 等结果。

MoneyTest 覆盖不同币种精度、异币种拒绝、边界溢出、最终和溢出、舍入、格式化、枚举目录和 Jackson 3 两字段往返。测试源码不代表执行成功。

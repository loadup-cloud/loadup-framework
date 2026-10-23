# Loadup Commons

通用基础能力的聚合模块，不承载业务流程。

## 子模块

- [`loadup-commons-error`](loadup-commons-error/README.md)：JDK-only ResultCode 枚举与异常基础。

- [`loadup-commons-dto`](loadup-commons-dto/README.md)
- [`loadup-commons-util`](loadup-commons-util/README.md)
- [`loadup-commons-log`](loadup-commons-log/README.md)

此 POM 用于 Maven 聚合；在消费工程中选择需要的具体 jar 坐标。

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 接入步骤

此目录是 Maven 聚合模块，不作为业务运行依赖。按用途选择子模块：

- [`loadup-commons-dto`](loadup-commons-dto/README.md)：运行实现。
- [`loadup-commons-log`](loadup-commons-log/README.md)：运行实现。
- [`loadup-commons-util`](loadup-commons-util/README.md)：运行实现。

## 展示脱敏

- [`loadup-commons-masking`](loadup-commons-masking/README.md)：纯 Java 展示规则与 `@Masked` 输出元数据，可供 WebMVC、显式日志与导出使用。

## 执行链上下文

[`loadup-commons-context`](loadup-commons-context/README.md) 提供 JDK 25 ScopedValue 类型化只读 `ContextHolder`、不可变 ExecutionContext 与可选 ServiceTemplate。租户数据复用该存储，登录身份和 Trace 仍采用各自标准上下文。

## JSON 诊断

[commons-json](loadup-commons-json/README.md) 统一提供 JsonUtil、日期序列化规则及受限、脱敏的 JSON `toString()`，供 DTO、领域数据和 DO 共用；WebMVC 复用其日期配置，诊断输出使用独立 mapper。

## 共享值与业务时间

commons-error 提供 JDK-only `DomainViolation`；commons-dto 提供 `BusinessTime`、通用 `EventEnvelope`、`ContractReference`、共享支付词汇和显式 `DomainChecks.cnyAmount` 守卫。它们不依赖技术组件；CNY 守卫不改变 Money 的多币种能力。BusinessTime 每次读取系统默认 zone，不固定区域，不修改 JVM 默认值。

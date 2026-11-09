# Commons 架构

Commons 提供通用值、数据契约和基础工具，生产代码依赖方向保持向下：

```text
error / context / masking / log
             ↑
            json
             ↑
             dto
             ↑
             util
```

箭头表示上层可依赖下层；实际依赖以各 POM 为准。commons 不依赖 components、modules 或 Pay。
error、context、masking 的生产代码只依赖 JDK；日志依赖 SLF4J，JSON 依赖 Jackson；DTO 可带 JSON、Validation
和 OpenAPI 元数据，不引入 ORM 或 MapStruct。

- `Money` 支持多币种和有符号金额。计算与格式化放 util，不在通用值对象中固定 CNY 或支付金额限制。
- `DomainChecks` 只验证通用标识、文本和正整数。渠道金额限制由 payment-channel-contracts 的 `ChannelAmounts` 表达。
- `EventEnvelope` 提供通用事件信封；商业合约引用归 Pay 的 commercial 契约制品。
- `BaseDO` 和 ORM 注解归 database。Spring 装配和 `LoadUpMapStructConfig` 归 runtime。
- `commons-util` 只声明源码或运行行为需要的依赖。`ValidateUtils` 使用 Jakarta Validation，因此保留 Hibernate Validator
  provider；不再附带未使用的 Guava、Commons Text、Pool 或 IO。

上下文保持不可变，只绑定轻量业务值。登录身份与 Trace 分别使用安全和观测组件的标准上下文。JSON 诊断输出遵循
脱敏、循环引用和资源访问限制，HTTP 输出由 WebMVC 适配。

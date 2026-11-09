# LoadUp Commons

供技术组件和业务模块按需消费的通用基础，不承载数据库映射、Spring 装配或支付业务规则。

| 制品 | 职责 |
|---|---|
| `loadup-commons-error` | JDK-only 错误码、异常与领域校验失败 |
| `loadup-commons-context` | JDK 25 ScopedValue 不可变执行链上下文 |
| `loadup-commons-masking` | 纯 Java 展示脱敏规则与元数据 |
| `loadup-commons-log` | SLF4J 日志与 MDC 约定 |
| `loadup-commons-json` | JSON、日期格式与受限诊断输出 |
| `loadup-commons-dto` | 通用响应、分页、金额值、事件信封与通用校验 |
| `loadup-commons-util` | 日期、字符串、金额计算及其他实际复用工具 |

此目录仅作 Maven 聚合。消费工程引入 `loadup-dependencies` BOM 后，声明需要的具体 JAR；内部依赖由制品 POM 明确表达。

Spring Boot 的 Clock、日志、JSON 装配与 MapStruct 共享配置由
[runtime](../loadup-components/platform/loadup-components-runtime/README.md) 提供。
`BusinessTime` 跟随系统默认时区，通用 `Money` 保留多币种、有符号金额语义。

`BaseDO` 由 [database](../loadup-components/data/loadup-components-database/README.md) 提供；渠道枚举与固定 CNY
金额校验由 [payment-channel-contracts](../loadup-components/integration/loadup-components-payment-channel/loadup-components-payment-channel-contracts/README.md)
提供。商业合约引用由消费方 Pay 的 `pay-contracts-commercial` 拥有。

依赖方向和职责见 [ARCHITECTURE.md](ARCHITECTURE.md)。

# LoadUp Transfer Test Architecture

## 职责

应用与持久化组合装配的定向回归测试。

ApplicationContextRunner 按 infrastructure → app 组合加载，验证服务/映射器装配、默认 Gateway 可覆盖以及 enabled=false 禁用。替身只用于装配单测，不声明真实数据库验收成功。

整体职责、事务和失败语义见 [业务架构](../ARCHITECTURE.md)，接入见 [README.md](README.md)。

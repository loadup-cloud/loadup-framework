# LoadUp Notification Test

应用与持久化组合装配的定向回归测试。

## 接入

此模块仅用于测试。用户在准备好依赖后定向执行 `NotificationAutoConfigurationTest`；本次未编译或运行。

```bash
mvn -pl loadup-modules/loadup-modules-notification/loadup-modules-notification-test clean test -Dtest=NotificationAutoConfigurationTest -Dskip.spotless=true -Dskip.spotbugs=true
```

测试使用 ApplicationContextRunner 与替身依赖验证 Bean 装配，不执行 SQL，也不代替真实 MySQL 或 HTTP 集成验收。

## 能力契约

| 能力                                 | 本层提供                      |
|--------------------------------------|-------------------------------|
| 应用与持久化组合装配的定向回归测试。 | 是                            |
| HTTP 适配                            | 由同一业务目录下可选 web 提供 |

完整业务 API、配置及使用示例见 [模块接入手册](../README.md)；内部职责见 [ARCHITECTURE.md](ARCHITECTURE.md)。

`NotificationSubmissionIT` 使用 PostgreSQL Testcontainers 验证异步入站、请求幂等、并发受理、来源隔离、内容冲突、
外层回滚、目标重放、收件人规范化与过期新请求回滚。该批次仅编写及静态检查，尚未执行。

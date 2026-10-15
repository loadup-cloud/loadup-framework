# Loadup Gotone Binder Push 架构

## 职责与边界

多渠道通知的具体后端适配模块；由集成方按需引入。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-gotone-api`
- `loadup-commons-error`
- `loadup-commons-log`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-autoconfigure`
- `org.slf4j:slf4j-api`

## 实现入口

主要入口文件：

- [`PushChannelAutoConfiguration`](src/main/java/io/github/loadup/components/gotone/channel/push/config/PushChannelAutoConfiguration.java)
- [`FcmPushProvider`](src/main/java/io/github/loadup/components/gotone/channel/push/FcmPushProvider.java)

## 分层与调用路径

`NotificationService` 根据消息配置路由到多个 `NotificationChannelProvider`；JDBC store 只提供配置与记录持久化。

```text
业务代码 → API/facade → 当前 binder → 第三方引擎或基础设施
```
binder 实现框架契约，把实现库及其配置隔离在业务 API 之外。

## 装配规则

- [`PushChannelAutoConfiguration`](src/main/java/io/github/loadup/components/gotone/channel/push/config/PushChannelAutoConfiguration.java) 是自动配置入口。
  - `@ConditionalOnProperty( prefix = "loadup.gotone.binder.push.fcm", name = "enabled", havingValue = "true", matchIfMissing = true)`
  - `@ConditionalOnMissingBean(name = "fcmPushProvider")`

## 配置归属

- [`FcmPushConfig`](src/main/java/io/github/loadup/components/gotone/channel/push/FcmPushConfig.java) 绑定 `loadup.gotone.binder.push.fcm`。

## 设计取舍

FCM 的真实传输尚未接入。占位 `sendMessage` 显式抛出规范的 `INVALID_STATE` 错误，由渠道调用汇总为接收人失败结果；配置凭证也不能产生虚假的成功投递。私有配置解析依赖不可变 `ChannelSendRequest` 的非空集合约束，移除未使用的方法及不可达空值分支。边界单测通过子类模拟传输，只验证接收人隔离、失败汇总及 payload 传递，不作为真实 Firebase 集成证明。

选择独立 binder，使通知发送实现的依赖留在集成应用；替换底层实现时，业务侧仍使用 `NotificationService` 契约。

集成方式与配置示例见 [README.md](README.md)。

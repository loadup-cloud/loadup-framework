# Loadup Gotone Store JDBC 架构

## 职责与边界

多渠道通知的可选 JDBC 持久化模块。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-gotone-api`
- `loadup-components-database`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter-flyway`
- `org.flywaydb:flyway-mysql`
- `tools.jackson.core:jackson-databind`
- `org.springframework.boot:spring-boot-autoconfigure`
- `org.slf4j:slf4j-api`

## 实现入口

主要入口文件：

- [
  `GotoneStoreJdbcAutoConfiguration`](src/main/java/io/github/loadup/components/gotone/store/config/GotoneStoreJdbcAutoConfiguration.java)
- [
  `JdbcChannelConfigProvider`](src/main/java/io/github/loadup/components/gotone/store/config/JdbcChannelConfigProvider.java)
- [
  `JdbcServiceConfigProvider`](src/main/java/io/github/loadup/components/gotone/store/config/JdbcServiceConfigProvider.java)

## 分层与调用路径

`NotificationService` 根据消息配置路由到多个 `NotificationChannelProvider`；JDBC store 只提供配置与记录持久化。

```text
通知引擎 → 配置/记录 SPI → Spring JDBC → PostgreSQL
```

## 装配规则

- [
  `GotoneStoreJdbcAutoConfiguration`](src/main/java/io/github/loadup/components/gotone/store/config/GotoneStoreJdbcAutoConfiguration.java)
  是自动配置入口。
    - `@ConditionalOnClass({ChannelConfigProvider.class, NamedParameterJdbcTemplate.class})`

## 扩展契约

通过 `ServiceConfigProvider`、`ChannelConfigProvider` 和 `RecordHandler` 提供自定义实现；默认实现使用参数化 JDBC
查询并显式限定当前租户。

## 设计取舍

保持组件职责独立：业务调用依赖公开契约，自动配置处理框架装配，基础设施细节留在实现层。

集成方式与配置示例见 [README.md](README.md)。

## 发送结果记录与诊断

`JdbcRecordHandler` 为每个请求收件人写入一条记录。邮件和短信按该收件人的明确状态判定成功；缺少状态视为失败。Webhook
是整组投递，收件人用于提及而非独立投递，因此各条记录使用 `SendAttemptResult.success`，不把缺少逐人状态误判为发送失败。这表示机器人消息投递结果，不表示用户已读。

渠道配置或回退列表 JSON 损坏时保留原有空配置/空列表回退规则；诊断只打印操作与异常类型，不打印输入、异常消息或异常链。单测验证失败路径和日志边界，真实
MySQL IT 验证成功/失败状态实际落库。

## 表结构演进

V2 为通知记录表补充可空的 `trace_id VARCHAR(64)`，与现有 DO 映射一致，修复读取/写入出现未知列错误。V1 保持原样，已有 V1
的环境通过 Flyway 应用 V2；新环境依次应用两版迁移。该迁移只补齐字段，不自动生成或填充 traceId。

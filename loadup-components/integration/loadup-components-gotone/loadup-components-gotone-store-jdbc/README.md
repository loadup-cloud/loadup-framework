# Loadup Gotone Store JDBC

多渠道通知的可选 JDBC 持久化模块。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-gotone-store-jdbc</artifactId>
</dependency>
```

## 装配与配置

模块提供以下自动配置入口；实际启用条件和配置项以对应类及父模块 README 为准：

- `GotoneStoreJdbcAutoConfiguration`

## 设计

内部边界、依赖与源码入口见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 接入步骤

引入 engine 和需要的渠道 binder；渠道可同时存在，存储模块按需引入。

## 对外契约

使用 Gotone API 的 `NotificationService`、`ServiceConfigProvider`、`ChannelConfigProvider` 和 `RecordHandler`；持久化
Mapper 由 APT 生成在 target 中，仅供存储实现使用。

## 自动装配

- [
  `GotoneStoreJdbcAutoConfiguration`](src/main/java/io/github/loadup/components/gotone/store/config/GotoneStoreJdbcAutoConfiguration.java)

## 数据库迁移与记录语义

通过框架 Flyway 集成应用本模块的 `classpath:db/migration` 脚本。V1 建立表结构，V2 为通知记录补充可空
`trace_id VARCHAR(64)`；已有 V1 的数据库也需要应用 V2，否则当前 DO 的查询和写入会报未知列。V2 不回填 traceId。

邮件、短信按逐收件人结果记录；Webhook 按整组投递结果记录各提及对象。记录成功表示投递成功，不表示用户已读。损坏的渠道 JSON
沿用空配置回退，日志只保留异常类型。

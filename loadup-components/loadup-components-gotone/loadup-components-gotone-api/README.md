# LoadUp Gotone API

`loadup-components-gotone-api` 是 gotone 通知组件的契约层，**零框架依赖**（不依赖 Spring /
MyBatis / 渠道 SDK）。业务代码只依赖本模块。

## 内容

- `NotificationService`：业务门面（`send` / `sendAsync`），ServiceCode 驱动。
- `NotificationChannelProvider`：渠道 SPI，由各 `binder-*` 实现。
- `config.ServiceConfigProvider` / `config.ChannelConfigProvider`（可选）：serviceCode
  配置与渠道映射存储 SPI。
- `record.RecordHandler`（可选）：发送记录回调 SPI。
- `template.TemplateRenderer`（可选）：模板渲染 SPI。
- `model`：Java `record` 模型（`NotificationRequest` / `NotificationResponse` /
  `ChannelSendRequest` / `ChannelSendResponse`）。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-gotone-api</artifactId>
</dependency>
```

仅引入 api 不会产生任何 bean；装配由 `-engine`、`-store-jdbc` 与 `-binder-*` 负责。

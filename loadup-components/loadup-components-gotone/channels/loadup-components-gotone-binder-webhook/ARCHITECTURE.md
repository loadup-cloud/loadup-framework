# Loadup Gotone Binder Webhook 架构

## 职责与边界

多渠道通知的具体后端适配模块；由集成方按需引入。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-gotone-api`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-autoconfigure`
- `org.slf4j:slf4j-api`
- `tools.jackson.core:jackson-databind`

## 实现入口

主要入口文件：

- [`WebhookChannelAutoConfiguration`](src/main/java/io/github/loadup/components/gotone/channel/webhook/config/WebhookChannelAutoConfiguration.java)
- [`DingtalkWebhookProvider`](src/main/java/io/github/loadup/components/gotone/channel/webhook/provider/DingtalkWebhookProvider.java)
- [`FeishuWebhookProvider`](src/main/java/io/github/loadup/components/gotone/channel/webhook/provider/FeishuWebhookProvider.java)
- [`WechatWebhookProvider`](src/main/java/io/github/loadup/components/gotone/channel/webhook/provider/WechatWebhookProvider.java)

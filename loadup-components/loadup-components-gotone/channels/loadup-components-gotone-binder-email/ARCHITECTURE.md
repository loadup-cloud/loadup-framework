# Loadup Gotone Binder Email 架构

## 职责与边界

多渠道通知的具体后端适配模块；由集成方按需引入。

## Maven 依赖边界

直接依赖的框架模块：

- `loadup-components-gotone-api`

直接依赖的外部坐标（不含测试与 provided scope）：

- `org.springframework.boot:spring-boot-starter-mail`

## 实现入口

主要入口文件：

- [`EmailChannelAutoConfiguration`](src/main/java/io/github/loadup/components/gotone/channel/email/config/EmailChannelAutoConfiguration.java)
- [`SmtpEmailProvider`](src/main/java/io/github/loadup/components/gotone/channel/email/SmtpEmailProvider.java)

# Loadup Gotone API 架构

## 职责与边界

多渠道通知的业务契约与接口模块；实现由独立模块提供。

## 实现入口

主要入口文件：

- [`NotificationChannelProvider`](src/main/java/io/github/loadup/components/gotone/NotificationChannelProvider.java)
- [`NotificationService`](src/main/java/io/github/loadup/components/gotone/NotificationService.java)
- [`ChannelConfigProvider`](src/main/java/io/github/loadup/components/gotone/config/ChannelConfigProvider.java)
- [`ServiceConfigProvider`](src/main/java/io/github/loadup/components/gotone/config/ServiceConfigProvider.java)

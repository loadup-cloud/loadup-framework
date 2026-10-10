# LoadUp Notification App

应用用例编排、Spring Bean 装配与客户端 DTO 映射。

## 接入

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-modules-notification-app</artifactId>
</dependency>
```

版本由 `loadup-dependencies` BOM 管理。程序化接入注入 `io.github.loadup.modules.notification.client.facade.InboxFacade`，对外返回 client DTO。自动带入 infrastructure；配置使用 `loadup.modules.notification.*`，所需 DFS/Gotone/RetryTask 依赖与服务操作示例见上层接入手册。

## 能力契约

| 能力 | 本层提供 |
| --- | --- |
| 应用用例编排、Spring Bean 装配与客户端 DTO 映射。 | 是 |
| HTTP 适配 | 由同一业务目录下可选 web 提供 |

完整业务 API、配置及使用示例见 [模块接入手册](../README.md)；内部职责见 [ARCHITECTURE.md](ARCHITECTURE.md)。

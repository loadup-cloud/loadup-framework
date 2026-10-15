# LoadUp Gotone Binder Push

gotone 的 **推送渠道**，提供 Firebase Cloud Messaging（FCM）的 `NotificationChannelProvider`
实现与配置类。

> **状态：stub**。provider 的 `send` 目前为占位实现（统一失败），真实 Firebase Admin SDK 传输仍待实现与验收。

即使配置了非空 server-key，有效设备令牌的占位发送仍返回失败（`INVALID_STATE`），不会将日志打印视为成功投递。`isAvailable()` 仅检查凭证配置，不证明传输已实现或 FCM 可达。当前测试验证输入、结果与扩展边界，不代表真实 FCM 集成验收。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-gotone-binder-push</artifactId>
</dependency>
```

## 配置

```yaml
loadup:
  gotone:
    binder:
      push:
        fcm:
          enabled: true     # matchIfMissing=true
          server-key: ${FCM_SERVER_KEY}
          project-id: ${FCM_PROJECT_ID}
```

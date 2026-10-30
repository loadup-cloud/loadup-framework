# LoadUp Gotone Binder SMS

gotone 的 **短信渠道**，提供 aliyun / huawei / yunpian 三个 provider 的 `NotificationChannelProvider`
实现与配置类。

> **状态：stub**。provider 的 `send` 目前为占位实现（统一失败），待接厂商 SDK 后即可用；
> SPI 契约与配置结构已定型，业务侧与引擎不受影响。

## 引入

```xml
<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-gotone-binder-sms</artifactId>
</dependency>
```

## 配置

```yaml
loadup:
  gotone:
    binder:
      sms:
        aliyun:
          enabled: true
          access-key-id: ${ALIYUN_AK}
          access-key-secret: ${ALIYUN_SK}
          sign-name: LoadUp
          region-id: cn-hangzhou
        huawei:
          enabled: false
          app-key: ...
          app-secret: ...
          sender: ...
          signature: ...
          endpoint: ...
        yunpian:
          enabled: false
          api-key: ...
          api-url: https://sms.yunpian.com/v2/sms/single_send.json
```

每个 provider 均可独立开关；渠道级参数（模板 id 等）由 `ChannelConfig.channelConfig` 在
发送时解析。

## 当前发送与测试边界

三个内置发送路径在凭证存在时也明确返回失败，不把占位日志计为发送成功。`isAvailable()` 当前仅表示必需凭证已配置；真实厂商
SDK/HTTP 传输尚未接入。业务侧通过逐收件人的状态与错误判断发送结果。

渠道参数覆盖、无效号码、部分失败、配置装配和开关由契约测试验证。测试替身只验证 SPI 边界，不代表阿里云、华为云或云片已完成真实发送验收。
`ChannelSendRequest` 已复制并规范化收件人及配置，provider 依赖这一输入契约。

# LoadUp Gotone Channel Binders

Each channel binder is an independent Maven artifact that registers one or more
`NotificationChannelProvider` beans. Add the binders you need; the engine discovers every
registered provider and builds its fallback chains automatically.

| Artifact | Providers | Status |
| --- | --- | --- |
| `loadup-components-gotone-binder-email` | `smtp` (Spring Mail) | Production-ready |
| `loadup-components-gotone-binder-sms` | `aliyun` / `huawei` / `yunpian` | Stub (vendor SDK call pending) |
| `loadup-components-gotone-binder-push` | `fcm` | Stub (Firebase Admin SDK call pending) |
| `loadup-components-gotone-binder-webhook` | `dingtalk` / `wechat` / `feishu` | Production-ready (real HTTP) |

Every provider can be disabled with `loadup.gotone.binder.<channel>.<provider>.enabled=false`.
Channel-level configuration (subject, template id, webhook URL, ...) is resolved at send time from
the `channelConfig` map of the matched `ChannelConfig`.

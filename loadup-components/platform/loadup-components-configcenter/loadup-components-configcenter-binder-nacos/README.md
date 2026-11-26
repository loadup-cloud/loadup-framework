# ConfigCenter Binder Nacos

使用 `loadup-components-configcenter-binder-nacos` 和 `loadup.configcenter.binder-type: nacos`。在 `loadup.configcenter.binder.nacos` 配置 `server-addr`、`namespace`、`data-id`、`group`、`file-extension`、毫秒级 `timeout` 和可选的 `username`、`password`。文档格式支持 `properties`、`yaml` 和 `yml`。

| 能力 | 支持 |
|---|---|
| 查值、列出键 | SDK 文档快照 |
| 注解或程序化监听 | SDK 文档监听，按键对比变更 |
| 写入和删除 | 读取最新完整文档后携带 MD5 原子比较并发布，冲突时返回 `false` |

变更通过 SDK 推送后进入本地快照。每次写入会重新渲染整个配置文档，适合将配置单独放在应用专用的 `data-id`。

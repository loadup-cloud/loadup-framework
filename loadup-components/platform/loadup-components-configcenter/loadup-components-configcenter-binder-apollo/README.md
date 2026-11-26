# ConfigCenter Binder Apollo

使用 `loadup-components-configcenter-binder-apollo` 和 `loadup.configcenter.binder-type: apollo`。在 `loadup.configcenter.binder.apollo` 配置 `meta`、`app-id`、`env`、`cluster` 和 `namespace`。默认环境、集群和 namespace 分别为 `DEV`、`default` 和 `application`。

| 能力 | 支持 |
|---|---|
| 查值、列出键 | Apollo SDK 已发布的 namespace |
| 注解或程序化监听 | Apollo SDK namespace 监听 |
| 写入和删除 | 可选。配置 `portal-url`、`portal-token` 和 `portal-operator` 后启用，token 需要目标 namespace 的编辑和发布权限 |

Portal 发布以 namespace 为单位，发布时会一并提交该 namespace 下的其他待发布变更。建议为程序化发布配置专用 namespace。编辑成功后发布仍可能失败，此时方法返回 `false`，编辑内容可能保留在 Portal 草稿中。

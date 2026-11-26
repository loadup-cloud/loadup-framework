# ConfigCenter Apollo 架构

`ApolloConfigCenterAutoConfiguration` 在 `binder-type=apollo` 时创建 Provider。Apollo SDK 提供已发布的配置查询和监听，销毁 Provider 时移除 SDK 回调。启用 Portal 凭证后，`setConfig` 和 `removeConfig` 通过 Apollo OpenAPI SDK 编辑配置，并提交 namespace 发布。应用通过 Apollo SDK 的发布通知取得变更。

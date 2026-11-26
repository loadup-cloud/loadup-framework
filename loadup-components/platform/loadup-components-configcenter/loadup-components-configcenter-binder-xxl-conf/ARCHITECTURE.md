# ConfigCenter XXL-Conf 架构

`XxlConfConfigCenterAutoConfiguration` 在 `binder-type=xxl-conf` 时创建 Provider。Provider 使用应用和环境限定 OpenAPI 请求范围，用访问令牌认证。

单个后台任务轮询当前应用的变更。响应、超时或失败后，任务向 OpenAPI 批量读取正在订阅的键并逐键对比。查询失败会保留上一次的监听值，已删除的键按 `null` 分发。Bean 销毁时停止任务并关闭 HTTP 客户端。

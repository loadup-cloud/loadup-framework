# ConfigCenter Nacos 架构

`NacosConfigCenterAutoConfiguration` 在 `binder-type=nacos` 时创建 Provider。Provider 用 SDK 获取初始文档并注册一个监听器，将文档变更分发给对应键的订阅者。Bean 销毁时解除 SDK 监听并停止客户端。

写入和删除按整个配置文档进行。Provider 读取最新内容与 MD5，根据键修改文档，并调用 SDK 的 CAS 发布。写操作的返回值反映提交结果，读取快照和回调随 SDK 通知更新。

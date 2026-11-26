# ConfigCenter API 架构

`ConfigCenterAutoConfiguration` 在存在唯一 `ConfigCenterProvider` 时装配默认 `ConfigCenterTemplate` 和注解处理器。自定义 Template 可覆盖默认实现，注解处理器仍通过活跃 Provider 注册订阅。

`ConfigCenterProvider.subscribe` 返回单个订阅的解除操作。注解处理器在 bean 销毁时解除对应订阅，Template 只管理通过自身登记的订阅。缺失配置和配置删除统一表示为 `null`，Template 和注解处理器分别应用调用方提供的默认值。查询失败由 Provider 报错，程序化发布以布尔值报告结果。

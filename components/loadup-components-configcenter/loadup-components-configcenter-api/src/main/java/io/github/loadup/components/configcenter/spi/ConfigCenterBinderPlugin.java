package io.github.loadup.components.configcenter.spi;

/**
 * 配置中心 Binder 插件标记接口。
 *
 * <p>预留给第三方扩展使用（如 Spring Cloud Config、Consul、etcd 等），
 * 实现此接口并注册为 Spring Bean 即可被框架自动发现。
 *
 * <p>当前版本不对此接口定义任何方法约束，
 * 实际扩展应参照现有 binder 实现（Local / Nacos / Apollo）的方式注册
 * {@link io.github.loadup.framework.api.manager.BindingMetadata} Bean。
 */
public interface ConfigCenterBinderPlugin {}

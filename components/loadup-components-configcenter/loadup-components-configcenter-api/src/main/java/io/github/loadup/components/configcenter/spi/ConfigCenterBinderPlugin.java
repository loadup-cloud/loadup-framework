package io.github.loadup.components.configcenter.spi;

/**
 * Marker interface for third-party config-center binder plugins.
 *
 * <p>Reserved for extensions such as Spring Cloud Config, Consul, etcd, etc.
 * Implement this interface and register a Spring Bean to be auto-discovered.
 *
 * <p>The current version imposes no method constraints on this interface.
 * Extensions should follow the pattern of existing binder implementations
 * (Local / Nacos / Apollo) and register a
 * {@link io.github.loadup.framework.api.manager.BindingMetadata} bean.
 */
public interface ConfigCenterBinderPlugin {}

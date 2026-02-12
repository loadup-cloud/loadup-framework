package io.github.loadup.framework.api.binder;

public interface BinderConfig {
    /**
     * 强制返回配置的唯一特征码
     * 实现类可以返回所有影响物理连接的字段拼接串，或者直接返回自身的 Hash
     */
    Object getIdentity();
}

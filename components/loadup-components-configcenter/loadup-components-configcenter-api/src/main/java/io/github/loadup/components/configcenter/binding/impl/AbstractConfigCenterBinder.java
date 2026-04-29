package io.github.loadup.components.configcenter.binding.impl;

import io.github.loadup.components.configcenter.binder.ConfigCenterBinder;
import io.github.loadup.components.configcenter.cfg.ConfigCenterBinderCfg;
import io.github.loadup.components.configcenter.cfg.ConfigCenterBindingCfg;
import io.github.loadup.framework.api.binder.AbstractBinder;

/**
 * 配置中心 Binder 抽象基类。
 *
 * <p>继承自 {@link AbstractBinder}，提供公共的配置访问方法，
 * 使 {@link DefaultConfigCenterBinding} 能通过类型检查获取 binderCfg。
 *
 * @param <C> binder 级别配置
 * @param <S> binding 级别配置
 */
public abstract class AbstractConfigCenterBinder<C extends ConfigCenterBinderCfg, S extends ConfigCenterBindingCfg>
        extends AbstractBinder<C, S> implements ConfigCenterBinder<C, S> {}

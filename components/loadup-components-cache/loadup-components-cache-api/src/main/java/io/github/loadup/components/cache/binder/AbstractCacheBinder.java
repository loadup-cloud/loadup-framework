package io.github.loadup.components.cache.binder;

/*-
 * #%L
 * loadup-components-cache-api
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.commons.util.StringUtils;
import io.github.loadup.components.cache.cfg.CacheBinderCfg;
import io.github.loadup.components.cache.cfg.CacheBindingCfg;
import io.github.loadup.components.cache.model.CacheValueWrapper;
import io.github.loadup.components.cache.serializer.CacheSerializer;
import io.github.loadup.framework.api.binder.AbstractBinder;
import io.github.loadup.framework.api.manager.ConfigurationResolver;

public abstract class AbstractCacheBinder<C extends CacheBinderCfg, S extends CacheBindingCfg>
    extends AbstractBinder<C, S> implements CacheBinder<C, S> {
    protected CacheSerializer serializer;
    protected CacheTicker ticker; // 时间源

    @Override
    protected void afterConfigInjected(String name, C binderCfg, S bindingCfg) {
        this.binderCfg = binderCfg;
        this.bindingCfg = bindingCfg;
        // 自动从容器中根据配置的名称获取序列化器
        this.resolveInternalComponents();
        // 执行子类特有的初始化逻辑
        onInit();
    }

    private void resolveInternalComponents() {
        // 1. 确定序列化器：优先级 Binding > Binder
        String serializerName =
            ConfigurationResolver.resolve(bindingCfg.getSerializerBeanName(), binderCfg.getSerializerBeanName());
        if (StringUtils.isNotBlank(serializerName)) {
            this.serializer = context.getBean(serializerName, CacheSerializer.class);
        }
        // 2. 确定时间源
        String tickerName = binderCfg.getTickerBeanName();
        this.ticker =
            context.containsBean(tickerName) ? context.getBean(tickerName, CacheTicker.class) : CacheTicker.SYSTEM;
    }

    protected abstract void onInit();

    public CacheSerializer getSerializer() {
        return serializer;
    }

    protected Object wrapValue(CacheValueWrapper value) {
        if (value == null) return null;

        // 如果当前驱动配置了序列化器，则转为字节数组
        if (this.serializer != null) {
            return this.serializer.serialize(value);
        }

        // 如果没配置（如纯内存模式），直接返回原始对象
        return value;
    }
}

package io.github.loadup.framework.api.manager;

/*-
 * #%L
 * Loadup Common Api
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import io.github.loadup.exception.BinderNotFoundException;
import io.github.loadup.framework.api.binder.Binder;
import io.github.loadup.framework.api.binding.Binding;
import io.github.loadup.framework.api.cfg.BaseBinderCfg;
import io.github.loadup.framework.api.cfg.BaseBindingCfg;
import io.github.loadup.framework.api.context.BindingContext;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

public abstract class BindingManagerSupport<B extends Binder, T extends Binding> {
    protected final ApplicationContext context;
    protected final String configPrefix;

    protected final Map<String, BindingMetadata<?, ?, ?, ?>> registry = new ConcurrentHashMap<>();
    protected final Map<String, T> bindingCache = new ConcurrentHashMap<>();
    protected final Map<String, Object> binderCfgCache = new ConcurrentHashMap<>();

    protected BindingManagerSupport(ApplicationContext context, String configPrefix) {
        this.context = context;
        this.configPrefix = configPrefix;
    }

    public void register(String type, BindingMetadata<?, ?, ?, ?> meta) {
        registry.put(type.toLowerCase(), meta);
    }

    public <R extends T> R getBinding() {
        return getBinding("default");
    }

    @SuppressWarnings("unchecked")
    public <R extends T> R getBinding(String bizTag) {
        if (!StringUtils.hasText(bizTag)) {
            bizTag = "default";
        }
        return (R) bindingCache.computeIfAbsent(bizTag, tag -> {
            // 1. 探测类型 (逻辑不变)
            BaseBindingCfg basic = resolveConfig(configPrefix + ".bindings." + tag, BaseBindingCfg.class);
            String binderTypeStr =
                    (basic.getBinderType() != null) ? basic.getBinderType().toLowerCase() : getDefaultBinderType();
            // 这里取第一个类型作为 Meta 获取的 Key (通常同领域的 Meta 是共享的)
            String primaryType = binderTypeStr.split(",")[0].trim().toLowerCase();
            // 2. 获取元数据
            BindingMetadata<?, ?, ?, ?> meta = registry.get(primaryType);
            if (meta == null) {
                throw new BinderNotFoundException("Binder not registered: " + primaryType);
            }

            // 3. 关键：调用泛型捕获方法，将通配符转换为具体的泛型 B, C_BIND, C_BINDR
            return (T) captureAndCreate(tag, binderTypeStr, (BindingMetadata) meta);
        });
    }

    /** 捕获方法：在这个方法内，泛型被重新绑定，编译器不再抱怨 Object 转换问题 */
    @SuppressWarnings("unchecked")
    private <B_SUB extends B, CBIND extends BaseBindingCfg, CBINDR extends BaseBinderCfg, T_SUB extends T>
            T_SUB captureAndCreate(String tag, String binderTypes, BindingMetadata<B_SUB, CBIND, CBINDR, T_SUB> meta) {
        // 解析配置
        CBIND bindingCfg = resolveConfig(configPrefix + ".bindings." + tag, meta.bindingCfgClass);

        // 创建驱动列表 (支持多级缓存核心逻辑)
        List<B_SUB> binders = new ArrayList<>();
        for (String type : binderTypes.split(",")) {
            String trimmedType = type.trim().toLowerCase();

            // 获取或缓存组件层配置 (如 Redis 的 Host/DB)
            CBINDR binderCfg = (CBINDR) binderCfgCache.computeIfAbsent(
                    trimmedType, t -> resolveConfig(configPrefix + ".binders." + t, meta.binderCfgClass));

            // 创建并注入配置
            binders.add(createOne(meta, tag, binderCfg, bindingCfg));
        }
        // 构造上下文
        BindingContext<B_SUB, CBIND> ctx = new BindingContext<>(tag, binderTypes, bindingCfg, binders, context);

        // 调用工厂创建 Binding 并初始化
        T_SUB bindingInstance = meta.factory.create(ctx);
        bindingInstance.init(ctx);
        return bindingInstance;
    }

    private <B_SUB extends B, CBIND extends BaseBindingCfg, CBINDR extends BaseBinderCfg, T_SUB extends T>
            B_SUB createOne(
                    BindingMetadata<B_SUB, CBIND, CBINDR, T_SUB> meta,
                    String name,
                    CBINDR binderCfg,
                    CBIND bindingCfg) {

        // 利用 Spring 容器创建实例，保证 @Autowired 生效
        B_SUB binderInstance = (B_SUB) context.getAutowireCapableBeanFactory().createBean(meta.binderClass);

        // 执行双重配置注入：业务名、组件配置、业务覆盖配置
        binderInstance.injectConfigs(name, binderCfg, bindingCfg);

        return binderInstance;
    }

    protected abstract String getDefaultBinderType();

    public abstract Class<T> getBindingInterface();

    public abstract Class<B> getBinderInterface();

    protected <C> C resolveConfig(String path, Class<C> configClass) {
        return org.springframework.boot.context.properties.bind.Binder.get(context.getEnvironment())
                .bind(path, configClass)
                .orElseGet(() -> createDefaultConfig(configClass));
    }

    private <C> C createDefaultConfig(Class<C> configClass) {
        // 这里可以根据业务决定是直接报错还是返回空对象
        try {
            return configClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } // 如果是 getBinding 探测，建议报错：bizTag 未在配置文件中定义
    }
}

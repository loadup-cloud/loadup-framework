package io.github.loadup.framework.api.core;

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

import io.github.loadup.framework.api.annotation.BindingClient;
import io.github.loadup.framework.api.manager.BindingManagerSupport;
import io.github.loadup.framework.api.manager.BindingMetadata;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ReflectionUtils;

/** 核心注入处理器 优化点：支持 AOP 代理类、增加类元数据缓存、解耦元数据注册逻辑 */
public class BindingPostProcessor implements BeanPostProcessor, PriorityOrdered, InitializingBean {

    private final ApplicationContext context;

    // 缓存：字段类型 -> 对应的 Manager 实例
    private final Map<Class<?>, BindingManagerSupport<?, ?>> managerCache = new ConcurrentHashMap<>();

    // 缓存：Class -> 该类中带有 @BindingClient 的所有字段，避免重复反射扫描
    private final Map<Class<?>, Field[]> injectionMetadataCache = new ConcurrentHashMap<>();

    public BindingPostProcessor(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public int getOrder() {
        // 建议比 AutowiredAnnotationBeanPostProcessor 优先级略低或持平
        return PriorityOrdered.HIGHEST_PRECEDENCE;
    }

    /** 在注入器初始化时，自动完成所有 Manager 对 Metadata 的收集注册 */
    @Override
    public void afterPropertiesSet() {
        Map<String, BindingManagerSupport> allManagers = context.getBeansOfType(BindingManagerSupport.class);
        Map<String, BindingMetadata> allMetas = context.getBeansOfType(BindingMetadata.class);

        allManagers.values().forEach(manager -> {
            allMetas.values().forEach(meta -> {
                // 只有当 Metadata 的驱动接口属于该 Manager 领域时才注册
                // 例如 CacheBindingManager 只注册继承了 CacheBinder 的 Metadata
                if (manager.getBinderInterface().isAssignableFrom(meta.binderClass)) {
                    manager.register(meta.type, meta);
                }
            });
        });
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 获取原始类（处理 CGLIB 代理）
        Class<?> targetClass = AopUtils.getTargetClass(bean);

        Field[] fields = injectionMetadataCache.computeIfAbsent(targetClass, this::findBindingFields);

        for (Field field : fields) {
            BindingClient annotation = field.getAnnotation(BindingClient.class);
            if (annotation != null) {
                injectBinding(bean, field, annotation);
            }
        }

        return bean;
    }

    private Field[] findBindingFields(Class<?> clazz) {
        final java.util.List<Field> fields = new java.util.ArrayList<>();
        ReflectionUtils.doWithFields(clazz, field -> {
            if (field.isAnnotationPresent(BindingClient.class)) {
                fields.add(field);
            }
        });
        return fields.toArray(new Field[0]);
    }

    private void injectBinding(Object bean, Field field, BindingClient annotation) {
        String bizTag = annotation.value();
        Class<?> fieldType = field.getType();

        BindingManagerSupport<?, ?> manager = managerCache.computeIfAbsent(fieldType, this::findManagerForType);

        if (manager == null) {
            throw new IllegalStateException(String.format(
                    "Domain mismatch: No BindingManager found for field [%s] of type [%s]. "
                            + "Ensure the correct Starter is in your classpath.",
                    field.getName(), fieldType.getSimpleName()));
        }

        Object binding = manager.getBinding(bizTag);

        ReflectionUtils.makeAccessible(field);
        try {
            field.set(bean, binding);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to inject binding into field: " + field.getName(), e);
        }
    }

    @SuppressWarnings("rawtypes")
    private BindingManagerSupport<?, ?> findManagerForType(Class<?> targetType) {
        Map<String, BindingManagerSupport> allManagers = context.getBeansOfType(BindingManagerSupport.class);
        for (BindingManagerSupport manager : allManagers.values()) {
            if (manager.getBindingInterface().isAssignableFrom(targetType)) {
                return manager;
            }
        }
        return null;
    }
}

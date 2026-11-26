/*
 * Copyright (C) 2026 LoadUp Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.loadup.components.configcenter.autoconfigure;

import io.github.loadup.components.configcenter.ConfigCenterProvider;
import io.github.loadup.components.configcenter.DynamicConfig;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

final class DynamicConfigBeanPostProcessor implements DestructionAwareBeanPostProcessor, BeanFactoryAware {
    private final ObjectProvider<ConfigCenterProvider> providers;
    private final Environment environment;
    private final Map<String, List<Runnable>> subscriptions = new ConcurrentHashMap<>();
    private BeanFactory beanFactory;

    DynamicConfigBeanPostProcessor(ObjectProvider<ConfigCenterProvider> providers, Environment environment) {
        this.providers = providers;
        this.environment = environment;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Object target = singletonTarget(bean);
        Class<?> type = AopUtils.getTargetClass(target);
        List<Binding> bindings = findBindings(bean, target, type);
        if (bindings.isEmpty()) {
            return bean;
        }
        if (!beanFactory.isSingleton(beanName)) {
            throw new BeanCreationException(beanName, "@DynamicConfig requires a singleton bean");
        }

        List<Runnable> active = new ArrayList<>();
        try {
            ConfigCenterProvider provider = providers.getObject();
            for (Binding binding : bindings) {
                active.add(binding.register(provider));
            }
            subscriptions.put(beanName, active);
            return bean;
        } catch (RuntimeException e) {
            active.forEach(Runnable::run);
            throw new BeanCreationException(beanName, "Could not initialize @DynamicConfig bindings", e);
        }
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return true;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) {
        List<Runnable> active = subscriptions.remove(beanName);
        if (active != null) {
            active.forEach(Runnable::run);
        }
    }

    private List<Binding> findBindings(Object bean, Object target, Class<?> type) {
        List<Binding> bindings = new ArrayList<>();
        ReflectionUtils.doWithFields(
                type,
                field -> {
                    DynamicConfig annotation = field.getAnnotation(DynamicConfig.class);
                    int modifiers = field.getModifiers();
                    if (Modifier.isFinal(modifiers)
                            || Modifier.isStatic(modifiers)
                            || !Modifier.isVolatile(modifiers)
                            || !field.getDeclaringClass().isInstance(target)) {
                        throw new IllegalArgumentException(
                                "@DynamicConfig requires a volatile instance field: " + field);
                    }
                    ReflectionUtils.makeAccessible(field);
                    TypeDescriptor fieldType = new TypeDescriptor(field);
                    bindings.add(binding(
                            annotation,
                            value -> ReflectionUtils.setField(
                                    field,
                                    target,
                                    DefaultConversionService.getSharedInstance()
                                            .convert(value, TypeDescriptor.valueOf(String.class), fieldType))));
                },
                field -> field.isAnnotationPresent(DynamicConfig.class));

        MethodIntrospector.selectMethods(type, (MethodIntrospector.MetadataLookup<DynamicConfig>)
                        method -> method.getAnnotation(DynamicConfig.class))
                .forEach((method, annotation) -> {
                    if (Modifier.isStatic(method.getModifiers())
                            || method.getReturnType() != void.class
                            || method.getParameterCount() != 1
                            || method.getParameterTypes()[0] != String.class) {
                        throw new IllegalArgumentException(
                                "@DynamicConfig requires a void method with one String argument: " + method);
                    }
                    Object receiver = Modifier.isPrivate(method.getModifiers()) ? target : bean;
                    Method invocable = AopUtils.selectInvocableMethod(method, receiver.getClass());
                    ReflectionUtils.makeAccessible(invocable);
                    bindings.add(
                            binding(annotation, value -> ReflectionUtils.invokeMethod(invocable, receiver, value)));
                });
        return bindings;
    }

    private Binding binding(DynamicConfig annotation, Consumer<String> update) {
        String key = environment.resolveRequiredPlaceholders(annotation.value());
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("@DynamicConfig requires a nonblank key");
        }
        String fallback = DynamicConfig.NO_DEFAULT.equals(annotation.defaultValue())
                ? null
                : environment.resolveRequiredPlaceholders(annotation.defaultValue());
        return new Binding(key, fallback, update);
    }

    private static Object singletonTarget(Object bean) {
        Object target = bean;
        Object nested = AopProxyUtils.getSingletonTarget(target);
        while (nested != null) {
            target = nested;
            nested = AopProxyUtils.getSingletonTarget(target);
        }
        return target;
    }

    private static final class Binding {
        private final String key;
        private final String fallback;
        private final Consumer<String> update;
        private boolean initialized;
        private volatile boolean closed;
        private String current;

        private Binding(String key, String fallback, Consumer<String> update) {
            this.key = key;
            this.fallback = fallback;
            this.update = update;
        }

        private synchronized Runnable register(ConfigCenterProvider provider) {
            Runnable unsubscribe = provider.subscribe(key, this::apply);
            try {
                apply(provider.getConfig(key));
            } catch (RuntimeException e) {
                stop(unsubscribe);
                throw e;
            }
            return () -> stop(unsubscribe);
        }

        private synchronized void apply(String value) {
            String resolved = value == null ? fallback : value;
            if (!closed && (!initialized || !Objects.equals(current, resolved))) {
                update.accept(resolved);
                current = resolved;
                initialized = true;
            }
        }

        private void stop(Runnable unsubscribe) {
            closed = true;
            unsubscribe.run();
        }
    }
}

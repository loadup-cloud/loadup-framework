/*
 * #%L
 * Loadup Components Extension
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
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
 * #L%
 */
package io.github.loadup.components.extension.register;

import io.github.loadup.commons.json.ToStringAsJson;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.extension.annotation.Extension;
import io.github.loadup.components.extension.api.IExtensionPoint;
import io.github.loadup.components.extension.core.BizScenario;
import io.github.loadup.components.extension.spi.ExtensionProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class ExtensionRegistry implements ApplicationListener<ContextRefreshedEvent> {

    private final Map<Class<?>, List<ExtensionCoordinate>> extensionRegister = new ConcurrentHashMap<>();

    private final Map<String, List<ExtensionCoordinate>> bizCodeIndex = new ConcurrentHashMap<>();

    private final Map<ScenarioKey, List<ExtensionCoordinate>> scenarioCache =
            Collections.synchronizedMap(new WeakHashMap<>());

    private volatile boolean initialized = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();

        if (applicationContext.getParent() == null) {
            LogUtil.info(ExtensionRegistry.class, "Starting to register all extensions...");

            Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Extension.class);
            beans.values().forEach(this::registerExtension);

            Map<String, ExtensionProvider> providers = applicationContext.getBeansOfType(ExtensionProvider.class);
            providers.values().forEach(this::registerSpiExtension);

            prewarmCache();

            initialized = true;

            LogUtil.info(
                    ExtensionRegistry.class,
                    "Extension registration completed. Total {} extension point types loaded with {} implementations.",
                    extensionRegister.size(),
                    extensionRegister.values().stream().mapToInt(List::size).sum());
        }
    }

    private void registerExtension(Object bean) {
        Class<?> beanClass = bean.getClass();
        Extension extensionAnn = beanClass.getAnnotation(Extension.class);
        if (extensionAnn == null) {
            return;
        }

        Class<?>[] interfaces = beanClass.getInterfaces();
        for (Class<?> intf : interfaces) {
            if (IExtensionPoint.class.isAssignableFrom(intf)) {
                ExtensionCoordinate coordinate = new ExtensionCoordinate(bean, extensionAnn);
                registerCoordinate(intf, coordinate);
                LogUtil.debug(
                        ExtensionRegistry.class,
                        "Registered extension [{} | bizCode:{}, useCase:{}, scenario:{}] for interface [{}].",
                        bean.getClass().getSimpleName(),
                        extensionAnn.bizCode(),
                        extensionAnn.useCase(),
                        extensionAnn.scenario(),
                        intf.getSimpleName());
            }
        }
    }

    private void registerSpiExtension(ExtensionProvider provider) {
        Class<? extends IExtensionPoint> extensionType = provider.getExtensionType();
        IExtensionPoint instance = provider.createExtension();

        Extension virtualExtension = new Extension() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return Extension.class;
            }

            @Override
            public String bizCode() {
                return provider.getBizCode();
            }

            @Override
            public String useCase() {
                return provider.getUseCase();
            }

            @Override
            public String scenario() {
                return provider.getScenario();
            }

            @Override
            public int priority() {
                return provider.getPriority();
            }
        };

        ExtensionCoordinate coordinate = new ExtensionCoordinate(instance, virtualExtension);
        registerCoordinate(extensionType, coordinate);

        LogUtil.debug(
                ExtensionRegistry.class,
                "Registered SPI extension [{} | bizCode:{}, useCase:{}, scenario:{}] for interface [{}].",
                instance.getClass().getSimpleName(),
                provider.getBizCode(),
                provider.getUseCase(),
                provider.getScenario(),
                extensionType.getSimpleName());
    }

    private void registerCoordinate(Class<?> extensionType, ExtensionCoordinate coordinate) {

        List<ExtensionCoordinate> coordinates =
                extensionRegister.computeIfAbsent(extensionType, k -> new ArrayList<>());
        coordinates.add(coordinate);

        String bizCode = coordinate.extensionMetadata().bizCode();
        List<ExtensionCoordinate> bizCodeList = bizCodeIndex.computeIfAbsent(bizCode, k -> new ArrayList<>());
        bizCodeList.add(coordinate);
    }

    public <T> List<ExtensionCoordinate> getExtensionCoordinates(Class<T> extensionPointClass) {
        return extensionRegister.getOrDefault(extensionPointClass, Collections.emptyList());
    }

    public List<ExtensionCoordinate> getExtensionsByBizCode(String bizCode) {
        return bizCodeIndex.getOrDefault(bizCode, Collections.emptyList());
    }

    public <T> List<ExtensionCoordinate> getExtensionsByScenario(Class<T> extensionPointClass, BizScenario scenario) {

        if (initialized) {
            ScenarioKey key = new ScenarioKey(extensionPointClass, scenario);
            return scenarioCache.computeIfAbsent(key, k -> computeExtensionsByScenario(extensionPointClass, scenario));
        }

        return computeExtensionsByScenario(extensionPointClass, scenario);
    }

    private <T> List<ExtensionCoordinate> computeExtensionsByScenario(
            Class<T> extensionPointClass, BizScenario scenario) {
        List<ExtensionCoordinate> candidates = getExtensionCoordinates(extensionPointClass);
        return candidates.stream()
                .filter(c -> matchesScenario(c.extensionMetadata(), scenario))
                .collect(Collectors.toList());
    }

    private void prewarmCache() {
        LogUtil.debug(ExtensionRegistry.class, "Prewarming extension cache...");
        int cacheEntries = 0;

        Set<ScenarioKey> processedScenarios = new HashSet<>();

        for (Map.Entry<Class<?>, List<ExtensionCoordinate>> entry : extensionRegister.entrySet()) {
            Class<?> extensionType = entry.getKey();
            List<ExtensionCoordinate> coordinates = entry.getValue();

            for (ExtensionCoordinate coordinate : coordinates) {
                Extension metadata = coordinate.extensionMetadata();
                BizScenario scenario = BizScenario.valueOf(metadata.bizCode(), metadata.useCase(), metadata.scenario());

                ScenarioKey key = new ScenarioKey(extensionType, scenario);

                if (processedScenarios.add(key)) {
                    scenarioCache.put(key, computeExtensionsByScenario(extensionType, scenario));
                    cacheEntries++;
                }
            }
        }

        LogUtil.debug(ExtensionRegistry.class, "Extension cache prewarmed with {} entries", cacheEntries);
    }

    public Set<Class<?>> getAllExtensionTypes() {
        return extensionRegister.keySet();
    }

    public Set<String> getAllBizCodes() {
        return bizCodeIndex.keySet();
    }

    private boolean matchesScenario(Extension extension, BizScenario scenario) {
        return extension.bizCode().equals(scenario.bizCode())
                && extension.useCase().equals(scenario.useCase())
                && extension.scenario().equals(scenario.scenario());
    }

    private static class ScenarioKey {
        private final Class<?> extensionType;
        private final String bizCode;
        private final String useCase;
        private final String scenario;

        ScenarioKey(Class<?> extensionType, BizScenario bizScenario) {
            this.extensionType = extensionType;
            this.bizCode = bizScenario.bizCode();
            this.useCase = bizScenario.useCase();
            this.scenario = bizScenario.scenario();
        }
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}

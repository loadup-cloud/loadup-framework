/*
 * #%L
 * Loadup Gotone Engine
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
package io.github.loadup.components.gotone.engine;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.NotificationService;
import io.github.loadup.components.gotone.config.ChannelConfigProvider;
import io.github.loadup.components.gotone.record.RecordHandler;
import io.github.loadup.components.gotone.template.TemplateRenderer;
import io.github.loadup.components.resilience4j.ResilienceRegistries;
import io.github.loadup.components.resilience4j.core.Resilience4jCoreAutoConfiguration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;

@AutoConfiguration(after = Resilience4jCoreAutoConfiguration.class)
@EnableConfigurationProperties(GotoneResilienceProperties.class)
public class GotoneEngineAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NotificationChannelManager notificationChannelManager(
            List<NotificationChannelProvider> providers,
            ObjectProvider<ResilienceRegistries> registries,
            GotoneResilienceProperties properties) {
        ResilienceRegistries resilience = registries.getIfAvailable();
        List<NotificationChannelProvider> effective = providers;
        if (properties.isEnabled() && resilience != null) {
            effective = providers.stream()
                    .map(provider -> (NotificationChannelProvider)
                            ResilientNotificationChannelProvider.wrap(provider, resilience))
                    .toList();
        }
        return new NotificationChannelManager(effective);
    }

    @Bean
    @ConditionalOnMissingBean
    public TemplateRenderer simpleTemplateRenderer() {
        return new SimpleTemplateRenderer();
    }

    @Bean
    @ConditionalOnMissingBean(NotificationService.class)
    public NotificationService notificationService(
            NotificationChannelManager channelManager,
            Optional<ChannelConfigProvider> channelConfigProvider,
            Optional<TemplateRenderer> templateRenderer,
            Optional<RecordHandler> recordHandler,
            Map<String, TaskExecutor> taskExecutors) {
        return new DefaultNotificationService(
                channelManager,
                channelConfigProvider,
                templateRenderer,
                recordHandler,
                selectTaskExecutor(taskExecutors));
    }

    /**
     * Resolve the executor used for asynchronous delivery.
     *
     * <p>Several {@link TaskExecutor} beans are commonly registered: Spring Boot's
     * {@code applicationTaskExecutor} plus a {@code taskScheduler}, which also implements the
     * {@link TaskExecutor} contract. Prefer the application executor, fall back to the only
     * candidate when exactly one exists, and otherwise disable asynchronous delivery so the
     * service sends synchronously.
     */
    private static Optional<TaskExecutor> selectTaskExecutor(Map<String, TaskExecutor> taskExecutors) {
        TaskExecutor applicationExecutor = taskExecutors.get("applicationTaskExecutor");
        if (applicationExecutor != null) {
            return Optional.of(applicationExecutor);
        }
        return taskExecutors.size() == 1
                ? Optional.of(taskExecutors.values().iterator().next())
                : Optional.empty();
    }
}

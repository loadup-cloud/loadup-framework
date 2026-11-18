/*
 * Copyright (C) 2025-2026 LoadUp Cloud
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
package io.github.loadup.components.scheduler;

import io.github.loadup.components.scheduler.config.SchedulerProperties;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

@AutoConfiguration
@EnableConfigurationProperties(SchedulerProperties.class)
@Conditional(SchedulerAutoConfiguration.SchedulerRequested.class)
public class SchedulerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public SchedulerTemplate schedulerTemplate(
            List<SchedulerProvider> providers, List<ScheduledTask> tasks, SchedulerProperties properties) {
        if (providers.size() != 1) {
            throw new IllegalStateException("Expected one scheduler binder for loadup.scheduler.binder-type="
                    + properties.binderType() + ", found " + providers.size());
        }
        return new SchedulerTemplate(providers.getFirst(), tasks, properties.tasks());
    }

    static class SchedulerRequested extends AnyNestedCondition {
        SchedulerRequested() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnBean(SchedulerProvider.class)
        static class BinderAvailable {}

        @ConditionalOnProperty("loadup.scheduler.binder-type")
        static class BinderRequested {}

        @ConditionalOnProperty("loadup.scheduler.tasks[0].name")
        static class SchedulesConfigured {}
    }
}

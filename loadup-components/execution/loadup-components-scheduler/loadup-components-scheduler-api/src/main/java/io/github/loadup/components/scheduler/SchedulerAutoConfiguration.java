/*
 * #%L
 * LoadUp Components Scheduler
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
package io.github.loadup.components.scheduler;

import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(
        afterName = {
            "io.github.loadup.components.scheduler.local.LocalSchedulerAutoConfiguration",
            "io.github.loadup.components.scheduler.powerjob.PowerJobSchedulerAutoConfiguration"
        })
@ConditionalOnBean(SchedulerProvider.class)
public class SchedulerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public SchedulerTemplate schedulerTemplate(List<SchedulerProvider> providers, List<ScheduledTask> tasks) {
        if (providers.size() != 1)
            throw new io.github.loadup.commons.error.CommonException(
                    io.github.loadup.commons.enums.CommonResultCodeEnum.CONFLICT);
        return new SchedulerTemplate(providers.getFirst(), tasks);
    }
}

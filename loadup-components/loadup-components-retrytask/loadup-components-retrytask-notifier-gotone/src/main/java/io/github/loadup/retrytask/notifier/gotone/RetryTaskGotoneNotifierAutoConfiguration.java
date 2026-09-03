/*-
 * #%L
 * Loadup Components Retrytask Notifier Gotone
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
package io.github.loadup.retrytask.notifier.gotone;

import io.github.loadup.components.gotone.NotificationService;
import io.github.loadup.retrytask.facade.RetryTaskNotifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the gotone-backed retry task notifier.
 *
 * <p>Activates after the gotone engine has been configured and before the retry task binder so the
 * notifier is present when the binder collects all {@link RetryTaskNotifier} beans into its
 * failure filter. Only active when a {@link NotificationService} bean exists; the notifier is
 * additive: the default logging notifier of the retry task binder keeps working alongside it.
 */
@AutoConfiguration(
        afterName = "io.github.loadup.components.gotone.engine.GotoneEngineAutoConfiguration",
        beforeName = "io.github.loadup.retrytask.jobrunr.autoconfig.JobRunrRetryTaskAutoConfiguration")
@ConditionalOnClass({RetryTaskNotifier.class, NotificationService.class})
@ConditionalOnBean(NotificationService.class)
@EnableConfigurationProperties(RetryTaskNotifyProperties.class)
public class RetryTaskGotoneNotifierAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "gotoneRetryTaskNotifier")
    public RetryTaskNotifier gotoneRetryTaskNotifier(
            NotificationService notificationService, RetryTaskNotifyProperties properties) {
        return new GotoneRetryTaskNotifier(notificationService, properties);
    }
}

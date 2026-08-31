package io.github.loadup.components.gotone.engine;

/*-
 * #%L
 * Loadup Gotone Engine
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import io.github.loadup.components.gotone.GotoneProvider;
import io.github.loadup.components.gotone.GotoneTemplate;
import io.github.loadup.components.gotone.config.ChannelConfigProvider;
import io.github.loadup.components.gotone.record.RecordHandler;
import io.github.loadup.components.gotone.template.TemplateRenderer;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class GotoneEngineAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NotificationChannelManager notificationChannelManager(List<GotoneProvider> providers) {
        return new NotificationChannelManager(providers);
    }

    @Bean
    @ConditionalOnMissingBean(GotoneTemplate.class)
    public GotoneTemplate gotoneTemplate(
            NotificationChannelManager channelManager,
            Optional<ChannelConfigProvider> channelConfigProvider,
            Optional<TemplateRenderer> templateRenderer,
            Optional<RecordHandler> recordHandler) {
        return new DefaultGotoneTemplate(channelManager, channelConfigProvider, templateRenderer, recordHandler);
    }
}

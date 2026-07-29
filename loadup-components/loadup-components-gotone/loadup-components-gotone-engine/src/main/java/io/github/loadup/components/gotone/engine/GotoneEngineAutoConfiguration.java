package io.github.loadup.components.gotone.engine;

/*-
 * #%L
 * Loadup Gotone Engine
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

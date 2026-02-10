package io.github.loadup.components.gotone.starter.config;

/*-
 * #%L
 * loadup-components-gotone-starter
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import io.github.loadup.components.gotone.api.NotificationChannelProvider;
import io.github.loadup.components.gotone.api.NotificationService;
import io.github.loadup.components.gotone.core.manager.NotificationChannelManager;
import io.github.loadup.components.gotone.core.processor.TemplateProcessor;
import io.github.loadup.components.gotone.core.repository.NotificationRecordRepository;
import io.github.loadup.components.gotone.core.repository.NotificationServiceRepository;
import io.github.loadup.components.gotone.core.repository.ServiceChannelRepository;
import io.github.loadup.components.gotone.core.service.NotificationServiceImpl;
import io.github.loadup.components.gotone.starter.properties.GotoneProperties;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Gotone Auto Configuration.
 *
 * <p>Unified auto-configuration for all gotone components:
 * <ul>
 *   <li>Core services (NotificationService, TemplateService)</li>
 *   <li>Processors (TemplateProcessor)</li>
 *   <li>Managers (NotificationChannelManager)</li>
 *   <li>Channel providers are registered by their own modules</li>
 * </ul>
 */
@Slf4j
@AutoConfiguration
@EnableAsync
@EnableConfigurationProperties(GotoneProperties.class)
@MapperScan("io.github.loadup.components.gotone.core.repository")
@ConditionalOnProperty(prefix = "loadup.gotone", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GotoneAutoConfiguration {

    /**
     * Template Processor Bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public TemplateProcessor templateProcessor() {
        log.info(">>> [GOTONE] TemplateProcessor initialized");
        return new TemplateProcessor();
    }

    /**
     * Notification Channel Manager Bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public NotificationChannelManager notificationChannelManager(List<NotificationChannelProvider> providers) {
        log.info(">>> [GOTONE] NotificationChannelManager initialized with {} providers", providers.size());
        return new NotificationChannelManager(providers);
    }

    /**
     * Notification Service Bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public NotificationService notificationService(
            NotificationServiceRepository serviceRepository,
            ServiceChannelRepository channelRepository,
            NotificationChannelManager channelManager,
            NotificationRecordRepository notificationRecordRepository,
            TemplateProcessor templateProcessor) {
        log.info(">>> [GOTONE] NotificationService initialized");
        return new NotificationServiceImpl(
                serviceRepository, channelRepository, notificationRecordRepository, channelManager, templateProcessor);
    }
}

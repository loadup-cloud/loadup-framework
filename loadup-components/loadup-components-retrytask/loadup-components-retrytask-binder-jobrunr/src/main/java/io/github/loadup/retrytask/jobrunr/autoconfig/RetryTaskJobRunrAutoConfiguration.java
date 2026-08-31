/*-
 * #%L
 * Loadup Components Retrytask Binder JobRunr
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

package io.github.loadup.retrytask.jobrunr.autoconfig;

import io.github.loadup.retrytask.facade.RetryTaskFacade;
import io.github.loadup.retrytask.facade.RetryTaskProcessor;
import io.github.loadup.retrytask.facade.RetryTaskProcessorRegistry;
import io.github.loadup.retrytask.jobrunr.DefaultRetryTaskProcessorRegistry;
import io.github.loadup.retrytask.jobrunr.JobRunrRetryTaskFacade;
import io.github.loadup.retrytask.jobrunr.RetryTaskFailureLoggingFilter;
import io.github.loadup.retrytask.jobrunr.RetryTaskJobRequestHandler;
import io.github.loadup.retrytask.jobrunr.RetryTaskProperties;
import java.util.List;
import org.jobrunr.jobs.lambdas.JobRequest;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.jobrunr.server.BackgroundJobServer;
import org.jobrunr.spring.autoconfigure.JobRunrAutoConfiguration;
import org.jobrunr.storage.StorageProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Wires the JobRunr binder on top of the official {@code jobrunr-spring-boot-4-starter}
 * auto-configuration: facade implementation, processor registry and the JobRequest handler that
 * dispatches jobs to business processors.
 */
@AutoConfiguration(after = JobRunrAutoConfiguration.class)
@ConditionalOnClass({JobRequestScheduler.class, JobRequest.class})
@EnableConfigurationProperties(RetryTaskProperties.class)
public class RetryTaskJobRunrAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RetryTaskProcessorRegistry retryTaskProcessorRegistry(List<RetryTaskProcessor> processors) {
        return new DefaultRetryTaskProcessorRegistry(processors);
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryTaskJobRequestHandler retryTaskJobRequestHandler(RetryTaskProcessorRegistry processorRegistry) {
        return new RetryTaskJobRequestHandler(processorRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryTaskFacade retryTaskFacade(
            JobRequestScheduler scheduler, StorageProvider storageProvider, RetryTaskProperties properties) {
        return new JobRunrRetryTaskFacade(scheduler, storageProvider, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryTaskFailureLoggingFilter retryTaskFailureLoggingFilter() {
        return new RetryTaskFailureLoggingFilter();
    }

    /**
     * Appends the failure filter to the JobRunr {@link BackgroundJobServer} bean without replacing
     * the built-in retry filter registered by the official starter.
     */
    @Bean
    public BeanPostProcessor retryTaskBackgroundJobServerFilterPostProcessor(
            ObjectProvider<RetryTaskFailureLoggingFilter> filterProvider) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof BackgroundJobServer backgroundJobServer) {
                    filterProvider.ifAvailable(filter -> backgroundJobServer.getJobFilters().addAll(List.of(filter)));
                }
                return bean;
            }
        };
    }
}

/*-
 * #%L
 * Loadup Components Retrytask Binder JobRunr
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

package io.github.loadup.retrytask.jobrunr.autoconfig;

import io.github.loadup.retrytask.facade.RetryTaskFacade;
import io.github.loadup.retrytask.facade.RetryTaskNotifier;
import io.github.loadup.retrytask.facade.RetryTaskProcessor;
import io.github.loadup.retrytask.facade.RetryTaskProcessorRegistry;
import io.github.loadup.retrytask.jobrunr.DefaultRetryTaskNotifier;
import io.github.loadup.retrytask.jobrunr.DefaultRetryTaskProcessorRegistry;
import io.github.loadup.retrytask.jobrunr.JobRunrRetryTaskFacade;
import io.github.loadup.retrytask.jobrunr.RetryTaskFailureNotifyingFilter;
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
    public RetryTaskNotifier defaultRetryTaskNotifier() {
        return new DefaultRetryTaskNotifier();
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryTaskFailureNotifyingFilter retryTaskFailureNotifyingFilter(List<RetryTaskNotifier> notifiers) {
        return new RetryTaskFailureNotifyingFilter(notifiers);
    }

    /**
     * Appends the failure filter to the JobRunr {@link BackgroundJobServer} bean without replacing
     * the built-in retry filter registered by the official starter.
     */
    @Bean
    public BeanPostProcessor retryTaskBackgroundJobServerFilterPostProcessor(
            ObjectProvider<RetryTaskFailureNotifyingFilter> filterProvider) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof BackgroundJobServer backgroundJobServer) {
                    filterProvider.ifAvailable(
                            filter -> backgroundJobServer.getJobFilters().addAll(List.of(filter)));
                }
                return bean;
            }
        };
    }
}

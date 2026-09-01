package io.github.loadup.common.tracer.async;

/*-
 * #%L
 * Loadup Common Tracer
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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * {@link BeanPostProcessor} that automatically wires the {@link TracingTaskDecorator}
 * into every {@link ThreadPoolTaskExecutor} found in the application context.
 *
 * <p>This ensures that trace context is propagated for all {@code @Async} methods
 * and any manually constructed executor without requiring individual wiring.
 */
public class AsyncTracingConfiguration implements BeanPostProcessor {

    private final TracingTaskDecorator tracingTaskDecorator;

    public AsyncTracingConfiguration(TracingTaskDecorator tracingTaskDecorator) {
        this.tracingTaskDecorator = tracingTaskDecorator;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ThreadPoolTaskExecutor executor) {
            executor.setTaskDecorator(tracingTaskDecorator);
        }
        return bean;
    }
}

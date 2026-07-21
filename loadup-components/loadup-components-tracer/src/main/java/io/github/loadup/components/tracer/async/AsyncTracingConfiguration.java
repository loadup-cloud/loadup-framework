package io.github.loadup.components.tracer.async;

/*-
 * #%L
 * Loadup Components Tracer
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

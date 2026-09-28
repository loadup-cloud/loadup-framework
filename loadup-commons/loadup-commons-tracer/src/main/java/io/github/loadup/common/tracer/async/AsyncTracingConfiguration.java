package io.github.loadup.common.tracer.async;

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

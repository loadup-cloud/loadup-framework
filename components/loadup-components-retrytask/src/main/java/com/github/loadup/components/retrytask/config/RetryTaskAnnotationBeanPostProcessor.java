package com.github.loadup.components.retrytask.config;

import com.github.loadup.components.retrytask.annotation.RetryTask;
import com.github.loadup.components.retrytask.handler.RetryTaskExecutorHandler;
import com.github.loadup.components.retrytask.registry.TaskHandlerRegistry;
import com.github.loadup.components.retrytask.registry.TaskStrategyRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class RetryTaskAnnotationBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    private TaskHandlerRegistry  taskHandlerRegistry;
    @Autowired
    private TaskStrategyRegistry taskStrategyRegistry;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RetryTask.class)) {
            RetryTask annotation = bean.getClass().getAnnotation(RetryTask.class);

            //注册策略
            taskStrategyRegistry.register(annotation);
            // 注册 Handler
            if (bean instanceof RetryTaskExecutorHandler) {
                taskHandlerRegistry.register(annotation.businessType(), (RetryTaskExecutorHandler) bean);
            }
        }
        return bean;
    }
}

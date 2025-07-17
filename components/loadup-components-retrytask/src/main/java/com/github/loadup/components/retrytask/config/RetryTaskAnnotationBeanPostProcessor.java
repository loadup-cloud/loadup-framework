package com.github.loadup.components.retrytask.config;

import com.github.loadup.components.retrytask.annotation.RetryTask;
import com.github.loadup.components.retrytask.factory.RetryStrategyFactory;
import com.github.loadup.components.retrytask.handler.RetryTaskExecutorHandler;
import jakarta.annotation.Resource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class RetryTaskAnnotationBeanPostProcessor implements BeanPostProcessor {

    @Resource
    private RetryStrategyFactory taskStrategyFactory;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RetryTask.class)) {
            RetryTask annotation = bean.getClass().getAnnotation(RetryTask.class);

            //注册策略
            taskStrategyFactory.registerStrategy(annotation);
            // 注册 Handler
            if (bean instanceof RetryTaskExecutorHandler handler) {
                taskStrategyFactory.registerHandler(annotation.businessType(), handler);
            }
        }
        return bean;
    }
}

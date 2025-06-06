package com.github.loadup.components.extension;

import com.github.loadup.components.extension.annotation.Extension;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 自动注册所有带有 @Extension 注解的 Bean
 */
public class ExtensionRegistrar implements BeanFactoryPostProcessor {

    private final ExtensionRegistry extensionRegistry;

    public ExtensionRegistrar(ExtensionRegistry extensionRegistry) {
        this.extensionRegistry = extensionRegistry;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Class<?> beanClass = beanFactory.getType(beanName);
            if (beanClass != null && beanClass.isAnnotationPresent(Extension.class)) {
                try {
                    Extension annotation = beanClass.getAnnotation(Extension.class);
                    String bizCode = annotation.bizCode();

                    if (bizCode.isEmpty()) {
                        bizCode = beanClass.getInterfaces()[0].getName(); // 默认使用接口全限定名
                    }

                    Object instance = beanFactory.getBean(beanName);
                    extensionRegistry.register(bizCode, instance);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to register extension: " + beanClass.getName(), e);
                }
            }
        }
    }
}

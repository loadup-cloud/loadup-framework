/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.database.autoconfigure;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Registers the framework root package as an auto-configuration package so repository interfaces
 * declared by framework and pay modules are discovered by Boot's Spring Data JDBC scanner.
 */
final class DataJdbcRepositoryPackagesRegistrar implements ImportBeanDefinitionRegistrar {
    static final String ROOT_PACKAGE = "io.github.loadup";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        if (registry instanceof BeanFactory beanFactory && AutoConfigurationPackages.has(beanFactory)) {
            if (AutoConfigurationPackages.get(beanFactory).contains(ROOT_PACKAGE)) {
                return;
            }
        }
        AutoConfigurationPackages.register(registry, ROOT_PACKAGE);
    }
}

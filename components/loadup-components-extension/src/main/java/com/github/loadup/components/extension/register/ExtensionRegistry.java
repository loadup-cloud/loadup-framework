/* Copyright (C) LoadUp Cloud 2025 */
package com.github.loadup.components.extension.register;

import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.components.extension.api.IExtensionPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 扩展点注册中心
 */
@Slf4j
public class ExtensionRegistry implements ApplicationListener<ContextRefreshedEvent> {

    // 内部存储结构，Key是扩展点接口类，Value是该接口的所有实现
    private final Map<Class<?>, List<ExtensionCoordinate>> extensionRegister = new ConcurrentHashMap<>();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        // 避免在父子容器环境中重复注册
        if (applicationContext.getParent() == null) {
            log.info("Starting to register all extensions...");
            Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Extension.class);
            beans.values().forEach(this::registerExtension);
            log.info("Extension registration completed. Total {} extension points loaded.", extensionRegister.size());
        }
    }

    private void registerExtension(Object bean) {
        Class<?> beanClass = bean.getClass();
        Extension extensionAnn = beanClass.getAnnotation(Extension.class);
        if (extensionAnn == null) {
            return;
        }

        // 找到该Bean实现的所有IExtensionPoint接口
        Class<?>[] interfaces = beanClass.getInterfaces();
        for (Class<?> intf : interfaces) {
            if (IExtensionPoint.class.isAssignableFrom(intf)) {
                ExtensionCoordinate coordinate = new ExtensionCoordinate(bean, extensionAnn);
                List<ExtensionCoordinate> coordinates = extensionRegister.computeIfAbsent(intf, k -> new ArrayList<>());
                coordinates.add(coordinate);
                log.debug("Registered extension [{} | bizCode:{}] for interface [{}].",
                        bean.getClass().getSimpleName(), extensionAnn.bizCode(), intf.getSimpleName());
            }
        }
    }

    /**
     * 根据扩展点接口类获取其所有实现
     */
    public <T> List<ExtensionCoordinate> getExtensionCoordinates(Class<T> extensionPointClass) {
        return extensionRegister.get(extensionPointClass);
    }
}

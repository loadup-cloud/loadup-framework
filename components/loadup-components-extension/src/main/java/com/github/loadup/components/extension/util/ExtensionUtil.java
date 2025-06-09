/* Copyright (C) LoadUp Cloud 2025 */
package com.github.loadup.components.extension.util;

import com.github.loadup.components.extension.ExtensionRegistry;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 静态工具类，用于获取扩展点实现
 */
@Component
public class ExtensionUtil implements ApplicationContextAware {

    private static ExtensionRegistry registry;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 从 Spring 容器中获取 ExtensionRegistry 实例
        registry = applicationContext.getBean(ExtensionRegistry.class);
    }

    /**
     * 获取指定 bizCode 的扩展实现，并转换为目标接口类型
     *
     * @param interfaceClass 接口类型
     * @param bizCode        扩展点标识
     * @param <T>            泛型
     * @return 实现对象
     */
    public static <T> T getExtension(Class<T> interfaceClass, String bizCode) {
        Object extension = registry.getExtension(bizCode);
        if (extension == null) {
            throw new IllegalArgumentException("No extension found for bizCode: " + bizCode);
        }
        return interfaceClass.cast(extension);
    }

    /**
     * 获取所有已注册的扩展点
     *
     * @return Map<bizCode, 扩展实例</bizCode,>
     */
    public static Map<String, Object> getAllExtensions() {
        return registry.getAllExtensions();
    }
}

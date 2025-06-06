package com.github.loadup.components.extension;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 扩展点注册中心
 */
public class ExtensionRegistry {

    private final Map<String, Object> registry = new ConcurrentHashMap<>();

    public void register(String bizCode, Object extensionInstance) {
        registry.put(bizCode, extensionInstance);
    }

    public Object getExtension(String bizCode) {
        return registry.get(bizCode);
    }

    public Map<String, Object> getAllExtensions() {
        return Map.copyOf(registry);
    }
}

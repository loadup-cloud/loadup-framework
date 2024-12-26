package com.github.loadup.components.gateway.common.util;

public interface ExtensionPointLoader {

    static <T> T get(Class<T> clazz, String bizCode) {
        return null;//ExtensionRegistration.get(clazz, bizCode);
    }
}

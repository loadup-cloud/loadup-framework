/* Copyright (C) LoadUp Cloud 2025 */
package com.github.loadup.components.extension.config;

import com.github.loadup.components.extension.ExtensionRegistrar;
import com.github.loadup.components.extension.ExtensionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExtensionConfig {
    @Bean
    public ExtensionRegistry extensionRegistry() {
        return new ExtensionRegistry();
    }

    @Bean
    public ExtensionRegistrar extensionRegistrar(ExtensionRegistry extensionRegistry) {
        return new ExtensionRegistrar(extensionRegistry);
    }
}

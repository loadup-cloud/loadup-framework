package io.github.loadup.components.configcenter.local;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.configcenter.binder.local")
public class LocalConfigCenterConfig {
    // Local存配置无特殊配置项，保留占位
}

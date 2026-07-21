package io.github.loadup.components.configcenter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.configcenter")
public class ConfigCenterProperties {
    private String binderType = "local";

    public String getBinderType() { return binderType; }
    public void setBinderType(String binderType) { this.binderType = binderType; }
}

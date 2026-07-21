package io.github.loadup.components.dfs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.dfs")
public class DfsProperties {
    private String binderType = "local";

    public String getBinderType() { return binderType; }
    public void setBinderType(String binderType) { this.binderType = binderType; }
}

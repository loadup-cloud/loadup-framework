package io.github.loadup.components.dfs;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Shared DFS backend selection settings. */
@ConfigurationProperties(prefix = "loadup.dfs")
public class DfsProperties {
    private BinderType binderType = BinderType.LOCAL;

    public enum BinderType {
        LOCAL,
        S3,
        DATABASE
    }

    public BinderType getBinderType() {
        return binderType;
    }

    public void setBinderType(BinderType binderType) {
        this.binderType = binderType;
    }
}

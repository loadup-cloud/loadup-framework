package io.github.loadup.components.dfs.local;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Local filesystem binder settings. */
@ConfigurationProperties(prefix = "loadup.dfs.binder.local")
public class LocalDfsProperties {
    private String basePath = System.getProperty("java.io.tmpdir") + "/loadup-dfs";

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}

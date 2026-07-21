package io.github.loadup.components.dfs.local;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.dfs.binder.local")
public class LocalDfsConfig {
    private String uploadDir = "/tmp/dfs";

    public String getUploadDir() { return uploadDir; }
    public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }
}

package io.github.loadup.components.gotone.channel.push;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the Firebase Cloud Messaging push binder ({@code loadup.gotone.binder.push.fcm.*}).
 */
@ConfigurationProperties(prefix = "loadup.gotone.binder.push.fcm")
public class FcmPushConfig {

    private String serverKey;
    private String projectId;

    public String getServerKey() {
        return serverKey;
    }

    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}

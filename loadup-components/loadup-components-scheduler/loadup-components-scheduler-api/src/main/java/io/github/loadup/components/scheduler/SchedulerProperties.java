package io.github.loadup.components.scheduler;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.scheduler")
public class SchedulerProperties {
    private String binderType = "simplejob";

    public String getBinderType() { return binderType; }
    public void setBinderType(String binderType) { this.binderType = binderType; }
}

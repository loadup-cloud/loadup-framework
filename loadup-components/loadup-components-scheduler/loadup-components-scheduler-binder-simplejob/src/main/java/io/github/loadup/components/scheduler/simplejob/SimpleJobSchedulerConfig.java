package io.github.loadup.components.scheduler.simplejob;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.scheduler.binder.simplejob")
public class SimpleJobSchedulerConfig {
    private int poolSize = 4;

    public int getPoolSize() { return poolSize; }
    public void setPoolSize(int poolSize) { this.poolSize = poolSize; }
}

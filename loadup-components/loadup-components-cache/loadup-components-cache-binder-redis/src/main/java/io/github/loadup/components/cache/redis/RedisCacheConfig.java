package io.github.loadup.components.cache.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.cache.binder.redis")
public class RedisCacheConfig {
    private String host = "localhost";
    private int port = 6379;
    private int database = 0;
    private String password;

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public int getDatabase() { return database; }
    public void setDatabase(int database) { this.database = database; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

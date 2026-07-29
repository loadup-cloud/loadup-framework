package io.github.loadup.gateway.facade.config;

/*-
 * #%L
 * LoadUp Gateway Facade
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Gateway configuration properties (corresponds to loadup.gateway in application.yml)
 */
@ConfigurationProperties(prefix = "loadup.gateway")
public class GatewayProperties {

    /**
     * Whether to enable Gateway
     */
    private boolean enabled = true;

    /**
     * Route refresh interval (seconds)
     */
    private int routeRefreshInterval = 5;

    /**
     * Template cache size
     */
    private int templateCacheSize = 100;

    /**
     * Default timeout (milliseconds)
     */
    private long defaultTimeout = 10000L;

    /**
     * Default retry count
     */
    private int defaultRetryCount = 1;

    /**
     * Default verify response wrapper
     */
    private boolean defaultWrapResponse = false;

    /**
     * Security configuration
     */
    @NestedConfigurationProperty
    private SecurityConfig security = new SecurityConfig();

    public static class SecurityConfig {
        /**
         * JWT secret key (must be consistent with auth server)
         */
        private String secret = "loadup-gateway-secret-key-must-be-long-enough-32bytes";

        /**
         * Token header name
         */
        private String header = "Authorization";

        /**
         * Token prefix
         */
        private String prefix = "Bearer ";

        /**
         * HMAC signature app secrets: appId → secretKey.
         * Configure via: loadup.gateway.security.app-secrets.app-id=secret
         */
        private Map<String, String> appSecrets = new HashMap<>();

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public Map<String, String> getAppSecrets() {
            return appSecrets;
        }

        public void setAppSecrets(Map<String, String> appSecrets) {
            this.appSecrets = appSecrets;
        }
    }

    /**
     * Storage related configuration
     */

    // Replace the generic map with a strongly-typed Plugins holder so IDEs can provide YAML
    // autocompletion
    @NestedConfigurationProperty
    private ProxyPlugins proxyPlugins = new ProxyPlugins();

    // New storage-plugin holder: contains type and type-specific property groups
    @NestedConfigurationProperty
    private Storage storage = new Storage();

    private ResponseProperties response = new ResponseProperties();

    public static class PluginProperties {
        private boolean enabled = true;
        private int priority = 100;
        private Map<String, Object> properties = new HashMap<>();
    }

    // New strongly-typed holder for known plugins. Field names use camelCase and will map to
    // kebab-case in YAML
    public static class ProxyPlugins {
        @NestedConfigurationProperty
        private Bean bean = new Bean();

        @NestedConfigurationProperty
        private Http http = new Http();

        @NestedConfigurationProperty
        private Rpc rpc = new Rpc();

        public Bean getBean() {
            return bean;
        }

        public void setBean(Bean bean) {
            this.bean = bean;
        }

        public Http getHttp() {
            return http;
        }

        public void setHttp(Http http) {
            this.http = http;
        }

        public Rpc getRpc() {
            return rpc;
        }

        public void setRpc(Rpc rpc) {
            this.rpc = rpc;
        }
    }

    public static class Bean extends PluginProperties {
        // add plugin-specific properties here if needed in future
    }

    public static class Http extends PluginProperties {
        /**
         * Maximum number of connections for the HTTP proxy plugin
         */
        private int maxConnections = 100;
    }

    public static class Rpc extends PluginProperties {
        // RPC-specific configuration
        private String registryAddress;
        private Long timeout;
        private Long retries;

        public String getRegistryAddress() {
            return registryAddress;
        }

        public void setRegistryAddress(String registryAddress) {
            this.registryAddress = registryAddress;
        }

        public Long getTimeout() {
            return timeout;
        }

        public void setTimeout(Long timeout) {
            this.timeout = timeout;
        }

        public Long getRetries() {
            return retries;
        }

        public void setRetries(Long retries) {
            this.retries = retries;
        }
    }

    public static class StorageFile {
        // File storage specific properties can be added here
        /**
         * Base path for file storage
         */
        private String basePath;

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }
    }

    public static class StorageDatabase {}

    // Holder that selects storage type and provides type-specific config groups
    public static class Storage {
        /**
         * Storage type to use for repository. Allowed values: FILE, DATABASE
         */
        private StorageType type = StorageType.FILE;

        @NestedConfigurationProperty
        private StorageFile file = new StorageFile();

        @NestedConfigurationProperty
        private StorageDatabase database = new StorageDatabase();

        public enum StorageType {
            FILE,
            DATABASE
        }

        public StorageType getType() {
            return type;
        }

        public void setType(StorageType type) {
            this.type = type;
        }

        public StorageFile getFile() {
            return file;
        }

        public void setFile(StorageFile file) {
            this.file = file;
        }

        public StorageDatabase getDatabase() {
            return database;
        }

        public void setDatabase(StorageDatabase database) {
            this.database = database;
        }
    }

    public static class ResponseProperties {
        private boolean wrap = true;
        private boolean wrapResult = true;
        private boolean wrapMeta = true;

        public boolean isWrap() {
            return wrap;
        }

        public void setWrap(boolean wrap) {
            this.wrap = wrap;
        }

        public boolean isWrapResult() {
            return wrapResult;
        }

        public void setWrapResult(boolean wrapResult) {
            this.wrapResult = wrapResult;
        }

        public boolean isWrapMeta() {
            return wrapMeta;
        }

        public void setWrapMeta(boolean wrapMeta) {
            this.wrapMeta = wrapMeta;
        }
    }

    public GatewayProperties() {}

    public int getRouteRefreshInterval() {
        return this.routeRefreshInterval;
    }

    public int getTemplateCacheSize() {
        return this.templateCacheSize;
    }

    public long getDefaultTimeout() {
        return this.defaultTimeout;
    }

    public int getDefaultRetryCount() {
        return this.defaultRetryCount;
    }

    public boolean isDefaultWrapResponse() {
        return this.defaultWrapResponse;
    }

    public SecurityConfig getSecurity() {
        return this.security;
    }

    public ProxyPlugins getProxyPlugins() {
        return this.proxyPlugins;
    }

    public Storage getStorage() {
        return this.storage;
    }

    public ResponseProperties getResponse() {
        return this.response;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setRouteRefreshInterval(int routeRefreshInterval) {
        this.routeRefreshInterval = routeRefreshInterval;
    }

    public void setTemplateCacheSize(int templateCacheSize) {
        this.templateCacheSize = templateCacheSize;
    }

    public void setDefaultTimeout(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public void setDefaultRetryCount(int defaultRetryCount) {
        this.defaultRetryCount = defaultRetryCount;
    }

    public void setDefaultWrapResponse(boolean defaultWrapResponse) {
        this.defaultWrapResponse = defaultWrapResponse;
    }

    public void setSecurity(SecurityConfig security) {
        this.security = security;
    }

    public void setProxyPlugins(ProxyPlugins proxyPlugins) {
        this.proxyPlugins = proxyPlugins;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public void setResponse(ResponseProperties response) {
        this.response = response;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

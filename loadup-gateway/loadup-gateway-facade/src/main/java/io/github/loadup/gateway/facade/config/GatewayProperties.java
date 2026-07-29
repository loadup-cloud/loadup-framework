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
    }

    public static class StorageFile {
        // File storage specific properties can be added here
        /**
         * Base path for file storage
         */
        private String basePath;
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
    }

    public static class ResponseProperties {
        private boolean wrap = true;
        private boolean wrapResult = true;
        private boolean wrapMeta = true;
    }

    public GatewayProperties(boolean enabled, int routeRefreshInterval, int templateCacheSize, long defaultTimeout, int defaultRetryCount, boolean defaultWrapResponse, SecurityConfig security, String secret, String header, String prefix, Map<String, String> appSecrets, ProxyPlugins proxyPlugins, Storage storage, ResponseProperties response, boolean enabled, int priority, Map<String, Object> properties, Bean bean, Http http, Rpc rpc, int maxConnections, String registryAddress, Long timeout, Long retries, String basePath, StorageType type, StorageFile file, StorageDatabase database, boolean wrap, boolean wrapResult, boolean wrapMeta) {
        this.enabled = enabled;
        this.routeRefreshInterval = routeRefreshInterval;
        this.templateCacheSize = templateCacheSize;
        this.defaultTimeout = defaultTimeout;
        this.defaultRetryCount = defaultRetryCount;
        this.defaultWrapResponse = defaultWrapResponse;
        this.security = security;
        this.secret = secret;
        this.header = header;
        this.prefix = prefix;
        this.appSecrets = appSecrets;
        this.proxyPlugins = proxyPlugins;
        this.storage = storage;
        this.response = response;
        this.enabled = enabled;
        this.priority = priority;
        this.properties = properties;
        this.bean = bean;
        this.http = http;
        this.rpc = rpc;
        this.maxConnections = maxConnections;
        this.registryAddress = registryAddress;
        this.timeout = timeout;
        this.retries = retries;
        this.basePath = basePath;
        this.type = type;
        this.file = file;
        this.database = database;
        this.wrap = wrap;
        this.wrapResult = wrapResult;
        this.wrapMeta = wrapMeta;
    }

    public GatewayProperties() {
    }

    public boolean isEnabled() {
        return this.enabled;
    }

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

    public String getSecret() {
        return this.secret;
    }

    public String getHeader() {
        return this.header;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public Map<String, String> getAppSecrets() {
        return this.appSecrets;
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

    public int getPriority() {
        return this.priority;
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public Bean getBean() {
        return this.bean;
    }

    public Http getHttp() {
        return this.http;
    }

    public Rpc getRpc() {
        return this.rpc;
    }

    public int getMaxConnections() {
        return this.maxConnections;
    }

    public String getRegistryAddress() {
        return this.registryAddress;
    }

    public Long getTimeout() {
        return this.timeout;
    }

    public Long getRetries() {
        return this.retries;
    }

    public String getBasePath() {
        return this.basePath;
    }

    public StorageType getType() {
        return this.type;
    }

    public StorageFile getFile() {
        return this.file;
    }

    public StorageDatabase getDatabase() {
        return this.database;
    }

    public boolean isWrap() {
        return this.wrap;
    }

    public boolean isWrapResult() {
        return this.wrapResult;
    }

    public boolean isWrapMeta() {
        return this.wrapMeta;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setAppSecrets(Map<String, String> appSecrets) {
        this.appSecrets = appSecrets;
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

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void setBean(Bean bean) {
        this.bean = bean;
    }

    public void setHttp(Http http) {
        this.http = http;
    }

    public void setRpc(Rpc rpc) {
        this.rpc = rpc;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public void setRetries(Long retries) {
        this.retries = retries;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setType(StorageType type) {
        this.type = type;
    }

    public void setFile(StorageFile file) {
        this.file = file;
    }

    public void setDatabase(StorageDatabase database) {
        this.database = database;
    }

    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }

    public void setWrapResult(boolean wrapResult) {
        this.wrapResult = wrapResult;
    }

    public void setWrapMeta(boolean wrapMeta) {
        this.wrapMeta = wrapMeta;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), enabled, routeRefreshInterval, templateCacheSize, defaultTimeout, defaultRetryCount, defaultWrapResponse, security, secret, header, prefix, appSecrets, proxyPlugins, storage, response, enabled, priority, properties, bean, http, rpc, maxConnections, registryAddress, timeout, retries, basePath, type, file, database, wrap, wrapResult, wrapMeta);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GatewayProperties other = (GatewayProperties) o;
        if (!java.util.Objects.equals(enabled, other.enabled)) return false;
        if (!java.util.Objects.equals(routeRefreshInterval, other.routeRefreshInterval)) return false;
        if (!java.util.Objects.equals(templateCacheSize, other.templateCacheSize)) return false;
        if (!java.util.Objects.equals(defaultTimeout, other.defaultTimeout)) return false;
        if (!java.util.Objects.equals(defaultRetryCount, other.defaultRetryCount)) return false;
        if (!java.util.Objects.equals(defaultWrapResponse, other.defaultWrapResponse)) return false;
        if (!java.util.Objects.equals(security, other.security)) return false;
        if (!java.util.Objects.equals(secret, other.secret)) return false;
        if (!java.util.Objects.equals(header, other.header)) return false;
        if (!java.util.Objects.equals(prefix, other.prefix)) return false;
        if (!java.util.Objects.equals(appSecrets, other.appSecrets)) return false;
        if (!java.util.Objects.equals(proxyPlugins, other.proxyPlugins)) return false;
        if (!java.util.Objects.equals(storage, other.storage)) return false;
        if (!java.util.Objects.equals(response, other.response)) return false;
        if (!java.util.Objects.equals(enabled, other.enabled)) return false;
        if (!java.util.Objects.equals(priority, other.priority)) return false;
        if (!java.util.Objects.equals(properties, other.properties)) return false;
        if (!java.util.Objects.equals(bean, other.bean)) return false;
        if (!java.util.Objects.equals(http, other.http)) return false;
        if (!java.util.Objects.equals(rpc, other.rpc)) return false;
        if (!java.util.Objects.equals(maxConnections, other.maxConnections)) return false;
        if (!java.util.Objects.equals(registryAddress, other.registryAddress)) return false;
        if (!java.util.Objects.equals(timeout, other.timeout)) return false;
        if (!java.util.Objects.equals(retries, other.retries)) return false;
        if (!java.util.Objects.equals(basePath, other.basePath)) return false;
        if (!java.util.Objects.equals(type, other.type)) return false;
        if (!java.util.Objects.equals(file, other.file)) return false;
        if (!java.util.Objects.equals(database, other.database)) return false;
        if (!java.util.Objects.equals(wrap, other.wrap)) return false;
        if (!java.util.Objects.equals(wrapResult, other.wrapResult)) return false;
        if (!java.util.Objects.equals(wrapMeta, other.wrapMeta)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "GatewayProperties(" + "super=" + super.toString() + ", " + "enabled=" + enabled + ", " + "routeRefreshInterval=" + routeRefreshInterval + ", " + "templateCacheSize=" + templateCacheSize + ", " + "defaultTimeout=" + defaultTimeout + ", " + "defaultRetryCount=" + defaultRetryCount + ", " + "defaultWrapResponse=" + defaultWrapResponse + ", " + "security=" + security + ", " + "secret=" + secret + ", " + "header=" + header + ", " + "prefix=" + prefix + ", " + "appSecrets=" + appSecrets + ", " + "proxyPlugins=" + proxyPlugins + ", " + "storage=" + storage + ", " + "response=" + response + ", " + "enabled=" + enabled + ", " + "priority=" + priority + ", " + "properties=" + properties + ", " + "bean=" + bean + ", " + "http=" + http + ", " + "rpc=" + rpc + ", " + "maxConnections=" + maxConnections + ", " + "registryAddress=" + registryAddress + ", " + "timeout=" + timeout + ", " + "retries=" + retries + ", " + "basePath=" + basePath + ", " + "type=" + type + ", " + "file=" + file + ", " + "database=" + database + ", " + "wrap=" + wrap + ", " + "wrapResult=" + wrapResult + ", " + "wrapMeta=" + wrapMeta + ")";
    }
}

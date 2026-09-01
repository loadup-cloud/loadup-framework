package io.github.loadup.gateway.facade.config;

/*-
 * #%L
 * LoadUp Gateway Facade
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
         * Whether to enable the standard OAuth2 resource server auto-configuration
         * (Nimbus JwtDecoder + BearerTokenAuthenticationFilter).
         */
        private boolean enabled = true;

        /**
         * JWT secret key (must be consistent with auth server)
         */
        private String secret = "loadup-gateway-secret-key-must-be-long-enough-32bytes";

        /**
         * OIDC issuer URI used to fetch the JWK set via discovery. When set together with
         * {@code jwk-set-uri}, takes precedence over the shared secret.
         */
        private String issuerUri;

        /**
         * JWK set URI used for token verification (e.g. Keycloak's
         * {@code /protocol/openid-connect/certs}). Takes precedence over {@code issuer-uri}
         * and the shared secret.
         */
        private String jwkSetUri;

        /**
         * HMAC signature app secrets: appId → secretKey.
         * Configure via: loadup.gateway.security.app-secrets.app-id=secret
         */
        private Map<String, String> appSecrets = new HashMap<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getIssuerUri() {
            return issuerUri;
        }

        public void setIssuerUri(String issuerUri) {
            this.issuerUri = issuerUri;
        }

        public String getJwkSetUri() {
            return jwkSetUri;
        }

        public void setJwkSetUri(String jwkSetUri) {
            this.jwkSetUri = jwkSetUri;
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

    // Strongly-typed plugin config holder so IDEs can provide YAML autocompletion
    @NestedConfigurationProperty
    private ProxyPlugins proxyPlugins = new ProxyPlugins();

    // New storage-plugin holder: contains type and type-specific property groups
    @NestedConfigurationProperty
    private Storage storage = new Storage();

    private ResponseProperties response = new ResponseProperties();

    // Strongly-typed holder for proxy plugin config. Field names use camelCase and map to
    // kebab-case in YAML.
    public static class ProxyPlugins {
        @NestedConfigurationProperty
        private Rpc rpc = new Rpc();

        public Rpc getRpc() {
            return rpc;
        }

        public void setRpc(Rpc rpc) {
            this.rpc = rpc;
        }
    }

    public static class Rpc {
        /**
         * Dubbo registry address for the RPC proxy plugin
         */
        private String registryAddress;

        /**
         * Dubbo invocation timeout in milliseconds
         */
        private Long timeout;

        /**
         * Dubbo retry count
         */
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

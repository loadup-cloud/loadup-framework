package io.github.loadup.gateway.facade.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * YAML-friendly route definition.
 *
 * <p>This is the configuration-layer model. The core engine compiles it into
 * a {@link RouteConfig} at startup for runtime use.
 *
 * <p>YAML example:
 * <pre>
 * routes:
 *   - id: user-api
 *     path: /api/users
 *     method: POST
 *     backend:
 *       protocol: http
 *       url: http://user-service:8080/users
 *     filters:
 *       - name: rate-limit
 *         props: { capacity: 100 }
 *       - name: jwt-auth
 *     responseFilters:
 *       - name: result-wrapper
 * </pre>
 */
public class RouteDefinition {

    /**
     * Unique route identifier. Auto-generated if not specified.
     */
    private String id = UUID.randomUUID().toString().substring(0, 8);

    /**
     * URL path pattern (supports Ant-style: /api/users/&#42;&#42;, /api/user/{id}).
     */
    private String path;

    /**
     * HTTP method (GET, POST, PUT, DELETE, PATCH).
     */
    private String method = "POST";

    /**
     * Whether this route is active.
     */
    private boolean enabled = true;

    /**
     * Backend target configuration.
     */
    private BackendDefinition backend;

    /**
     * Ordered list of request-phase filters.
     */
    private List<FilterDefinition> filters = new ArrayList<>();

    /**
     * Ordered list of response-phase filters (run after the backend call).
     */
    private List<FilterDefinition> responseFilters = new ArrayList<>();

    /**
     * Security code hint: OFF, default, signature, internal. Overrides auto-detection.
     */
    private String securityCode;

    /**
     * Route-level timeout in milliseconds. Overrides global default.
     */
    private Long timeout;

    /**
     * Whether to wrap the response in a standard Result envelope.
     */
    private Boolean wrapResponse;

    public List<FilterDefinition> getFilters() {
        return filters == null ? Collections.emptyList() : Collections.unmodifiableList(filters);
    }

    public List<FilterDefinition> getResponseFilters() {
        return responseFilters == null ? Collections.emptyList() : Collections.unmodifiableList(responseFilters);
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public BackendDefinition getBackend() {
        return backend;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public Long getTimeout() {
        return timeout;
    }

    public Boolean getWrapResponse() {
        return wrapResponse;
    }

    /**
     * Backend target definition.
     */
    public static class BackendDefinition {

        /**
         * Protocol: http, bean, rpc.
         */
        private String protocol;

        /**
         * Target URL (for http protocol).
         */
        private String url;

        /**
         * Bean name (for bean protocol).
         */
        private String beanName;

        /**
         * Method name (for bean protocol).
         */
        private String methodName;

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getBeanName() {
            return beanName;
        }

        public void setBeanName(String beanName) {
            this.beanName = beanName;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }
    }


    public RouteDefinition() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setBackend(BackendDefinition backend) {
        this.backend = backend;
    }

    public void setFilters(List<FilterDefinition> filters) {
        this.filters = filters;
    }

    public void setResponseFilters(List<FilterDefinition> responseFilters) {
        this.responseFilters = responseFilters;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public void setWrapResponse(Boolean wrapResponse) {
        this.wrapResponse = wrapResponse;
    }


}

package io.github.loadup.gateway.facade.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

    /** Unique route identifier. Auto-generated if not specified. */
    private String id = UUID.randomUUID().toString().substring(0, 8);

    /** URL path pattern (supports Ant-style: /api/users/&#42;&#42;, /api/user/{id}). */
    private String path;

    /** HTTP method (GET, POST, PUT, DELETE, PATCH). */
    private String method = "POST";

    /** Whether this route is active. */
    private boolean enabled = true;

    /** Backend target configuration. */
    private BackendDefinition backend;

    /** Ordered list of request-phase filters. */
    private List<FilterDefinition> filters = new ArrayList<>();

    /** Ordered list of response-phase filters (run after the backend call). */
    private List<FilterDefinition> responseFilters = new ArrayList<>();

    /** Security code hint: OFF, default, signature, internal. Overrides auto-detection. */
    private String securityCode;

    /** Route-level timeout in milliseconds. Overrides global default. */
    private Long timeout;

    /** Whether to wrap the response in a standard Result envelope. */
    private Boolean wrapResponse;

    public List<FilterDefinition> getFilters() {
        return filters == null ? Collections.emptyList() : Collections.unmodifiableList(filters);
    }

    public List<FilterDefinition> getResponseFilters() {
        return responseFilters == null ? Collections.emptyList() : Collections.unmodifiableList(responseFilters);
    }

    /**
     * Backend target definition.
     */
    public static class BackendDefinition {

        /** Protocol: http, bean, rpc. */
        private String protocol;

        /** Target URL (for http protocol). */
        private String url;

        /** Bean name (for bean protocol). */
        private String beanName;

        /** Method name (for bean protocol). */
        private String methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RouteDefinition that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public RouteDefinition(String id, String path, String method, boolean enabled, BackendDefinition backend, List<FilterDefinition> filters, List<FilterDefinition> responseFilters, String securityCode, Long timeout, Boolean wrapResponse, String protocol, String url, String beanName, String methodName) {
        this.id = id;
        this.path = path;
        this.method = method;
        this.enabled = enabled;
        this.backend = backend;
        this.filters = filters;
        this.responseFilters = responseFilters;
        this.securityCode = securityCode;
        this.timeout = timeout;
        this.wrapResponse = wrapResponse;
        this.protocol = protocol;
        this.url = url;
        this.beanName = beanName;
        this.methodName = methodName;
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

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return "RouteDefinition(" + "id=" + id + ", " + "path=" + path + ", " + "method=" + method + ", " + "enabled=" + enabled + ", " + "backend=" + backend + ", " + "filters=" + filters + ", " + "responseFilters=" + responseFilters + ", " + "securityCode=" + securityCode + ", " + "timeout=" + timeout + ", " + "wrapResponse=" + wrapResponse + ", " + "protocol=" + protocol + ", " + "url=" + url + ", " + "beanName=" + beanName + ", " + "methodName=" + methodName + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id = UUID.randomUUID().toString().substring(0, 8);
        private String path;
        private String method = "POST";
        private boolean enabled = true;
        private BackendDefinition backend;
        private List<FilterDefinition> filters = new ArrayList<>();
        private List<FilterDefinition> responseFilters = new ArrayList<>();
        private String securityCode;
        private Long timeout;
        private Boolean wrapResponse;
        private String protocol;
        private String url;
        private String beanName;
        private String methodName;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder backend(BackendDefinition backend) {
            this.backend = backend;
            return this;
        }

        public Builder filters(List<FilterDefinition> filters) {
            this.filters = filters;
            return this;
        }

        public Builder responseFilters(List<FilterDefinition> responseFilters) {
            this.responseFilters = responseFilters;
            return this;
        }

        public Builder securityCode(String securityCode) {
            this.securityCode = securityCode;
            return this;
        }

        public Builder timeout(Long timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder wrapResponse(Boolean wrapResponse) {
            this.wrapResponse = wrapResponse;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder beanName(String beanName) {
            this.beanName = beanName;
            return this;
        }

        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public RouteDefinition build() {
            return new RouteDefinition(this.id, this.path, this.method, this.enabled, this.backend, this.filters, this.responseFilters, this.securityCode, this.timeout, this.wrapResponse, this.protocol, this.url, this.beanName, this.methodName);
        }
    }
}

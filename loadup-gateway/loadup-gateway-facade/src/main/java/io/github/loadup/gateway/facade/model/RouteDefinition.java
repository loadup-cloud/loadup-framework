package io.github.loadup.gateway.facade.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteDefinition {

    /** Unique route identifier. Auto-generated if not specified. */
    @Builder.Default
    private String id = UUID.randomUUID().toString().substring(0, 8);

    /** URL path pattern (supports Ant-style: /api/users/&#42;&#42;, /api/user/{id}). */
    private String path;

    /** HTTP method (GET, POST, PUT, DELETE, PATCH). */
    @Builder.Default
    private String method = "POST";

    /** Whether this route is active. */
    @Builder.Default
    private boolean enabled = true;

    /** Backend target configuration. */
    private BackendDefinition backend;

    /** Ordered list of request-phase filters. */
    @Builder.Default
    private List<FilterDefinition> filters = new ArrayList<>();

    /** Ordered list of response-phase filters (run after the backend call). */
    @Builder.Default
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
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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
}

package io.github.loadup.gateway.plugins.entity;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("gateway_routes")
public class RouteEntity {

    @Id
    private String id;

    private String path;

    private String method;

    /** Target string: http://..., bean://service:method, rpc://... */
    private String target;

    /** Security code: OFF, default, signature, internal */
    private String securityCode;

    /** Request filter chain names, comma-separated */
    private String requestFilters;

    /** Response filter chain names, comma-separated */
    private String responseFilters;

    /** Filter properties as JSON: {"filter-name": {"key": "value"}} */
    private String filterProps;

    /** Whether this route is active */
    private Boolean enabled;

    /** Route-level timeout in milliseconds */
    private Long timeout;

    /** Whether to wrap the response */
    private Boolean wrapResponse;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

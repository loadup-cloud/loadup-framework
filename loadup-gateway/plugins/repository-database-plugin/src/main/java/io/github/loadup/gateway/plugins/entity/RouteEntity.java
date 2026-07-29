package io.github.loadup.gateway.plugins.entity;

/*-
 * #%L
 * Repository Database Plugin
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

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

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

    public String getId() {
        return this.id;
    }

    public String getPath() {
        return this.path;
    }

    public String getMethod() {
        return this.method;
    }

    public String getTarget() {
        return this.target;
    }

    public String getSecurityCode() {
        return this.securityCode;
    }

    public String getRequestFilters() {
        return this.requestFilters;
    }

    public String getResponseFilters() {
        return this.responseFilters;
    }

    public String getFilterProps() {
        return this.filterProps;
    }

    public Boolean isEnabled() {
        return this.enabled;
    }

    public Long getTimeout() {
        return this.timeout;
    }

    public Boolean isWrapResponse() {
        return this.wrapResponse;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
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

    public void setTarget(String target) {
        this.target = target;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public void setRequestFilters(String requestFilters) {
        this.requestFilters = requestFilters;
    }

    public void setResponseFilters(String responseFilters) {
        this.responseFilters = responseFilters;
    }

    public void setFilterProps(String filterProps) {
        this.filterProps = filterProps;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public void setWrapResponse(Boolean wrapResponse) {
        this.wrapResponse = wrapResponse;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

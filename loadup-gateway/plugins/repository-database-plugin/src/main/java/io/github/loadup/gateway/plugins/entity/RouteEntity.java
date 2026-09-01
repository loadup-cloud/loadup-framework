package io.github.loadup.gateway.plugins.entity;

/*-
 * #%L
 * Repository Database Plugin
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

    /** Route-level authorization expression (SpEL or comma-separated authorities) */
    private String authorize;

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

    public String getAuthorize() {
        return this.authorize;
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

    public void setAuthorize(String authorize) {
        this.authorize = authorize;
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

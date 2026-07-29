package io.github.loadup.components.authorization.model;

/*-
 * #%L
 * LoadUp Components Authorization
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

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * User model for authorization context.
 *
 * <p>This is a lightweight user model without Spring Security dependencies.</p>
 */
public class LoadUpUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Unique user ID
     */
    private String userId;

    /**
     * Username
     */
    private String username;

    /**
     * User roles (e.g., ["ADMIN", "USER"] or ["ROLE_ADMIN", "ROLE_USER"])
     */
    private List<String> roles = Collections.emptyList();

    /**
     * User permissions (e.g., ["user:read", "user:write"])
     */
    private List<String> permissions = Collections.emptyList();

    /**
     * Additional attributes
     */
    private Map<String, Object> attributes;

    /**
     * Check if user has a specific role
     */
    public boolean hasRole(String role) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.contains(role) || roles.contains("ROLE_" + role);
    }

    /**
     * Check if user has any of the specified roles
     */
    public boolean hasAnyRole(String... roles) {
        if (this.roles == null || this.roles.isEmpty()) {
            return false;
        }
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user has all of the specified roles
     */
    public boolean hasAllRoles(String... roles) {
        if (this.roles == null || this.roles.isEmpty()) {
            return false;
        }
        for (String role : roles) {
            if (!hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if user has a specific permission
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    public LoadUpUser(
            String userId,
            String username,
            List<String> roles,
            List<String> permissions,
            Map<String, Object> attributes) {
        this.userId = userId;
        this.username = username;
        this.roles = roles;
        this.permissions = permissions;
        this.attributes = attributes;
    }

    public LoadUpUser() {}

    public String getUserId() {
        return this.userId;
    }

    public String getUsername() {
        return this.username;
    }

    public List<String> getRoles() {
        return this.roles;
    }

    public List<String> getPermissions() {
        return this.permissions;
    }

    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userId;
        private String username;
        private List<String> roles = Collections.emptyList();
        private List<String> permissions = Collections.emptyList();
        private Map<String, Object> attributes;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder permissions(List<String> permissions) {
            this.permissions = permissions;
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        public LoadUpUser build() {
            return new LoadUpUser(this.userId, this.username, this.roles, this.permissions, this.attributes);
        }
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}

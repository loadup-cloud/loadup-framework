package io.github.loadup.components.authorization.model;

/*-
 * #%L
 * LoadUp Components Authorization
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
        return "LoadUpUser{userId='" + userId + "', username='" + username + "', roles=" + roles + ", permissions="
                + permissions + ", attributes=" + attributes + '}';
    }
}

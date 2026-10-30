/*
 * #%L
 * Loadup Components Database
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
package io.github.loadup.components.database.config;

import io.github.loadup.commons.constant.CommonConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the LoadUp database component.
 */
@ConfigurationProperties(prefix = "loadup.database")
@Validated
public class DatabaseProperties {

    @Valid
    private MultiTenant multiTenant = new MultiTenant();

    public MultiTenant getMultiTenant() {
        return multiTenant;
    }

    public void setMultiTenant(MultiTenant multiTenant) {
        this.multiTenant = multiTenant;
    }

    /**
     * Schema-per-tenant policy. The tenant identity selects the schema; business tables never carry
     * a tenant column.
     */
    public static class MultiTenant {
        private boolean enabled = false;
        private boolean required = true;

        @NotBlank
        @Size(max = 64)
        private String defaultTenantId = CommonConstants.DEFAULT_TENANT_ID;

        @NotBlank
        @Pattern(regexp = "[a-z_][a-z0-9_]{0,62}")
        private String defaultSchema = CommonConstants.DEFAULT_SCHEMA;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public String getDefaultTenantId() {
            return defaultTenantId;
        }

        public void setDefaultTenantId(String defaultTenantId) {
            this.defaultTenantId = defaultTenantId;
        }

        public String getDefaultSchema() {
            return defaultSchema;
        }

        public void setDefaultSchema(String defaultSchema) {
            this.defaultSchema = defaultSchema;
        }
    }
}

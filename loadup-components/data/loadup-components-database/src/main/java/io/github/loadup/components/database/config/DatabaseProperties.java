/*-
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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Configuration properties for the LoadUp database component. */
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

    public static class MultiTenant {
        private boolean enabled = false;
        private boolean required = true;

        @NotBlank
        @Size(max = 64)
        private String defaultTenantId = "__default__";

        private List<String> ignoreTables = new ArrayList<>();
        private Request request = new Request();

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

        public List<String> getIgnoreTables() {
            return ignoreTables;
        }

        public void setIgnoreTables(List<String> ignoreTables) {
            this.ignoreTables = ignoreTables;
        }

        public Request getRequest() {
            return request;
        }

        public void setRequest(Request request) {
            this.request = request;
        }
    }

    public static class Request {
        private String headerName = "X-Tenant-Id";
        private String parameterName;
        private boolean subdomainEnabled;
        private List<String> excludedSubdomains = new ArrayList<>(List.of("www", "api"));

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }

        public String getParameterName() {
            return parameterName;
        }

        public void setParameterName(String parameterName) {
            this.parameterName = parameterName;
        }

        public boolean isSubdomainEnabled() {
            return subdomainEnabled;
        }

        public void setSubdomainEnabled(boolean subdomainEnabled) {
            this.subdomainEnabled = subdomainEnabled;
        }

        public List<String> getExcludedSubdomains() {
            return excludedSubdomains;
        }

        public void setExcludedSubdomains(List<String> excludedSubdomains) {
            this.excludedSubdomains = excludedSubdomains;
        }
    }
}

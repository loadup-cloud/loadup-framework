/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Configuration properties for the LoadUp database component. */
@ConfigurationProperties(prefix = "loadup.database")
@Validated
public class DatabaseProperties {

    @Valid
    private Audit audit = new Audit();

    @Valid
    private IdGenerator idGenerator = new IdGenerator();

    @Valid
    private MultiTenant multiTenant = new MultiTenant();

    @Valid
    private LogicalDelete logicalDelete = new LogicalDelete();

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public MultiTenant getMultiTenant() {
        return multiTenant;
    }

    public void setMultiTenant(MultiTenant multiTenant) {
        this.multiTenant = multiTenant;
    }

    public LogicalDelete getLogicalDelete() {
        return logicalDelete;
    }

    public void setLogicalDelete(LogicalDelete logicalDelete) {
        this.logicalDelete = logicalDelete;
    }

    public static class Audit {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class IdGenerator {
        private boolean enabled = true;
        private Strategy strategy = Strategy.RANDOM;

        @Min(1)
        @Max(64)
        private int randomLength = 20;

        private boolean uuidWithHyphens;

        @Min(0)
        @Max(31)
        private long snowflakeWorkerId;

        @Min(0)
        @Max(31)
        private long snowflakeDatacenterId;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Strategy getStrategy() {
            return strategy;
        }

        public void setStrategy(Strategy strategy) {
            this.strategy = strategy;
        }

        public int getRandomLength() {
            return randomLength;
        }

        public void setRandomLength(int randomLength) {
            this.randomLength = randomLength;
        }

        public boolean isUuidWithHyphens() {
            return uuidWithHyphens;
        }

        public void setUuidWithHyphens(boolean uuidWithHyphens) {
            this.uuidWithHyphens = uuidWithHyphens;
        }

        public long getSnowflakeWorkerId() {
            return snowflakeWorkerId;
        }

        public void setSnowflakeWorkerId(long snowflakeWorkerId) {
            this.snowflakeWorkerId = snowflakeWorkerId;
        }

        public long getSnowflakeDatacenterId() {
            return snowflakeDatacenterId;
        }

        public void setSnowflakeDatacenterId(long snowflakeDatacenterId) {
            this.snowflakeDatacenterId = snowflakeDatacenterId;
        }
    }

    public enum Strategy {
        RANDOM,
        UUID_V4,
        UUID_V7,
        SNOWFLAKE
    }

    public static class MultiTenant {
        private boolean enabled = false;
        private boolean required = true;

        @NotBlank
        private String columnName = "tenant_id";

        private String defaultTenantId;
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

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
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

    public static class LogicalDelete {
        private boolean enabled;

        @NotBlank
        private String columnName = "deleted";

        private int normalValue;
        private int deletedValue = 1;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public int getNormalValue() {
            return normalValue;
        }

        public void setNormalValue(int normalValue) {
            this.normalValue = normalValue;
        }

        public int getDeletedValue() {
            return deletedValue;
        }

        public void setDeletedValue(int deletedValue) {
            this.deletedValue = deletedValue;
        }
    }
}

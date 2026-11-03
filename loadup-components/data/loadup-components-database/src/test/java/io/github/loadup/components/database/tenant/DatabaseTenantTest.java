/*
 * #%L
 * LoadUp Components Database
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
package io.github.loadup.components.database.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.database.config.DatabaseProperties;
import org.junit.jupiter.api.Test;

class DatabaseTenantTest {
    @Test
    void singleTenantAlwaysUsesConfiguredDefault() {
        var properties = new DatabaseProperties();
        properties.getMultiTenant().setDefaultTenantId("single");
        properties.setDefaultSchema("tenant_single");
        var resolver = new DatabaseTenant(properties);
        TenantUtil.runWithTenant("untrusted", () -> {
            assertThat(resolver.currentTenantId()).isEqualTo("single");
            assertThat(resolver.currentSchema()).isEqualTo("tenant_single");
        });
    }

    @Test
    void multiTenantUsesContextTenantAsSchema() {
        var properties = new DatabaseProperties();
        properties.getMultiTenant().setEnabled(true);
        var resolver = new DatabaseTenant(properties);
        TenantUtil.runWithTenant("tenant_a", () -> {
            assertThat(resolver.currentTenantId()).isEqualTo("tenant_a");
            assertThat(resolver.currentSchema()).isEqualTo("tenant_a");
        });
    }

    @Test
    void optionalTenantFallsBackToDefaultWithoutInventingIdentity() {
        var properties = new DatabaseProperties();
        properties.getMultiTenant().setEnabled(true);
        properties.getMultiTenant().setRequired(false);
        properties.getMultiTenant().setDefaultTenantId("fallback");
        properties.setDefaultSchema("ledger");
        var resolver = new DatabaseTenant(properties);
        TenantUtil.runWithTenant(null, () -> {
            assertThat(resolver.currentTenantId()).isEqualTo("fallback");
            assertThat(resolver.currentSchema()).isEqualTo("ledger");
        });
    }

    @Test
    void requiredTenantRejectsMissingContextWithCodedFailure() {
        var properties = new DatabaseProperties();
        properties.getMultiTenant().setEnabled(true);
        var resolver = new DatabaseTenant(properties);
        TenantUtil.runWithTenant(
                null,
                () -> assertThatThrownBy(resolver::currentTenantId)
                        .isInstanceOf(CommonException.class)
                        .hasMessage("Tenant context is required for database access"));
    }

    @Test
    void invalidTenantSchemaIsRejectedBeforeRouting() {
        var properties = new DatabaseProperties();
        properties.getMultiTenant().setEnabled(true);
        properties.getMultiTenant().setRequired(false);
        var resolver = new DatabaseTenant(properties);
        TenantUtil.runWithTenant(
                "Tenant-A; DROP SCHEMA public",
                () -> assertThatThrownBy(resolver::currentSchema)
                        .isInstanceOf(CommonException.class)
                        .hasMessage("Tenant schema name is invalid"));
    }

    @Test
    void tenantContextReadsTheBoundResolver() {
        var properties = new DatabaseProperties();
        properties.getMultiTenant().setEnabled(true);
        TenantContext.bind(new DatabaseTenant(properties));
        try {
            TenantUtil.runWithTenant("tenant_b", () -> {
                assertThat(TenantContext.tenantId()).isEqualTo("tenant_b");
                assertThat(TenantContext.schema()).isEqualTo("tenant_b");
            });
        } finally {
            TenantContext.bind(null);
        }
    }
}

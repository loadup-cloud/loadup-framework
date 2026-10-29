/*
 * #%L
 * LoadUp Components Global Unique
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
package io.github.loadup.components.globalunique;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.globalunique.model.GlobalUniqueClaim;
import io.github.loadup.components.globalunique.model.GlobalUniqueRecord;
import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class GlobalUniqueContractTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final DatabaseProperties properties = new DatabaseProperties();
    private final DefaultGlobalUniqueTemplate template =
            new DefaultGlobalUniqueTemplate(jdbc, properties, () -> "id", Clock.systemUTC());

    @Test
    void tenantDefaultsTrustedContextAndOptionalGlobalScopeRemainDistinct() {
        when(jdbc.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1);
        properties.getMultiTenant().setDefaultTenantId("single-tenant");
        assertThat(template.claim("order", "1")).isTrue();
        properties.getMultiTenant().setEnabled(true);
        assertThatThrownBy(() -> template.claim("order", "2")).isInstanceOf(IllegalStateException.class);
        TenantUtil.runWithTenant(
                "trusted", () -> assertThat(template.claim("order", "3")).isTrue());
        properties.getMultiTenant().setRequired(false);
        assertThat(template.claim("order", "4")).isTrue();
        var tenants = ArgumentCaptor.forClass(String.class);
        verify(jdbc, times(3)).update(anyString(), any(), tenants.capture(), any(), any(), any(), any(), any(), any());
        assertThat(tenants.getAllValues()).containsExactly("single-tenant", "trusted", "__loadup_global__");
    }

    @Test
    void onlyDuplicateBusinessClaimsAreConvertedToFalse() {
        var failure = new DuplicateKeyException("synthetic duplicate");
        when(jdbc.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(failure);
        assertThatThrownBy(() -> template.claim("order", "key")).isSameAs(failure);
        doReturn(0).when(jdbc).update(anyString(), any(), any(), any(), any(), any(), any(), any(), any());
        var stored = new GlobalUniqueRecord("id", "tenant", "order", "key", null, null, null, null);
        when(jdbc.query(
                        anyString(),
                        org.mockito.ArgumentMatchers.<RowMapper<GlobalUniqueRecord>>any(),
                        any(),
                        any(),
                        any()))
                .thenReturn(List.of(stored));
        assertThat(template.claim("order", "key")).isFalse();
    }

    @Test
    void failedInsertAndNullInputCannotBeReportedAsAcquired() {
        when(jdbc.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0);
        assertThatThrownBy(() -> template.claim("order", "key"))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThatThrownBy(() -> template.claim((GlobalUniqueClaim) null)).isInstanceOf(NullPointerException.class);
        assertThat(template.find("order", "missing")).isEmpty();
    }

    @Test
    void claimsNormalizeRequiredFieldsAndEnforceLengthBoundaries() {
        var claim = new GlobalUniqueClaim(" order ", " key ", "b".repeat(100), "diagnostic");
        assertThat(claim.bizType()).isEqualTo("order");
        assertThat(claim.uniqueKey()).isEqualTo("key");
        assertThat(new GlobalUniqueClaim("b".repeat(64), "k".repeat(255), null, null).uniqueKey())
                .hasSize(255);
        for (String invalid : new String[] {null, " ", "b".repeat(65)}) {
            assertThatThrownBy(() -> new GlobalUniqueClaim(invalid, "key", null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        for (String invalid : new String[] {null, " ", "k".repeat(256)}) {
            assertThatThrownBy(() -> new GlobalUniqueClaim("order", invalid, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> new GlobalUniqueClaim("order", "key", "b".repeat(101), null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

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

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.database.config.DatabaseProperties;
import io.github.loadup.components.globalunique.dataobject.GlobalUniqueDO;
import io.github.loadup.components.globalunique.mapper.GlobalUniqueDOMapper;
import io.github.loadup.components.globalunique.model.GlobalUniqueClaim;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;

class GlobalUniqueContractTest {
    private final GlobalUniqueDOMapper mapper = mock(GlobalUniqueDOMapper.class);
    private final DatabaseProperties properties = new DatabaseProperties();
    private final org.springframework.transaction.PlatformTransactionManager transactionManager = mock(org.springframework.transaction.PlatformTransactionManager.class);
    private final DefaultGlobalUniqueTemplate template = new DefaultGlobalUniqueTemplate(mapper, properties, transactionManager);

    @org.junit.jupiter.api.BeforeEach
    void transaction() {
        when(transactionManager.getTransaction(any())).thenReturn(new org.springframework.transaction.support.SimpleTransactionStatus());
    }

    @Test
    void tenantDefaultsTrustedContextAndOptionalGlobalScopeRemainDistinct() {
        when(mapper.insert(any(GlobalUniqueDO.class))).thenReturn(1);
        properties.getMultiTenant().setDefaultTenantId("single-tenant");
        assertThat(template.claim("order", "1")).isTrue();
        properties.getMultiTenant().setEnabled(true);
        assertThatThrownBy(() -> template.claim("order", "2")).isInstanceOf(IllegalStateException.class);
        TenantUtil.runWithTenant(
                "trusted", () -> assertThat(template.claim("order", "3")).isTrue());
        properties.getMultiTenant().setRequired(false);
        assertThat(template.claim("order", "4")).isTrue();
        var values = ArgumentCaptor.forClass(GlobalUniqueDO.class);
        verify(mapper, times(3)).insert(values.capture());
        assertThat(values.getAllValues())
                .extracting(GlobalUniqueDO::getTenantId)
                .containsExactly("single-tenant", "trusted", "__loadup_global__");
    }

    @Test
    void onlyDuplicateBusinessClaimsAreConvertedToFalse() {
        var failure = new DuplicateKeyException("synthetic duplicate");
        when(mapper.insert(any(GlobalUniqueDO.class))).thenThrow(failure);
        when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
        assertThatThrownBy(() -> template.claim("order", "key")).isSameAs(failure);
        var stored = new GlobalUniqueDO();
        stored.setId("id");
        stored.setBizType("order");
        stored.setUniqueKey("key");
        when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(stored);
        assertThat(template.claim("order", "key")).isFalse();
    }

    @Test
    void failedInsertAndNullInputCannotBeReportedAsAcquired() {
        when(mapper.insert(any(GlobalUniqueDO.class))).thenReturn(0);
        assertThat(template.claim("order", "key")).isFalse();
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

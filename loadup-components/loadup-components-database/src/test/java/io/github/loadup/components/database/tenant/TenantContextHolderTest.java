/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2026 LoadUp Cloud
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantContextHolderTest {
    @AfterEach
    void clearContext() {
        TenantContextHolder.clear();
    }

    @Test
    void restoresNestedTenantContext() {
        TenantContextHolder.setTenantId("outer");

        TenantContextHolder.runWithTenant(
                "inner", () -> assertThat(TenantContextHolder.getTenantId()).isEqualTo("inner"));

        assertThat(TenantContextHolder.getTenantId()).isEqualTo("outer");
    }

    @Test
    void clearsContextAfterCallbackFailure() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> TenantContextHolder.runWithTenant("tenant", () -> {
                    throw new IllegalStateException("failed");
                }))
                .isInstanceOf(IllegalStateException.class);

        assertThat(TenantContextHolder.hasTenantId()).isFalse();
    }
}

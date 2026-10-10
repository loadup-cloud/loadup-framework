/*-
 * #%L
 * Loadup Common Utils
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
package io.github.loadup.commons.util;

import static org.assertj.core.api.Assertions.*;

import io.github.loadup.commons.context.ContextHolder;
import io.github.loadup.commons.context.ContextKey;
import io.github.loadup.commons.context.ContextKeys;
import io.github.loadup.commons.context.ExecutionContext;
import org.junit.jupiter.api.Test;

class TenantUtilTest {
    @Test
    void scopesTenantWithoutChangingBusinessMetadata() {
        var key = new ContextKey<>("orders.id", String.class);
        var outer = ExecutionContext.empty().with(key, "order").with(ContextKeys.TENANT_ID, "outer");
        ContextHolder.runWith(outer, () -> {
            TenantUtil.runWithTenant(" inner ", () -> {
                assertThat(TenantUtil.getTenantId()).isEqualTo("inner");
                assertThat(ContextHolder.get(key)).isEqualTo("order");
                TenantUtil.runWithTenant(
                        " ", () -> assertThat(TenantUtil.hasTenantId()).isFalse());
                assertThat(TenantUtil.getTenantId()).isEqualTo("inner");
            });
            assertThat(TenantUtil.getTenantId()).isEqualTo("outer");
        });
        assertThat(TenantUtil.hasTenantId()).isFalse();
    }

    @Test
    void restoresAfterFailure() {
        assertThatThrownBy(() -> TenantUtil.runWithTenant("tenant", () -> {
                    throw new IllegalStateException("failed");
                }))
                .isInstanceOf(IllegalStateException.class);
        assertThat(TenantUtil.getTenantId()).isNull();
    }
}

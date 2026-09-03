package io.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-util
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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantUtilTest {
    @AfterEach
    void clearContext() {
        TenantUtil.clear();
    }

    @Test
    void restoresNestedTenantContext() {
        TenantUtil.setTenantId("outer");

        TenantUtil.runWithTenant(
                "inner", () -> assertThat(TenantUtil.getTenantId()).isEqualTo("inner"));

        assertThat(TenantUtil.getTenantId()).isEqualTo("outer");
    }

    @Test
    void clearsContextAfterCallbackFailure() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> TenantUtil.runWithTenant("tenant", () -> {
                    throw new IllegalStateException("failed");
                }))
                .isInstanceOf(IllegalStateException.class);

        assertThat(TenantUtil.hasTenantId()).isFalse();
    }
}

/*
 * #%L
 * LoadUp Merchant
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
package io.github.loadup.modules.merchant;

import static org.assertj.core.api.Assertions.*;

import io.github.loadup.modules.merchant.domain.model.*;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class MerchantBasicInfoTest {
    @Test
    void nullPrivateFieldsPreserveDataAndEmptyValuesClearIt() {
        var previous = info("M001", "13800138000");
        var retained = info("M001", null).preservePrivateFields(previous);
        assertThat(retained.contactPhone()).isEqualTo("13800138000");
        assertThat(info("M001", "").preservePrivateFields(previous).contactPhone())
                .isEmpty();
    }

    @Test
    void rejectsCodeMutationAndInvalidBasicValues() {
        var at = LocalDateTime.of(2026, 10, 10, 0, 0);
        var merchant =
                new Merchant("id", "tenant", info("M001", null), MerchantStatus.ACTIVE, 1, "actor", "actor", at, at);
        assertThatThrownBy(() -> merchant.update(info("M002", null), "actor", at))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> info("M001", "138****8000")).isInstanceOf(IllegalArgumentException.class);
        assertThat(merchant.changeStatus(MerchantStatus.INACTIVE, "actor", at).rowVersion())
                .isEqualTo(2);
    }

    private MerchantBasicInfo info(String code, String phone) {
        return new MerchantBasicInfo(
                code,
                "Retail",
                null,
                MerchantType.ENTERPRISE,
                "RETAIL",
                "CN",
                null,
                null,
                null,
                null,
                null,
                phone,
                null);
    }
}

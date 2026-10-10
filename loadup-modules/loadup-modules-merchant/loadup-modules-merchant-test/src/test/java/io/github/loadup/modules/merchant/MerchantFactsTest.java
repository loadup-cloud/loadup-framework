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
import static org.mockito.Mockito.*;

import io.github.loadup.modules.contract.client.spi.MerchantFactsProvider;
import io.github.loadup.modules.merchant.client.api.MerchantLookup;
import io.github.loadup.modules.merchant.client.dto.MerchantProfileDTO;
import io.github.loadup.modules.merchant.contract.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class MerchantFactsTest {
    @Test
    void basicFactsDoNotInventQualificationAndMissingRegionRemainsUnknown() {
        MerchantLookup lookup = (tenant, id) ->
                Optional.of(new MerchantProfileDTO(id, tenant, "M001", "ENTERPRISE", "RETAIL", "CN", null, "", true));
        var profile = new MerchantContractFactsProvider(lookup).load("tenant", "id");
        assertThat(profile.active()).isTrue();
        assertThat(profile.facts())
                .containsOnlyKeys("merchant.code", "merchant.type", "merchant.industry", "merchant.country");
        assertThat(profile.facts().get("merchant.industry").value()).isEqualTo("RETAIL");
    }

    @Test
    void missingAndDisabledMerchantsAreNotPromotedToActive() {
        assertThat(new MerchantContractFactsProvider((tenant, id) -> Optional.empty()).load("tenant", "missing"))
                .isNull();
        var provider = new MerchantContractFactsProvider((tenant, id) -> Optional.of(
                new MerchantProfileDTO(id, tenant, "M001", "ENTERPRISE", "RETAIL", "CN", null, null, false)));
        assertThat(provider.load("tenant", "id").active()).isFalse();
    }

    @Test
    void mismatchedTenantOrMerchantIdentityIsRejected() {
        var provider = new MerchantContractFactsProvider((tenant, id) -> Optional.of(
                new MerchantProfileDTO(id, "other", "M001", "ENTERPRISE", "RETAIL", "CN", null, null, true)));
        assertThatThrownBy(() -> provider.load("tenant", "id")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void optionalAdapterRequiresLookupAndBacksOffForCustomProvider() {
        var runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(MerchantContractAutoConfiguration.class));
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(MerchantFactsProvider.class);
        });
        runner.withBean(MerchantLookup.class, () -> mock(MerchantLookup.class)).run(context -> {
            assertThat(context).hasSingleBean(MerchantFactsProvider.class);
        });
        var custom = mock(MerchantFactsProvider.class);
        runner.withBean(MerchantLookup.class, () -> mock(MerchantLookup.class))
                .withBean(MerchantFactsProvider.class, () -> custom)
                .run(context -> {
                    assertThat(context).hasSingleBean(MerchantFactsProvider.class);
                    assertThat(context.getBean(MerchantFactsProvider.class)).isSameAs(custom);
                });
    }
}

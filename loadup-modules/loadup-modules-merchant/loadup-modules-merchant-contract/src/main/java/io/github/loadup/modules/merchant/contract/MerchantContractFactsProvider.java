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
package io.github.loadup.modules.merchant.contract;

import io.github.loadup.modules.contract.client.dto.MerchantProfileDTO;
import io.github.loadup.modules.contract.client.dto.ValueDTO;
import io.github.loadup.modules.contract.client.spi.MerchantFactsProvider;
import io.github.loadup.modules.merchant.client.api.MerchantLookup;
import java.util.HashMap;
import java.util.Map;

/** Supplies only managed basic facts, never unverified qualification or private contact fields. */
public class MerchantContractFactsProvider implements MerchantFactsProvider {
    private final MerchantLookup lookup;

    public MerchantContractFactsProvider(MerchantLookup lookup) {
        this.lookup = lookup;
    }

    public MerchantProfileDTO load(String tenant, String id) {
        var found = lookup.find(tenant, id);
        if (found.isEmpty()) return null;
        var merchant = found.get();
        if (!tenant.equals(merchant.tenantId()) || !id.equals(merchant.id()))
            throw new IllegalStateException("Merchant identity mismatch");
        Map<String, ValueDTO> facts = new HashMap<>();
        put(facts, "merchant.code", merchant.merchantCode());
        put(facts, "merchant.type", merchant.type());
        put(facts, "merchant.industry", merchant.industry());
        put(facts, "merchant.country", merchant.country());
        put(facts, "merchant.province", merchant.province());
        put(facts, "merchant.city", merchant.city());
        return new MerchantProfileDTO(id, merchant.active(), Map.copyOf(facts));
    }

    private static void put(Map<String, ValueDTO> facts, String key, String value) {
        if (value != null && !value.isBlank()) facts.put(key, new ValueDTO("STRING", value));
    }
}

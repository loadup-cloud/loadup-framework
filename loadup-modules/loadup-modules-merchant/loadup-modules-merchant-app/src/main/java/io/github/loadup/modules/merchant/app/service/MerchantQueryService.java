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
package io.github.loadup.modules.merchant.app.service;

import io.github.loadup.modules.merchant.app.converter.MerchantConverter;
import io.github.loadup.modules.merchant.client.dto.MerchantProfileDTO;
import io.github.loadup.modules.merchant.client.facade.MerchantQueryFacade;
import io.github.loadup.modules.merchant.domain.gateway.MerchantGateway;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Uses the primary data source without a read-only replica-routing hint. */
@Service
public class MerchantQueryService implements MerchantQueryFacade {
    private final MerchantGateway gateway;
    private final MerchantConverter converter;

    public MerchantQueryService(MerchantGateway gateway, MerchantConverter converter) {
        this.gateway = gateway;
        this.converter = converter;
    }

    public Optional<MerchantProfileDTO> find(String tenant, String id) {
        if (!MerchantIdentity.tenant().equals(tenant))
            throw new IllegalArgumentException("Merchant lookup tenant mismatch");
        return gateway.find(tenant, id).map(converter::toProfile);
    }
}

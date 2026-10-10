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

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.merchant.app.converter.MerchantConverter;
import io.github.loadup.modules.merchant.client.command.*;
import io.github.loadup.modules.merchant.client.dto.*;
import io.github.loadup.modules.merchant.client.query.MerchantQuery;
import io.github.loadup.modules.merchant.domain.gateway.MerchantGateway;
import io.github.loadup.modules.merchant.domain.model.*;
import java.time.*;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantService implements io.github.loadup.modules.merchant.client.facade.MerchantFacade {
    private final MerchantGateway gateway;
    private final MerchantConverter converter;
    private final Clock clock;

    public MerchantService(MerchantGateway gateway, MerchantConverter converter, Clock clock) {
        this.gateway = gateway;
        this.converter = converter;
        this.clock = clock;
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    @Transactional
    public MerchantDTO create(MerchantCreateCommand command, String actor) {
        String tenant = MerchantIdentity.tenant();
        MerchantIdentity.actor(actor);
        var at = now();
        var merchant = new Merchant(
                UUID.randomUUID().toString(),
                tenant,
                converter.toInfo(command),
                MerchantStatus.ACTIVE,
                1,
                actor,
                actor,
                at,
                at);
        try {
            gateway.insert(merchant);
        } catch (DuplicateKeyException e) {
            throw new CommonException(MerchantError.CONFLICT, "Merchant code already exists", e);
        }
        return converter.toDTO(merchant);
    }

    @Transactional
    public MerchantDTO update(MerchantUpdateCommand command, String actor) {
        MerchantIdentity.actor(actor);
        var current = require(command.id());
        expected(current, command.expectedRowVersion());
        var updated = current.update(converter.merge(converter.toInfo(command), current.info()), actor, now());
        gateway.update(updated, current.rowVersion());
        return converter.toDTO(updated);
    }

    @Transactional
    public MerchantDTO changeStatus(MerchantStatusCommand command, String actor) {
        MerchantIdentity.actor(actor);
        var current = require(command.id());
        expected(current, command.expectedRowVersion());
        var updated = current.changeStatus(MerchantStatus.valueOf(command.status()), actor, now());
        gateway.update(updated, current.rowVersion());
        return converter.toDTO(updated);
    }

    private void expected(Merchant current, long expected) {
        if (current.rowVersion() != expected) throw new CommonException(MerchantError.CONFLICT);
    }

    private Merchant require(String id) {
        return gateway.find(MerchantIdentity.tenant(), id)
                .orElseThrow(() -> new CommonException(MerchantError.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public MerchantDTO detail(String id) {
        return converter.toDTO(require(id));
    }

    @Transactional(readOnly = true)
    public MerchantPageDTO page(MerchantQuery query) {
        if (query.page() < 1 || query.size() < 1 || query.size() > 100)
            throw new IllegalArgumentException("Invalid page");
        var result = gateway.page(
                MerchantIdentity.tenant(),
                query.merchantCode(),
                query.name(),
                query.status() == null || query.status().isBlank() ? null : MerchantStatus.valueOf(query.status()),
                query.page(),
                query.size());
        return new MerchantPageDTO(
                result.items().stream().map(converter::toDTO).toList(), result.total(), query.page(), query.size());
    }
}

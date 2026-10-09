/*
 * #%L
 * LoadUp Contract
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
package io.github.loadup.modules.contract.app.service;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.contract.app.converter.ContractConverter;
import io.github.loadup.modules.contract.app.support.*;
import io.github.loadup.modules.contract.client.command.*;
import io.github.loadup.modules.contract.client.dto.*;
import io.github.loadup.modules.contract.client.query.CatalogQuery;
import io.github.loadup.modules.contract.domain.gateway.CatalogGateway;
import io.github.loadup.modules.contract.domain.model.*;
import java.time.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {
    private final CatalogGateway gateway;
    private final ContractCodec codec;
    private final CatalogAssembler assembler;
    private final ContractConverter converter;
    private final Clock clock;

    public CatalogService(
            CatalogGateway gateway,
            ContractCodec codec,
            CatalogAssembler assembler,
            ContractConverter converter,
            Clock clock) {
        this.gateway = gateway;
        this.codec = codec;
        this.assembler = assembler;
        this.converter = converter;
        this.clock = clock;
    }

    @Transactional
    public CatalogVersionDTO save(CatalogSaveCommand command, String actor) {
        String tenant = ContractIdentity.tenant();
        ContractIdentity.actor(actor);
        String code = ContractIdentity.code(command.code(), 128);
        CatalogKind kind = CatalogKind.valueOf(command.kind());
        if (command.version() < 1 || command.definition() == null)
            throw new IllegalArgumentException("Invalid catalog definition");
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        CatalogEntry previous = command.id() == null
                ? null
                : gateway.lock(tenant, command.id()).orElseThrow(() -> new CommonException(ContractError.NOT_FOUND));
        if (previous != null
                && (previous.status() != CatalogStatus.DRAFT
                        || command.expectedRowVersion() == null
                        || previous.rowVersion() != command.expectedRowVersion()
                        || previous.kind() != kind
                        || !previous.code().equals(code)
                        || previous.version() != command.version())) {
            throw new CommonException(
                    ContractError.CONFLICT,
                    "Only the expected draft can be edited; copy to a new version to change identity");
        }
        CatalogEntry entry = new CatalogEntry(
                previous == null ? UUID.randomUUID().toString() : previous.id(),
                tenant,
                kind,
                code,
                command.version(),
                CatalogStatus.DRAFT,
                previous == null ? 1 : Math.addExact(previous.rowVersion(), 1),
                codec.write(command.definition()),
                actor,
                previous == null ? now : previous.createdAt(),
                now);
        assembler.validate(entry, false);
        if (previous == null) {
            try {
                gateway.insert(entry);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                throw new CommonException(ContractError.CONFLICT, "Catalog code and version already exist", e);
            }
        } else gateway.update(entry, previous.rowVersion());
        return converter.toCatalog(entry);
    }

    @Transactional
    public CatalogVersionDTO publish(CatalogActionCommand command, String actor) {
        return transition(command, actor, CatalogStatus.DRAFT, CatalogStatus.PUBLISHED);
    }

    @Transactional
    public CatalogVersionDTO retire(CatalogActionCommand command, String actor) {
        return transition(command, actor, CatalogStatus.PUBLISHED, CatalogStatus.RETIRED);
    }

    private CatalogVersionDTO transition(
            CatalogActionCommand command, String actor, CatalogStatus from, CatalogStatus to) {
        String tenant = ContractIdentity.tenant();
        ContractIdentity.actor(actor);
        CatalogEntry entry =
                gateway.lock(tenant, command.id()).orElseThrow(() -> new CommonException(ContractError.NOT_FOUND));
        if (entry.rowVersion() != command.expectedRowVersion()) throw new CommonException(ContractError.CONFLICT);
        if (entry.status() != from) throw new CommonException(ContractError.INVALID_STATE);
        if (to == CatalogStatus.PUBLISHED) assembler.validate(entry, true);
        var updated = new CatalogEntry(
                entry.id(),
                tenant,
                entry.kind(),
                entry.code(),
                entry.version(),
                to,
                Math.addExact(entry.rowVersion(), 1),
                entry.content(),
                actor,
                entry.createdAt(),
                LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC));
        gateway.update(updated, entry.rowVersion());
        return converter.toCatalog(updated);
    }

    @Transactional(readOnly = true)
    public CatalogVersionDTO detail(String id) {
        return converter.toCatalog(gateway.find(ContractIdentity.tenant(), id)
                .orElseThrow(() -> new CommonException(ContractError.NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public ContractPageDTO<CatalogVersionDTO> page(CatalogQuery query) {
        ContractIdentity.page(query.page(), query.size());
        var page = gateway.page(
                ContractIdentity.tenant(),
                CatalogKind.valueOf(query.kind()),
                query.status() == null ? null : CatalogStatus.valueOf(query.status()),
                query.code(),
                query.page(),
                query.size());
        return new ContractPageDTO<>(
                page.items().stream().map(converter::toCatalog).toList(), page.total(), query.page(), query.size());
    }
}

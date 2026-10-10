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
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.contract.app.converter.ContractConverter;
import io.github.loadup.modules.contract.app.support.*;
import io.github.loadup.modules.contract.client.command.*;
import io.github.loadup.modules.contract.client.dto.*;
import io.github.loadup.modules.contract.client.query.MerchantContractPageQuery;
import io.github.loadup.modules.contract.domain.gateway.*;
import io.github.loadup.modules.contract.domain.model.*;
import java.time.*;
import java.util.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class MerchantContractService implements io.github.loadup.modules.contract.client.facade.MerchantContractFacade {
    private final CatalogGateway catalog;
    private final MerchantContractGateway gateway;
    private final CatalogAssembler assembler;
    private final ContractCodec codec;
    private final ContractConverter converter;
    private final MerchantFacts facts;
    private final Clock clock;
    private final TransactionTemplate transactions;

    public MerchantContractService(
            CatalogGateway catalog,
            MerchantContractGateway gateway,
            CatalogAssembler assembler,
            ContractCodec codec,
            ContractConverter converter,
            MerchantFacts facts,
            Clock clock,
            PlatformTransactionManager transactionManager) {
        this.catalog = catalog;
        this.gateway = gateway;
        this.assembler = assembler;
        this.codec = codec;
        this.converter = converter;
        this.facts = facts;
        this.clock = clock;
        this.transactions = new TransactionTemplate(transactionManager);
        this.transactions.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public MerchantContractDTO preview(ContractSignCommand command, String actor) {
        String tenant = ContractIdentity.tenant();
        validate(command, actor);
        var profile = facts.load(tenant, command.merchantId());
        var entry = catalog.find(tenant, command.planVersionId())
                .orElseThrow(() -> new CommonException(ContractError.NOT_FOUND));
        requirePlan(entry);
        Instant now = clock.instant();
        String id = UUID.randomUUID().toString();
        var snapshot = sign(tenant, id, entry, command, profile, now);
        return converter.toContract(header(tenant, id, command, "preview", actor, now), snapshot);
    }

    public MerchantContractDTO sign(ContractSignCommand command, String actor) {
        String tenant = ContractIdentity.tenant();
        validate(command, actor);
        String digest = requestDigest(command);
        var existing = gateway.findByRequest(tenant, command.requestKey());
        if (existing.isPresent()) return replay(existing.get(), digest);
        // External merchant lookup completes before the transaction acquires catalog/database locks.
        var profile = facts.load(tenant, command.merchantId());
        return Objects.requireNonNull(transactions.execute(status -> {
            var replay = gateway.findByRequest(tenant, command.requestKey());
            if (replay.isPresent()) return replay(replay.get(), digest);
            var entry = catalog.lock(tenant, command.planVersionId())
                    .orElseThrow(() -> new CommonException(ContractError.NOT_FOUND));
            requirePlan(entry);
            Instant now = clock.instant();
            String id = UUID.randomUUID().toString();
            var snapshot = sign(tenant, id, entry, command, profile, now);
            var header = header(tenant, id, command, digest, actor, now);
            var revision = new ContractRevisionRecord(
                    UUID.randomUUID().toString(),
                    tenant,
                    id,
                    1,
                    codec.write(snapshot),
                    snapshot.snapshotHash(),
                    header.createdAt(),
                    header.createdAt());
            try {
                gateway.insert(header, revision);
            } catch (DuplicateKeyException e) {
                var winner = gateway.findByRequest(tenant, command.requestKey());
                if (winner.isPresent()) return replay(winner.get(), digest);
                throw new CommonException(ContractError.CONFLICT, "Merchant scope already has a contract", e);
            }
            return converter.toContract(header, snapshot);
        }));
    }

    private void validate(ContractSignCommand command, String actor) {
        ContractIdentity.actor(actor);
        ContractIdentity.code(command.merchantId(), 64);
        ContractIdentity.code(command.scopeKey(), 128);
        ContractIdentity.code(command.requestKey(), 128);
        if (command.selectedItems() == null || command.values() == null)
            throw new IllegalArgumentException("Selection and values required");
    }

    private void requirePlan(CatalogEntry entry) {
        if (entry.kind() != CatalogKind.SALES_PLAN || entry.status() != CatalogStatus.PUBLISHED)
            throw new CommonException(ContractError.INVALID_STATE, "Plan must be published and on sale");
    }

    private MerchantContractRevision sign(
            String tenant,
            String id,
            CatalogEntry entry,
            ContractSignCommand command,
            Map<String, TypedValue> profile,
            Instant now) {
        var plan = assembler.plan(entry, false);
        return new ContractSigner()
                .sign(
                        tenant,
                        command.merchantId(),
                        command.scopeKey(),
                        id,
                        1,
                        plan,
                        command.selectedItems(),
                        assembler.merchantValues(plan, command.values()),
                        profile,
                        now,
                        command.effectiveFrom() == null ? now : command.effectiveFrom(),
                        command.effectiveTo());
    }

    private ContractRecord header(
            String tenant, String id, ContractSignCommand command, String digest, String actor, Instant now) {
        LocalDateTime at = LocalDateTime.ofInstant(now, ZoneOffset.UTC);
        return new ContractRecord(
                id,
                tenant,
                command.merchantId(),
                command.scopeKey(),
                command.planVersionId(),
                ContractStatus.NORMAL,
                1,
                command.requestKey(),
                digest,
                actor,
                at,
                at);
    }

    private String requestDigest(ContractSignCommand command) {
        Map<String, Object> value = new HashMap<>();
        value.put("merchantId", command.merchantId());
        value.put("scopeKey", command.scopeKey());
        value.put("planVersionId", command.planVersionId());
        value.put("selectedItems", command.selectedItems());
        value.put("values", command.values());
        value.put(
                "effectiveFrom",
                command.effectiveFrom() == null ? null : command.effectiveFrom().toString());
        value.put(
                "effectiveTo",
                command.effectiveTo() == null ? null : command.effectiveTo().toString());
        return codec.digest(value);
    }

    private MerchantContractDTO replay(ContractRecord existing, String digest) {
        if (!existing.requestDigest().equals(digest))
            throw new CommonException(ContractError.CONFLICT, "Request key reused with different payload");
        return toDTO(existing);
    }

    public MerchantContractRevision snapshot(ContractRecord header) {
        var stored = gateway.revision(header.tenantId(), header.id(), 1)
                .orElseThrow(() -> new CommonException(ContractError.NOT_FOUND));
        var snapshot = codec.read(stored.snapshot(), MerchantContractRevision.class);
        if (!snapshot.snapshotHash().equals(stored.snapshotHash())
                || !snapshot.tenantId().equals(header.tenantId())
                || !snapshot.merchantId().equals(header.merchantId())
                || !snapshot.scopeKey().equals(header.scopeKey())
                || !snapshot.contractId().equals(header.id())
                || snapshot.revision() != 1) {
            throw new IllegalStateException("Stored contract snapshot integrity failure");
        }
        return snapshot;
    }

    private MerchantContractDTO toDTO(ContractRecord header) {
        return converter.toContract(header, snapshot(header));
    }

    @Transactional(readOnly = true)
    public MerchantContractDTO detail(String id) {
        return toDTO(gateway.find(ContractIdentity.tenant(), id)
                .orElseThrow(() -> new CommonException(ContractError.NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public PageDTO<MerchantContractDTO> page(MerchantContractPageQuery query) {
        ContractIdentity.page(query.page(), query.size());
        var page = gateway.page(ContractIdentity.tenant(), query.merchantId(), query.page(), query.size());
        return PageDTO.of(page.items().stream().map(this::toDTO).toList(), page.total(), query.page(), query.size());
    }

    @Transactional
    public MerchantContractDTO changeStatus(ContractStatusCommand command, String actor) {
        ContractIdentity.actor(actor);
        String tenant = ContractIdentity.tenant();
        ContractStatus target = ContractStatus.valueOf(command.status());
        var header = gateway.find(tenant, command.id()).orElseThrow(() -> new CommonException(ContractError.NOT_FOUND));
        if (header.status() == ContractStatus.TERMINATED) throw new CommonException(ContractError.INVALID_STATE);
        if (header.generation() != command.expectedGeneration()) throw new CommonException(ContractError.CONFLICT);
        if (header.status() != target) gateway.updateStatus(tenant, header.id(), header.generation(), target);
        return detail(command.id());
    }
}

/*
 * #%L
 * LoadUp Contract Infrastructure
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
package io.github.loadup.modules.contract.infrastructure.repository;

import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.contract.client.error.ContractResultCodeEnum;
import io.github.loadup.modules.contract.domain.gateway.MerchantContractGateway;
import io.github.loadup.modules.contract.domain.gateway.RequestKeyConflictException;
import io.github.loadup.modules.contract.domain.model.ContractRecord;
import io.github.loadup.modules.contract.domain.model.ContractRevisionRecord;
import io.github.loadup.modules.contract.domain.model.ContractStatus;
import io.github.loadup.modules.contract.infrastructure.dataobject.ContractRevisionRow;
import io.github.loadup.modules.contract.infrastructure.dataobject.MerchantContractRow;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MerchantContractGatewayImpl implements MerchantContractGateway {
    private final NamedParameterJdbcTemplate jdbc;
    private final MerchantContractRepository contracts;
    private final ContractRevisionRepository revisions;
    private final Clock clock;

    public MerchantContractGatewayImpl(
            NamedParameterJdbcTemplate jdbc,
            MerchantContractRepository contracts,
            ContractRevisionRepository revisions,
            @Qualifier("databaseClock") Clock clock) {
        this.jdbc = jdbc;
        this.contracts = contracts;
        this.revisions = revisions;
        this.clock = clock;
    }

    private static String required(String value) {
        if (value == null || value.isBlank())
            throw new CommonException(ContractResultCodeEnum.MERCHANT_LOOKUP_ID_REQUIRED);
        return value;
    }

    private Optional<ContractRecord> find(String condition, MapSqlParameterSource args) {
        return jdbc
                .query(
                        "SELECT * FROM merchant_contract WHERE tenant_id = :tenant AND deleted = FALSE AND "
                                + condition,
                        args,
                        MerchantContractGatewayImpl::contract)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<ContractRecord> find(String tenant, String id) {
        required(tenant);
        return contracts.find(required(id)).map(MerchantContractGatewayImpl::toContract);
    }

    @Override
    public Optional<ContractRecord> findByScope(String tenant, String merchant, String key) {
        required(tenant);
        return contracts.findByScope(required(merchant), required(key)).map(MerchantContractGatewayImpl::toContract);
    }

    @Override
    public Optional<ContractRecord> lockByScope(String tenant, String merchant, String key) {
        return find(
                "merchant_id = :merchant AND scope_key = :key FOR UPDATE",
                new MapSqlParameterSource(
                                "tenant", io.github.loadup.components.database.tenant.TenantContext.tenantId())
                        .addValue("merchant", required(merchant))
                        .addValue("key", required(key)));
    }

    @Override
    public Optional<ContractRecord> findByRequest(String tenant, String key) {
        required(tenant);
        return contracts.findByRequest(required(key)).map(MerchantContractGatewayImpl::toContract);
    }

    @Override
    public Optional<ContractRevisionRecord> revision(String tenant, String id, int version) {
        required(tenant);
        return revisions.revision(required(id), version).map(MerchantContractGatewayImpl::toRevision);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void insert(ContractRecord record, ContractRevisionRecord revision) {
        if (!required(record.id()).equals(required(revision.contractId()))
                || !required(record.tenantId()).equals(required(revision.tenantId())))
            throw new IllegalArgumentException("Contract revision must belong to the new contract and tenant");
        LocalDateTime now = LocalDateTime.now(clock);
        var args = new MapSqlParameterSource("id", record.id())
                .addValue("tenant", record.tenantId())
                .addValue("merchant", record.merchantId())
                .addValue("scope", record.scopeKey())
                .addValue("plan", record.planVersionId())
                .addValue("status", record.status().name())
                .addValue("generation", record.generation())
                .addValue("key", record.requestKey())
                .addValue("digest", record.requestDigest())
                .addValue("actor", record.createdBy())
                .addValue("created", record.createdAt() == null ? now : record.createdAt())
                .addValue("updated", now);
        int inserted;
        try {
            inserted = jdbc.update("""
                INSERT INTO merchant_contract (id, tenant_id, merchant_id, scope_key, plan_version_id, status, generation,
                    request_key, request_digest, created_by, created_at, updated_at, deleted)
                VALUES (:id, :tenant, :merchant, :scope, :plan, :status, :generation, :key, :digest, :actor, :created, :updated, FALSE)
                ON CONFLICT (tenant_id, request_key) DO NOTHING
                """, args);
        } catch (DuplicateKeyException failure) {
            if (scopeConflict(failure)) throw new CommonException(ContractResultCodeEnum.CONFLICT, failure);
            throw failure;
        }
        if (inserted == 0) throw new RequestKeyConflictException();
        if (inserted != 1) throw new IllegalStateException("Unexpected contract insert count");
        var rev = new MapSqlParameterSource("id", required(revision.id()))
                .addValue("tenant", revision.tenantId())
                .addValue("contract", revision.contractId())
                .addValue("revision", revision.revision())
                .addValue("snapshot", revision.snapshot())
                .addValue("hash", revision.snapshotHash())
                .addValue("created", revision.createdAt() == null ? now : revision.createdAt())
                .addValue("updated", now);
        if (jdbc.update("""
                INSERT INTO merchant_contract_revision (id, tenant_id, contract_id, revision, snapshot, snapshot_hash,
                    created_at, updated_at, deleted) VALUES (:id, :tenant, :contract, :revision, :snapshot, :hash, :created, :updated, FALSE)
                """, rev) != 1) throw new IllegalStateException("Unexpected contract revision insert count");
    }

    private static boolean scopeConflict(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof PSQLException sql
                    && "23505".equals(sql.getSQLState())
                    && sql.getServerErrorMessage() != null
                    && "uk_merchant_contract_scope"
                            .equals(sql.getServerErrorMessage().getConstraint())) return true;
        }
        return false;
    }

    @Override
    public void updateStatus(String tenant, String id, long generation, ContractStatus status) {
        var args = new MapSqlParameterSource("tenant", required(tenant))
                .addValue("id", required(id))
                .addValue("generation", generation)
                .addValue("next", Math.addExact(generation, 1))
                .addValue("status", status.name())
                .addValue("updated", LocalDateTime.now(clock));
        int changed = jdbc.update("""
                UPDATE merchant_contract SET status = :status, generation = :next, updated_at = :updated
                WHERE tenant_id = :tenant AND id = :id AND generation = :generation AND status <> 'TERMINATED' AND deleted = FALSE
                """, args);
        if (changed != 1) throw new CommonException(ContractResultCodeEnum.CONTRACT_STATE_CHANGED_OR_TERMINATED);
    }

    @Override
    public PageResult<ContractRecord> page(String tenant, String merchant, int page, int size) {
        var sql = new StringBuilder(" FROM merchant_contract WHERE tenant_id = :tenant AND deleted = FALSE");
        var args = new MapSqlParameterSource(
                "tenant", io.github.loadup.components.database.tenant.TenantContext.tenantId());
        if (merchant != null && !merchant.isBlank()) {
            sql.append(" AND merchant_id = :merchant");
            args.addValue("merchant", merchant);
        }
        Long total = jdbc.queryForObject("SELECT COUNT(*)" + sql, args, Long.class);
        args.addValue("limit", size).addValue("offset", (long) (page - 1) * size);
        var rows = jdbc.query(
                "SELECT *" + sql + " ORDER BY created_at DESC, id ASC LIMIT :limit OFFSET :offset",
                args,
                MerchantContractGatewayImpl::contract);
        return new PageResult<>(rows, total == null ? 0 : total, page, size);
    }

    private static ContractRecord toContract(MerchantContractRow row) {
        return new ContractRecord(
                row.id(),
                row.tenantId(),
                row.merchantId(),
                row.scopeKey(),
                row.planVersionId(),
                ContractStatus.valueOf(row.status()),
                row.generation(),
                row.requestKey(),
                row.requestDigest(),
                row.createdBy(),
                row.createdAt(),
                row.updatedAt());
    }

    private static ContractRevisionRecord toRevision(ContractRevisionRow row) {
        return new ContractRevisionRecord(
                row.id(),
                row.tenantId(),
                row.contractId(),
                row.revision(),
                row.snapshot(),
                row.snapshotHash(),
                row.createdAt(),
                row.updatedAt());
    }

    private static ContractRecord contract(ResultSet rs, int row) throws SQLException {
        return new ContractRecord(
                rs.getString("id"),
                rs.getString("tenant_id"),
                rs.getString("merchant_id"),
                rs.getString("scope_key"),
                rs.getString("plan_version_id"),
                ContractStatus.valueOf(rs.getString("status")),
                rs.getLong("generation"),
                rs.getString("request_key"),
                rs.getString("request_digest"),
                rs.getString("created_by"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("updated_at", LocalDateTime.class));
    }
}

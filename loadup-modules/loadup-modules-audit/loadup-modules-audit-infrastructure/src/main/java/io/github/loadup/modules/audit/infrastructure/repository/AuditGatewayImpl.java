/*
 * #%L
 * LoadUp Audit Infrastructure
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
package io.github.loadup.modules.audit.infrastructure.repository;

import static io.github.loadup.modules.audit.infrastructure.dataobject.table.Tables.*;

import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.modules.audit.domain.gateway.AuditGateway;
import io.github.loadup.modules.audit.domain.model.*;
import io.github.loadup.modules.audit.infrastructure.converter.AuditStorageConverter;
import io.github.loadup.modules.audit.infrastructure.dataobject.*;
import io.github.loadup.modules.audit.infrastructure.mapper.*;
import java.util.*;

public class AuditGatewayImpl implements AuditGateway {

    private final AuditEventMapper mapper;
    private final AuditStorageConverter converter;

    public AuditGatewayImpl(AuditEventMapper mapper, AuditStorageConverter converter) {
        this.mapper = mapper;
        this.converter = converter;
    }

    @Override
    public void insert(AuditEvent event) {
        mapper.insert(converter.toDO(event));
    }

    @Override
    public AuditPage search(AuditQuery query) {
        var q = QueryWrapper.create()
                .from(AUDIT_EVENT_DO)
                .where(
                        query.tenantId() == null
                                ? AUDIT_EVENT_DO.TENANT_ID.isNull()
                                : AUDIT_EVENT_DO.TENANT_ID.eq(query.tenantId()));
        if (query.actorId() != null && !query.actorId().isBlank()) q.and(AUDIT_EVENT_DO.ACTOR_ID.eq(query.actorId()));
        if (query.action() != null && !query.action().isBlank()) q.and(AUDIT_EVENT_DO.ACTION.eq(query.action()));
        if (query.outcome() != null && !query.outcome().isBlank()) q.and(AUDIT_EVENT_DO.OUTCOME.eq(query.outcome()));
        if (query.from() != null) q.and(AUDIT_EVENT_DO.OCCURRED_AT.ge(query.from()));
        if (query.to() != null) q.and(AUDIT_EVENT_DO.OCCURRED_AT.le(query.to()));
        long total = mapper.selectCountByQuery(q);
        q.orderBy(AUDIT_EVENT_DO.OCCURRED_AT.desc(), AUDIT_EVENT_DO.ID.desc())
                .limit(query.size())
                .offset((long) (query.page() - 1) * query.size());
        return new AuditPage(
                mapper.selectListByQuery(q).stream().map(converter::toDomain).toList(),
                total,
                query.page(),
                query.size());
    }
}

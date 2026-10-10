/*
 * #%L
 * Loadup Modules UPMS App Layer
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
package io.github.loadup.modules.upms.app.service;

import io.github.loadup.modules.upms.app.converter.UserSensitiveConverter;
import io.github.loadup.modules.upms.client.dto.UserSensitiveDTO;
import io.github.loadup.modules.upms.client.query.UserSensitiveQuery;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.service.AccessDecisionService;
import io.github.loadup.modules.upms.domain.valueobject.ResourceAttributes;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class UserSensitiveReadService implements io.github.loadup.modules.upms.client.facade.UserSensitiveReadFacade {
    public static final String PERMISSION = "upms:user:sensitive:read";
    private final UserGateway users;
    private final AccessDecisionService decisions;
    private final ObjectProvider<SensitiveReadAudit> audits;
    private final UserSensitiveConverter converter;

    public UserSensitiveReadService(
            UserGateway users,
            AccessDecisionService decisions,
            ObjectProvider<SensitiveReadAudit> audits,
            UserSensitiveConverter converter) {
        this.users = users;
        this.decisions = decisions;
        this.audits = audits;
        this.converter = converter;
    }

    public UserSensitiveDTO read(String actorId, UserSensitiveQuery query) {
        if (actorId == null
                || actorId.isBlank()
                || query == null
                || query.id() == null
                || !query.id().matches("[A-Za-z0-9_-]{1,64}")
                || query.purpose() == null) {
            throw new IllegalArgumentException("Invalid sensitive read request");
        }
        var subject = users.findById(query.id()).orElseThrow(() -> new AccessDeniedException("Sensitive read denied"));
        var decision =
                decisions.decide(actorId, PERMISSION, new ResourceAttributes(subject.getId(), subject.getDeptId()));
        if (!decision.allowed()) throw new AccessDeniedException("Sensitive read denied");
        var audit = audits.getIfAvailable();
        if (audit == null) throw new IllegalStateException("Sensitive read auditing is unavailable");
        audit.record(actorId, subject.getId(), query.purpose());
        return converter.toDTO(subject);
    }
}

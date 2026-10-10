/*
 * #%L
 * LoadUp Audit Domain
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
package io.github.loadup.modules.audit.domain.gateway;

import io.github.loadup.modules.audit.domain.model.AuditEvent;
import io.github.loadup.modules.audit.domain.model.AuditPage;
import io.github.loadup.modules.audit.domain.model.AuditQuery;

/** Append-only event persistence with tenant-scoped searches. */
public interface AuditGateway {
    void insert(AuditEvent event);

    AuditPage search(AuditQuery query);
}

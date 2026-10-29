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
package io.github.loadup.modules.audit.infrastructure.dataobject;

import io.github.loadup.commons.dataobject.BaseDO;
import java.time.LocalDateTime;

public class AuditEventDO extends BaseDO {
    private String actorId;
    private String action;

    private String method;

    private String path;

    private String outcome;
    private String traceId;
    private LocalDateTime occurredAt;

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String value) {
        this.actorId = value;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String value) {
        this.action = value;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String value) {
        this.method = value;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String value) {
        this.path = value;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String value) {
        this.outcome = value;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String value) {
        this.traceId = value;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime value) {
        this.occurredAt = value;
    }
}

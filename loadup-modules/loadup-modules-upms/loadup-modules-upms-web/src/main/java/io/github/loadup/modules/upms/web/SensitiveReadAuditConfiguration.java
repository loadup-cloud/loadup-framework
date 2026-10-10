/*
 * #%L
 * LoadUp UPMS Web Adapter
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
package io.github.loadup.modules.upms.web;

import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.modules.audit.client.command.AuditRecordCommand;
import io.github.loadup.modules.audit.client.facade.AuditFacade;
import io.github.loadup.modules.upms.app.service.SensitiveReadAudit;
import io.github.loadup.modules.upms.client.query.SensitiveReadPurpose;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(AuditFacade.class)
@ConditionalOnBean(AuditFacade.class)
public class SensitiveReadAuditConfiguration {
    @Bean
    @ConditionalOnMissingBean(SensitiveReadAudit.class)
    public SensitiveReadAudit upmsSensitiveReadAudit(AuditFacade audit) {
        return new AuditRecorder(audit);
    }

    public static class AuditRecorder implements SensitiveReadAudit {
        private final AuditFacade audit;

        public AuditRecorder(AuditFacade audit) {
            this.audit = audit;
        }

        @Override
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void record(String actorId, String subjectId, SensitiveReadPurpose purpose) {
            audit.record(new AuditRecordCommand(
                    TenantUtil.getTenantId(),
                    actorId,
                    "UPMS_SENSITIVE_READ:" + purpose.name(),
                    "POST",
                    "/api/upms/user/sensitive/" + subjectId,
                    "SUCCESS",
                    MDC.get("traceId")));
        }
    }
}

/*
 * #%L
 * Loadup UPMS Test
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
package io.github.loadup.modules.upms.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.loadup.modules.upms.app.converter.UserSensitiveConverter;
import io.github.loadup.modules.upms.app.service.SensitiveReadAudit;
import io.github.loadup.modules.upms.app.service.UserSensitiveReadService;
import io.github.loadup.modules.upms.client.dto.UserSensitiveDTO;
import io.github.loadup.modules.upms.client.query.SensitiveReadPurpose;
import io.github.loadup.modules.upms.client.query.UserSensitiveQuery;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.service.AccessDecisionService;
import io.github.loadup.modules.upms.domain.valueobject.AccessDecision;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.security.access.AccessDeniedException;

class UserSensitiveReadServiceTest {
    private final UserGateway users = mock(UserGateway.class);
    private final AccessDecisionService decisions = mock(AccessDecisionService.class);
    private final UserSensitiveConverter converter = mock(UserSensitiveConverter.class);
    private final SensitiveReadAudit audit = mock(SensitiveReadAudit.class);
    private final UserSensitiveQuery query = new UserSensitiveQuery("subject", SensitiveReadPurpose.CUSTOMER_SUPPORT);

    private UserSensitiveReadService service(boolean audited) {
        User subject = new User();
        subject.setId("subject");
        subject.setDeptId("department");
        subject.setMobile("13812345678");
        subject.setEmail("alice@example.com");
        subject.setRealName("Alice");
        when(users.findById("subject")).thenReturn(Optional.of(subject));
        var factory = new StaticListableBeanFactory();
        if (audited) factory.addBean("audit", audit);
        return new UserSensitiveReadService(
                users, decisions, factory.getBeanProvider(SensitiveReadAudit.class), converter);
    }

    @Test
    void deniesBeforeAuditWhenScopeDoesNotMatch() {
        var service = service(true);
        when(decisions.decide(eq("actor"), eq(UserSensitiveReadService.PERMISSION), any()))
                .thenReturn(AccessDecision.deny("NO_MATCHING_GRANT"));
        assertThatThrownBy(() -> service.read("actor", query)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(audit, converter);
    }

    @Test
    void requiresAuditAndPropagatesFailure() {
        when(decisions.decide(anyString(), anyString(), any())).thenReturn(AccessDecision.permit("SUPPORT"));
        var unavailable = service(false);
        assertThatIllegalStateException().isThrownBy(() -> unavailable.read("actor", query));
        var available = service(true);
        doThrow(new IllegalStateException("audit unavailable")).when(audit).record("actor", "subject", query.purpose());
        assertThatIllegalStateException().isThrownBy(() -> available.read("actor", query));
        verifyNoInteractions(converter);
    }

    @Test
    void returnsPlaintextOnlyAfterSuccessfulAuditWithoutLeakingToString() {
        var service = service(true);
        when(decisions.decide(eq("actor"), eq(UserSensitiveReadService.PERMISSION), any()))
                .thenReturn(AccessDecision.permit("SUPPORT"));
        when(converter.toDTO(any()))
                .thenReturn(new UserSensitiveDTO("subject", "Alice", "alice@example.com", "13812345678"));
        var result = service.read("actor", query);
        var ordered = inOrder(audit, converter);
        ordered.verify(audit).record("actor", "subject", query.purpose());
        ordered.verify(converter).toDTO(any());
        assertThat(result.mobile()).isEqualTo("13812345678");
        assertThat(result.toString()).doesNotContain(result.mobile(), result.email(), result.realName());
    }

    @Test
    void rejectsUntrustedQueryMetadata() {
        var service = service(true);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.read("actor", new UserSensitiveQuery("../subject", query.purpose())));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.read("actor", new UserSensitiveQuery("subject", null)));
        verifyNoInteractions(decisions, audit);
    }
}

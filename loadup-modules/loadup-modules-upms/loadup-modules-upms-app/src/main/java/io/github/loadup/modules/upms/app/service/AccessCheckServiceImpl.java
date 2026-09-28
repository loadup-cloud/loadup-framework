/*-
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

import io.github.loadup.modules.upms.client.command.AccessCheckCommand;
import io.github.loadup.modules.upms.client.dto.AccessDecisionDTO;
import io.github.loadup.modules.upms.client.service.AccessCheckService;
import io.github.loadup.modules.upms.domain.service.AccessDecisionService;
import io.github.loadup.modules.upms.domain.valueobject.AccessDecision;
import io.github.loadup.modules.upms.domain.valueobject.ResourceAttributes;
import org.springframework.stereotype.Service;

@Service
public class AccessCheckServiceImpl implements AccessCheckService {
    private final AccessDecisionService decisionService;

    public AccessCheckServiceImpl(AccessDecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @Override
    public AccessDecisionDTO check(AccessCheckCommand command) {
        if (command == null) return new AccessDecisionDTO(false, "INVALID_REQUEST", null);
        AccessDecision decision = decisionService.decide(
                command.userId(),
                command.permissionCode(),
                new ResourceAttributes(command.resourceOwnerUserId(), command.resourceDepartmentId()));
        return new AccessDecisionDTO(decision.allowed(), decision.reason(), decision.grantingRoleCode());
    }
}

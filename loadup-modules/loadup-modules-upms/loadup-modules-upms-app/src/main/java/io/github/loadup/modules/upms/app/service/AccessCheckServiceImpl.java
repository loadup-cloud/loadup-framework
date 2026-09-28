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

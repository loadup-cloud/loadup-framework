package io.github.loadup.modules.upms.client.service;

import io.github.loadup.modules.upms.client.command.AccessCheckCommand;
import io.github.loadup.modules.upms.client.dto.AccessDecisionDTO;

public interface AccessCheckService {
    AccessDecisionDTO check(AccessCheckCommand command);
}

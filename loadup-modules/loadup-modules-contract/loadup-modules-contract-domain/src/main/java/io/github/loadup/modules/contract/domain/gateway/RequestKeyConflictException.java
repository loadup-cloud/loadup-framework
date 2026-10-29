package io.github.loadup.modules.contract.domain.gateway;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.contract.client.error.ContractResultCodeEnum;

/** The persisted request key was claimed by a concurrent transaction. */
public final class RequestKeyConflictException extends CommonException {
    public RequestKeyConflictException() {
        super(ContractResultCodeEnum.CONFLICT);
    }
}

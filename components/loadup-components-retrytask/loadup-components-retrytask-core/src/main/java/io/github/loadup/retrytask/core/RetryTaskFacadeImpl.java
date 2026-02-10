package io.github.loadup.retrytask.core;

import io.github.loadup.retrytask.facade.RetryTaskFacade;
import io.github.loadup.retrytask.facade.request.RetryTaskRegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The default implementation of the {@link RetryTaskFacade}.
 */
@Service
public class RetryTaskFacadeImpl implements RetryTaskFacade {

    private final RetryTaskService retryTaskService;

    @Autowired
    public RetryTaskFacadeImpl(RetryTaskService retryTaskService) {
        this.retryTaskService = retryTaskService;
    }

    @Override
    public Long register(RetryTaskRegisterRequest request) {
        return retryTaskService.register(request);
    }

    @Override
    public void delete(String bizType, String bizId) {
        retryTaskService.delete(bizType, bizId);
    }

    @Override
    public void reset(String bizType, String bizId) {
        retryTaskService.reset(bizType, bizId);
    }
}

package com.github.loadup.components.gateway.core.ctrl.action;

import com.github.loadup.components.gateway.core.common.annotation.LogTraceId;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;

/**
 * <p>
 * AbstractBussinessAction.java
 * </p>
 */
public abstract class AbstractBusinessAction implements BusinessAction {

    /**
     * The next action we will run
     */
    protected BusinessAction nextAction;

    protected abstract void doBusiness(GatewayRuntimeProcessContext context);

    @LogTraceId
    @Override
    public void process(GatewayRuntimeProcessContext context) {
        doBusiness(context);
        nextAction.process(context);
    }

    public abstract void setNextAction(BusinessAction nextAction);

    public BusinessAction getNextAction() {
        return nextAction;
    }

}
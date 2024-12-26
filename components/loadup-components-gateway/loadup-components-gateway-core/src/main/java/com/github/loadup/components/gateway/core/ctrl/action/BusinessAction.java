package com.github.loadup.components.gateway.core.ctrl.action;

import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;

/**
 * <p>
 * BussinessAction.java
 * </p>
 */
public interface BusinessAction {

    public void process(GatewayRuntimeProcessContext context);

}
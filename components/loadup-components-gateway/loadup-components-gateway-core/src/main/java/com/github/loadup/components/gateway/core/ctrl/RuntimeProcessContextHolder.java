package com.github.loadup.components.gateway.core.ctrl;

import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;

/**
 * <p>
 * ProcessHolder.java
 * </p>
 */
public class RuntimeProcessContextHolder<T> {
    private final static ThreadLocal<GatewayRuntimeProcessContext> processContextLocal = new ThreadLocal<GatewayRuntimeProcessContext>();

    /**
     * create a new runtime process context
     */
    public static GatewayRuntimeProcessContext createRuntimeProcessContext() {
        GatewayRuntimeProcessContext context = new GatewayRuntimeProcessContext();
        processContextLocal.set(context);
        return context;
    }

    /**
     * get runtime process context
     */
    public static GatewayRuntimeProcessContext getRuntimeProcessContext() {
        return processContextLocal.get();
    }

    /**
     * clear thread local
     */
    public static void cleanActionContext() {
        processContextLocal.remove();
    }

}
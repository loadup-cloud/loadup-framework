package com.github.loadup.components.gateway.core.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Gateway Filter Interface
 * 网关过滤器接口，所有的过滤器都需要实现这个接口
 */
public interface GatewayFilter {

    /**
     * 过滤器执行顺序，数字越小优先级越高
     */
    int getOrder();

    /**
     * 是否启用该过滤器
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * 前置处理
     */
    default boolean preHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return true;
    }

    /**
     * 后置处理
     */
    default void postHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
    }

    /**
     * 完成处理
     */
    default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Exception ex) throws Exception {
    }
}

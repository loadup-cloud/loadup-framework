package io.github.loadup.gateway.webmvc.support;

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.model.RouteDefinition;
import io.github.loadup.gateway.facade.model.RouteDefinition.BackendDefinition;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts {@link RouteDefinition} (configuration model) into a compiled {@link RouteConfig}.
 */
public final class RouteConfigConverter {

    private RouteConfigConverter() {}

    public static RouteConfig convert(RouteDefinition def, GatewayProperties properties) {
        BackendDefinition backend = def.getBackend();
        RouteConfig rc = new RouteConfig();
        rc.setRouteId(
                def.getId() != null && !"auto".equals(def.getId())
                        ? def.getId()
                        : "route-" + Integer.toUnsignedString((def.getPath() + ":" + def.getMethod()).hashCode()));
        rc.setRouteName(def.getPath());
        rc.setPath(def.getPath());
        rc.setMethod(def.getMethod() != null ? def.getMethod() : "POST");
        rc.setSecurityCode(def.getSecurityCode());
        rc.setEnabled(def.isEnabled());

        Map<String, Object> props = new HashMap<>();
        if (def.getTimeout() != null) {
            props.putIfAbsent("timeout", def.getTimeout());
        }
        if (def.getWrapResponse() != null) {
            props.putIfAbsent("wrapResponse", def.getWrapResponse());
        }
        rc.setProperties(props);

        rc.setParsedTimeout(parseLong(props.get("timeout"), properties.getDefaultTimeout()));
        rc.setParsedRetryCount(parseInt(props.get("retryCount"), properties.getDefaultRetryCount()));
        Object wrap = props.get("wrapResponse");
        rc.setParsedWrapResponse(wrap instanceof Boolean b ? b : null);

        if (backend != null && backend.getProtocol() != null) {
            String protocol = backend.getProtocol().toUpperCase();
            rc.setProtocol(protocol);
            switch (protocol) {
                case "HTTP" -> {
                    String url = backend.getUrl();
                    rc.setTarget(url != null ? url : "");
                    rc.setTargetUrl(url);
                }
                case "BEAN" -> {
                    String bn = backend.getBeanName() != null ? backend.getBeanName() : "";
                    String mn = backend.getMethodName() != null ? backend.getMethodName() : "";
                    rc.setTarget("bean://" + bn + ":" + mn);
                    rc.setTargetBean(bn);
                    rc.setTargetMethod(mn);
                }
                case "RPC" -> {
                    String rpcUrl = backend.getUrl();
                    rc.setTarget(rpcUrl != null ? "rpc://" + rpcUrl : "");
                    rc.setTargetUrl(rpcUrl);
                }
                default -> rc.setTarget("");
            }
        } else {
            rc.setProtocol("");
            rc.setTarget("");
        }
        return rc;
    }

    private static long parseLong(Object value, long def) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        if (value instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return def;
    }

    private static int parseInt(Object value, int def) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return def;
    }
}

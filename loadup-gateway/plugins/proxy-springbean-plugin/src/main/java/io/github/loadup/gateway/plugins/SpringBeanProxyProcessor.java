package io.github.loadup.gateway.plugins;

/*-
 * #%L
 * Proxy SpringBean Plugin
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.commons.util.JsonUtil;
import io.github.loadup.gateway.facade.constants.GatewayConstants;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.ProxyProcessor;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class SpringBeanProxyProcessor implements ProxyProcessor {
    private static final Logger log = LoggerFactory.getLogger(SpringBeanProxyProcessor.class);

    private final ApplicationContext applicationContext;

    public SpringBeanProxyProcessor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        log.info("SpringBeanProxyProcessor initialized");
    }

    @Override
    public String getName() {
        return "SpringBeanProxyPlugin";
    }

    @Override
    public String getType() {
        return "PROXY";
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void initialize() {}

    @Override
    public void destroy() {}

    @Override
    public String getSupportedProtocol() {
        return GatewayConstants.Protocol.BEAN;
    }

    @Override
    public GatewayResponse proxy(GatewayRequest request, RouteConfig route) throws Exception {
        setupUserContext(request);
        try {
            String beanName = route.getTargetBean();
            String methodName = route.getTargetMethod();

            if (beanName == null || methodName == null) {
                throw GatewayExceptionFactory.systemError("Invalid bean target: " + route.getTarget());
            }

            Object bean = applicationContext.getBean(beanName);
            Method method = findMethod(bean.getClass(), methodName);
            if (method == null) {
                throw GatewayExceptionFactory.systemError("Method not found: " + beanName + "." + methodName);
            }

            Object[] args = prepareMethodArgs(request, method);
            Object result = method.invoke(bean, args);

            return GatewayResponse.builder()
                    .requestId(request.getRequestId())
                    .statusCode(GatewayConstants.Status.SUCCESS)
                    .headers(new HashMap<>())
                    .body(result != null ? JsonUtil.toJson(result) : null)
                    .contentType(GatewayConstants.ContentType.JSON)
                    .responseTime(LocalDateTime.now())
                    .build();
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw GatewayExceptionFactory.systemError("Bean invocation failed: " + route.getTargetBean() + "."
                    + route.getTargetMethod() + " — " + cause.getMessage());
        } finally {
            clearUserContext();
        }
    }

    private Method findMethod(Class<?> clazz, String methodName) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) return m;
        }
        return null;
    }

    private Object[] prepareMethodArgs(GatewayRequest request, Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == GatewayRequest.class) {
                args[i] = request;
            } else if (paramTypes[i] == String.class) {
                args[i] = request.getBody();
            } else {
                try {
                    args[i] = JsonUtil.fromJson(request.getBody(), paramTypes[i]);
                } catch (Exception e) {
                    args[i] = null;
                }
            }
        }
        return args;
    }

    @SuppressWarnings("unchecked")
    private void setupUserContext(GatewayRequest request) {
        try {
            String userId = (String) request.getAttributes().get("userId");
            if (userId == null) return;
            String username = (String) request.getAttributes().get("username");
            List<String> roles = (List<String>) request.getAttributes().get("roles");

            Class<?> ucClass = Class.forName("io.github.loadup.components.authorization.context.UserContext");
            Class<?> userClass = Class.forName("io.github.loadup.components.authorization.model.LoadUpUser");
            Object userBuilder = userClass.getMethod("builder").invoke(null);
            userBuilder.getClass().getMethod("userId", String.class).invoke(userBuilder, userId);
            userBuilder.getClass().getMethod("username", String.class).invoke(userBuilder, username);
            userBuilder.getClass().getMethod("roles", List.class).invoke(userBuilder, roles);
            Object user = userBuilder.getClass().getMethod("build").invoke(userBuilder);
            ucClass.getMethod("set", userClass).invoke(null, user);
        } catch (ClassNotFoundException e) {
            // authorization component not on classpath, skip
        } catch (Exception e) {
            log.debug("Failed to setup UserContext", e);
        }
    }

    private void clearUserContext() {
        try {
            Class<?> ucClass = Class.forName("io.github.loadup.components.authorization.context.UserContext");
            ucClass.getMethod("clear").invoke(null);
        } catch (Exception ignored) {
        }
    }
}

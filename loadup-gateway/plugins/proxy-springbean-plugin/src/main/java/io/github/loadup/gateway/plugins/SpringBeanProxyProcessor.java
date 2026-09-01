/*-
 * #%L
 * Proxy SpringBean Plugin
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.gateway.plugins;

import io.github.loadup.commons.util.JsonUtil;
import io.github.loadup.gateway.facade.constants.GatewayConstants;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.ProxyProcessor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
                .body(serializeBody(result))
                .contentType(GatewayConstants.ContentType.JSON)
                .responseTime(LocalDateTime.now())
                .build();
    }

    /**
     * Serializes a backend result into a valid JSON document.
     *
     * <p>{@link JsonUtil#toJson(Object)} returns {@code String} values as-is, which would
     * produce an invalid JSON body (e.g. a bare {@code hello} instead of {@code "hello"}).
     * Serializing through the underlying {@link com.fasterxml.jackson.databind.ObjectMapper}
     * keeps the gateway body contract: it is always a JSON document.
     */
    private static String serializeBody(Object result) throws Exception {
        if (result == null) {
            return null;
        }
        try {
            return JsonUtil.getObjectMapper().writeValueAsString(result);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw GatewayExceptionFactory.systemError("Failed to serialize bean result: " + e.getMessage());
        }
    }

    private Method findMethod(Class<?> clazz, String methodName) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) return m;
        }
        return null;
    }

    /**
     * Resolve method arguments from the request.
     *
     * <p>Strategy (in priority order):
     * <ol>
     *   <li>If BodyParserFilter has run, use {@code parsedBody} map — match by param name</li>
     *   <li>Single POJO param (not String/int/long) → deserialize whole body to that type</li>
     *   <li>Single String param → pass raw body as-is</li>
     *   <li>Multiple params → match by param name from JSON body if possible</li>
     * </ol>
     */
    @SuppressWarnings("unchecked")
    private Object[] prepareMethodArgs(GatewayRequest request, Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[paramTypes.length];

        // Check for parsed body from BodyParserFilter
        Map<String, Object> parsedBody = null;
        Object parsedAttr = request.getAttributes().get("parsedBody");
        if (parsedAttr instanceof Map) {
            parsedBody = (Map<String, Object>) parsedAttr;
        }

        // Single parameter
        if (paramTypes.length == 1) {
            if (paramTypes[0] == GatewayRequest.class) {
                args[0] = request;
            } else if (!isSimpleType(paramTypes[0])) {
                // POJO type: deserialize full body
                args[0] = deserializeBody(request.getBody(), paramTypes[0]);
            } else if (parsedBody != null && !parsedBody.isEmpty()) {
                // Simple type with parsed body: match by param name first, then by single value
                String paramName = parameters[0].getName();
                if (parsedBody.containsKey(paramName)) {
                    args[0] = convertValue(parsedBody.get(paramName), paramTypes[0]);
                } else if (parsedBody.size() == 1) {
                    args[0] = convertValue(parsedBody.values().iterator().next(), paramTypes[0]);
                } else {
                    args[0] = request.getBody();
                }
            } else {
                args[0] = request.getBody();
            }
            return args;
        }

        // Multiple parameters: match by param name from parsed body or JSON body
        for (int i = 0; i < paramTypes.length; i++) {
            String paramName = parameters[i].getName();
            if (parsedBody != null && parsedBody.containsKey(paramName)) {
                args[i] = convertValue(parsedBody.get(paramName), paramTypes[i]);
            } else if (isSimpleType(paramTypes[i])) {
                // Try to extract from raw JSON body
                args[i] = extractFromJson(request.getBody(), paramName, paramTypes[i]);
            } else {
                args[i] = deserializeBody(request.getBody(), paramTypes[i]);
            }
        }

        return args;
    }

    private Object deserializeBody(String body, Class<?> targetType) {
        if (body == null || body.isBlank()) return null;
        try {
            return JsonUtil.fromJson(body, targetType);
        } catch (Exception e) {
            log.debug("Failed to deserialize body to {}: {}", targetType.getSimpleName(), e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Object extractFromJson(String body, String paramName, Class<?> targetType) {
        if (body == null || body.isBlank()) return null;
        try {
            Map<String, Object> map = JsonUtil.toMap(body);
            return convertValue(map.get(paramName), targetType);
        } catch (Exception e) {
            return null;
        }
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isInstance(value)) return value;
        if (targetType == String.class) return value.toString();
        if (targetType == Long.class || targetType == long.class) {
            if (value instanceof Number) return ((Number) value).longValue();
            return Long.parseLong(value.toString());
        }
        if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) return ((Number) value).intValue();
            return Integer.parseInt(value.toString());
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Boolean) return value;
            return Boolean.parseBoolean(value.toString());
        }
        // Fallback: JSON round-trip conversion
        try {
            return JsonUtil.fromJson(JsonUtil.toJson(value), targetType);
        } catch (Exception e) {
            return value;
        }
    }

    private boolean isSimpleType(Class<?> type) {
        return type == String.class
                || type == Integer.class
                || type == int.class
                || type == Long.class
                || type == long.class
                || type == Boolean.class
                || type == boolean.class;
    }
}

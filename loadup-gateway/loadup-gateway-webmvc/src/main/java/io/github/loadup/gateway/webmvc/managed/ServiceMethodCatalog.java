package io.github.loadup.gateway.webmvc.managed;

import io.github.loadup.gateway.api.GatewayExpose;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import tools.jackson.databind.ObjectMapper;

/** Finds explicitly exposed methods once and invokes the Spring proxy for each request. */
public final class ServiceMethodCatalog {
    private final Map<String, HandlerFunction<ServerResponse>> handlers;
    private final int maxBodyBytes;

    public ServiceMethodCatalog(ListableBeanFactory beans, Validator validator, ObjectMapper mapper) {
        this(beans, validator, mapper, 1_048_576);
    }

    public ServiceMethodCatalog(ListableBeanFactory beans, Validator validator, ObjectMapper mapper, int maxBodyBytes) {
        if (maxBodyBytes < 1 || maxBodyBytes == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("service-body-bytes must be between 1 and 2147483646");
        }
        this.maxBodyBytes = maxBodyBytes;
        ConversionService conversion = DefaultConversionService.getSharedInstance();
        Map<String, HandlerFunction<ServerResponse>> discovered = new HashMap<>();
        for (String beanName : beans.getBeanNamesForAnnotation(Service.class)) {
            Object bean;
            try {
                bean = beans.getBean(beanName);
            } catch (Exception ignored) {
                continue;
            }
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            if (targetClass == null) {
                continue;
            }
            for (Method method : targetClass.getMethods()) {
                GatewayExpose expose = AnnotatedElementUtils.findMergedAnnotation(method, GatewayExpose.class);
                if (expose == null) {
                    continue;
                }
                if (!Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalStateException("Gateway method must be public and non-static: " + method);
                }
                Method invocable = AopUtils.selectInvocableMethod(method, bean.getClass());
                validateParameters(method);
                String methodId = expose.value().isBlank() ? method.getName() : expose.value();
                String key = beanName + ":" + methodId;
                HandlerFunction<ServerResponse> handler = request -> {
                    try {
                        return invoke(
                                bean, invocable, method, request, conversion, validator, mapper, this.maxBodyBytes);
                    } catch (Exception e) {
                        return error(e, request, mapper);
                    }
                };
                if (discovered.putIfAbsent(key, handler) != null) {
                    throw new IllegalStateException("Duplicate exposed gateway method: " + key);
                }
            }
        }
        handlers = Map.copyOf(discovered);
    }

    public HandlerFunction<ServerResponse> require(String bean, String method) {
        HandlerFunction<ServerResponse> handler = handlers.get(bean + ":" + method);
        if (handler == null) {
            throw new IllegalArgumentException("Service method is not exposed: " + bean + ":" + method);
        }
        return handler;
    }

    private static void validateParameters(Method method) {
        int bodyCount = 0;
        for (Parameter parameter : method.getParameters()) {
            boolean explicitBody = parameter.isAnnotationPresent(RequestBody.class);
            boolean implicitBody = method.getParameterCount() == 1
                    && !isSimple(parameter.getType())
                    && !parameter.isAnnotationPresent(PathVariable.class)
                    && !parameter.isAnnotationPresent(RequestParam.class)
                    && !parameter.isAnnotationPresent(RequestHeader.class);
            if (explicitBody || implicitBody) {
                bodyCount++;
            } else if (!parameter.isAnnotationPresent(PathVariable.class)
                    && !parameter.isAnnotationPresent(RequestParam.class)
                    && !parameter.isAnnotationPresent(RequestHeader.class)) {
                throw new IllegalStateException("Gateway parameter needs a binding annotation: " + method);
            }
        }
        if (bodyCount > 1) {
            throw new IllegalStateException("Gateway method has multiple body parameters: " + method);
        }
    }

    private static boolean isSimple(Class<?> type) {
        return type.isPrimitive()
                || type.isEnum()
                || type == String.class
                || Number.class.isAssignableFrom(type)
                || type == Boolean.class
                || type == Character.class;
    }

    private static ServerResponse invoke(
            Object bean,
            Method invocable,
            Method definition,
            ServerRequest request,
            ConversionService conversion,
            Validator validator,
            ObjectMapper mapper,
            int maxBodyBytes)
            throws Exception {
        Object[] args = new Object[definition.getParameterCount()];
        Parameter[] parameters = definition.getParameters();
        try {
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                String raw = null;
                if (parameter.isAnnotationPresent(PathVariable.class)) {
                    String name =
                            name(parameter.getAnnotation(PathVariable.class).value(), parameter);
                    raw = request.pathVariable(name);
                } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                    String name =
                            name(parameter.getAnnotation(RequestParam.class).value(), parameter);
                    raw = request.param(name).orElse(null);
                } else if (parameter.isAnnotationPresent(RequestHeader.class)) {
                    String name =
                            name(parameter.getAnnotation(RequestHeader.class).value(), parameter);
                    raw = request.headers().firstHeader(name);
                } else {
                    byte[] body = request.body(byte[].class);
                    if (body.length > maxBodyBytes) {
                        throw new IllegalArgumentException("Service request body exceeds configured limit");
                    }
                    try {
                        args[i] = mapper.readValue(body, parameter.getType());
                    } catch (tools.jackson.core.JacksonException e) {
                        throw new IllegalArgumentException("Invalid JSON request body", e);
                    }
                }
                if (raw != null) {
                    args[i] = conversion.convert(raw, parameter.getType());
                } else if (args[i] == null) {
                    throw new IllegalArgumentException("Missing required parameter: " + parameter.getName());
                }
                Set<ConstraintViolation<Object>> violations = validator.validate(args[i]);
                if (!violations.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Invalid parameter: " + violations.iterator().next().getMessage());
                }
            }
            Object result = invocable.invoke(bean, args);
            return definition.getReturnType() == void.class
                    ? ServerResponse.noContent().build()
                    : ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(mapper.writeValueAsString(result));
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IllegalStateException(cause);
        }
    }

    private static String name(String configured, Parameter parameter) {
        if (!configured.isBlank()) {
            return configured;
        }
        if (!parameter.isNamePresent()) {
            throw new IllegalStateException("Gateway parameter name is unavailable: " + parameter);
        }
        return parameter.getName();
    }

    private static ServerResponse error(Exception error, ServerRequest request, ObjectMapper mapper) throws Exception {
        HttpStatus status;
        String code;
        if (error instanceof AuthenticationException) {
            status = HttpStatus.UNAUTHORIZED;
            code = "UNAUTHORIZED";
        } else if (error instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
            code = "FORBIDDEN";
        } else if (error instanceof IllegalArgumentException
                || error instanceof org.springframework.http.converter.HttpMessageNotReadableException) {
            status = HttpStatus.BAD_REQUEST;
            code = "BAD_REQUEST";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            code = "INTERNAL_ERROR";
        }
        String requestId = request.headers().firstHeader("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        return ServerResponse.status(status)
                .header("X-Request-Id", requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(Map.of("code", code, "requestId", requestId)));
    }
}

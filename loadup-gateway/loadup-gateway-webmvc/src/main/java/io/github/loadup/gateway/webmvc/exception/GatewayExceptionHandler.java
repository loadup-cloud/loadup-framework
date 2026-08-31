/*-
 * #%L
 * Loadup Gateway WebMVC Engine
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
package io.github.loadup.gateway.webmvc.exception;

import io.github.loadup.commons.result.ResultMeta;
import io.github.loadup.commons.util.JsonUtil;
import io.github.loadup.gateway.facade.exception.ErrorType;
import io.github.loadup.gateway.facade.exception.GatewayException;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Converts any exception thrown inside a gateway route into the unified {@code {result, data, meta}} error JSON.
 */
public class GatewayExceptionHandler implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    private static final Logger log = LoggerFactory.getLogger(GatewayExceptionHandler.class);

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next) throws Exception {
        try {
            return next.handle(request);
        } catch (GatewayException e) {
            return buildErrorResponse(request, e);
        } catch (Exception e) {
            log.error(
                    "Unexpected gateway error: type={}, message={}",
                    e.getClass().getName(),
                    e.getMessage(),
                    e);
            return buildErrorResponse(request, GatewayExceptionFactory.wrap(e, "GATEWAY"));
        }
    }

    private ServerResponse buildErrorResponse(ServerRequest request, GatewayException e) {
        String requestId = request.headers().firstHeader("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        if (e.getErrorType() == ErrorType.SYSTEM || e.getErrorType() == ErrorType.NETWORK) {
            log.error(
                    "Gateway error: requestId={}, errorType={}, code={}, message={}",
                    requestId,
                    e.getErrorType().getDescription(),
                    e.getErrorCode(),
                    e.getMessage(),
                    e);
        } else {
            log.warn(
                    "Gateway error: requestId={}, errorType={}, code={}, message={}",
                    requestId,
                    e.getErrorType(),
                    e.getErrorCode(),
                    e.getMessage());
        }

        String code = e.getErrorCode() != null ? e.getErrorCode() : "PROCESS_FAIL";
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", code);
        result.put("status", "FAIL");
        result.put("message", e.getMessage());

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("result", result);
        wrapper.put("data", null);
        wrapper.put("meta", ResultMeta.of(requestId));

        return ServerResponse.status(mapHttpStatus(e))
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonUtil.toJson(wrapper));
    }

    private int mapHttpStatus(GatewayException e) {
        return switch (e.getErrorType()) {
            case ROUTING -> 404;
            case VALIDATION -> 400;
            case SECURITY, AUTHORIZATION -> 401;
            case RATE_LIMIT -> 429;
            case SYSTEM, NETWORK -> 500;
            default -> 500;
        };
    }
}

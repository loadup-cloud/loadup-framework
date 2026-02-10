package io.github.loadup.gateway.core.action;

/*-
 * #%L
 * LoadUp Gateway Core
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

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.result.Result;
import io.github.loadup.commons.result.ResultMeta;
import io.github.loadup.commons.util.JsonUtil;
import io.github.loadup.gateway.facade.constants.GatewayConstants;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.ErrorType;
import io.github.loadup.gateway.facade.exception.GatewayException;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception handling action - catches all exceptions from downstream actions.
 *
 * <p>This action wraps the entire action chain in a try-catch block, ensuring that
 * all exceptions are properly handled and converted to appropriate error responses
 * in unified Result format.</p>
 *
 * <p>Response format (same as ResponseWrapperAction):
 * <pre>
 * {
 *   "result": {
 *     "code": "PROCESS_FAIL",
 *     "status": "FAIL",
 *     "message": "error message"
 *   },
 *   "data": null,
 *   "meta": {
 *     "requestId": "xxx",
 *     "timestamp": "2026-02-05T10:00:00"
 *   }
 * }
 * </pre>
 *
 * <p>Note: This class is registered as a bean in GatewayAutoConfiguration,
 * no need for @Component annotation.</p>
 */
@Slf4j
public class ExceptionAction implements GatewayAction {

    @Override
    public void execute(GatewayContext context, GatewayActionChain chain) {
        long startTime = System.currentTimeMillis();

        try {
            // Proceed to next action in chain
            chain.proceed(context);

        } catch (GatewayException e) {
            // Handle known gateway exceptions
            handleGatewayException(context, e, startTime);

        } catch (Exception e) {
            // Handle unexpected exceptions
            handleUnknownException(context, e, startTime);
        }
    }

    /**
     * Handle known gateway exceptions (routing, validation, business errors, etc.)
     */
    private void handleGatewayException(GatewayContext context, GatewayException e, long startTime) {
        long processingTime = System.currentTimeMillis() - startTime;
        String requestId = context.getRequest().getRequestId();

        // Log based on error type
        if (e.getErrorType() == ErrorType.SYSTEM || e.getErrorType() == ErrorType.NETWORK) {
            // Server errors - log as error
            log.error(
                    "Gateway error: requestId={}, errorType={}, errorCode={}, message={}, processingTime={}ms",
                    requestId,
                    e.getErrorType(),
                    e.getErrorCode(),
                    e.getMessage(),
                    processingTime,
                    e);
        } else {
            // Other errors (configuration, routing, validation, etc.) - log as warning
            log.warn(
                    "Gateway error: requestId={}, errorType={}, errorCode={}, message={}, processingTime={}ms",
                    requestId,
                    e.getErrorType(),
                    e.getErrorCode(),
                    e.getMessage(),
                    processingTime);
        }

        // Build error response in unified Result format
        GatewayResponse errorResponse = buildErrorResponse(requestId, e, processingTime);

        // Set to context
        context.setResponse(errorResponse);
    }

    /**
     * Handle unexpected exceptions (should not happen in normal flow)
     */
    private void handleUnknownException(GatewayContext context, Exception e, long startTime) {
        long processingTime = System.currentTimeMillis() - startTime;
        String requestId = context.getRequest().getRequestId();

        log.error(
                "Unexpected exception in gateway: requestId={}, exceptionType={}, message={}, processingTime={}ms",
                requestId,
                e.getClass().getName(),
                e.getMessage(),
                processingTime,
                e);

        // Wrap as gateway exception
        GatewayException wrapped =
                io.github.loadup.gateway.facade.exception.GatewayExceptionFactory.wrap(e, "UNEXPECTED_ERROR");

        // Build error response
        GatewayResponse errorResponse = buildErrorResponse(requestId, wrapped, processingTime);

        // Set to context
        context.setResponse(errorResponse);
    }

    /**
     * Build error response in unified Result format (same structure as ResponseWrapperAction).
     *
     * <p>Response structure:
     * <pre>
     * {
     *   "result": { "code": "xxx", "status": "xxx", "message": "xxx" },
     *   "data": null,
     *   "meta": { "requestId": "xxx", "timestamp": "xxx" }
     * }
     * </pre>
     */
    private GatewayResponse buildErrorResponse(String requestId, GatewayException e, long processingTime) {
        // 1. Build Result object
        Result result = buildResultFromException(e);

        // 2. Build response wrapper (same structure as ResponseWrapperAction)
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("result", result);
        wrapper.put("data", null);
        wrapper.put("meta", ResultMeta.of(requestId));

        // 3. Convert to JSON
        String responseBody = JsonUtil.toJson(wrapper);

        // 4. Map error type to HTTP status code
        int httpStatus = mapToHttpStatus(e);

        // 5. Build GatewayResponse
        return GatewayResponse.builder()
                .requestId(requestId)
                .statusCode(httpStatus)
                .headers(new HashMap<>())
                .body(responseBody)
                .contentType(GatewayConstants.ContentType.JSON)
                .responseTime(LocalDateTime.now())
                .errorMessage(e.getMessage())
                .build();
    }

    /**
     * Build Result object from GatewayException.
     *
     * <p>Maps Gateway ErrorType to CommonResultCodeEnum, then builds Result.</p>
     */
    private Result buildResultFromException(GatewayException e) {
        // Map Gateway error type to CommonResultCodeEnum
        CommonResultCodeEnum resultCode = mapErrorTypeToResultCode(e.getErrorType());

        // Build Result with error details
        Result result = Result.buildFailure(resultCode);

        // Override message with actual exception message if available
        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            result.setMessage(e.getMessage());
        }

        return result;
    }

    /**
     * Map Gateway ErrorType to CommonResultCodeEnum.
     */
    private CommonResultCodeEnum mapErrorTypeToResultCode(ErrorType errorType) {
        return switch (errorType) {
            case ROUTING -> CommonResultCodeEnum.NOT_FOUND;
            case VALIDATION -> CommonResultCodeEnum.PARAM_ILLEGAL;
            case BUSINESS -> CommonResultCodeEnum.PROCESS_FAIL;
            case SYSTEM, NETWORK -> CommonResultCodeEnum.SYS_ERROR;
            default -> CommonResultCodeEnum.PROCESS_FAIL;
        };
    }

    /**
     * Map Gateway ErrorType to HTTP status code.
     */
    private int mapToHttpStatus(GatewayException e) {
        return switch (e.getErrorType()) {
            case ROUTING -> 404;
            case VALIDATION -> 400;
            case SYSTEM, NETWORK -> 500;
            default -> 500;
        };
    }
}

package io.github.loadup.gateway.core.filter;

import io.github.loadup.commons.result.Result;
import io.github.loadup.commons.result.ResultMeta;
import io.github.loadup.commons.util.JsonUtil;
import io.github.loadup.gateway.facade.constants.GatewayConstants;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.ErrorType;
import io.github.loadup.gateway.facade.exception.GatewayException;
import io.github.loadup.gateway.facade.exception.GatewayExceptionFactory;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.spi.FilterChain;
import io.github.loadup.gateway.facade.spi.GatewayFilter;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Outermost exception filter — catches all errors from the downstream chain
 * and converts them to a unified {@code {result, data, meta}} error response.
 *
 * <p>This replaces both the old ExceptionAction and the inconsistent
 * ExceptionHandler utility. All error responses are now consistently formatted.
 */
public class ExceptionFilter implements GatewayFilter {
    private static final Logger log = LoggerFactory.getLogger(ExceptionFilter.class);


    @Override
    public String name() {
        return "exception";
    }

    @Override
    public void filter(GatewayContext context, FilterChain chain) {
        long startTime = System.currentTimeMillis();
        try {
            chain.filter(context);
        } catch (GatewayException e) {
            handleGatewayException(context, e, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            handleUnknownException(context, e, System.currentTimeMillis() - startTime);
        }
    }

    private void handleGatewayException(GatewayContext context, GatewayException e, long elapsedMs) {
        String requestId = context.getRequest().getRequestId();
        log(e, requestId, elapsedMs);
        context.setResponse(buildErrorResponse(requestId, e, elapsedMs));
    }

    private void handleUnknownException(GatewayContext context, Exception e, long elapsedMs) {
        String requestId = context.getRequest().getRequestId();
        log.error("Unexpected gateway error: requestId={}, type={}, message={}, elapsed={}ms",
                requestId, e.getClass().getName(), e.getMessage(), elapsedMs, e);
        GatewayException wrapped = GatewayExceptionFactory.wrap(e, "GATEWAY");
        context.setResponse(buildErrorResponse(requestId, wrapped, elapsedMs));
    }

    private void log(GatewayException e, String requestId, long elapsedMs) {
        if (e.getErrorType() == ErrorType.SYSTEM || e.getErrorType() == ErrorType.NETWORK) {
            log.error("Gateway error: requestId={}, errorType={}, code={}, message={}, elapsed={}ms",
                    requestId, e.getErrorType(), e.getErrorCode(), e.getMessage(), elapsedMs, e);
        } else {
            log.warn("Gateway error: requestId={}, errorType={}, code={}, message={}, elapsed={}ms",
                    requestId, e.getErrorType(), e.getErrorCode(), e.getMessage(), elapsedMs);
        }
    }

    private GatewayResponse buildErrorResponse(String requestId, GatewayException e, long elapsedMs) {
        String code = e.getErrorCode() != null ? e.getErrorCode() : "PROCESS_FAIL";
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", code);
        result.put("status", "FAIL");
        result.put("message", e.getMessage());

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("result", result);
        wrapper.put("data", null);
        wrapper.put("meta", ResultMeta.of(requestId));

        return GatewayResponse.builder()
                .requestId(requestId)
                .statusCode(mapHttpStatus(e))
                .body(JsonUtil.toJson(wrapper))
                .contentType(GatewayConstants.ContentType.JSON)
                .responseTime(LocalDateTime.now())
                .processingTime(elapsedMs)
                .errorMessage(e.getMessage())
                .build();
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

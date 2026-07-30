package io.github.loadup.gateway.test.unit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.gateway.core.filter.ExceptionFilter;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.ErrorType;
import io.github.loadup.gateway.facade.exception.GatewayException;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExceptionFilter")
class ExceptionFilterTest {

    private final ExceptionFilter filter = new ExceptionFilter();

    private GatewayContext context() {
        GatewayContext ctx = new GatewayContext();
        ctx.setRequest(GatewayRequest.builder()
                .requestId("req-1")
                .path("/test")
                .method("GET")
                .clientIp("127.0.0.1")
                .headers(java.util.Map.of())
                .attributes(java.util.Map.of())
                .build());
        ctx.setRoute(createRoute());
        return ctx;
    }

    private RouteConfig createRoute() {
        RouteConfig route = new RouteConfig();
        route.setPath("/test");
        route.setMethod("GET");
        route.setTarget("http://localhost/api");
        route.setEnabled(true);
        return route;
    }

    @Nested
    @DisplayName("name")
    class Name {
        @Test
        @DisplayName("returns exception")
        void returnsException() {
            assertThat(filter.name()).isEqualTo("exception");
        }
    }

    @Nested
    @DisplayName("filter — happy path")
    class HappyPath {

        @Test
        @DisplayName("proceeds when no exception occurs")
        void proceedsOnNoException() {
            AtomicBoolean called = new AtomicBoolean(false);
            GatewayContext ctx = context();
            filter.filter(ctx, c -> called.set(true));
            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("response body left unchanged on success")
        void responseBodyUnchanged() {
            GatewayContext ctx = context();
            ctx.setResponse(GatewayResponse.builder()
                    .requestId("req-1")
                    .statusCode(200)
                    .body("{\"ok\":true}")
                    .build());
            filter.filter(ctx, c -> {});
            assertThat(ctx.getResponse().getBody()).isEqualTo("{\"ok\":true}");
        }
    }

    @Nested
    @DisplayName("filter — error handling")
    class ErrorHandling {

        @Test
        @DisplayName("GatewayException produces error response")
        void gatewayExceptionProducesErrorResponse() {
            GatewayContext ctx = context();
            filter.filter(ctx, c -> {
                throw new GatewayException("TEST_ERR", ErrorType.VALIDATION, "TEST", "validation failed");
            });
            assertThat(ctx.getResponse()).isNotNull();
        }

        @Test
        @DisplayName("ROUTING error produces response")
        void routingProducesResponse() {
            GatewayContext ctx = context();
            filter.filter(ctx, c -> {
                throw new GatewayException("ROUTE_ERR", ErrorType.ROUTING, "ROUTE", "not found");
            });
            assertThat(ctx.getResponse()).isNotNull();
        }

        @Test
        @DisplayName("unknown exception produces response")
        void unknownExceptionProducesResponse() {
            GatewayContext ctx = context();
            filter.filter(ctx, c -> {
                throw new RuntimeException("unexpected");
            });
            assertThat(ctx.getResponse()).isNotNull();
        }
    }
}

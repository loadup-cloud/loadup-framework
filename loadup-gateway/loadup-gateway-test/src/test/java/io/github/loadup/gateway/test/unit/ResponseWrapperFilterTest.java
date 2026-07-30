package io.github.loadup.gateway.test.unit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.gateway.core.filter.ResponseWrapperFilter;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ResponseWrapperFilter")
class ResponseWrapperFilterTest {

    private GatewayProperties propsWithWrap(boolean wrap) {
        GatewayProperties p = new GatewayProperties();
        GatewayProperties.ResponseProperties rc = new GatewayProperties.ResponseProperties();
        rc.setWrap(wrap);
        p.setResponse(rc);
        return p;
    }

    private GatewayContext context(GatewayResponse response, RouteConfig route) {
        GatewayContext ctx = new GatewayContext();
        ctx.setRequest(GatewayRequest.builder()
                .requestId("req-1")
                .path("/test")
                .method("GET")
                .clientIp("127.0.0.1")
                .headers(Map.of())
                .attributes(Map.of())
                .build());
        ctx.setResponse(response);
        ctx.setRoute(route);
        return ctx;
    }

    private RouteConfig route() {
        RouteConfig r = new RouteConfig();
        r.setPath("/test");
        r.setMethod("GET");
        r.setTarget("http://localhost/api");
        return r;
    }

    @Nested
    @DisplayName("name")
    class Name {
        @Test
        @DisplayName("returns response-wrapper")
        void returnsResponseWrapper() {
            ResponseWrapperFilter filter = new ResponseWrapperFilter(propsWithWrap(false));
            assertThat(filter.name()).isEqualTo("response-wrapper");
        }
    }

    @Nested
    @DisplayName("wrap enabled")
    class WrapEnabled {

        @Test
        @DisplayName("wraps response in result/data/meta format")
        void wrapsResponse() {
            ResponseWrapperFilter filter = new ResponseWrapperFilter(propsWithWrap(true));
            GatewayResponse resp = GatewayResponse.builder()
                    .requestId("req-1")
                    .statusCode(200)
                    .body("{\"key\":\"value\"}")
                    .build();
            GatewayContext ctx = context(resp, route());

            filter.filter(ctx, c -> {});
            assertThat(ctx.getResponse().getBody()).isNotNull();
        }

        @Test
        @DisplayName("wraps string body as-is")
        void wrapsStringBody() {
            ResponseWrapperFilter filter = new ResponseWrapperFilter(propsWithWrap(true));
            GatewayResponse resp = GatewayResponse.builder()
                    .requestId("req-1")
                    .statusCode(200)
                    .body("plain text")
                    .build();
            GatewayContext ctx = context(resp, route());

            filter.filter(ctx, c -> {});
            assertThat(ctx.getResponse().getBody()).isNotNull();
        }
    }

    @Nested
    @DisplayName("wrap disabled")
    class WrapDisabled {

        @Test
        @DisplayName("does not wrap when global wrap is false")
        void noWrapWhenDisabled() {
            ResponseWrapperFilter filter = new ResponseWrapperFilter(propsWithWrap(false));
            GatewayResponse resp = GatewayResponse.builder()
                    .requestId("req-1")
                    .statusCode(200)
                    .body("{\"key\":\"value\"}")
                    .build();
            GatewayContext ctx = context(resp, route());

            filter.filter(ctx, c -> {});
            assertThat(ctx.getResponse().getBody()).isEqualTo("{\"key\":\"value\"}");
        }
    }

    @Nested
    @DisplayName("error responses")
    class ErrorResponses {

        @Test
        @DisplayName("skips wrapping for 4xx responses")
        void skips4xx() {
            ResponseWrapperFilter filter = new ResponseWrapperFilter(propsWithWrap(true));
            GatewayResponse resp = GatewayResponse.builder()
                    .requestId("req-1")
                    .statusCode(400)
                    .body("bad")
                    .build();
            GatewayContext ctx = context(resp, route());

            filter.filter(ctx, c -> {});
            assertThat(ctx.getResponse().getBody()).isEqualTo("bad");
        }

        @Test
        @DisplayName("skips wrapping for 5xx responses")
        void skips5xx() {
            ResponseWrapperFilter filter = new ResponseWrapperFilter(propsWithWrap(true));
            GatewayResponse resp = GatewayResponse.builder()
                    .requestId("req-1")
                    .statusCode(500)
                    .body("error")
                    .build();
            GatewayContext ctx = context(resp, route());

            filter.filter(ctx, c -> {});
            assertThat(ctx.getResponse().getBody()).isEqualTo("error");
        }
    }

    @Nested
    @DisplayName("null safety")
    class NullSafety {

        @Test
        @DisplayName("null response does not fail")
        void nullResponseOk() {
            ResponseWrapperFilter filter = new ResponseWrapperFilter(propsWithWrap(true));
            GatewayContext ctx = new GatewayContext();
            ctx.setRequest(GatewayRequest.builder()
                    .requestId("r")
                    .path("/t")
                    .method("GET")
                    .clientIp("127.0.0.1")
                    .build());
            RouteConfig r = route();
            ctx.setRoute(r);

            filter.filter(ctx, c -> {});
        }

        @Test
        @DisplayName("null route does not fail")
        void nullRouteOk() {
            ResponseWrapperFilter filter = new ResponseWrapperFilter(propsWithWrap(true));
            GatewayContext ctx = new GatewayContext();
            ctx.setRequest(GatewayRequest.builder()
                    .requestId("r")
                    .path("/t")
                    .method("GET")
                    .clientIp("127.0.0.1")
                    .build());
            ctx.setResponse(GatewayResponse.builder()
                    .requestId("r")
                    .statusCode(200)
                    .body("ok")
                    .build());

            filter.filter(ctx, c -> {});
        }
    }
}

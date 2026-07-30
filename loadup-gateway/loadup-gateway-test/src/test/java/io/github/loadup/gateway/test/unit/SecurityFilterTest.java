package io.github.loadup.gateway.test.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.gateway.core.filter.SecurityFilter;
import io.github.loadup.gateway.core.security.SecurityStrategyManager;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.exception.GatewayException;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.RouteConfig;
import io.github.loadup.gateway.facade.spi.SecurityStrategy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SecurityFilter")
class SecurityFilterTest {

    @Nested
    @DisplayName("name")
    class Name {
        @Test
        @DisplayName("returns security")
        void returnsSecurity() {
            SecurityStrategyManager mgr = new SecurityStrategyManager(null);
            SecurityFilter filter = new SecurityFilter(mgr);
            assertThat(filter.name()).isEqualTo("security");
        }
    }

    @Nested
    @DisplayName("OFF code skips security")
    class OffCode {

        @Test
        @DisplayName("OFF security code allows request through")
        void offCodeAllows() {
            SecurityStrategyManager mgr = new SecurityStrategyManager(null);
            SecurityFilter filter = new SecurityFilter(mgr);
            GatewayContext ctx = context("OFF");
            AtomicBoolean called = new AtomicBoolean(false);
            filter.filter(ctx, c -> called.set(true));
            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("null security code treats as OFF")
        void nullCodeTreatsAsOff() {
            SecurityStrategyManager mgr = new SecurityStrategyManager(null);
            SecurityFilter filter = new SecurityFilter(mgr);
            GatewayContext ctx = context(null);
            AtomicBoolean called = new AtomicBoolean(false);
            filter.filter(ctx, c -> called.set(true));
            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("blank security code treats as OFF")
        void blankCodeTreatsAsOff() {
            SecurityStrategyManager mgr = new SecurityStrategyManager(null);
            SecurityFilter filter = new SecurityFilter(mgr);
            GatewayContext ctx = context("");
            AtomicBoolean called = new AtomicBoolean(false);
            filter.filter(ctx, c -> called.set(true));
            assertThat(called.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("custom strategy")
    class CustomStrategy {

        @Test
        @DisplayName("registered strategy is invoked")
        void registeredStrategyInvoked() {
            AtomicBoolean invoked = new AtomicBoolean(false);
            SecurityStrategy custom = new SecurityStrategy() {
                @Override
                public String getCode() {
                    return "CUSTOM";
                }

                @Override
                public void process(GatewayContext ctx) {
                    invoked.set(true);
                }
            };
            SecurityStrategyManager mgr = new SecurityStrategyManager(List.of(custom));
            SecurityFilter filter = new SecurityFilter(mgr);

            GatewayContext ctx = context("CUSTOM");
            filter.filter(ctx, c -> {});
            assertThat(invoked.get()).isTrue();
        }

        @Test
        @DisplayName("unknown code throws exception")
        void unknownCodeThrows() {
            SecurityStrategyManager mgr = new SecurityStrategyManager(Collections.emptyList());
            SecurityFilter filter = new SecurityFilter(mgr);

            GatewayContext ctx = context("UNKNOWN");
            assertThatThrownBy(() -> filter.filter(ctx, c -> {})).isInstanceOf(GatewayException.class);
        }
    }

    @Nested
    @DisplayName("null route")
    class NullRoute {

        @Test
        @DisplayName("null route does not fail")
        void nullRouteOk() {
            SecurityStrategyManager mgr = new SecurityStrategyManager(null);
            SecurityFilter filter = new SecurityFilter(mgr);
            GatewayContext ctx = new GatewayContext();
            ctx.setRequest(GatewayRequest.builder()
                    .requestId("r")
                    .path("/t")
                    .method("GET")
                    .clientIp("127.0.0.1")
                    .build());

            AtomicBoolean called = new AtomicBoolean(false);
            filter.filter(ctx, c -> called.set(true));
            assertThat(called.get()).isTrue();
        }
    }

    private GatewayContext context(String securityCode) {
        GatewayContext ctx = new GatewayContext();
        ctx.setRequest(GatewayRequest.builder()
                .requestId("req-1")
                .path("/test")
                .method("GET")
                .clientIp("127.0.0.1")
                .headers(Map.of())
                .attributes(Map.of())
                .build());
        RouteConfig route = new RouteConfig();
        route.setPath("/test");
        route.setMethod("GET");
        route.setTarget("http://localhost/api");
        route.setSecurityCode(securityCode);
        ctx.setRoute(route);
        return ctx;
    }
}

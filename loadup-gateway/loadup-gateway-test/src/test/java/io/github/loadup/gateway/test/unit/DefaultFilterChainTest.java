package io.github.loadup.gateway.test.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.gateway.core.engine.DefaultFilterChain;
import io.github.loadup.gateway.facade.context.GatewayContext;
import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.spi.GatewayFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DefaultFilterChain")
class DefaultFilterChainTest {

    private GatewayContext context() {
        GatewayContext ctx = new GatewayContext();
        ctx.setRequest(GatewayRequest.builder()
                .requestId("test")
                .path("/test")
                .method("GET")
                .clientIp("127.0.0.1")
                .build());
        return ctx;
    }

    private static GatewayFilter named(
            String name,
            java.util.function.BiConsumer<GatewayContext, io.github.loadup.gateway.facade.spi.FilterChain> fn) {
        return new GatewayFilter() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public void filter(GatewayContext ctx, io.github.loadup.gateway.facade.spi.FilterChain chain) {
                fn.accept(ctx, chain);
            }
        };
    }

    @Nested
    @DisplayName("empty chain")
    class EmptyChain {

        @Test
        @DisplayName("calling filter on empty chain does nothing")
        void emptyChainDoesNothing() {
            DefaultFilterChain chain = new DefaultFilterChain(Collections.emptyList());
            GatewayContext ctx = context();
            chain.filter(ctx);
            assertThat(ctx).isNotNull();
        }
    }

    @Nested
    @DisplayName("single filter chain")
    class SingleFilter {

        @Test
        @DisplayName("single filter is called")
        void singleFilterCalled() {
            AtomicBoolean called = new AtomicBoolean(false);
            GatewayFilter filter = named("test", (ctx, chain) -> called.set(true));
            DefaultFilterChain chain = new DefaultFilterChain(List.of(filter));
            chain.filter(context());
            assertThat(called.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("multi-filter chain")
    class MultiFilter {

        @Test
        @DisplayName("filters execute in order")
        void filtersExecuteInOrder() {
            List<Integer> order = new ArrayList<>();
            GatewayFilter f1 = named("f1", (ctx, chain) -> {
                order.add(1);
                chain.filter(ctx);
            });
            GatewayFilter f2 = named("f2", (ctx, chain) -> {
                order.add(2);
                chain.filter(ctx);
            });
            GatewayFilter f3 = named("f3", (ctx, chain) -> {
                order.add(3);
                chain.filter(ctx);
            });

            DefaultFilterChain chain = new DefaultFilterChain(List.of(f1, f2, f3));
            chain.filter(context());

            assertThat(order).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("filter that does not call chain stops execution")
        void nonProceedingFilterStopsChain() {
            AtomicBoolean secondCalled = new AtomicBoolean(false);
            GatewayFilter terminating = named("term", (ctx, chain) -> {});
            GatewayFilter after = named("after", (ctx, chain) -> secondCalled.set(true));

            DefaultFilterChain chain = new DefaultFilterChain(List.of(terminating, after));
            chain.filter(context());

            assertThat(secondCalled.get()).isFalse();
        }

        @Test
        @DisplayName("filter can modify context before proceeding")
        void filterModifiesContext() {
            GatewayFilter modifier = named("mod", (ctx, chain) -> {
                ctx.setAttribute("key", "value");
                chain.filter(ctx);
            });
            AtomicBoolean verified = new AtomicBoolean(false);
            GatewayFilter verifier = named("ver", (ctx, chain) -> {
                assertThat((String) ctx.getAttribute("key")).isEqualTo("value");
                verified.set(true);
            });

            DefaultFilterChain chain = new DefaultFilterChain(List.of(modifier, verifier));
            chain.filter(context());
            assertThat(verified.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("error propagation")
    class ErrorPropagation {

        @Test
        @DisplayName("exception in filter propagates up")
        void exceptionPropagates() {
            GatewayFilter throwing = named("throw", (ctx, chain) -> {
                throw new RuntimeException("test error");
            });
            DefaultFilterChain chain = new DefaultFilterChain(List.of(throwing));
            assertThatThrownBy(() -> chain.filter(context()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("test error");
        }

        @Test
        @DisplayName("later filters not called after exception")
        void laterFiltersNotCalled() {
            AtomicBoolean laterCalled = new AtomicBoolean(false);
            GatewayFilter throwing = named("throw", (ctx, chain) -> {
                throw new RuntimeException("test error");
            });
            GatewayFilter later = named("later", (ctx, chain) -> laterCalled.set(true));

            DefaultFilterChain chain = new DefaultFilterChain(List.of(throwing, later));
            try {
                chain.filter(context());
            } catch (RuntimeException ignored) {
            }
            assertThat(laterCalled.get()).isFalse();
        }
    }
}

package io.github.loadup.gateway.test.unit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.gateway.core.router.PatternRouteRegistry;
import io.github.loadup.gateway.facade.model.RouteConfig;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PatternRouteRegistry")
class PatternRouteRegistryTest {

    private PatternRouteRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new PatternRouteRegistry();
    }

    private RouteConfig buildRoute(String path, String method) {
        RouteConfig c = new RouteConfig();
        c.setPath(path);
        c.setMethod(method);
        c.setTarget("http://localhost:8080" + path);
        c.setEnabled(true);
        c.setProperties(java.util.Collections.emptyMap());
        return c;
    }

    @Nested
    @DisplayName("exact route resolution")
    class ExactRoutes {

        @Test
        @DisplayName("resolves exact route by path and method")
        void resolvesExactRoute() {
            List<RouteConfig> routes = List.of(buildRoute("/api/user/login", "POST"));
            registry.loadRoutes(routes);
            Optional<RouteConfig> result = registry.resolve("POST", "/api/user/login");
            assertThat(result).isPresent();
            assertThat(result.get().getPath()).isEqualTo("/api/user/login");
        }

        @Test
        @DisplayName("returns empty for wrong method")
        void wrongMethodReturnsEmpty() {
            List<RouteConfig> routes = List.of(buildRoute("/api/user/login", "POST"));
            registry.loadRoutes(routes);
            assertThat(registry.resolve("GET", "/api/user/login")).isEmpty();
        }

        @Test
        @DisplayName("multiple exact routes coexist")
        void multipleExactRoutes() {
            registry.loadRoutes(List.of(
                    buildRoute("/api/user/login", "POST"),
                    buildRoute("/api/user/logout", "POST"),
                    buildRoute("/api/user/me", "GET")));
            assertThat(registry.resolve("POST", "/api/user/login")).isPresent();
            assertThat(registry.resolve("POST", "/api/user/logout")).isPresent();
            assertThat(registry.resolve("GET", "/api/user/me")).isPresent();
        }
    }

    @Nested
    @DisplayName("pattern route resolution")
    class PatternRoutes {

        @Test
        @DisplayName("resolves pattern route")
        void resolvesPatternRoute() {
            registry.loadRoutes(List.of(buildRoute("/api/user/{id}", "GET")));
            Optional<RouteConfig> result = registry.resolve("GET", "/api/user/42");
            assertThat(result).isPresent();
            assertThat(result.get().getPath()).isEqualTo("/api/user/{id}");
        }

        @Test
        @DisplayName("returns empty for wrong method on pattern")
        void wrongMethodOnPattern() {
            registry.loadRoutes(List.of(buildRoute("/api/user/{id}", "GET")));
            assertThat(registry.resolve("POST", "/api/user/42")).isEmpty();
        }
    }

    @Nested
    @DisplayName("priority: exact over pattern")
    class Priority {

        @Test
        @DisplayName("exact match takes priority over pattern match")
        void exactOverPattern() {
            registry.loadRoutes(List.of(buildRoute("/api/user/{id}", "GET"), buildRoute("/api/user/special", "GET")));
            Optional<RouteConfig> result = registry.resolve("GET", "/api/user/special");
            assertThat(result).isPresent();
            assertThat(result.get().getPath()).isEqualTo("/api/user/special");
        }
    }

    @Nested
    @DisplayName("disabled routes")
    class DisabledRoutes {

        @Test
        @DisplayName("disabled routes excluded from exact match")
        void disabledExactRouteExcluded() {
            RouteConfig c = buildRoute("/api/user/login", "POST");
            c.setEnabled(false);
            registry.loadRoutes(List.of(c));
            assertThat(registry.resolve("POST", "/api/user/login")).isEmpty();
        }

        @Test
        @DisplayName("disabled routes excluded from pattern match")
        void disabledPatternRouteExcluded() {
            RouteConfig c = buildRoute("/api/user/{id}", "GET");
            c.setEnabled(false);
            registry.loadRoutes(List.of(c));
            assertThat(registry.resolve("GET", "/api/user/42")).isEmpty();
        }
    }

    @Nested
    @DisplayName("empty and edge cases")
    class EmptyAndEdges {

        @Test
        @DisplayName("empty route list resolves nothing")
        void emptyRouteList() {
            registry.loadRoutes(Collections.emptyList());
            assertThat(registry.resolve("GET", "/api/user")).isEmpty();
        }

        @Test
        @DisplayName("null path returns empty")
        void nullPathReturnsEmpty() {
            registry.loadRoutes(List.of(buildRoute("/api/user/{id}", "GET")));
            assertThat(registry.resolve("GET", null)).isEmpty();
        }
    }
}

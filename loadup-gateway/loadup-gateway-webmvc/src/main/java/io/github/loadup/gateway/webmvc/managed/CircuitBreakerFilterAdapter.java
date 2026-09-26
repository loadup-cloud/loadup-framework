package io.github.loadup.gateway.webmvc.managed;

import io.github.loadup.gateway.api.model.ManagedRoute;
import java.util.Set;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.web.servlet.function.RouterFunctions;

/** Binds a managed HTTP route to SCG MVC's circuit breaker filter. */
public final class CircuitBreakerFilterAdapter implements ManagedFilterAdapter {
    private final boolean factoryAvailable;

    public CircuitBreakerFilterAdapter(boolean factoryAvailable) {
        this.factoryAvailable = factoryAvailable;
    }

    @Override
    public String name() {
        return "CircuitBreaker";
    }

    @Override
    public void configure(RouterFunctions.Builder builder, ManagedRoute route, ManagedRoute.Filter filter) {
        if (!"http".equals(route.target().type())) {
            throw new IllegalArgumentException("CircuitBreaker applies only to HTTP targets: " + route.id());
        }
        if (!factoryAvailable) {
            throw new IllegalArgumentException("CircuitBreaker requires a Spring Cloud CircuitBreakerFactory bean");
        }
        if (!filter.args().keySet().equals(Set.of("name"))) {
            throw new IllegalArgumentException("CircuitBreaker requires only name");
        }
        String name = filter.args().get("name");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("CircuitBreaker name is required");
        }
        builder.filter(CircuitBreakerFilterFunctions.circuitBreaker(name));
    }
}

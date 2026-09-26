package io.github.loadup.gateway.webmvc.managed;

import io.github.loadup.gateway.api.model.RouteDocument;
import java.util.List;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

/** Read-only view of the active version and last refresh outcome. */
@Endpoint(id = "gatewayRoutes")
public final class GatewayRoutesEndpoint {
    private final ManagedRouteRegistry registry;

    public GatewayRoutesEndpoint(ManagedRouteRegistry registry) {
        this.registry = registry;
    }

    @ReadOperation
    public Status status() {
        RouteDocument document = registry.currentDocument();
        List<Route> routes = document.routes().stream()
                .map(route -> new Route(
                        route.id(),
                        route.order(),
                        route.path(),
                        route.methods(),
                        route.target().type(),
                        route.access().type(),
                        route.access().signature()))
                .toList();
        return new Status(
                document.sourceId(),
                document.revision(),
                registry.publishedAt().toString(),
                registry.snapshotAgeSeconds(),
                registry.lastRefreshAt().toString(),
                registry.lastRefreshOutcome(),
                registry.lastRefreshDurationMillis(),
                registry.lastError(),
                routes);
    }

    public record Status(
            String sourceId,
            String revision,
            String publishedAt,
            long snapshotAgeSeconds,
            String lastRefreshAt,
            String lastRefreshOutcome,
            long lastRefreshDurationMillis,
            String lastError,
            List<Route> routes) {}

    public record Route(
            String id,
            int order,
            String path,
            List<String> methods,
            String targetType,
            String access,
            boolean signature) {}
}

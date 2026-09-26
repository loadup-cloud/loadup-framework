package io.github.loadup.gateway.api.model;

import java.util.List;

/** One complete published revision from a route source. */
public record RouteDocument(String sourceId, String revision, int schemaVersion, List<ManagedRoute> routes) {
    public RouteDocument {
        routes = routes == null ? List.of() : List.copyOf(routes);
    }
}

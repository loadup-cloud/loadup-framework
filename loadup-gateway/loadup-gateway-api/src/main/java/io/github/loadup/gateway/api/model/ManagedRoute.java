package io.github.loadup.gateway.api.model;

import java.util.List;
import java.util.Map;

/** Immutable, source-independent route configuration. */
public record ManagedRoute(
        String id, int order, String path, List<String> methods, Target target, Access access, List<Filter> filters) {

    public ManagedRoute {
        methods = methods == null ? List.of() : List.copyOf(methods);
        filters = filters == null ? List.of() : List.copyOf(filters);
    }

    public record Target(String type, String bean, String method, String uri) {}

    public record Access(String type, List<String> anyOf, boolean signature) {
        public Access {
            anyOf = anyOf == null ? List.of() : List.copyOf(anyOf);
        }

        public Access(String type, List<String> anyOf) {
            this(type, anyOf, false);
        }
    }

    public record Filter(String name, Map<String, String> args) {
        public Filter {
            args = args == null ? Map.of() : Map.copyOf(args);
        }
    }
}

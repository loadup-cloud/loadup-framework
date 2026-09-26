package io.github.loadup.gateway.webmvc.managed;

import io.github.loadup.gateway.api.model.ManagedRoute;
import java.util.Set;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.web.servlet.function.RouterFunctions;

/** Exposes SCG MVC's path rewrite filter to managed HTTP routes. */
public final class RewritePathFilterAdapter implements ManagedFilterAdapter {
    @Override
    public String name() {
        return "RewritePath";
    }

    @Override
    public void configure(RouterFunctions.Builder builder, ManagedRoute route, ManagedRoute.Filter filter) {
        if (!"http".equals(route.target().type())) {
            throw new IllegalArgumentException("RewritePath applies only to HTTP targets: " + route.id());
        }
        if (!filter.args().keySet().equals(Set.of("regexp", "replacement"))) {
            throw new IllegalArgumentException("RewritePath requires regexp and replacement");
        }
        String regexp = filter.args().get("regexp");
        String replacement = filter.args().get("replacement");
        if (regexp == null || regexp.isBlank() || replacement == null || replacement.isBlank()) {
            throw new IllegalArgumentException("RewritePath regexp and replacement must be nonblank");
        }
        builder.before(BeforeFilterFunctions.rewritePath(regexp, replacement));
    }
}

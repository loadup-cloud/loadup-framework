package io.github.loadup.gateway.webmvc.managed;

import io.github.loadup.gateway.api.model.ManagedRoute;
import java.util.Set;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.web.servlet.function.RouterFunctions;

public final class StripPrefixFilterAdapter implements ManagedFilterAdapter {
    @Override
    public String name() {
        return "StripPrefix";
    }

    @Override
    public void configure(RouterFunctions.Builder builder, ManagedRoute route, ManagedRoute.Filter filter) {
        if (!"http".equals(route.target().type())) {
            throw new IllegalArgumentException("StripPrefix applies only to HTTP targets: " + route.id());
        }
        if (!filter.args().keySet().equals(Set.of("parts"))) {
            throw new IllegalArgumentException("StripPrefix requires only parts");
        }
        int count = Integer.parseInt(filter.args().get("parts"));
        if (count < 0) {
            throw new IllegalArgumentException("StripPrefix parts must be nonnegative");
        }
        builder.before(BeforeFilterFunctions.stripPrefix(count));
    }
}

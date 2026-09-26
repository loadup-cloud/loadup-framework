package io.github.loadup.gateway.webmvc.managed;

import io.github.loadup.gateway.api.model.ManagedRoute;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.web.servlet.function.RouterFunctions;

/** Exposes SCG MVC's request header filter to managed HTTP routes. */
public final class SetRequestHeaderFilterAdapter implements ManagedFilterAdapter {
    private static final Pattern HEADER_NAME = Pattern.compile("[!#$%&'*+.^_`|~0-9A-Za-z-]+");

    @Override
    public String name() {
        return "SetRequestHeader";
    }

    @Override
    public void configure(RouterFunctions.Builder builder, ManagedRoute route, ManagedRoute.Filter filter) {
        if (!"http".equals(route.target().type())) {
            throw new IllegalArgumentException("SetRequestHeader applies only to HTTP targets: " + route.id());
        }
        if (!filter.args().keySet().equals(Set.of("name", "value"))) {
            throw new IllegalArgumentException("SetRequestHeader requires name and value");
        }
        String name = filter.args().get("name");
        String value = filter.args().get("value");
        if (name == null
                || !HEADER_NAME.matcher(name).matches()
                || value == null
                || value.contains("\r")
                || value.contains("\n")) {
            throw new IllegalArgumentException("SetRequestHeader requires a valid name and a single-line value");
        }
        builder.before(BeforeFilterFunctions.setRequestHeader(name, value));
    }
}

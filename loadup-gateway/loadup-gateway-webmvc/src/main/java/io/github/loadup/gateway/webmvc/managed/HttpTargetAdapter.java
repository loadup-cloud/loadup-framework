package io.github.loadup.gateway.webmvc.managed;

import io.github.loadup.gateway.api.model.ManagedRoute;
import java.net.URI;
import java.util.Set;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

/** Uses SCG MVC's native HTTP handler and URI before filter. */
public final class HttpTargetAdapter implements TargetHandlerAdapter {
    private final boolean loadBalancerAvailable;

    public HttpTargetAdapter() {
        this(false);
    }

    public HttpTargetAdapter(boolean loadBalancerAvailable) {
        this.loadBalancerAvailable = loadBalancerAvailable;
    }

    @Override
    public String type() {
        return "http";
    }

    @Override
    public HandlerFunction<ServerResponse> compile(ManagedRoute route) {
        String target = route.target().uri();
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("HTTP route requires target.uri: " + route.id());
        }
        if (route.target().bean() != null || route.target().method() != null) {
            throw new IllegalArgumentException("HTTP route cannot declare service target fields: " + route.id());
        }
        URI uri = URI.create(target);
        if (!Set.of("http", "https", "lb").contains(uri.getScheme()) || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("Unsupported HTTP target URI: " + route.id());
        }
        if ("lb".equals(uri.getScheme()) && (!loadBalancerAvailable || uri.getHost() == null)) {
            throw new IllegalArgumentException("lb:// target requires Spring Cloud LoadBalancer: " + route.id());
        }
        return HandlerFunctions.http();
    }

    @Override
    public void configure(RouterFunctions.Builder builder, ManagedRoute route) {
        if (!"lb".equals(URI.create(route.target().uri()).getScheme())) {
            builder.before(BeforeFilterFunctions.uri(route.target().uri()));
        }
    }

    @Override
    public void configureAfterFilters(RouterFunctions.Builder builder, ManagedRoute route) {
        URI uri = URI.create(route.target().uri());
        if ("lb".equals(uri.getScheme())) {
            builder.filter(LoadBalancerFilterFunctions.lb(uri.getHost()));
        }
    }
}

package io.github.loadup.gateway.webmvc.managed;

import io.github.loadup.gateway.api.model.ManagedRoute;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerResponse;

/** Compiles a service route to a whitelisted Spring proxy method. */
public final class ServiceTargetAdapter implements TargetHandlerAdapter {
    private final ServiceMethodCatalog catalog;

    public ServiceTargetAdapter(ServiceMethodCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public String type() {
        return "service";
    }

    @Override
    public HandlerFunction<ServerResponse> compile(ManagedRoute route) {
        if (route.target().uri() != null) {
            throw new IllegalArgumentException("Service route cannot declare target.uri: " + route.id());
        }
        ManagedRoute.Target target = route.target();
        if (target.bean() == null
                || target.bean().isBlank()
                || target.method() == null
                || target.method().isBlank()) {
            throw new IllegalArgumentException("Service route requires target.bean and target.method: " + route.id());
        }
        return catalog.require(target.bean(), target.method());
    }
}

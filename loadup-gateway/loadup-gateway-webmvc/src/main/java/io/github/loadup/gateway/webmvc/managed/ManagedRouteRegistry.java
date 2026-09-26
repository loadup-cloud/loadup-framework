package io.github.loadup.gateway.webmvc.managed;

import io.github.loadup.gateway.api.event.RouteSourceChangedEvent;
import io.github.loadup.gateway.api.model.ManagedRoute;
import io.github.loadup.gateway.api.model.RouteDocument;
import io.github.loadup.gateway.api.spi.RouteSource;
import io.github.loadup.gateway.webmvc.security.RequestSignatureVerifier;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RequestPredicate;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/** Atomically publishes complete SCG MVC routing snapshots. */
public final class ManagedRouteRegistry implements RouterFunction<ServerResponse> {
    private static final Logger log = LoggerFactory.getLogger(ManagedRouteRegistry.class);
    private final RouteSource source;
    private final Map<String, TargetHandlerAdapter> targets;
    private final Map<String, ManagedFilterAdapter> filters;
    private final MeterRegistry meters;
    private final RequestSignatureVerifier signatures;
    private final Set<String> reservedRouteIds;
    private final AtomicReference<Snapshot> active =
            new AtomicReference<>(new Snapshot(null, request -> Optional.empty(), Instant.EPOCH));
    private volatile String lastError;
    private volatile Instant lastRefreshAt = Instant.EPOCH;
    private volatile String lastRefreshOutcome = "never";
    private volatile long lastRefreshDurationNanos;

    public ManagedRouteRegistry(RouteSource source, ServiceMethodCatalog services) {
        this(source, services, (MeterRegistry) null);
    }

    public ManagedRouteRegistry(RouteSource source, ServiceMethodCatalog services, MeterRegistry meters) {
        this(
                source,
                List.of(new ServiceTargetAdapter(services), new HttpTargetAdapter()),
                List.of(new StripPrefixFilterAdapter(), new CircuitBreakerFilterAdapter(false)),
                meters,
                null,
                Set.of());
    }

    public ManagedRouteRegistry(RouteSource source, ServiceMethodCatalog services, Set<String> reservedRouteIds) {
        this(
                source,
                List.of(new ServiceTargetAdapter(services), new HttpTargetAdapter()),
                List.of(new StripPrefixFilterAdapter(), new CircuitBreakerFilterAdapter(false)),
                null,
                null,
                reservedRouteIds);
    }

    public ManagedRouteRegistry(
            RouteSource source,
            List<TargetHandlerAdapter> targetAdapters,
            List<ManagedFilterAdapter> filterAdapters,
            MeterRegistry meters,
            RequestSignatureVerifier signatures,
            Set<String> reservedRouteIds) {
        this.source = source;
        Map<String, TargetHandlerAdapter> targetMap = new HashMap<>();
        for (TargetHandlerAdapter adapter : targetAdapters) {
            requireText(adapter.type(), "target adapter type");
            if (targetMap.putIfAbsent(adapter.type(), adapter) != null) {
                throw new IllegalStateException("Duplicate gateway target adapter: " + adapter.type());
            }
        }
        Map<String, ManagedFilterAdapter> filterMap = new HashMap<>();
        for (ManagedFilterAdapter adapter : filterAdapters) {
            requireText(adapter.name(), "filter adapter name");
            if (filterMap.putIfAbsent(adapter.name(), adapter) != null) {
                throw new IllegalStateException("Duplicate gateway filter adapter: " + adapter.name());
            }
        }
        this.targets = Map.copyOf(targetMap);
        this.filters = Map.copyOf(filterMap);
        this.meters = meters;
        this.signatures = signatures;
        this.reservedRouteIds = Set.copyOf(reservedRouteIds);
        if (meters != null) {
            Gauge.builder("loadup.gateway.snapshot.age.seconds", this, ManagedRouteRegistry::snapshotAgeSeconds)
                    .register(meters);
        }
    }

    @PostConstruct
    public void init() {
        refresh(true);
    }

    public synchronized void refresh() {
        refresh(false);
    }

    private synchronized void refresh(boolean failFast) {
        long started = System.nanoTime();
        try {
            RouteDocument document = source.loadCurrent();
            requireText(document.sourceId(), "sourceId");
            requireText(document.revision(), "revision");
            if (document.schemaVersion() != 1) {
                throw new IllegalArgumentException("Unsupported gateway schemaVersion: " + document.schemaVersion());
            }
            Snapshot previous = active.get();
            if (previous.document() != null && previous.document().revision().equals(document.revision())) {
                lastRefreshOutcome = "unchanged";
                return;
            }
            RouterFunction<ServerResponse> router = compile(document.routes());
            active.set(new Snapshot(document, router, Instant.now()));
            lastError = null;
            lastRefreshOutcome = "success";
            recordRefresh("success");
            log.info(
                    "Gateway snapshot published: source={}, revision={}, routes={}",
                    document.sourceId(),
                    document.revision(),
                    document.routes().size());
        } catch (Exception e) {
            lastError = e.getMessage();
            lastRefreshOutcome = "failure";
            recordRefresh("failure");
            if (failFast) {
                throw new IllegalStateException("Cannot initialize managed gateway routes", e);
            }
            log.error("Gateway refresh failed; previous snapshot remains active", e);
        } finally {
            lastRefreshAt = Instant.now();
            lastRefreshDurationNanos = System.nanoTime() - started;
        }
    }

    @EventListener
    public void sourceChanged(RouteSourceChangedEvent event) {
        if (event.getSource() == source) {
            refresh();
        }
    }

    public RouteDocument currentDocument() {
        return active.get().document();
    }

    public Instant publishedAt() {
        return active.get().publishedAt();
    }

    public String lastError() {
        return lastError;
    }

    public Instant lastRefreshAt() {
        return lastRefreshAt;
    }

    public String lastRefreshOutcome() {
        return lastRefreshOutcome;
    }

    public long lastRefreshDurationMillis() {
        return TimeUnit.NANOSECONDS.toMillis(lastRefreshDurationNanos);
    }

    public long snapshotAgeSeconds() {
        Instant published = active.get().publishedAt();
        return published.equals(Instant.EPOCH)
                ? 0
                : Math.max(0, Instant.now().getEpochSecond() - published.getEpochSecond());
    }

    @Override
    public Optional<HandlerFunction<ServerResponse>> route(ServerRequest request) {
        return active.get().router().route(request);
    }

    private RouterFunction<ServerResponse> compile(List<ManagedRoute> routes) {
        Set<String> ids = new HashSet<>();
        Set<String> exactMatches = new HashSet<>();
        RouterFunction<ServerResponse> result = request -> Optional.empty();
        for (ManagedRoute route : routes.stream()
                .sorted(Comparator.comparingInt(ManagedRoute::order))
                .toList()) {
            requireText(route.id(), "id");
            requireText(route.path(), "path");
            if (!route.path().startsWith("/api/") || route.methods().isEmpty()) {
                throw new IllegalArgumentException("Route must have /api/ path and methods: " + route.id());
            }
            if (!ids.add(route.id())) {
                throw new IllegalArgumentException("Duplicate route id: " + route.id());
            }
            if (reservedRouteIds.contains(route.id())) {
                throw new IllegalArgumentException("Managed route conflicts with SCG route id: " + route.id());
            }
            if (route.target() == null || route.access() == null) {
                throw new IllegalArgumentException("Route needs target and access: " + route.id());
            }
            validateAccess(route.access());
            if (route.access().signature() && (signatures == null || !signatures.isConfigured())) {
                throw new IllegalArgumentException("Signed route requires configured app secrets: " + route.id());
            }
            requireText(route.target().type(), "target.type");
            TargetHandlerAdapter target = targets.get(route.target().type());
            if (target == null) {
                throw new IllegalArgumentException(
                        "Unsupported target type: " + route.target().type());
            }
            HandlerFunction<ServerResponse> handler = target.compile(route);
            RouterFunctions.Builder builder = GatewayRouterFunctions.route(route.id());
            builder.filter((request, next) -> execute(route, request, next));
            for (String methodName : route.methods()) {
                HttpMethod method = HttpMethod.valueOf(methodName.toUpperCase(Locale.ROOT));
                if (!exactMatches.add(method + " " + route.path())) {
                    throw new IllegalArgumentException("Duplicate route match: " + method + " " + route.path());
                }
                RequestPredicate predicate = RequestPredicates.method(method).and(RequestPredicates.path(route.path()));
                builder.route(predicate, handler);
            }
            target.configure(builder, route);
            for (ManagedRoute.Filter filter : route.filters()) {
                ManagedFilterAdapter adapter = filters.get(filter.name());
                if (adapter == null) {
                    throw new IllegalArgumentException("Unsupported route filter: " + filter.name());
                }
                adapter.configure(builder, route, filter);
            }
            target.configureAfterFilters(builder, route);
            result = result.and(builder.build());
        }
        return result;
    }

    private ServerResponse execute(ManagedRoute route, ServerRequest request, HandlerFunction<ServerResponse> next)
            throws Exception {
        long start = System.nanoTime();
        String outcome = "error";
        try {
            ServerResponse response = authorize(route, request, next);
            outcome = response.statusCode().isError() ? "error" : "success";
            return response;
        } finally {
            if (meters != null) {
                Timer.builder("loadup.gateway.requests")
                        .tags("route", route.id(), "target", route.target().type(), "outcome", outcome)
                        .register(meters)
                        .record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            }
        }
    }

    private ServerResponse authorize(ManagedRoute route, ServerRequest request, HandlerFunction<ServerResponse> next)
            throws Exception {
        ManagedRoute.Access access = route.access();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        switch (access.type()) {
            case "public" -> {}
            case "authenticated" -> {
                if (!authenticated) {
                    return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
                }
            }
            case "authority" -> {
                if (!authenticated) {
                    return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
                }
                boolean allowed = authentication.getAuthorities().stream()
                        .anyMatch(authority -> access.anyOf().contains(authority.getAuthority()));
                if (!allowed) {
                    return ServerResponse.status(HttpStatus.FORBIDDEN).build();
                }
            }
            default -> throw new IllegalArgumentException("Unsupported access type: " + access.type());
        }
        if (access.signature()) {
            try {
                ServerRequest verified =
                        signatures.verify(request, "http".equals(route.target().type()));
                if (verified == null) {
                    return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
                }
                return next.handle(verified);
            } catch (RequestSignatureVerifier.SignedBodyTooLargeException e) {
                return ServerResponse.status(HttpStatus.CONTENT_TOO_LARGE).build();
            }
        }
        return next.handle(request);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Required gateway field: " + field);
        }
    }

    private static void validateAccess(ManagedRoute.Access access) {
        requireText(access.type(), "access.type");
        if (!Set.of("public", "authenticated", "authority").contains(access.type())) {
            throw new IllegalArgumentException("Unsupported access type: " + access.type());
        }
        if ("authority".equals(access.type()) && access.anyOf().isEmpty()) {
            throw new IllegalArgumentException("Authority access needs anyOf");
        }
        if (!"authority".equals(access.type()) && !access.anyOf().isEmpty()) {
            throw new IllegalArgumentException("Only authority access accepts anyOf");
        }
    }

    private void recordRefresh(String outcome) {
        if (meters != null) {
            meters.counter("loadup.gateway.route.refresh", "outcome", outcome).increment();
        }
    }

    private record Snapshot(RouteDocument document, RouterFunction<ServerResponse> router, Instant publishedAt) {}
}

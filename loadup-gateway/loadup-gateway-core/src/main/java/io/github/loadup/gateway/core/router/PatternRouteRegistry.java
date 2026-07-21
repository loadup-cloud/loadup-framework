package io.github.loadup.gateway.core.router;

/*-
 * #%L
 * LoadUp Gateway Core
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.gateway.facade.model.PathPattern;
import io.github.loadup.gateway.facade.model.RouteConfig;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Pattern-aware route registry that supports Ant-style path variables.
 *
 * <p>Maintains two lookup structures:
 * <ul>
 *   <li>exactRoutes — fast O(1) lookup for routes without path variables</li>
 *   <li>patternRoutes — ordered list for variable-path matching, sorted by specificity</li>
 * </ul>
 *
 * <p>Matching priority for the same path:
 * <ol>
 *   <li>Exact match (e.g., /api/user/profile)</li>
 *   <li>Pattern with fewer variables first (more specific)</li>
 *   <li>Pattern with more variables last (less specific)</li>
 * </ol>
 */
@Slf4j
public class PatternRouteRegistry {

    /** Exact-match routes keyed by method:path */
    private final ConcurrentHashMap<String, RouteConfig> exactRoutes = new ConcurrentHashMap<>();

    /** Pattern routes ordered by specificity (fewer variables = more specific = first) */
    private volatile List<PatternEntry> patternRoutes = new ArrayList<>();

    /**
     * Load a full set of routes into the registry using atomic swap.
     *
     * @param routes all enabled routes from storage
     */
    public void loadRoutes(List<RouteConfig> routes) {
        ConcurrentHashMap<String, RouteConfig> newExact = new ConcurrentHashMap<>();
        List<PatternEntry> newPatterns = new ArrayList<>();

        for (RouteConfig route : routes) {
            if (!route.isEnabled()) {
                continue;
            }
            PathPattern pattern = PathPattern.compile(route.getPath());
            String exactKey = buildRouteKey(route.getMethod(), route.getPath());

            if (pattern.isExact()) {
                newExact.put(exactKey, route);
            } else {
                newPatterns.add(new PatternEntry(pattern, route));
            }
        }

        // Sort patterns by specificity: fewer segments and fewer variables = more specific
        newPatterns.sort(Comparator.comparingInt(PatternEntry::specificityScore));

        // Atomic swap
        this.exactRoutes.clear();
        this.exactRoutes.putAll(newExact);
        this.patternRoutes = newPatterns;

        log.info(
                "PatternRouteRegistry loaded: {} exact routes, {} pattern routes",
                newExact.size(),
                newPatterns.size());
    }

    /**
     * Resolve a route by method and request path.
     *
     * <p>First checks exact match (O(1)), then iterates pattern routes in
     * specificity order (O(n) where n = number of pattern routes).
     *
     * @param method HTTP method
     * @param requestPath the actual request URI
     * @return matched RouteConfig with path parameters populated, or empty
     */
    public Optional<RouteConfig> resolve(String method, String requestPath) {
        // Fast path: exact match
        String exactKey = buildRouteKey(method, requestPath);
        RouteConfig exact = exactRoutes.get(exactKey);
        if (exact != null) {
            return Optional.of(exact);
        }

        // Pattern match: iterate in specificity order
        List<PatternEntry> currentPatterns = this.patternRoutes;
        for (PatternEntry entry : currentPatterns) {
            if (!entry.route.getMethod().equalsIgnoreCase(method)) {
                continue;
            }
            PathPattern.MatchResult result = entry.pattern.match(requestPath);
            if (result != null) {
                // Found match — enrich the route with extracted path parameters
                RouteConfig enriched = enrichWithPathParams(entry.route, result.getPathParameters());
                return Optional.of(enriched);
            }
        }

        return Optional.empty();
    }

    /**
     * Create a new RouteConfig with path parameters populated in the request path parameters map.
     * The path parameters are stored in the properties map as a sub-map so they can be accessed
     * by templates and downstream processors.
     */
    private RouteConfig enrichWithPathParams(RouteConfig route, Map<String, String> pathParams) {
        if (pathParams.isEmpty()) {
            return route;
        }

        // Store path params in route properties for downstream access
        java.util.Map<String, Object> enrichedProps =
                new java.util.HashMap<>(route.getProperties());
        enrichedProps.put("_pathParams", pathParams);
        enrichedProps.put("_matchedPattern", route.getPath());

        return RouteConfig.builderFrom(route).properties(enrichedProps).build();
    }

    /**
     * Get the total number of registered routes.
     */
    public int size() {
        return exactRoutes.size() + patternRoutes.size();
    }

    private String buildRouteKey(String method, String path) {
        return method.toUpperCase() + ":" + path;
    }

    /**
     * Internal entry holding a compiled pattern and its route.
     */
    private static class PatternEntry {
        final PathPattern pattern;
        final RouteConfig route;

        PatternEntry(PathPattern pattern, RouteConfig route) {
            this.pattern = pattern;
            this.route = route;
        }

        /**
         * Lower score = more specific = matched first.
         *
         * Logic: count the number of literal (non-variable) path segments, minus
         * the number of variable segments. More literal segments = more specific.
         */
        int specificityScore() {
            String path = route.getPath();
            int literalSegments = 0;
            int variableSegments = 0;
            if (path != null) {
                for (String segment : path.split("/")) {
                    if (segment.isEmpty()) continue;
                    if (segment.startsWith("{") && segment.endsWith("}")) {
                        variableSegments++;
                    } else {
                        literalSegments++;
                    }
                }
            }
            // More literal, fewer variables = more specific (lower score wins)
            return variableSegments * 1000 - literalSegments;
        }
    }
}

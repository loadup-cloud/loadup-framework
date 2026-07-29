package io.github.loadup.gateway.facade.model;

/*-
 * #%L
 * LoadUp Gateway Facade
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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compiled representation of a route path pattern.
 *
 * <p>Supports Ant-style path variables: /api/user/{id}, /api/order/{orderId}/item/{itemId}.
 * Path variables are extracted during matching and made available as pathParameters.
 *
 * <p>Patterns with no variables are treated as exact-match for backward compatibility.
 */
public final class PathPattern {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([^/}]+)}");

    private final String rawPattern;
    private final boolean isExact;
    private final Pattern compiledPattern;
    private final java.util.List<String> variableNames;

    private PathPattern(
            String rawPattern, boolean isExact, Pattern compiledPattern, java.util.List<String> variableNames) {
        this.rawPattern = rawPattern;
        this.isExact = isExact;
        this.compiledPattern = compiledPattern;
        this.variableNames = Collections.unmodifiableList(variableNames);
    }

    /**
     * Compile a path pattern string into a PathPattern.
     *
     * @param pattern the path pattern (e.g., "/api/user/{id}" or "/api/user/profile")
     * @return compiled PathPattern
     */
    public static PathPattern compile(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return new PathPattern(pattern, true, null, Collections.emptyList());
        }

        java.util.List<String> vars = new java.util.ArrayList<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(pattern);

        if (!matcher.find()) {
            // No variables — exact match
            return new PathPattern(pattern, true, null, Collections.emptyList());
        }

        // Has variables — build regex
        matcher.reset();
        StringBuilder regex = new StringBuilder("^");
        int lastEnd = 0;

        while (matcher.find()) {
            // Append literal text before this variable
            String literal = pattern.substring(lastEnd, matcher.start());
            regex.append(Pattern.quote(literal));

            // Append variable capture group
            regex.append("([^/]+)");
            vars.add(matcher.group(1));

            lastEnd = matcher.end();
        }

        // Append remaining literal text after last variable
        String trailing = pattern.substring(lastEnd);
        regex.append(Pattern.quote(trailing));
        regex.append("$");

        Pattern compiled = Pattern.compile(regex.toString());
        return new PathPattern(pattern, false, compiled, vars);
    }

    /**
     * Try to match an incoming request path against this pattern.
     *
     * @param requestPath the actual request path (e.g., "/api/user/123")
     * @return MatchResult if matched, null otherwise
     */
    public MatchResult match(String requestPath) {
        if (requestPath == null) {
            return null;
        }

        if (isExact) {
            if (rawPattern.equals(requestPath)) {
                return new MatchResult(rawPattern, Collections.emptyMap());
            }
            return null;
        }

        Matcher matcher = compiledPattern.matcher(requestPath);
        if (!matcher.matches()) {
            return null;
        }

        // Extract path variables
        Map<String, String> pathParams = new LinkedHashMap<>();
        for (int i = 0; i < variableNames.size(); i++) {
            pathParams.put(variableNames.get(i), matcher.group(i + 1));
        }

        return new MatchResult(rawPattern, Collections.unmodifiableMap(pathParams));
    }

    public String getRawPattern() {
        return rawPattern;
    }

    public boolean isExact() {
        return isExact;
    }

    /**
     * Result of a successful path pattern match.
     */
    public static final class MatchResult {
        private final String matchedPattern;
        private final Map<String, String> pathParameters;

        MatchResult(String matchedPattern, Map<String, String> pathParameters) {
            this.matchedPattern = matchedPattern;
            this.pathParameters = pathParameters;
        }

        public String getMatchedPattern() {
            return matchedPattern;
        }

        public Map<String, String> getPathParameters() {
            return pathParameters;
        }

        public boolean hasParameters() {
            return !pathParameters.isEmpty();
        }
    }
}

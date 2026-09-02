/*-
 * #%L
 * Loadup Gateway WebMVC Engine
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package io.github.loadup.gateway.webmvc.router;

import io.github.loadup.gateway.facade.model.RouteDefinition;
import io.github.loadup.gateway.facade.model.RouteDefinition.BackendDefinition;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

/** Validates route definitions before a routing snapshot is compiled. */
public final class RouteDefinitionValidator {
    private static final Set<String> HTTP_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS");

    private RouteDefinitionValidator() {}

    public static void validate(List<RouteDefinition> definitions, Predicate<String> protocolSupported) {
        List<String> errors = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Set<String> mappings = new HashSet<>();

        for (RouteDefinition definition : definitions) {
            if (!definition.isEnabled()) {
                continue;
            }
            String id = definition.getId();
            String label = id == null || id.isBlank() ? "<missing-id>" : id;
            if (id == null || id.isBlank()) {
                errors.add(label + ": id must not be blank");
            } else if (!ids.add(id)) {
                errors.add(label + ": duplicate id");
            }

            String path = definition.getPath();
            String method = definition.getMethod() == null
                    ? "POST"
                    : definition.getMethod().toUpperCase(Locale.ROOT);
            if (path == null || path.isBlank() || !path.startsWith("/")) {
                errors.add(label + ": path must start with '/'");
            } else if (!mappings.add(method + " " + path)) {
                errors.add(label + ": duplicate route mapping " + method + " " + path);
            }
            if (!HTTP_METHODS.contains(method)) {
                errors.add(label + ": unsupported HTTP method " + method);
            }

            validateBackend(label, definition.getBackend(), protocolSupported, errors);
        }

        if (!errors.isEmpty()) {
            throw new IllegalStateException("Invalid gateway routes:\n - " + String.join("\n - ", errors));
        }
    }

    private static void validateBackend(
            String label, BackendDefinition backend, Predicate<String> protocolSupported, List<String> errors) {
        if (backend == null
                || backend.getProtocol() == null
                || backend.getProtocol().isBlank()) {
            errors.add(label + ": backend.protocol must not be blank");
            return;
        }
        String protocol = backend.getProtocol().toUpperCase(Locale.ROOT);
        if (!protocolSupported.test(protocol)) {
            errors.add(label + ": no proxy processor registered for protocol " + protocol);
            return;
        }
        switch (protocol) {
            case "BEAN" -> {
                if (backend.getBeanName() == null || backend.getBeanName().isBlank()) {
                    errors.add(label + ": backend.beanName must not be blank");
                }
                if (backend.getMethodName() == null || backend.getMethodName().isBlank()) {
                    errors.add(label + ": backend.methodName must not be blank");
                }
            }
            case "HTTP", "RPC" -> validateUri(label, backend.getUrl(), errors);
            default -> {
                // Custom protocols only require a registered processor.
            }
        }
    }

    private static void validateUri(String label, String value, List<String> errors) {
        try {
            URI uri = URI.create(value == null ? "" : value);
            if (!uri.isAbsolute()) {
                errors.add(label + ": backend.url must be an absolute URI");
            }
        } catch (IllegalArgumentException exception) {
            errors.add(label + ": backend.url is invalid");
        }
    }
}

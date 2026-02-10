package io.github.loadup.gateway.facade.context;

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

import io.github.loadup.gateway.facade.model.GatewayRequest;
import io.github.loadup.gateway.facade.model.GatewayResponse;
import io.github.loadup.gateway.facade.model.RouteConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Gateway Context to hold request lifecycle objects. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayContext {

    /** The incoming request */
    private GatewayRequest request;

    /** The original HttpServletRequest */
    private HttpServletRequest originalRequest;

    /** The outgoing response */
    private GatewayResponse response;

    /** The original HttpServletResponse */
    private HttpServletResponse originalResponse;

    /** The matched route configuration */
    private RouteConfig route;

    /** Context attributes for sharing data between components */
    @Builder.Default
    private Map<String, Object> attributes = new ConcurrentHashMap<>();

    /** Exception occurred during processing */
    private Throwable exception;

    /**
     * Add an attribute
     *
     * @param key key
     * @param value value
     */
    public void setAttribute(String key, Object value) {
        if (attributes == null) {
            attributes = new ConcurrentHashMap<>();
        }
        attributes.put(key, value);
    }

    /**
     * Get an attribute
     *
     * @param key key
     * @param <T> type
     * @return value
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        if (attributes == null) {
            return null;
        }
        return (T) attributes.get(key);
    }

    /**
     * Remove an attribute
     *
     * @param key key
     */
    public void removeAttribute(String key) {
        if (attributes != null) {
            attributes.remove(key);
        }
    }
}

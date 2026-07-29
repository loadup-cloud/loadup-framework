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
import java.util.HashMap;
import java.util.Map;

/**
 * YAML-friendly filter definition.
 *
 * <p>Each entry in a route's {@code filters} list maps to one of these.
 * The {@code name} field matches {@link io.github.loadup.gateway.facade.spi.GatewayFilter#name()}.
 * {@code props} are filter-specific key-value parameters.
 *
 * <p>YAML example:
 * <pre>
 * filters:
 *   - name: rate-limit
 *     props:
 *       capacity: 100
 *       refillRate: 10
 *   - name: jwt-auth
 * </pre>
 */
public class FilterDefinition {

    /** Filter name matching a registered GatewayFilter bean. */
    private String name;

    /** Filter-specific properties. */
    private Map<String, Object> props = new HashMap<>();

    public Map<String, Object> getProps() {
        return props == null ? Collections.emptyMap() : Collections.unmodifiableMap(props);
    }

    public FilterDefinition(String name, Map<String, Object> props) {
        this.name = name;
        this.props = props;
    }

    public FilterDefinition() {}

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProps(Map<String, Object> props) {
        this.props = props;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Map<String, Object> props = new HashMap<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder props(Map<String, Object> props) {
            this.props = props;
            return this;
        }

        public FilterDefinition build() {
            return new FilterDefinition(this.name, this.props);
        }
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}

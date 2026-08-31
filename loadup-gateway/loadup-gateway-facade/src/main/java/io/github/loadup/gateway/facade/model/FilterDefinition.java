package io.github.loadup.gateway.facade.model;

/*-
 * #%L
 * LoadUp Gateway Facade
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * YAML-friendly filter definition.
 *
 * <p>Each entry in a route's {@code filters} list maps to one of these.
 * The {@code name} field is the filter identifier and {@code props} are
 * filter-specific key-value parameters.
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

    /** Filter identifier. */
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

package io.github.loadup.gateway.facade.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilterDefinition that)) return false;
        return Objects.equals(name, that.name) && Objects.equals(props, that.props);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, props);
    }

    public FilterDefinition(String name, Map<String, Object> props) {
        this.name = name;
        this.props = props;
    }

    public FilterDefinition() {
    }

    public String getName() {
        return this.name;
    }



    public void setName(String name) {
        this.name = name;
    }

    public void setProps(Map<String, Object> props) {
        this.props = props;
    }

    @Override
    public String toString() {
        return "FilterDefinition(" + "name=" + name + ", " + "props=" + props + ")";
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
}

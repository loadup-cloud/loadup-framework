package io.github.loadup.gateway.plugins.yaml;

import java.util.regex.Pattern;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.resolver.Resolver;

/**
 * YAML resolver that only treats {@code true}/{@code false} as booleans.
 *
 * <p>SnakeYAML's default resolver follows YAML 1.1 and also resolves {@code ON/OFF/YES/NO}
 * as booleans, which silently corrupts string tokens such as {@code securityCode: OFF} in
 * the route DSL. This resolver keeps the other implicit tags (int, float, null, timestamp,
 * merge, yaml) untouched.
 */
final class StrictBooleanResolver extends Resolver {

    private static final Pattern STRICT_BOOL = Pattern.compile("^(?:true|True|TRUE|false|False|FALSE)$");

    @Override
    protected void addImplicitResolvers() {
        addImplicitResolver(Tag.BOOL, STRICT_BOOL, "tTfF", 10);
        addImplicitResolver(Tag.INT, INT, "-+0123456789");
        addImplicitResolver(Tag.FLOAT, FLOAT, "-+0123456789.");
        addImplicitResolver(Tag.MERGE, MERGE, "<", 10);
        addImplicitResolver(Tag.NULL, NULL, "~nN\u0000", 10);
        addImplicitResolver(Tag.NULL, EMPTY, null, 10);
        addImplicitResolver(Tag.TIMESTAMP, TIMESTAMP, "0123456789", 50);
        addImplicitResolver(Tag.YAML, YAML, "!&*", 10);
    }
}

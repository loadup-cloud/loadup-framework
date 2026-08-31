/*-
 * #%L
 * Repository YAML Plugin
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

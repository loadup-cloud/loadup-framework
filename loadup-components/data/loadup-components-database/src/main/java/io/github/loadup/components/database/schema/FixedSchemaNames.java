/*
 * Copyright (C) 2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.database.schema;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Canonical rules for the single business schema of one deployment.
 *
 * <p>A schema name is an explicit configuration value. It is never derived from a request, never
 * concatenated into SQL and never a reserved or shared fallback location.
 */
public final class FixedSchemaNames {

    /** Bounded PostgreSQL identifier: lowercase, leading letter or underscore, at most 63 chars. */
    public static final String PATTERN = "[a-z_][a-z0-9_]{0,62}";

    private static final Pattern SCHEMA = Pattern.compile(PATTERN);

    private FixedSchemaNames() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Validates a configured schema name and returns it unchanged.
     *
     * @throws IllegalStateException when the value is missing, malformed, reserved or shared
     */
    public static String require(String schema) {
        if (schema == null || !SCHEMA.matcher(schema).matches()) {
            throw new IllegalStateException("loadup.database.schema must match " + PATTERN + " but was " + schema);
        }
        if (Objects.equals("public", schema)) {
            throw new IllegalStateException("loadup.database.schema must not be the shared public schema");
        }
        if (schema.startsWith("pg_")) {
            throw new IllegalStateException("loadup.database.schema must not use the reserved pg_ prefix");
        }
        return schema;
    }
}

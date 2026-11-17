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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Applies the one configured schema to the connection pool and rejects conflicting declarations.
 *
 * <p>{@code loadup.database.schema} is the only application source of the schema. The pool receives
 * it as a physical-connection setting, so no request, task or actor can change the target. An
 * explicit pool schema, JDBC {@code currentSchema}, {@code search_path} option or init SQL that
 * names a different schema fails startup instead of silently drifting.
 */
public final class FixedSchemaEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String SCHEMA_PROPERTY = "loadup.database.schema";
    static final String POOL_SCHEMA_PROPERTY = "spring.datasource.hikari.schema";
    static final String URL_PROPERTY = "spring.datasource.url";
    static final String INIT_SQL_PROPERTY = "spring.datasource.hikari.connection-init-sql";
    private static final String SOURCE_NAME = "loadupFixedSchema";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String schema = property(environment, SCHEMA_PROPERTY);
        if (schema == null || schema.isBlank()) {
            // The binder reports the missing required value; no fallback schema is applied.
            return;
        }
        FixedSchemaNames.require(schema);
        requireDeclaredTargets(environment, schema);
        if (property(environment, POOL_SCHEMA_PROPERTY) == null) {
            environment
                    .getPropertySources()
                    .addFirst(new MapPropertySource(SOURCE_NAME, Map.of(POOL_SCHEMA_PROPERTY, schema)));
        }
    }

    private static void requireDeclaredTargets(ConfigurableEnvironment environment, String schema) {
        String poolSchema = property(environment, POOL_SCHEMA_PROPERTY);
        if (poolSchema != null && !Objects.equals(poolSchema, schema)) {
            throw new IllegalStateException("spring.datasource.hikari.schema " + poolSchema
                    + " conflicts with loadup.database.schema " + schema);
        }
        String url = property(environment, URL_PROPERTY);
        if (url != null) {
            requireUrlTarget(url, schema);
        }
        String initSql = property(environment, INIT_SQL_PROPERTY);
        if (initSql != null && !initSql.isBlank() && mentionsSearchPath(initSql) && !initSql.contains(schema)) {
            throw new IllegalStateException(
                    "connection init SQL sets a search_path that conflicts with loadup.database.schema " + schema);
        }
    }

    private static void requireUrlTarget(String url, String schema) {
        int queryStart = url.indexOf('?');
        if (queryStart < 0) {
            return;
        }
        for (String parameter : url.substring(queryStart + 1).split("&")) {
            int separator = parameter.indexOf('=');
            if (separator < 0) {
                continue;
            }
            String name = parameter.substring(0, separator).toLowerCase(Locale.ROOT);
            String value = URLDecoder.decode(parameter.substring(separator + 1), StandardCharsets.UTF_8);
            if (Objects.equals("currentschema", name) && !Objects.equals(value, schema)) {
                throw conflict("currentSchema", value, schema);
            }
            if (Objects.equals("options", name) && mentionsSearchPath(value) && !value.contains(schema)) {
                throw conflict("options search_path", value, schema);
            }
        }
    }

    private static IllegalStateException conflict(String source, String value, String schema) {
        return new IllegalStateException(
                "JDBC " + source + " " + value + " conflicts with loadup.database.schema " + schema);
    }

    private static boolean mentionsSearchPath(String value) {
        return value.toLowerCase(Locale.ROOT).contains("search_path");
    }

    private static String property(ConfigurableEnvironment environment, String name) {
        try {
            return environment.getProperty(name);
        } catch (IllegalArgumentException unresolvedPlaceholder) {
            // An unresolved external placeholder is reported by the data source binding later.
            return null;
        }
    }

    @Override
    public int getOrder() {
        // Must run before the data source pool binds its own configuration.
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}

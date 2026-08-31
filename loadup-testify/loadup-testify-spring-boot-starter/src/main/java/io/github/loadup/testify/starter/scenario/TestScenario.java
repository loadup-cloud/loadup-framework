/*-
 * #%L
 * Testify Spring Boot Starter
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
package io.github.loadup.testify.starter.scenario;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Small DSL for JUnit 5 integration tests: data cleanup, data setup, and async DB assertions.
 */
public class TestScenario {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z0-9_]+");
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private final JdbcTemplate jdbcTemplate;

    public TestScenario(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String uuid() {
        return UUID.randomUUID().toString();
    }

    public TestScenario clean(String table) {
        jdbcTemplate.update("DELETE FROM " + identifier(table));
        return this;
    }

    public TestScenario clean(String table, String column, Object value) {
        jdbcTemplate.update("DELETE FROM " + identifier(table) + " WHERE " + identifier(column) + " = ?", value);
        return this;
    }

    public TestScenario insert(String table, Map<String, Object> row) {
        List<String> columns = new ArrayList<>(row.keySet());
        String columnList = columns.stream().map(this::identifier).collect(Collectors.joining(", "));
        String placeholders = String.join(", ", Collections.nCopies(columns.size(), "?"));
        Object[] args = columns.stream().map(row::get).toArray();
        jdbcTemplate.update(
                "INSERT INTO " + identifier(table) + " (" + columnList + ") VALUES (" + placeholders + ")", args);
        return this;
    }

    public DbAssert assertDb(String table) {
        return new DbAssert(jdbcTemplate, identifier(table), DEFAULT_TIMEOUT);
    }

    private String identifier(String value) {
        if (!IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + value);
        }
        return value;
    }
}

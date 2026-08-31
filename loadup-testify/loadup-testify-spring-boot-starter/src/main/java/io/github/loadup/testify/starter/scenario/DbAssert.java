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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Fluent async assertion against a JDBC table.
 */
public class DbAssert {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z0-9_]+");
    private static final long POLL_INTERVAL_MILLIS = 100L;

    private final JdbcTemplate jdbcTemplate;
    private final String table;
    private final Map<String, Object> criteria = new LinkedHashMap<>();
    private Duration timeout;

    DbAssert(JdbcTemplate jdbcTemplate, String table, Duration timeout) {
        this.jdbcTemplate = jdbcTemplate;
        this.table = table;
        this.timeout = timeout;
    }

    public DbAssert where(String column, Object value) {
        criteria.put(identifier(column), value);
        return this;
    }

    public DbAssert withTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public DbAssert exists() {
        await(() -> count() > 0, "expected at least one row in " + table);
        return this;
    }

    public DbAssert notExists() {
        await(() -> count() == 0, "expected no rows in " + table);
        return this;
    }

    public DbAssert has(String column, Object expected) {
        await(
                () -> {
                    Map<String, Object> row = findRow();
                    if (row == null) {
                        return false;
                    }
                    Object actual = row.get(identifier(column));
                    return Objects.equals(actual, expected)
                            || (actual != null && String.valueOf(actual).equals(String.valueOf(expected)));
                },
                "expected " + table + "." + column + " = " + expected);
        return this;
    }

    private long count() {
        Long count = jdbcTemplate.queryForObject(
                countSql(), Long.class, criteria.values().toArray());
        return count == null ? 0L : count;
    }

    private Map<String, Object> findRow() {
        List<Map<String, Object>> rows =
                jdbcTemplate.queryForList(rowSql(), criteria.values().toArray());
        return rows.isEmpty() ? null : rows.get(0);
    }

    private String countSql() {
        return "SELECT COUNT(*) FROM " + table + whereSql();
    }

    private String rowSql() {
        return "SELECT * FROM " + table + whereSql();
    }

    private String whereSql() {
        String where = criteria.entrySet().stream()
                .map(entry -> entry.getKey() + " = ?")
                .collect(Collectors.joining(" AND "));
        return where.isEmpty() ? "" : " WHERE " + where;
    }

    private void await(BooleanSupplier condition, String message) {
        long deadline = System.nanoTime() + timeout.toNanos();
        AssertionError lastError = null;
        while (System.nanoTime() < deadline) {
            try {
                if (condition.getAsBoolean()) {
                    return;
                }
            } catch (RuntimeException ex) {
                lastError = new AssertionError(message, ex);
            }
            sleepQuietly();
        }
        if (lastError == null) {
            throw new AssertionError("Timed out after " + timeout.toMillis() + " ms: " + message);
        }
        throw new AssertionError("Timed out after " + timeout.toMillis() + " ms: " + message, lastError);
    }

    private void sleepQuietly() {
        try {
            Thread.sleep(POLL_INTERVAL_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while polling database", e);
        }
    }

    private String identifier(String value) {
        if (!IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + value);
        }
        return value;
    }
}

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

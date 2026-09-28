package io.github.loadup.testify.asserts.model;

public record FieldDiff(
        String path, // 字段路径，如 "db.users[0].age" 或 "api.response.code"
        Object expected,
        Object actual,
        String message) {}

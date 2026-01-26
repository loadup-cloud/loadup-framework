package io.github.loadup.components.cache.model;

public record CacheValueWrapper<T>(
    String type,   // "STR", "OBJ", "NULL"
    T data,        // 实际内容
    String clazz   // 原始类名（可选，方便排查故障）
) {}

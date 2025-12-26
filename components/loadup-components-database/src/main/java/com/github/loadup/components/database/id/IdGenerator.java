/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.database.id;

/**
 * ID Generator Interface
 */
@FunctionalInterface
public interface IdGenerator {

    /**
     * Generate unique ID
     *
     * @return unique identifier
     */
    String generate();

    /**
     * Get generator name
     *
     * @return generator name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}

